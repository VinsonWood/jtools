package com.jtools.jellyfin.config

import kotlinx.serialization.Serializable

/**
 * Jellyfin服务器配置
 */
@Serializable
data class JellyfinConfig(
    val serverUrl: String,
    val apiToken: String,
    val userId: String? = null,
    val timeout: Long = 30000L
) {
    /**
     * 获取格式化的服务器URL（确保以/结尾）
     */
    val formattedServerUrl: String
        get() = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
    
    /**
     * 验证配置是否有效
     */
    fun isValid(): Boolean {
        return serverUrl.isNotBlank() && 
               apiToken.isNotBlank() && 
               (serverUrl.startsWith("http://") || serverUrl.startsWith("https://"))
    }
    
    companion object {
        /**
         * 创建默认配置
         */
        fun default() = JellyfinConfig(
            serverUrl = "http://localhost:8096",
            apiToken = "",
            userId = null
        )
    }
}

/**
 * 应用程序设置配置
 */
@Serializable
data class AppConfig(
    val jellyfinConfig: JellyfinConfig? = null,
    val uiScale: Float = 1.0f,
    val rememberConnection: Boolean = true,
    val enableRealTimeLogs: Boolean = true,
    val showDetailedLogs: Boolean = false
) {
    /**
     * 验证UI缩放值是否有效
     */
    fun isValidUiScale(): Boolean {
        return uiScale >= 0.5f && uiScale <= 3.0f
    }
    
    companion object {
        /**
         * 创建默认配置
         */
        fun default() = AppConfig(
            jellyfinConfig = null,
            uiScale = 1.0f,
            rememberConnection = true,
            enableRealTimeLogs = true,
            showDetailedLogs = false
        )
    }
}
