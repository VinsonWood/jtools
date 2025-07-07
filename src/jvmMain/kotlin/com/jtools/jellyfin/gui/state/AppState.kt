package com.jtools.jellyfin.gui.state

import androidx.compose.runtime.*
import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.config.ConfigManager
import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.config.AppConfig
import com.jtools.jellyfin.model.*
import com.jtools.jellyfin.service.ExportImportService
import com.jtools.jellyfin.service.DuplicateMovieService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.net.URI

/**
 * 应用程序状态管理
 */
class AppState {
    // 配置状态
    var serverUrl by mutableStateOf("http://localhost:8096")
    var apiToken by mutableStateOf("")
    var selectedUserId by mutableStateOf("")
    var rememberConnection by mutableStateOf(true)  // 新增：是否记住连接信息
    
    // 连接状态
    var isConnected by mutableStateOf(false)
    var isConnecting by mutableStateOf(false)
    var connectionError by mutableStateOf("")
    
    // 用户列表
    var users by mutableStateOf<List<JellyfinUser>>(emptyList())
    
    // 导出状态
    var isExporting by mutableStateOf(false)
    var exportProgress by mutableStateOf("")
    var exportedData by mutableStateOf<JellyfinExportData?>(null)
    var exportError by mutableStateOf("")
    
    // 导入状态
    var isImporting by mutableStateOf(false)
    var importProgress by mutableStateOf("")
    var importResult by mutableStateOf<ImportResult?>(null)
    var importError by mutableStateOf("")
    
    // 文件选择
    var selectedExportFile by mutableStateOf<File?>(null)
    var selectedImportFile by mutableStateOf<File?>(null)
    
    // 日志
    var logs by mutableStateOf<List<String>>(emptyList())
    var enableRealTimeLogs by mutableStateOf(true)
    var showDetailedLogs by mutableStateOf(false)
    
    // 重复电影检测
    var isDuplicateScanning by mutableStateOf(false)
    var duplicateScanProgress by mutableStateOf("")
    var duplicateScanError by mutableStateOf("")
    var duplicateMovieResult by mutableStateOf<DuplicateMovieResult?>(null)

    private var apiClient: JellyfinApiClient? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // 公共访问属性
    val config: JellyfinConfig?
        get() = apiClient?.config
    
    fun getApiClient(): JellyfinApiClient? = apiClient
    
    init {
        // 应用启动时自动加载配置（不在初始化时立即调用，而是延迟调用）
        scope.launch {
            try {
                // 优先加载新的AppConfig
                val appConfig = ConfigManager.loadAppConfig()
                if (appConfig?.jellyfinConfig != null && appConfig.jellyfinConfig.isValid()) {
                    val config = appConfig.jellyfinConfig
                    serverUrl = config.serverUrl
                    apiToken = config.apiToken
                    selectedUserId = config.userId ?: ""
                    
                    // 应用其他设置
                    rememberConnection = appConfig.rememberConnection
                    enableRealTimeLogs = appConfig.enableRealTimeLogs
                    showDetailedLogs = appConfig.showDetailedLogs
                    
                    // 延迟日志记录，避免在UI未初始化时调用
                    kotlinx.coroutines.delay(100)
                    if (enableRealTimeLogs) {
                        addLog("✓ 已从配置文件加载连接信息")
                        addDetailedLog("服务器: ${config.serverUrl}")
                    }
                } else {
                    // 兼容性：尝试加载旧的Jellyfin配置
                    val config = ConfigManager.loadConfig()
                    if (config != null && config.isValid()) {
                        serverUrl = config.serverUrl
                        apiToken = config.apiToken
                        selectedUserId = config.userId ?: ""
                        // 延迟日志记录，避免在UI未初始化时调用
                        kotlinx.coroutines.delay(100)
                        if (enableRealTimeLogs) {
                            addLog("✓ 已从旧配置文件加载连接信息")
                            addDetailedLog("服务器: ${config.serverUrl}")
                        }
                    }
                }
            } catch (e: Exception) {
                // 忽略初始化时的错误
            }
        }
    }
    
