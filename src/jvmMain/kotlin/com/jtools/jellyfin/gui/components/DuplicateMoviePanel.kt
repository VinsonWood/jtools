package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.gui.state.AppState
import com.jtools.jellyfin.model.DuplicateMovieGroup
import com.jtools.jellyfin.model.JellyfinMovie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateMoviePanel(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FindInPage,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = "重复电影检测",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "检测媒体库中的重复电影",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 控制区域
        DuplicateMovieControlPanel(appState)

        // 结果区域
        appState.duplicateMovieResult?.let { result ->
            DuplicateMovieResults(result, appState)
        }
    }
}

@Composable
private fun DuplicateMovieControlPanel(appState: AppState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "重复检测控制",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "扫描媒体库中的所有电影，查找可能的重复项",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 连接状态提示
            if (!appState.isConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "请先连接到Jellyfin服务器",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 扫描进度
            if (appState.isDuplicateScanning) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = appState.duplicateScanProgress,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 扫描错误
            if (appState.duplicateScanError.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = appState.duplicateScanError,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 扫描按钮
            Button(
                onClick = { appState.scanForDuplicateMovies() },
                enabled = appState.isConnected && !appState.isDuplicateScanning && appState.selectedUserId.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (appState.isDuplicateScanning) "正在扫描..." else "开始扫描重复电影")
            }
        }
    }
}

@Composable
private fun DuplicateMovieResults(
    result: com.jtools.jellyfin.model.DuplicateMovieResult,
    appState: AppState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 结果摘要
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "扫描结果",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // 统计信息
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "扫描完成！",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "总电影数: ${result.totalMovies}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "重复组数: ${result.duplicateGroups.size}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "重复电影: ${result.totalDuplicates}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "扫描时间: ${result.scanDate.substringBefore('T')}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 重复电影列表
            if (result.duplicateGroups.isNotEmpty()) {
                Text(
                    text = "重复电影列表",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(result.duplicateGroups) { group ->
                        DuplicateMovieGroupCard(group, appState)
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "太好了！没有发现重复的电影。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuplicateMovieGroupCard(
    group: DuplicateMovieGroup,
    appState: AppState
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 组标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, false)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "${group.duplicateCount}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                    
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 详细信息
            if (expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.movies.forEachIndexed { index, movie ->
                        DuplicateMovieItemCard(movie, index == 0, appState)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateMovieItemCard(
    movie: JellyfinMovie,
    isRecommended: Boolean,
    appState: AppState
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecommended) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "推荐保留",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // 点击跳转按钮 - 增大尺寸和点击区域
                    ElevatedButton(
                        onClick = { 
                            println("按钮被点击，电影ID: ${movie.id}")
                            appState.openMovieInJellyfin(movie.id) 
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .defaultMinSize(minWidth = 80.dp)
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "查看电影详情",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "查看",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            // 基本信息行
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (movie.productionYear != null) {
                    InfoChip("年份", movie.productionYear.toString())
                }
                
                if (movie.communityRating != null) {
                    InfoChip("评分", "%.1f".format(movie.communityRating))
                }
                
                if (movie.runTimeTicks != null) {
                    val minutes = (movie.runTimeTicks / 10_000_000 / 60).toInt()
                    InfoChip("时长", "${minutes}分钟")
                }
            }
            
            // 技术信息行
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 分辨率和容器
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 从MediaSources获取信息，如果没有则从直接属性获取
                    val mediaSource = movie.mediaSources.firstOrNull()
                    val width = mediaSource?.width ?: movie.width
                    val height = mediaSource?.height ?: movie.height
                    val container = mediaSource?.container ?: movie.container
                    val size = mediaSource?.size ?: movie.size
                    
                    if (width != null && height != null) {
                        TechInfoChip("分辨率", "${width}x${height}")
                    }
                    
                    if (container != null) {
                        TechInfoChip("格式", container.uppercase())
                    }
                    
                    if (size != null) {
                        TechInfoChip("大小", formatFileSize(size))
                    }
                }
                
                // 编码器信息
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val mediaSource = movie.mediaSources.firstOrNull()
                    val videoCodec = mediaSource?.videoCodec ?: movie.videoCodec
                    val audioCodec = mediaSource?.audioCodec ?: movie.audioCodec
                    val bitrate = mediaSource?.bitrate ?: movie.bitrate
                    
                    if (videoCodec != null) {
                        TechInfoChip("视频", videoCodec.uppercase())
                    }
                    
                    if (audioCodec != null) {
                        TechInfoChip("音频", audioCodec.uppercase())
                    }
                    
                    if (bitrate != null) {
                        TechInfoChip("码率", "${bitrate / 1000}kbps")
                    }
                }
                
                // 文件路径信息
                if (movie.path != null) {
                    Text(
                        text = "路径: ${movie.path}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // ID信息
            Text(
                text = "ID: ${movie.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun TechInfoChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
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
    
    return "%.1f%s".format(size, units[unitIndex])
}