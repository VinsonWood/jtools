package com.jtools.jellyfin.api

import com.jtools.jellyfin.config.JellyfinConfig
import com.jtools.jellyfin.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Jellyfin API客户端
 */
class JellyfinApiClient(val config: JellyfinConfig) {
    
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout
            connectTimeoutMillis = config.timeout
            socketTimeoutMillis = config.timeout
        }
        
        defaultRequest {
            header("X-Emby-Token", config.apiToken)
            header("Accept", "application/json")
        }
    }
    
    /**
     * 获取所有用户
     */
    suspend fun getUsers(): List<JellyfinUser> {
        return try {
            httpClient.get("${config.formattedServerUrl}Users").body()
        } catch (e: Exception) {
            println("获取用户列表失败: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 获取当前用户ID
     */
    suspend fun getCurrentUserId(): String? {
        return config.userId ?: getUsers().firstOrNull()?.id
    }
    
    /**
     * 获取所有电影
     */
    suspend fun getAllMovies(userId: String): List<JellyfinMovie> {
        return try {
            val response: JellyfinResponse<JellyfinMovie> = httpClient.get("${config.formattedServerUrl}Users/$userId/Items") {
                parameter("IncludeItemTypes", "Movie")
                parameter("Fields", "Genres,People,UserData,Overview,Path,FileName,DateCreated,MediaSources,Size,Container,Width,Height,AspectRatio,Bitrate,VideoCodec,AudioCodec,DateModified")
                parameter("Recursive", "true")
                parameter("Limit", "10000") // 获取大量电影
                parameter("SortBy", "SortName")
                parameter("SortOrder", "Ascending")
            }.body()
            
            response.items
        } catch (e: Exception) {
            println("获取所有电影失败: ${e.message}")
            emptyList()
        }
    }

    /**
     * 获取喜爱的电影
     */
    suspend fun getFavoriteMovies(userId: String): List<JellyfinMovie> {
        return try {
            val response: JellyfinResponse<JellyfinMovie> = httpClient.get("${config.formattedServerUrl}Users/$userId/Items") {
                parameter("IncludeItemTypes", "Movie")
                parameter("Filters", "IsFavorite")
                parameter("Fields", "Genres,People,UserData,Overview")
                parameter("Recursive", "true")
                parameter("Limit", "1000")
            }.body()
            
            response.items
        } catch (e: Exception) {
            println("获取喜爱电影失败: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 获取喜爱的演员
     */
    suspend fun getFavoritePeople(userId: String): List<JellyfinPerson> {
        return try {
            println("正在获取用户 $userId 的喜爱演员...")
            println("请求URL: ${config.formattedServerUrl}Persons")
            
            val response: JellyfinResponse<JellyfinPerson> = httpClient.get("${config.formattedServerUrl}Persons") {
                parameter("StartIndex", "0")
                parameter("Limit", "100")
                parameter("Fields", "PrimaryImageAspectRatio,SortName,PrimaryImageAspectRatio")
                parameter("ImageTypeLimit", "1")
                parameter("Recursive", "true")
                parameter("IsFavorite", "true")
                parameter("SortBy", "SortName")
                parameter("SortOrder", "Ascending")
                parameter("userId", userId)
            }.body()
            
            println("API响应: TotalRecordCount=${response.totalRecordCount}, Items=${response.items.size}")
            
            if (response.items.isEmpty()) {
                println("没有找到喜爱的演员。可能的原因:")
                println("1. 用户确实没有标记任何演员为喜爱")
                println("2. API权限不足")
                println("3. 用户ID不正确")
                
                // 尝试获取所有演员来验证API是否工作
                println("尝试获取所有演员（前10个）来验证API...")
                val testResponse: JellyfinResponse<JellyfinPerson> = httpClient.get("${config.formattedServerUrl}Persons") {
                    parameter("StartIndex", "0")
                    parameter("Limit", "10")
                    parameter("Fields", "PrimaryImageAspectRatio,SortName")
                    parameter("userId", userId)
                }.body()
                println("测试响应: 找到 ${testResponse.items.size} 个演员")
            } else {
                println("成功获取到 ${response.items.size} 个喜爱的演员")
            }
            
            response.items
        } catch (e: Exception) {
            println("获取喜爱演员失败: ${e.message}")
            println("错误详情: ${e.stackTraceToString()}")
            
            // 尝试简化请求来诊断问题
            try {
                println("尝试简化请求进行诊断...")
                val simpleResponse: JellyfinResponse<JellyfinPerson> = httpClient.get("${config.formattedServerUrl}Persons") {
                    parameter("Limit", "5")
                    parameter("userId", userId)
                }.body()
                println("简化请求成功: 找到 ${simpleResponse.items.size} 个演员")
            } catch (e2: Exception) {
                println("简化请求也失败: ${e2.message}")
            }
            
            emptyList()
        }
    }
    
    /**
     * 设置电影为喜爱
     */
    suspend fun setMovieFavorite(userId: String, movieId: String, isFavorite: Boolean): Boolean {
        return try {
            val endpoint = if (isFavorite) "FavoriteItems" else "FavoriteItems"
            val method = if (isFavorite) HttpMethod.Post else HttpMethod.Delete
            
            httpClient.request("${config.formattedServerUrl}Users/$userId/$endpoint/$movieId") {
                this.method = method
            }
            
            // 添加延迟以避免API限制
            delay(100)
            true
        } catch (e: Exception) {
            println("设置电影喜爱状态失败 (ID: $movieId): ${e.message}")
            false
        }
    }
    
    /**
     * 设置演员为喜爱
     */
    suspend fun setPersonFavorite(userId: String, personId: String, isFavorite: Boolean): Boolean {
        return try {
            val endpoint = if (isFavorite) "FavoriteItems" else "FavoriteItems"
            val method = if (isFavorite) HttpMethod.Post else HttpMethod.Delete
            
            httpClient.request("${config.formattedServerUrl}Users/$userId/$endpoint/$personId") {
                this.method = method
            }
            
            // 添加延迟以避免API限制
            delay(100)
            true
        } catch (e: Exception) {
            println("设置演员喜爱状态失败 (ID: $personId): ${e.message}")
            false
        }
    }
    
    /**
     * 根据名称搜索电影
     */
    suspend fun searchMovieByName(name: String): List<JellyfinMovie> {
        return try {
            val response: JellyfinResponse<JellyfinMovie> = httpClient.get("${config.formattedServerUrl}Items") {
                parameter("searchTerm", name)
                parameter("IncludeItemTypes", "Movie")
                parameter("Fields", "Genres,People,UserData,Overview")
                parameter("Recursive", "true")
                parameter("Limit", "10")
            }.body()
            
            response.items
        } catch (e: Exception) {
            println("搜索电影失败 (名称: $name): ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 根据名称搜索演员
     */
    suspend fun searchPersonByName(name: String): List<JellyfinPerson> {
        return try {
            val response: JellyfinResponse<JellyfinPerson> = httpClient.get("${config.formattedServerUrl}Items") {
                parameter("searchTerm", name)
                parameter("IncludeItemTypes", "Person")
                parameter("Fields", "UserData")
                parameter("Recursive", "true")
                parameter("Limit", "10")
            }.body()
            
            response.items
        } catch (e: Exception) {
            println("搜索演员失败 (名称: $name): ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 测试连接
     */
    suspend fun testConnection(): Boolean {
        return try {
            httpClient.get("${config.formattedServerUrl}System/Info")
            true
        } catch (e: Exception) {
            println("连接测试失败: ${e.message}")
            false
        }
    }
    
    /**
     * 关闭客户端
     */
    fun close() {
        httpClient.close()
    }
}