    /**
     * 添加日志
     */
    fun addLog(message: String, isDetailed: Boolean = false) {
        if (!enableRealTimeLogs) return
        if (isDetailed && !showDetailedLogs) return

        val timestamp = java.time.LocalTime.now().toString().substring(0, 8)
        val logMessage = "[$timestamp] $message"
        logs = logs + logMessage

        // 限制日志数量，避免内存溢出
        if (logs.size > 1000) {
            logs = logs.takeLast(800)
        }
    }

    /**
     * 添加详细日志
     */
    fun addDetailedLog(message: String) {
        addLog("🔍 $message", isDetailed = true)
    }

    /**
     * 添加进度日志
     */
    fun addProgressLog(current: Int, total: Int, item: String) {
        addLog("📊 进度 ($current/$total): $item")
    }
    
    /**
     * 清除日志
     */
    fun clearLogs() {
        logs = emptyList()
    }
    
    /**
     * 加载配置
     */
    fun loadConfig() {
        scope.launch {
            try {
                val config = ConfigManager.loadConfig()
                if (config != null && config.isValid()) {
                    serverUrl = config.serverUrl
                    apiToken = config.apiToken
                    selectedUserId = config.userId ?: ""
                    addLog("✓ 已重新加载连接信息")
                    addDetailedLog("服务器: ${config.serverUrl}")
                } else {
                    addLog("未找到有效的配置文件")
                }
            } catch (e: Exception) {
                addLog("加载配置失败: ${e.message}")
            }
        }
    }
    
    /**
     * 保存配置
     */
    fun saveConfig() {
        if (!rememberConnection) {
            addLog("未启用记住连接信息，跳过保存")
            return
        }
        
        scope.launch {
            try {
                // 保存到新的AppConfig系统
                val jellyfinConfig = JellyfinConfig(
                    serverUrl = serverUrl,
                    apiToken = apiToken,
                    userId = selectedUserId.takeIf { it.isNotBlank() }
                )
                
                val currentAppConfig = ConfigManager.loadAppConfig() ?: AppConfig.default()
                val updatedAppConfig = currentAppConfig.copy(
                    jellyfinConfig = jellyfinConfig,
                    rememberConnection = rememberConnection,
                    enableRealTimeLogs = enableRealTimeLogs,
                    showDetailedLogs = showDetailedLogs
                )
                
                ConfigManager.saveAppConfig(updatedAppConfig)
                addLog("✓ 连接信息已保存")
            } catch (e: Exception) {
                addLog("保存配置失败: ${e.message}")
            }
        }
    }
    
    /**
     * 删除保存的配置
     */
    fun deleteConfig() {
        scope.launch {
            try {
                ConfigManager.deleteConfig()
                addLog("✓ 已删除保存的连接信息")
            } catch (e: Exception) {
                addLog("删除配置失败: ${e.message}")
            }
        }
    }
    
    /**
     * 检查是否有保存的配置
     */
    fun hasSavedConfig(): Boolean {
        return ConfigManager.configExists()
    }
    
    /**
     * 测试连接
     */
    fun testConnection() {
        if (serverUrl.isBlank() || apiToken.isBlank()) {
            connectionError = "请填写服务器URL和API Token"
            return
        }
        
        isConnecting = true
        connectionError = ""
        addLog("正在测试连接到: $serverUrl")
        
        scope.launch {
            try {
                val config = JellyfinConfig(serverUrl, apiToken)
                apiClient?.close()
                apiClient = JellyfinApiClient(config)
                
                val connected = apiClient!!.testConnection()
                if (connected) {
                    isConnected = true
                    addLog("✓ 连接成功！")
                    
                    // 获取用户列表
                    val userList = apiClient!!.getUsers()
                    users = userList
                    if (userList.isNotEmpty()) {
                        selectedUserId = userList.first().id
                        addLog("找到 ${userList.size} 个用户")
                        userList.forEach { user ->
                            addLog("  - ${user.name} (ID: ${user.id})")
                        }
                    }
                    
                    // 连接成功后自动保存配置
                    if (rememberConnection) {
                        saveConfig()
                    }
                } else {
                    isConnected = false
                    connectionError = "连接失败，请检查服务器URL和API Token"
                    addLog("✗ 连接失败")
                }
            } catch (e: Exception) {
                isConnected = false
                connectionError = "连接错误: ${e.message}"
                addLog("✗ 连接错误: ${e.message}")
            } finally {
                isConnecting = false
            }
        }
    }
    
