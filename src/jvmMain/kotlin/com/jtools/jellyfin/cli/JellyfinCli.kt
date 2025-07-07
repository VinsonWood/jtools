package com.jtools.jellyfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.choice
import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.config.ConfigManager
import com.jtools.jellyfin.service.ExportImportService
import com.jtools.jellyfin.service.ResolutionFilterService
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * 主命令行类
 */
class JellyfinCli : CliktCommand(name = "jtools") {
    override fun run() = Unit
}

/**
 * 导出命令
 */
class ExportCommand : CliktCommand(name = "export", help = "导出喜爱的电影和演员") {
    private val serverUrl by option("-s", "--server", help = "Jellyfin服务器URL")
    private val apiToken by option("-t", "--token", help = "API Token")
    private val userId by option("-u", "--user", help = "用户ID（可选，默认使用第一个用户）")
    private val outputFile by option("-o", "--output", help = "输出文件路径").file().required()
    private val saveConfig by option("--save-config", help = "保存连接配置到文件").flag(default = false)
    
    override fun run() = runBlocking {
        val config = getConfig()
        
        if (!config.isValid()) {
            echo("错误: 配置无效，请检查服务器URL和API Token", err = true)
            return@runBlocking
        }
        
        if (saveConfig) {
            ConfigManager.saveConfig(config)
        }
        
        val apiClient = JellyfinApiClient(config)
        
        try {
            // 测试连接
            echo("正在测试连接...")
            if (!apiClient.testConnection()) {
                echo("错误: 无法连接到Jellyfin服务器", err = true)
                return@runBlocking
            }
            
            // 获取用户ID
            val actualUserId = userId ?: config.userId ?: apiClient.getCurrentUserId()
            if (actualUserId == null) {
                echo("错误: 无法获取用户ID", err = true)
                return@runBlocking
            }
            
            echo("使用用户ID: $actualUserId")
            
            // 导出数据
            val exportService = ExportImportService(apiClient)
            val exportData = exportService.exportFavorites(actualUserId)
            val jsonString = exportService.exportToJson(exportData)
            
            // 写入文件
            outputFile.writeText(jsonString)
            
            echo("导出成功！")
            echo("文件保存到: ${outputFile.absolutePath}")
            echo("导出了 ${exportData.favoriteMovies.size} 部电影和 ${exportData.favoritePeople.size} 位演员")
            
        } catch (e: Exception) {
            echo("导出失败: ${e.message}", err = true)
        } finally {
            apiClient.close()
        }
    }
    
    private fun getConfig(): JellyfinConfig {
        // 优先使用命令行参数
        if (serverUrl != null && apiToken != null) {
            return JellyfinConfig(
                serverUrl = serverUrl!!,
                apiToken = apiToken!!,
                userId = userId
            )
        }
        
        // 尝试从文件加载配置
        val savedConfig = ConfigManager.loadConfig()
        if (savedConfig != null) {
            return savedConfig.copy(
                serverUrl = serverUrl ?: savedConfig.serverUrl,
                apiToken = apiToken ?: savedConfig.apiToken,
                userId = userId ?: savedConfig.userId
            )
        }
        
        // 如果没有保存的配置，命令行参数又不完整，返回默认配置
        return JellyfinConfig(
            serverUrl = serverUrl ?: "",
            apiToken = apiToken ?: "",
            userId = userId
        )
    }
}

/**
 * 导入命令
 */
class ImportCommand : CliktCommand(name = "import", help = "导入喜爱的电影和演员") {
    private val serverUrl by option("-s", "--server", help = "Jellyfin服务器URL").required()
    private val apiToken by option("-t", "--token", help = "API Token").required()
    private val userId by option("-u", "--user", help = "用户ID（可选，默认使用第一个用户）")
    private val inputFile by option("-i", "--input", help = "输入文件路径").file(mustExist = true).required()
    
