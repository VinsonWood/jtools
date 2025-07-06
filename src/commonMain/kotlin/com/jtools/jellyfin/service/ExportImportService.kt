package com.jtools.jellyfin.service

import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.model.*
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 导入导出服务
 */
class ExportImportService(private val apiClient: JellyfinApiClient) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    /**
     * 导出喜爱的电影和演员
     */
    suspend fun exportFavorites(userId: String): JellyfinExportData {
        println("开始导出用户 $userId 的喜爱内容...")
        
        // 获取喜爱的电影
        println("正在获取喜爱的电影...")
        val favoriteMovies = apiClient.getFavoriteMovies(userId)
        println("找到 ${favoriteMovies.size} 部喜爱的电影")
        
        // 获取喜爱的演员
        println("正在获取喜爱的演员...")
        val favoritePeople = apiClient.getFavoritePeople(userId)
        if (favoritePeople.isEmpty()) {
            println("警告：没有找到喜爱的演员，请检查网络连接和API权限")
        } else {
            println("成功找到 ${favoritePeople.size} 位喜爱的演员")
        }
        
        val exportData = JellyfinExportData(
            exportDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            serverUrl = apiClient.config.serverUrl,
            userId = userId,
            favoriteMovies = favoriteMovies,
            favoritePeople = favoritePeople
        )
        
        println("导出完成！电影: ${favoriteMovies.size} 部，演员: ${favoritePeople.size} 位")
        return exportData
    }
    
    /**
     * 将导出数据转换为JSON字符串
     */
    fun exportToJson(exportData: JellyfinExportData): String {
        return json.encodeToString(exportData)
    }
    
    /**
     * 从JSON字符串解析导入数据
     */
    fun parseFromJson(jsonString: String): JellyfinExportData {
        return json.decodeFromString<JellyfinExportData>(jsonString)
    }
    
    /**
     * 导入喜爱的电影和演员
     */
    suspend fun importFavorites(userId: String, importData: JellyfinExportData): ImportResult {
        println("开始导入到用户 $userId...")
        
        var importedMovies = 0
        var failedMovies = 0
        var importedPeople = 0
        var failedPeople = 0
        val errors = mutableListOf<String>()
        
        // 导入电影
        println("正在导入 ${importData.favoriteMovies.size} 部电影...")
        for ((index, movie) in importData.favoriteMovies.withIndex()) {
            try {
                // 首先尝试通过ID直接设置
                var success = apiClient.setMovieFavorite(userId, movie.id, true)
                
                if (!success) {
                    // 如果失败，尝试通过名称搜索
                    val searchResults = apiClient.searchMovieByName(movie.name)
                    val matchedMovie = searchResults.find { it.name == movie.name || it.originalTitle == movie.name }
                    
                    if (matchedMovie != null) {
                        success = apiClient.setMovieFavorite(userId, matchedMovie.id, true)
                    }
                }
                
                if (success) {
                    importedMovies++
                    println("✓ 导入电影: ${movie.name} (${index + 1}/${importData.favoriteMovies.size})")
                } else {
                    failedMovies++
                    val error = "✗ 导入电影失败: ${movie.name}"
                    println(error)
                    errors.add(error)
                }
                
                // 添加延迟以避免API限制
                delay(200)
                
            } catch (e: Exception) {
                failedMovies++
                val error = "✗ 导入电影异常: ${movie.name} - ${e.message}"
                println(error)
                errors.add(error)
            }
        }
        
        // 导入演员
        println("正在导入 ${importData.favoritePeople.size} 位演员...")
        for ((index, person) in importData.favoritePeople.withIndex()) {
            try {
                // 首先尝试通过ID直接设置
                var success = apiClient.setPersonFavorite(userId, person.id, true)
                
                if (!success) {
                    // 如果失败，尝试通过名称搜索
                    val searchResults = apiClient.searchPersonByName(person.name)
                    val matchedPerson = searchResults.find { it.name == person.name }
                    
                    if (matchedPerson != null) {
                        success = apiClient.setPersonFavorite(userId, matchedPerson.id, true)
                    }
                }
                
                if (success) {
                    importedPeople++
                    println("✓ 导入演员: ${person.name} (${index + 1}/${importData.favoritePeople.size})")
                } else {
                    failedPeople++
                    val error = "✗ 导入演员失败: ${person.name}"
                    println(error)
                    errors.add(error)
                }
                
                // 添加延迟以避免API限制
                delay(200)
                
            } catch (e: Exception) {
                failedPeople++
                val error = "✗ 导入演员异常: ${person.name} - ${e.message}"
                println(error)
                errors.add(error)
            }
        }
        
        val result = ImportResult(
            totalMovies = importData.favoriteMovies.size,
            importedMovies = importedMovies,
            failedMovies = failedMovies,
            totalPeople = importData.favoritePeople.size,
            importedPeople = importedPeople,
            failedPeople = failedPeople,
            errors = errors
        )
        
        println("\n导入完成！")
        println("电影: $importedMovies/${importData.favoriteMovies.size} 成功")
        println("演员: $importedPeople/${importData.favoritePeople.size} 成功")
        if (errors.isNotEmpty()) {
            println("错误数量: ${errors.size}")
        }
        
        return result
    }
}