    /**
     * 导出喜爱内容
     */
    fun exportFavorites() {
        if (!isConnected || apiClient == null || selectedUserId.isBlank()) {
            exportError = "请先连接到服务器并选择用户"
            return
        }

        isExporting = true
        exportError = ""
        exportProgress = "开始导出..."
        addLog("🚀 开始导出用户 $selectedUserId 的喜爱内容")
        addDetailedLog("服务器: ${apiClient!!.config.serverUrl}")
        addDetailedLog("用户ID: $selectedUserId")

        scope.launch {
            try {
                val service = ExportImportService(apiClient!!)

                // 获取喜爱的电影
                exportProgress = "正在获取喜爱的电影..."
                addLog("🎬 正在获取喜爱的电影...")
                addDetailedLog("发送API请求: /Users/$selectedUserId/Items?IncludeItemTypes=Movie&Filters=IsFavorite")

                val favoriteMovies = apiClient!!.getFavoriteMovies(selectedUserId)
                addLog("✓ 找到 ${favoriteMovies.size} 部喜爱的电影")

                // 记录详细的电影信息
                if (showDetailedLogs && favoriteMovies.isNotEmpty()) {
                    addDetailedLog("电影列表:")
                    favoriteMovies.forEachIndexed { index, movie ->
                        addDetailedLog("  ${index + 1}. ${movie.name} (${movie.productionYear ?: "未知年份"})")
                    }
                }

                // 获取喜爱的演员
                exportProgress = "正在获取喜爱的演员..."
                addLog("👥 正在获取喜爱的演员...")
                addDetailedLog("发送API请求: /Users/$selectedUserId/Items?IncludeItemTypes=Person&Filters=IsFavorite")

                val favoritePeople = apiClient!!.getFavoritePeople(selectedUserId)
                addLog("✓ 找到 ${favoritePeople.size} 位喜爱的演员")

                // 记录详细的演员信息
                if (showDetailedLogs && favoritePeople.isNotEmpty()) {
                    addDetailedLog("演员列表:")
                    favoritePeople.forEachIndexed { index, person ->
                        addDetailedLog("  ${index + 1}. ${person.name} (${person.type ?: "演员"})")
                    }
                }

                // 创建导出数据
                exportProgress = "正在生成导出数据..."
                addLog("📦 正在生成导出数据...")

                val exportData = JellyfinExportData(
                    exportDate = java.time.LocalDateTime.now().toString(),
                    serverUrl = apiClient!!.config.serverUrl,
                    userId = selectedUserId,
                    favoriteMovies = favoriteMovies,
                    favoritePeople = favoritePeople
                )

                exportedData = exportData
                exportProgress = "导出完成！"

                addLog("✅ 导出完成！")
                addLog("📊 统计信息:")
                addLog("  🎬 电影: ${exportData.favoriteMovies.size} 部")
                addLog("  👥 演员: ${exportData.favoritePeople.size} 位")
                addLog("  📅 导出时间: ${exportData.exportDate}")

                addDetailedLog("导出数据大小: ${service.exportToJson(exportData).length} 字符")

            } catch (e: Exception) {
                exportError = "导出失败: ${e.message}"
                addLog("❌ 导出失败: ${e.message}")
                addDetailedLog("错误堆栈: ${e.stackTraceToString()}")
            } finally {
                isExporting = false
            }
        }
    }
    
