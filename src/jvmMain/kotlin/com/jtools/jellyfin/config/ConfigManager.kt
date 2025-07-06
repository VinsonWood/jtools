package com.jtools.jellyfin.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

/**
 * 配置管理器，负责持久化连接信息和应用设置
 */
object ConfigManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val configFile = File("jellyfin-config.json")
    private val appConfigFile = File("jtools-config.json")
    
    /**
     * 保存Jellyfin配置到文件（兼容性保留）
     */
    fun saveConfig(config: JellyfinConfig) {
        try {
            val jsonString = json.encodeToString(config)
            configFile.writeText(jsonString)
            println("Jellyfin配置已保存到: ${configFile.absolutePath}")
        } catch (e: Exception) {
            println("保存Jellyfin配置失败: ${e.message}")
        }
    }
    
    /**
     * 保存应用配置到文件
     */
    fun saveAppConfig(config: AppConfig) {
        try {
            val jsonString = json.encodeToString(config)
            appConfigFile.writeText(jsonString)
            println("应用配置已保存到: ${appConfigFile.absolutePath}")
        } catch (e: Exception) {
            println("保存应用配置失败: ${e.message}")
        }
    }
    
    /**
     * 从文件加载Jellyfin配置（兼容性保留）
     */
    fun loadConfig(): JellyfinConfig? {
        return try {
            if (configFile.exists()) {
                val jsonString = configFile.readText()
                val config = json.decodeFromString<JellyfinConfig>(jsonString)
                println("Jellyfin配置已从文件加载: ${configFile.absolutePath}")
                config
            } else {
                println("Jellyfin配置文件不存在，使用默认配置")
                null
            }
        } catch (e: Exception) {
            println("加载Jellyfin配置失败: ${e.message}")
            null
        }
    }
    
    /**
     * 从文件加载应用配置
     */
    fun loadAppConfig(): AppConfig? {
        return try {
            if (appConfigFile.exists()) {
                val jsonString = appConfigFile.readText()
                val config = json.decodeFromString<AppConfig>(jsonString)
                println("应用配置已从文件加载: ${appConfigFile.absolutePath}")
                config
            } else {
                println("应用配置文件不存在，使用默认配置")
                // 尝试从旧的jellyfin配置迁移
                loadConfig()?.let { jellyfinConfig ->
                    val migratedConfig = AppConfig(
                        jellyfinConfig = jellyfinConfig,
                        uiScale = 1.0f,
                        rememberConnection = true,
                        enableRealTimeLogs = true,
                        showDetailedLogs = false
                    )
                    println("从旧配置迁移到新格式")
                    saveAppConfig(migratedConfig)
                    return migratedConfig
                }
                null
            }
        } catch (e: Exception) {
            println("加载应用配置失败: ${e.message}")
            null
        }
    }
    
    /**
     * 检查配置文件是否存在
     */
    fun configExists(): Boolean {
        return configFile.exists() || appConfigFile.exists()
    }
    
    /**
     * 删除配置文件
     */
    fun deleteConfig() {
        if (configFile.exists()) {
            configFile.delete()
            println("Jellyfin配置文件已删除")
        }
        if (appConfigFile.exists()) {
            appConfigFile.delete()
            println("应用配置文件已删除")
        }
    }
}