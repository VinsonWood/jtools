package com.jtools.jellyfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.config.ConfigManager
import com.jtools.jellyfin.service.ExportImportService
import kotlinx.coroutines.runBlocking

/**
 * 查看命令 - 从文件渲染演员和电影
 */
class ViewCommand : CliktCommand(name = "view", help = "查看导出文件中的电影和演员") {
    private val inputFile by option("-i", "--input", help = "输入文件路径").file(mustExist = true).required()
    private val showMovies by option("--movies", help = "显示电影列表").flag(default = false)
    private val showActors by option("--actors", help = "显示演员列表").flag(default = false)
    private val limit by option("-l", "--limit", help = "显示数量限制").int().default(10)
    
    override fun run() = runBlocking {
        try {
            val jsonString = inputFile.readText()
            val exportService = ExportImportService(JellyfinApiClient(JellyfinConfig.default()))
            val exportData = exportService.parseFromJson(jsonString)
            
            echo("============================================================")
            echo("📁 文件信息")
            echo("============================================================")
            echo("导出时间: ${exportData.exportDate}")
            echo("服务器: ${exportData.serverUrl}")
            echo("用户ID: ${exportData.userId}")
            echo("电影数量: ${exportData.favoriteMovies.size}")
            echo("演员数量: ${exportData.favoritePeople.size}")
            echo()
            
            // 显示电影列表
            if (showMovies || (!showMovies && !showActors)) {
                echo("============================================================")
                echo("🎬 喜爱的电影")
                echo("============================================================")
                
                if (exportData.favoriteMovies.isEmpty()) {
                    echo("没有找到喜爱的电影")
                } else {
                    val moviesToShow = exportData.favoriteMovies.take(limit)
                    moviesToShow.forEachIndexed { index, movie ->
                        echo("${index + 1}. ${movie.name}")
                        echo("   年份: ${movie.productionYear ?: "未知"}")
                        echo("   时长: ${movie.runTimeTicks?.let { it / 10000000 / 60 }?.let { "${it}分钟" } ?: "未知"}")
                        if (movie.genres.isNotEmpty()) {
                            echo("   类型: ${movie.genres.joinToString(", ")}")
                        }
                        if (movie.people.isNotEmpty()) {
                            val actors = movie.people.filter { it.type == "Actor" }.take(3)
                            if (actors.isNotEmpty()) {
                                echo("   演员: ${actors.joinToString(", ") { it.name }}")
                            }
                        }
                        if (movie.overview?.isNotBlank() == true) {
                            echo("   简介: ${movie.overview.take(100)}${if (movie.overview.length > 100) "..." else ""}")
                        }
                        echo()
                    }
                    
                    if (exportData.favoriteMovies.size > limit) {
                        echo("... 还有 ${exportData.favoriteMovies.size - limit} 部电影未显示")
                        echo("使用 -l 参数可以显示更多电影")
                        echo()
                    }
                }
            }
            
            // 显示演员列表
            if (showActors || (!showMovies && !showActors)) {
                echo("============================================================")
                echo("🎭 喜爱的演员")
                echo("============================================================")
                
                if (exportData.favoritePeople.isEmpty()) {
                    echo("没有找到喜爱的演员")
                } else {
                    val actorsToShow = exportData.favoritePeople.take(limit)
                    actorsToShow.forEachIndexed { index, person ->
                        echo("${index + 1}. ${person.name}")
                        echo("   类型: ${person.type}")
                        if (person.role?.isNotBlank() == true) {
                            echo("   角色: ${person.role}")
                        }
                        echo()
                    }
                    
                    if (exportData.favoritePeople.size > limit) {
                        echo("... 还有 ${exportData.favoritePeople.size - limit} 位演员未显示")
                        echo("使用 -l 参数可以显示更多演员")
                        echo()
                    }
                }
            }
            
        } catch (e: Exception) {
            echo("查看文件失败: ${e.message}", err = true)
        }
    }
}

/**
 * 配置命令
 */
class ConfigCommand : CliktCommand(name = "config", help = "管理连接配置") {
    private val serverUrl by option("-s", "--server", help = "Jellyfin服务器URL")
    private val apiToken by option("-t", "--token", help = "API Token")
    private val userId by option("-u", "--user", help = "用户ID")
    private val show by option("--show", help = "显示当前配置").flag(default = false)
    private val delete by option("--delete", help = "删除配置文件").flag(default = false)
    
    override fun run() = runBlocking {
        if (delete) {
            ConfigManager.deleteConfig()
            echo("配置已删除")
            return@runBlocking
        }
        
        if (show) {
            val config = ConfigManager.loadConfig()
            if (config != null) {
                echo("当前配置:")
                echo("服务器URL: ${config.serverUrl}")
                echo("API Token: ${config.apiToken.take(10)}...")
                echo("用户ID: ${config.userId ?: "未设置"}")
                echo("超时时间: ${config.timeout}ms")
            } else {
                echo("没有找到配置文件")
            }
            return@runBlocking
        }
        
        // 保存新配置
        if (serverUrl != null && apiToken != null) {
            val config = JellyfinConfig(
                serverUrl = serverUrl!!,
                apiToken = apiToken!!,
                userId = userId
            )
            
            if (config.isValid()) {
                ConfigManager.saveConfig(config)
                echo("配置已保存")
            } else {
                echo("配置无效，请检查服务器URL格式", err = true)
            }
        } else {
            echo("请提供服务器URL和API Token，或使用 --show 查看当前配置")
        }
    }
}