    /**
     * 保存导出数据到文件
     */
    fun saveExportData(file: File) {
        exportedData?.let { data ->
            try {
                val service = ExportImportService(apiClient!!)
                val jsonString = service.exportToJson(data)
                file.writeText(jsonString)
                addLog("✓ 数据已保存到: ${file.absolutePath}")
            } catch (e: Exception) {
                exportError = "保存失败: ${e.message}"
                addLog("✗ 保存失败: ${e.message}")
            }
        }
    }
    
    /**
     * 导入喜爱内容
     */
    fun importFavorites(file: File) {
        if (!isConnected || apiClient == null || selectedUserId.isBlank()) {
            importError = "请先连接到服务器并选择用户"
            return
        }

        isImporting = true
        importError = ""
        importProgress = "开始导入..."
        addLog("🚀 开始从文件导入: ${file.name}")
        addDetailedLog("文件路径: ${file.absolutePath}")
        addDetailedLog("文件大小: ${file.length()} 字节")

        scope.launch {
            try {
                // 读取和解析文件
                importProgress = "正在读取文件..."
                addLog("📖 正在读取导入文件...")

                val jsonString = file.readText()
                addDetailedLog("文件内容长度: ${jsonString.length} 字符")

                val service = ExportImportService(apiClient!!)
                val importData = service.parseFromJson(jsonString)

                addLog("✓ 文件解析成功")
                addLog("📊 导入数据统计:")
                addLog("  🎬 电影: ${importData.favoriteMovies.size} 部")
                addLog("  👥 演员: ${importData.favoritePeople.size} 位")
                addLog("  📅 原始导出时间: ${importData.exportDate}")
                addLog("  🌐 原始服务器: ${importData.serverUrl}")

                if (showDetailedLogs) {
                    addDetailedLog("电影列表预览:")
                    importData.favoriteMovies.take(5).forEachIndexed { index, movie ->
                        addDetailedLog("  ${index + 1}. ${movie.name} (${movie.productionYear ?: "未知"})")
                    }
                    if (importData.favoriteMovies.size > 5) {
                        addDetailedLog("  ... 还有 ${importData.favoriteMovies.size - 5} 部电影")
                    }

                    addDetailedLog("演员列表预览:")
                    importData.favoritePeople.take(5).forEachIndexed { index, person ->
                        addDetailedLog("  ${index + 1}. ${person.name} (${person.type ?: "演员"})")
                    }
                    if (importData.favoritePeople.size > 5) {
                        addDetailedLog("  ... 还有 ${importData.favoritePeople.size - 5} 位演员")
                    }
                }

                // 开始导入过程
                importProgress = "正在导入电影..."
                addLog("🎬 开始导入电影...")

                var importedMovies = 0
                var failedMovies = 0
                val movieErrors = mutableListOf<String>()

                // 导入电影
                importData.favoriteMovies.forEachIndexed { index, movie ->
                    try {
                        addProgressLog(index + 1, importData.favoriteMovies.size, movie.name)
                        addDetailedLog("尝试导入电影: ${movie.name} (ID: ${movie.id})")

                        // 首先尝试通过ID直接设置
                        var success = apiClient!!.setMovieFavorite(selectedUserId, movie.id, true)

                        if (!success) {
                            addDetailedLog("ID匹配失败，尝试名称搜索: ${movie.name}")
                            // 如果失败，尝试通过名称搜索
                            val searchResults = apiClient!!.searchMovieByName(movie.name)
                            val matchedMovie = searchResults.find { it.name == movie.name || it.originalTitle == movie.name }

                            if (matchedMovie != null) {
                                addDetailedLog("找到匹配电影: ${matchedMovie.name} (新ID: ${matchedMovie.id})")
                                success = apiClient!!.setMovieFavorite(selectedUserId, matchedMovie.id, true)
                            } else {
                                addDetailedLog("未找到匹配的电影: ${movie.name}")
                            }
                        }

                        if (success) {
                            importedMovies++
                            addDetailedLog("✓ 成功导入: ${movie.name}")
                        } else {
                            failedMovies++
                            val error = "✗ 导入失败: ${movie.name}"
                            movieErrors.add(error)
                            addDetailedLog(error)
                        }

                        // 添加延迟以避免API限制
                        kotlinx.coroutines.delay(200)

                    } catch (e: Exception) {
                        failedMovies++
                        val error = "✗ 导入异常: ${movie.name} - ${e.message}"
                        movieErrors.add(error)
                        addDetailedLog(error)
                    }
                }

                addLog("✓ 电影导入完成: $importedMovies/${importData.favoriteMovies.size} 成功")

                // 导入演员
                importProgress = "正在导入演员..."
                addLog("👥 开始导入演员...")

                var importedPeople = 0
                var failedPeople = 0
                val peopleErrors = mutableListOf<String>()

                importData.favoritePeople.forEachIndexed { index, person ->
                    try {
                        addProgressLog(index + 1, importData.favoritePeople.size, person.name)
                        addDetailedLog("尝试导入演员: ${person.name} (ID: ${person.id})")

                        // 首先尝试通过ID直接设置
                        var success = apiClient!!.setPersonFavorite(selectedUserId, person.id, true)

                        if (!success) {
                            addDetailedLog("ID匹配失败，尝试名称搜索: ${person.name}")
                            // 如果失败，尝试通过名称搜索
                            val searchResults = apiClient!!.searchPersonByName(person.name)
                            val matchedPerson = searchResults.find { it.name == person.name }

                            if (matchedPerson != null) {
                                addDetailedLog("找到匹配演员: ${matchedPerson.name} (新ID: ${matchedPerson.id})")
                                success = apiClient!!.setPersonFavorite(selectedUserId, matchedPerson.id, true)
                            } else {
                                addDetailedLog("未找到匹配的演员: ${person.name}")
                            }
                        }

                        if (success) {
                            importedPeople++
                            addDetailedLog("✓ 成功导入: ${person.name}")
                        } else {
                            failedPeople++
                            val error = "✗ 导入失败: ${person.name}"
                            peopleErrors.add(error)
                            addDetailedLog(error)
                        }

                        // 添加延迟以避免API限制
                        kotlinx.coroutines.delay(200)

                    } catch (e: Exception) {
                        failedPeople++
                        val error = "✗ 导入异常: ${person.name} - ${e.message}"
                        peopleErrors.add(error)
                        addDetailedLog(error)
                    }
                }

                addLog("✓ 演员导入完成: $importedPeople/${importData.favoritePeople.size} 成功")

                // 创建导入结果
                val allErrors = movieErrors + peopleErrors
                val result = ImportResult(
                    totalMovies = importData.favoriteMovies.size,
                    importedMovies = importedMovies,
                    failedMovies = failedMovies,
                    totalPeople = importData.favoritePeople.size,
                    importedPeople = importedPeople,
                    failedPeople = failedPeople,
                    errors = allErrors
                )

                importResult = result
                importProgress = "导入完成！"

                addLog("✅ 导入完成！")
                addLog("📊 最终统计:")
                addLog("  🎬 电影: $importedMovies/${importData.favoriteMovies.size} 成功")
                addLog("  👥 演员: $importedPeople/${importData.favoritePeople.size} 成功")

                if (allErrors.isNotEmpty()) {
                    addLog("⚠️ 遇到 ${allErrors.size} 个错误")
                    if (!showDetailedLogs) {
                        addLog("💡 启用详细日志可查看具体错误信息")
                    }
                }

            } catch (e: Exception) {
                importError = "导入失败: ${e.message}"
                addLog("❌ 导入失败: ${e.message}")
                addDetailedLog("错误堆栈: ${e.stackTraceToString()}")
            } finally {
                isImporting = false
            }
        }
    }
    
