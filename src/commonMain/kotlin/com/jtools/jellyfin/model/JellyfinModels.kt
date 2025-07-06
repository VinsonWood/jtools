package com.jtools.jellyfin.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jellyfin用户信息
 */
@Serializable
data class JellyfinUser(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
    @SerialName("ServerId") val serverId: String? = null,
    @SerialName("HasPassword") val hasPassword: Boolean = false,
    @SerialName("HasConfiguredPassword") val hasConfiguredPassword: Boolean = false,
    @SerialName("HasConfiguredEasyPassword") val hasConfiguredEasyPassword: Boolean = false,
    @SerialName("EnableAutoLogin") val enableAutoLogin: Boolean = false,
    @SerialName("LastLoginDate") val lastLoginDate: String? = null,
    @SerialName("LastActivityDate") val lastActivityDate: String? = null
)

/**
 * Jellyfin电影信息
 */
@Serializable
data class JellyfinMovie(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
    @SerialName("OriginalTitle") val originalTitle: String? = null,
    @SerialName("Overview") val overview: String? = null,
    @SerialName("ProductionYear") val productionYear: Int? = null,
    @SerialName("Genres") val genres: List<String> = emptyList(),
    @SerialName("CommunityRating") val communityRating: Double? = null,
    @SerialName("RunTimeTicks") val runTimeTicks: Long? = null,
    @SerialName("UserData") val userData: UserData? = null,
    @SerialName("People") val people: List<Person> = emptyList(),
    // 技术信息
    @SerialName("Path") val path: String? = null,
    @SerialName("FileName") val fileName: String? = null,
    @SerialName("Size") val size: Long? = null,
    @SerialName("Container") val container: String? = null,
    @SerialName("MediaSources") val mediaSources: List<MediaSource> = emptyList(),
    @SerialName("Width") val width: Int? = null,
    @SerialName("Height") val height: Int? = null,
    @SerialName("AspectRatio") val aspectRatio: String? = null,
    @SerialName("Bitrate") val bitrate: Int? = null,
    @SerialName("VideoCodec") val videoCodec: String? = null,
    @SerialName("AudioCodec") val audioCodec: String? = null,
    @SerialName("DateCreated") val dateCreated: String? = null,
    @SerialName("DateModified") val dateModified: String? = null
)

/**
 * 媒体源信息
 */
@Serializable
data class MediaSource(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String? = null,
    @SerialName("Path") val path: String? = null,
    @SerialName("Size") val size: Long? = null,
    @SerialName("Container") val container: String? = null,
    @SerialName("Bitrate") val bitrate: Int? = null,
    @SerialName("VideoType") val videoType: String? = null,
    @SerialName("Width") val width: Int? = null,
    @SerialName("Height") val height: Int? = null,
    @SerialName("AspectRatio") val aspectRatio: String? = null,
    @SerialName("VideoCodec") val videoCodec: String? = null,
    @SerialName("AudioCodec") val audioCodec: String? = null,
    @SerialName("MediaStreams") val mediaStreams: List<MediaStream> = emptyList()
)

/**
 * 媒体流信息
 */
@Serializable
data class MediaStream(
    @SerialName("Index") val index: Int,
    @SerialName("Codec") val codec: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("Width") val width: Int? = null,
    @SerialName("Height") val height: Int? = null,
    @SerialName("BitRate") val bitRate: Int? = null,
    @SerialName("Language") val language: String? = null,
    @SerialName("DisplayTitle") val displayTitle: String? = null
)

/**
 * Jellyfin演员信息
 */
@Serializable
data class JellyfinPerson(
    @SerialName("Id") val id: String,
    @SerialName("Name") val name: String,
    @SerialName("Type") val type: String? = null,
    @SerialName("Role") val role: String? = null,
    @SerialName("PrimaryImageTag") val primaryImageTag: String? = null,
    @SerialName("UserData") val userData: UserData? = null
)

/**
 * 人员信息（电影中的演员、导演等）
 */
@Serializable
data class Person(
    @SerialName("Id") val id: String? = null,
    @SerialName("Name") val name: String,
    @SerialName("Role") val role: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("PrimaryImageTag") val primaryImageTag: String? = null
)

/**
 * 用户数据（喜爱状态等）
 */
@Serializable
data class UserData(
    @SerialName("IsFavorite") val isFavorite: Boolean = false,
    @SerialName("Played") val played: Boolean = false,
    @SerialName("PlayCount") val playCount: Int = 0,
    @SerialName("PlaybackPositionTicks") val playbackPositionTicks: Long = 0,
    @SerialName("LastPlayedDate") val lastPlayedDate: String? = null
)

/**
 * Jellyfin API响应包装器
 */
@Serializable
data class JellyfinResponse<T>(
    @SerialName("Items") val items: List<T> = emptyList(),
    @SerialName("TotalRecordCount") val totalRecordCount: Int = 0,
    @SerialName("StartIndex") val startIndex: Int = 0
)

/**
 * 导出数据格式
 */
@Serializable
data class JellyfinExportData(
    val exportDate: String,
    val serverUrl: String,
    val userId: String,
    val favoriteMovies: List<JellyfinMovie> = emptyList(),
    val favoritePeople: List<JellyfinPerson> = emptyList()
)

/**
 * 导入结果
 */
@Serializable
data class ImportResult(
    val totalMovies: Int,
    val importedMovies: Int,
    val failedMovies: Int,
    val totalPeople: Int,
    val importedPeople: Int,
    val failedPeople: Int,
    val errors: List<String> = emptyList()
)

/**
 * 重复电影组
 */
@Serializable
data class DuplicateMovieGroup(
    val name: String,
    val movies: List<JellyfinMovie>,
    val duplicateCount: Int = movies.size
) {
    val isDuplicate: Boolean get() = duplicateCount > 1
}

/**
 * 重复电影检测结果
 */
@Serializable
data class DuplicateMovieResult(
    val totalMovies: Int,
    val duplicateGroups: List<DuplicateMovieGroup>,
    val totalDuplicates: Int = duplicateGroups.sumOf { it.duplicateCount - 1 },
    val scanDate: String
)
