package com.jtools.jellyfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.config.ConfigManager
import com.jtools.jellyfin.service.ExportImportService
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