    /**
     * 打开电影的Jellyfin页面
     */
    fun openMovieInJellyfin(movieId: String) {
        val baseUrl = serverUrl.trimEnd('/')
        val movieUrl = "$baseUrl/web/index.html#!/details?id=$movieId"
        
        try {
            println("按钮被点击，电影ID: $movieId")
            println("尝试打开URL: $movieUrl")
            println("Desktop支持状态: ${Desktop.isDesktopSupported()}")
            
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                println("Desktop浏览功能支持: ${desktop.isSupported(Desktop.Action.BROWSE)}")
                
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(movieUrl))
                    addLog("🌐 已打开电影页面: $movieUrl")
                    println("浏览器调用成功")
                    return
                } else {
                    println("系统不支持浏览器功能")
                }
            } else {
                println("系统不支持桌面操作")
            }
            
            // 备用方案：使用系统命令打开浏览器
            val osName = System.getProperty("os.name").lowercase()
            val command = when {
                osName.contains("windows") -> arrayOf("cmd", "/c", "start", movieUrl)
                osName.contains("mac") -> arrayOf("open", movieUrl)
                else -> arrayOf("xdg-open", movieUrl) // Linux和其他Unix系统
            }
            
            println("尝试使用系统命令打开浏览器: ${command.joinToString(" ")}")
            
            val processBuilder = ProcessBuilder(*command)
            processBuilder.start()
            addLog("🌐 已通过系统命令打开电影页面: $movieUrl")
            println("系统命令调用成功")
            
        } catch (e: Exception) {
            val errorMsg = "打开电影页面失败: ${e.message}"
            addLog("❌ $errorMsg")
            addLog("💡 您可以手动复制链接到浏览器打开：$movieUrl")
            println("异常: $errorMsg")
            e.printStackTrace()
        }
    }
    
    /**
     * 扫描重复电影
     */
    fun scanForDuplicateMovies() {
        if (!isConnected || apiClient == null || selectedUserId.isBlank()) {
            duplicateScanError = "请先连接到服务器并选择用户"
            return
        }
        
        isDuplicateScanning = true
        duplicateScanError = ""
        duplicateScanProgress = "开始扫描..."
        duplicateMovieResult = null
        addLog("🔍 开始扫描重复电影")
        
        scope.launch {
            try {
                val service = DuplicateMovieService(apiClient!!)
                
                duplicateScanProgress = "正在获取所有电影..."
                addLog("📖 正在获取所有电影...")
                
                val result = service.detectDuplicateMovies(selectedUserId)
                
                duplicateMovieResult = result
                duplicateScanProgress = "扫描完成！"
                
                addLog("✅ 重复电影扫描完成")
                addLog("📊 扫描结果:")
                addLog("  🎬 总电影数: ${result.totalMovies}")
                addLog("  🔄 重复组数: ${result.duplicateGroups.size}")
                addLog("  📝 重复电影: ${result.totalDuplicates}")
                
                if (result.duplicateGroups.isNotEmpty()) {
                    addLog("⚠️ 发现重复电影，请查看重复检测标签页")
                    result.duplicateGroups.take(3).forEach { group ->
                        addLog("  - ${group.name} (${group.duplicateCount}个版本)")
                    }
                } else {
                    addLog("✨ 太好了！没有发现重复电影")
                }
                
            } catch (e: Exception) {
                duplicateScanError = "扫描失败: ${e.message}"
                addLog("❌ 重复电影扫描失败: ${e.message}")
                addDetailedLog("错误堆栈: ${e.stackTraceToString()}")
            } finally {
                isDuplicateScanning = false
            }
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        apiClient?.close()
        apiClient = null
        isConnected = false
        users = emptyList()
        selectedUserId = ""
        addLog("已断开连接")
    }
}
