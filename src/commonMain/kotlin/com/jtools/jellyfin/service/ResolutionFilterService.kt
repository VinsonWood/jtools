package com.jtools.jellyfin.service

import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.model.JellyfinMovie
import com.jtools.jellyfin.model.MediaSource
import com.jtools.jellyfin.model.MediaStream
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 分辨率筛选服务
 */
class ResolutionFilterService(private val apiClient: JellyfinApiClient) {
    
    /**
     * 分辨率类型枚举
     */
    enum class ResolutionType(val displayName: String, val minWidth: Int, val minHeight: Int) {
        SD_480P("480p及以下", 0, 0),
        HD_720P("720p及以下", 0, 0),
        HD_1080P("1080p及以下", 0, 0),
        UHD_4K("4K及以下", 0, 0),
        CUSTOM("自定义", 0, 0);
        
        companion object {
            fun getMaxResolution(type: ResolutionType): Pair<Int, Int> {
                return when (type) {
                    SD_480P -> Pair(854, 480)
                    HD_720P -> Pair(1280, 720)
                    HD_1080P -> Pair(1920, 1080)
                    UHD_4K -> Pair(3840, 2160)
                    CUSTOM -> Pair(0, 0)
                }
            }
        }
    }
    
    /**
     * 筛选条件
     */
    @Serializable
    data class ResolutionFilter(
        val maxWidth: Int = 0,
        val maxHeight: Int = 0,
        val includeUnknown: Boolean = false,
        val resolutionType: ResolutionType = ResolutionType.HD_1080P
    )
    
    /**
     * 筛选结果
     */
    @Serializable
    data class ResolutionFilterResult(
        val totalMovies: Int,
        val filteredMovies: List<MovieWithResolution>,
        val filteredCount: Int = filteredMovies.size,
        val scanDate: String
    )
    
    /**
     * 带分辨率信息的电影
     */
    @Serializable
    data class MovieWithResolution(
        val movie: JellyfinMovie,
        val resolution: ResolutionInfo
    )
    
    /**
     * 分辨率信息
     */
    @Serializable
    data class ResolutionInfo(
        val width: Int?,
        val height: Int?,
        val displayResolution: String,
        val isLowResolution: Boolean,
        val source: String // 分辨率信息来源: "movie", "mediaSource", "mediaStream"
    )
    
    /**
     * 获取所有低分辨率电影
     */
    suspend fun getMoviesBelowResolution(
        userId: String,
        filter: ResolutionFilter
    ): ResolutionFilterResult {
        try {
            println("开始筛选低分辨率电影...")
            
            // 获取所有电影
            val allMovies = apiClient.getAllMovies(userId)
            println("共获取到 ${allMovies.size} 部电影")
            
            // 确定最大分辨率
            val (maxWidth, maxHeight) = if (filter.resolutionType == ResolutionType.CUSTOM) {
                Pair(filter.maxWidth, filter.maxHeight)
            } else {
                ResolutionType.getMaxResolution(filter.resolutionType)
            }
            
            println("筛选条件: 最大分辨率 ${maxWidth}x${maxHeight}")
            
            // 分析每部电影的分辨率
            val moviesWithResolution = allMovies.map { movie ->
                val resolutionInfo = getMovieResolutionInfo(movie)
                val isLowResolution = isLowResolution(resolutionInfo, maxWidth, maxHeight, filter.includeUnknown)
                
                MovieWithResolution(
                    movie = movie,
                    resolution = resolutionInfo.copy(isLowResolution = isLowResolution)
                )
            }
            
            // 筛选出低分辨率电影
            val filteredMovies = moviesWithResolution.filter { it.resolution.isLowResolution }
            
            println("筛选完成: 找到 ${filteredMovies.size} 部低分辨率电影")
            
            return ResolutionFilterResult(
                totalMovies = allMovies.size,
                filteredMovies = filteredMovies,
                scanDate = Clock.System.now().toString()
            )
            
        } catch (e: Exception) {
            println("筛选低分辨率电影失败: ${e.message}")
            e.printStackTrace()
            return ResolutionFilterResult(
                totalMovies = 0,
                filteredMovies = emptyList(),
                scanDate = Clock.System.now().toString()
            )
        }
    }
    