    override fun run() = runBlocking {
        val config = JellyfinConfig(
            serverUrl = serverUrl,
            apiToken = apiToken,
            userId = userId
        )
        
        if (!config.isValid()) {
            echo("错误: 配置无效，请检查服务器URL和API Token", err = true)
            return@runBlocking
        }
        
        val apiClient = JellyfinApiClient(config)
        
        try {
            // 测试连接
            echo("正在测试连接...")
            if (!apiClient.testConnection()) {
                echo("错误: 无法连接到Jellyfin服务器", err = true)
                return@runBlocking
            }
            
            // 获取用户ID
            val actualUserId = userId ?: apiClient.getCurrentUserId()
            if (actualUserId == null) {
                echo("错误: 无法获取用户ID", err = true)
                return@runBlocking
            }
            
            echo("使用用户ID: $actualUserId")
            
            // 读取导入文件
            val jsonString = inputFile.readText()
            val exportService = ExportImportService(apiClient)
            val importData = exportService.parseFromJson(jsonString)
            
            echo("准备导入 ${importData.favoriteMovies.size} 部电影和 ${importData.favoritePeople.size} 位演员")
            echo("原始导出时间: ${importData.exportDate}")
            echo("原始服务器: ${importData.serverUrl}")
            
            // 确认导入
            echo("是否继续导入？(y/N)")
            val confirmation = readLine()
            if (confirmation?.lowercase() != "y" && confirmation?.lowercase() != "yes") {
                echo("导入已取消")
                return@runBlocking
            }
            
            // 执行导入
            val result = exportService.importFavorites(actualUserId, importData)
            
            echo("\n导入完成！")
            echo("电影导入结果: ${result.importedMovies}/${result.totalMovies} 成功")
            echo("演员导入结果: ${result.importedPeople}/${result.totalPeople} 成功")
            
            if (result.errors.isNotEmpty()) {
                echo("\n遇到以下错误:")
                result.errors.forEach { echo("  $it") }
            }
            
        } catch (e: Exception) {
            echo("导入失败: ${e.message}", err = true)
        } finally {
            apiClient.close()
        }
    }
}

/**
 * 测试连接命令
 */
class TestCommand : CliktCommand(name = "test", help = "测试Jellyfin连接") {
    private val serverUrl by option("-s", "--server", help = "Jellyfin服务器URL").required()
    private val apiToken by option("-t", "--token", help = "API Token").required()
    
    override fun run() = runBlocking {
        val config = JellyfinConfig(
            serverUrl = serverUrl,
            apiToken = apiToken
        )
        
        if (!config.isValid()) {
            echo("错误: 配置无效，请检查服务器URL和API Token", err = true)
            return@runBlocking
        }
        
        val apiClient = JellyfinApiClient(config)
        
        try {
            echo("正在测试连接到: $serverUrl")
            
            if (apiClient.testConnection()) {
                echo("✓ 连接成功！")
                
                // 获取用户列表
                val users = apiClient.getUsers()
                if (users.isNotEmpty()) {
                    echo("\n可用用户:")
                    users.forEach { user ->
                        echo("  ID: ${user.id}, 名称: ${user.name}")
                    }
                } else {
                    echo("未找到用户")
                }
            } else {
                echo("✗ 连接失败", err = true)
            }
            
        } catch (e: Exception) {
            echo("连接测试失败: ${e.message}", err = true)
        } finally {
            apiClient.close()
        }
    }
}

/**
 * 分辨率筛选命令
 */
class ResolutionCommand : CliktCommand(name = "resolution", help = "筛选指定分辨率以下的电影") {
    private val serverUrl by option("-s", "--server", help = "Jellyfin服务器URL").required()
    private val apiToken by option("-t", "--token", help = "API Token").required()
    private val userId by option("-u", "--user", help = "用户ID（可选，默认使用第一个用户）")
    private val maxWidth by option("-w", "--width", help = "最大宽度").int()
    private val maxHeight by option("-h", "--height", help = "最大高度").int()
    private val preset by option("-p", "--preset", help = "预设分辨率 (480p, 720p, 1080p, 4k)").choice("480p", "720p", "1080p", "4k")
    private val outputFile by option("-o", "--output", help = "输出文件路径").file()
    private val includeUnknown by option("--include-unknown", help = "包含未知分辨率的电影").flag(default = false)
    private val statsOnly by option("--stats-only", help = "只显示统计信息").flag(default = false)
    
