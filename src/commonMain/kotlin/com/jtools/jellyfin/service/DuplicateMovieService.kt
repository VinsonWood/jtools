package com.jtools.jellyfin.service

import com.jtools.jellyfin.api.JellyfinApiClient
import com.jtools.jellyfin.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 重复电影检测服务
 */
class DuplicateMovieService(private val apiClient: JellyfinApiClient) {
    
    /**
     * 检测重复电影
     */
    suspend fun detectDuplicateMovies(userId: String): DuplicateMovieResult {
        println("开始检测重复电影...")
        
        // 获取所有电影
        val allMovies = apiClient.getAllMovies(userId)
        println("共获取到 ${allMovies.size} 部电影")
        
        // 按照电影名称分组
        val movieGroups = allMovies
            .groupBy { normalizeMovieName(it.name) }
            .mapValues { (_, movies) -> movies.sortedBy { it.name } }
            .filter { it.value.size > 1 } // 只保留有重复的组
        
        // 创建重复电影组
        val duplicateGroups = movieGroups.map { (normalizedName, movies) ->
            DuplicateMovieGroup(
                name = movies.first().name, // 使用第一个电影的原始名称
                movies = movies
            )
        }.sortedByDescending { it.duplicateCount }
        
        val result = DuplicateMovieResult(
            totalMovies = allMovies.size,
            duplicateGroups = duplicateGroups,
            scanDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        )
        
        println("检测完成：找到 ${duplicateGroups.size} 组重复电影，共 ${result.totalDuplicates} 个重复项")
        
        return result
    }
    
    /**
     * 标准化电影名称用于比较
     * 移除常见的变体以提高匹配准确性
     */
    private fun normalizeMovieName(name: String): String {
        return name
            .trim()
            .lowercase()
            // 移除年份 (例如: "电影 (2020)" -> "电影")
            .replace(Regex("""\s*\(\d{4}\)\s*"""), "")
            // 移除版本标识
            .replace(Regex("""\s*(director's cut|extended|unrated|remastered|4k|1080p|720p|bluray|dvd)\s*"""), "")
            // 移除多余的空格
            .replace(Regex("""\s+"""), " ")
            // 移除特殊字符
            .replace(Regex("""[^\w\s\u4e00-\u9fff]"""), "")
            .trim()
    }
    
    /**
     * 获取电影的详细信息用于显示
     */
    fun getMovieDisplayInfo(movie: JellyfinMovie): Map<String, String> {
        return mapOf(
            "名称" to movie.name,
            "原标题" to (movie.originalTitle ?: "无"),
            "年份" to (movie.productionYear?.toString() ?: "未知"),
            "类型" to movie.genres.joinToString(", ").ifEmpty { "未知" },
            "评分" to (movie.communityRating?.toString() ?: "无"),
            "时长" to formatRuntime(movie.runTimeTicks),
            "ID" to movie.id
        )
    }
    
    /**
     * 格式化运行时间
     */
    private fun formatRuntime(runTimeTicks: Long?): String {
        if (runTimeTicks == null) return "未知"
        
        val totalMinutes = (runTimeTicks / 10_000_000 / 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        
        return if (hours > 0) {
            "${hours}小时${minutes}分钟"
        } else {
            "${minutes}分钟"
        }
    }
    
    /**
     * 获取推荐删除的电影
     * 基于一些启发式规则来建议保留哪个版本
     */
    fun getRecommendedForDeletion(group: DuplicateMovieGroup): List<JellyfinMovie> {
        if (group.movies.size <= 1) return emptyList()
        
        // 按优先级排序：分辨率高 > 文件大小大 > 评分高 > 时长长 > 年份新 > 名称短
        val sortedMovies = group.movies.sortedWith(
            compareByDescending<JellyfinMovie> { movie ->
                // 分辨率优先级 (宽度 * 高度)
                val mediaSource = movie.mediaSources.firstOrNull()
                val width = mediaSource?.width ?: movie.width ?: 0
                val height = mediaSource?.height ?: movie.height ?: 0
                width * height
            }.thenByDescending { movie ->
                // 文件大小
                val mediaSource = movie.mediaSources.firstOrNull()
                mediaSource?.size ?: movie.size ?: 0L
            }.thenByDescending { movie ->
                // 码率
                val mediaSource = movie.mediaSources.firstOrNull()
                mediaSource?.bitrate ?: movie.bitrate ?: 0
            }.thenByDescending {
                // 评分
                it.communityRating ?: 0.0
            }.thenByDescending {
                // 时长
                it.runTimeTicks ?: 0L
            }.thenByDescending {
                // 年份
                it.productionYear ?: 0
            }.thenBy {
                // 名称长度（越短越好）
                it.name.length
            }
        )
        
        // 建议删除除了第一个（最高优先级）之外的所有电影
        return sortedMovies.drop(1)
    }
    
    /**
     * 获取电影的技术信息摘要
     */
    fun getMovieTechSummary(movie: JellyfinMovie): String {
        val parts = mutableListOf<String>()
        
        val mediaSource = movie.mediaSources.firstOrNull()
        val width = mediaSource?.width ?: movie.width
        val height = mediaSource?.height ?: movie.height
        val container = mediaSource?.container ?: movie.container
        val size = mediaSource?.size ?: movie.size
        val videoCodec = mediaSource?.videoCodec ?: movie.videoCodec
        
        if (width != null && height != null) {
            parts.add("${width}x${height}")
        }
        
        if (container != null) {
            parts.add(container.uppercase())
        }
        
        if (size != null) {
            parts.add(formatFileSize(size))
        }
        
        if (videoCodec != null) {
            parts.add(videoCodec.uppercase())
        }
        
        return parts.joinToString(" | ").ifEmpty { "信息不完整" }
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "${String.format("%.1f", size)}${units[unitIndex]}"
    }
}