    /**
     * 获取电影分辨率信息
     */
    internal fun getMovieResolutionInfo(movie: JellyfinMovie): ResolutionInfo {
        // 优先从电影直接字段获取
        if (movie.width != null && movie.height != null) {
            return ResolutionInfo(
                width = movie.width,
                height = movie.height,
                displayResolution = "${movie.width}x${movie.height}",
                isLowResolution = false,
                source = "movie"
            )
        }
        
        // 从媒体源获取
        val mediaSource = movie.mediaSources.firstOrNull()
        if (mediaSource != null && mediaSource.width != null && mediaSource.height != null) {
            return ResolutionInfo(
                width = mediaSource.width,
                height = mediaSource.height,
                displayResolution = "${mediaSource.width}x${mediaSource.height}",
                isLowResolution = false,
                source = "mediaSource"
            )
        }
        
        // 从媒体流获取
        val videoStream = mediaSource?.mediaStreams?.firstOrNull { it.type == "Video" }
        if (videoStream != null && videoStream.width != null && videoStream.height != null) {
            return ResolutionInfo(
                width = videoStream.width,
                height = videoStream.height,
                displayResolution = "${videoStream.width}x${videoStream.height}",
                isLowResolution = false,
                source = "mediaStream"
            )
        }
        
        // 无法获取分辨率信息
        return ResolutionInfo(
            width = null,
            height = null,
            displayResolution = "未知",
            isLowResolution = false,
            source = "unknown"
        )
    }
    
    /**
     * 判断是否为低分辨率
     */
    internal fun isLowResolution(
        resolutionInfo: ResolutionInfo,
        maxWidth: Int,
        maxHeight: Int,
        includeUnknown: Boolean
    ): Boolean {
        val width = resolutionInfo.width
        val height = resolutionInfo.height
        
        // 如果无法获取分辨率信息
        if (width == null || height == null) {
            return includeUnknown
        }
        
        // 检查是否低于指定分辨率
        return width < maxWidth || height < maxHeight
    }
    
    /**
     * 获取分辨率统计信息
     */
    suspend fun getResolutionStatistics(userId: String): ResolutionStatistics {
        try {
            val allMovies = apiClient.getAllMovies(userId)
            val statistics = mutableMapOf<String, Int>()
            var unknownCount = 0
            
            allMovies.forEach { movie ->
                val resolutionInfo = getMovieResolutionInfo(movie)
                if (resolutionInfo.width != null && resolutionInfo.height != null) {
                    val category = categorizeResolution(resolutionInfo.width, resolutionInfo.height)
                    statistics[category] = statistics[category]?.plus(1) ?: 1
                } else {
                    unknownCount++
                }
            }
            
            return ResolutionStatistics(
                totalMovies = allMovies.size,
                resolutionCounts = statistics,
                unknownCount = unknownCount
            )
            
        } catch (e: Exception) {
            println("获取分辨率统计失败: ${e.message}")
            return ResolutionStatistics(0, emptyMap(), 0)
        }
    }
    
    /**
     * 分类分辨率
     */
    internal fun categorizeResolution(width: Int, height: Int): String {
        return when {
            width <= 854 && height <= 480 -> "480p及以下"
            width <= 1280 && height <= 720 -> "720p"
            width <= 1920 && height <= 1080 -> "1080p"
            width <= 3840 && height <= 2160 -> "4K"
            else -> "超高清"
        }
    }
}

/**
 * 分辨率统计信息
 */
@Serializable
data class ResolutionStatistics(
    val totalMovies: Int,
    val resolutionCounts: Map<String, Int>,
    val unknownCount: Int
)