    override fun run() = runBlocking {
        val config = JellyfinConfig(
            serverUrl = serverUrl,
            apiToken = apiToken,
            userId = userId
        )
        
        if (!config.isValid()) {
            echo("错误: 配置无效，请检查服务器URL和API Token", err = true)
            return@runBlocking
        }
        
        val apiClient = JellyfinApiClient(config)
        
        try {
            // 测试连接
            echo("正在测试连接...")
            if (!apiClient.testConnection()) {
                echo("错误: 无法连接到Jellyfin服务器", err = true)
                return@runBlocking
            }
            
            // 获取用户ID
            val actualUserId = userId ?: apiClient.getCurrentUserId()
            if (actualUserId == null) {
                echo("错误: 无法获取用户ID", err = true)
                return@runBlocking
            }
            
            echo("使用用户ID: $actualUserId")
            
            val resolutionService = ResolutionFilterService(apiClient)
            
            // 如果只需要统计信息
            if (statsOnly) {
                echo("正在获取分辨率统计信息...")
                val stats = resolutionService.getResolutionStatistics(actualUserId)
                
                echo("\n=== 分辨率统计 ===")
                echo("总电影数: ${stats.totalMovies}")
                echo("未知分辨率: ${stats.unknownCount}")
                echo("")
                
                stats.resolutionCounts.forEach { (resolution, count) ->
                    echo("$resolution: $count 部")
                }
                
                return@runBlocking
            }
            
            // 确定分辨率筛选条件
            val (filterWidth, filterHeight, resolutionType) = when {
                preset != null -> {
                    val type = when (preset) {
                        "480p" -> ResolutionFilterService.ResolutionType.SD_480P
                        "720p" -> ResolutionFilterService.ResolutionType.HD_720P
                        "1080p" -> ResolutionFilterService.ResolutionType.HD_1080P
                        "4k" -> ResolutionFilterService.ResolutionType.UHD_4K
                        else -> ResolutionFilterService.ResolutionType.HD_1080P
                    }
                    val (w, h) = ResolutionFilterService.ResolutionType.getMaxResolution(type)
                    Triple(w, h, type)
                }
                maxWidth != null && maxHeight != null -> {
                    Triple(maxWidth!!, maxHeight!!, ResolutionFilterService.ResolutionType.CUSTOM)
                }
                else -> {
                    // 默认1080p
                    val (w, h) = ResolutionFilterService.ResolutionType.getMaxResolution(ResolutionFilterService.ResolutionType.HD_1080P)
                    Triple(w, h, ResolutionFilterService.ResolutionType.HD_1080P)
                }
            }
            
            val filter = ResolutionFilterService.ResolutionFilter(
                maxWidth = filterWidth,
                maxHeight = filterHeight,
                includeUnknown = includeUnknown,
                resolutionType = resolutionType
            )
            
            echo("正在筛选低分辨率电影...")
            echo("筛选条件: 最大分辨率 ${filterWidth}x${filterHeight}")
            echo("包含未知分辨率: ${if (includeUnknown) "是" else "否"}")
            
            val result = resolutionService.getMoviesBelowResolution(actualUserId, filter)
            
            echo("\n=== 筛选结果 ===")
            echo("总电影数: ${result.totalMovies}")
            echo("低分辨率电影数: ${result.filteredCount}")
            echo("扫描时间: ${result.scanDate}")
            echo("")
            
            if (result.filteredMovies.isNotEmpty()) {
                echo("低分辨率电影列表:")
                result.filteredMovies.forEach { movieWithRes ->
                    val movie = movieWithRes.movie
                    val resolution = movieWithRes.resolution
                    echo("  ${movie.name} (${movie.productionYear ?: "未知年份"}) - ${resolution.displayResolution}")
                }
            } else {
                echo("没有找到符合条件的低分辨率电影")
            }
            
            // 保存到文件
            if (outputFile != null) {
                val exportService = ExportImportService(apiClient)
                val exportData = com.jtools.jellyfin.model.JellyfinExportData(
                    exportDate = result.scanDate,
                    serverUrl = serverUrl,
                    userId = actualUserId,
                    favoriteMovies = result.filteredMovies.map { it.movie },
                    favoritePeople = emptyList()
                )
                
                val jsonString = exportService.exportToJson(exportData)
                outputFile!!.writeText(jsonString)
                
                echo("\n结果已保存到: ${outputFile!!.absolutePath}")
            }
            
        } catch (e: Exception) {
            echo("分辨率筛选失败: ${e.message}", err = true)
            e.printStackTrace()
        } finally {
            apiClient.close()
        }
    }
}
