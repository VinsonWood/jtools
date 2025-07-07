package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jtools.jellyfin.gui.state.AppState
import com.jtools.jellyfin.service.ResolutionFilterService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolutionFilterPanel(
    appState: AppState,
    uiScale: Float = 1.0f
) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf(ResolutionFilterService.ResolutionType.HD_1080P) }
    var customWidth by remember { mutableStateOf("1920") }
    var customHeight by remember { mutableStateOf("1080") }
    var includeUnknown by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    var statistics by remember { mutableStateOf<com.jtools.jellyfin.service.ResolutionStatistics?>(null) }
    var filterResult by remember { mutableStateOf<ResolutionFilterService.ResolutionFilterResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((24 * uiScale).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
    ) {
        // 标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
        ) {
            Icon(
                Icons.Default.VideoSettings,
                contentDescription = null,
                modifier = Modifier.size((32 * uiScale).dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = "分辨率筛选",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = (24 * uiScale).sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "筛选指定分辨率以下的电影",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        if (!appState.isConnected) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding((16 * uiScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "请先在主界面连接到Jellyfin服务器",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return
        }

        // 筛选条件设置
        Card {
            Column(
                modifier = Modifier.padding((16 * uiScale).dp),
                verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
            ) {
                Text(
                    text = "筛选条件",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                    fontWeight = FontWeight.Medium
                )

                // 分辨率预设选择
                Text(
                    text = "分辨率设置",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    fontWeight = FontWeight.Medium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    ResolutionFilterService.ResolutionType.values().forEach { type ->
                        if (type != ResolutionFilterService.ResolutionType.CUSTOM) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedResolution == type,
                                    onClick = { selectedResolution = type }
                                )
                                Spacer(modifier = Modifier.width((8 * uiScale).dp))
                                val (w, h) = ResolutionFilterService.ResolutionType.getMaxResolution(type)
                                Text(
                                    text = "${type.displayName} (${w}x${h})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                                )
                            }
                        }
                    }

                    // 自定义分辨率
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedResolution == ResolutionFilterService.ResolutionType.CUSTOM,
                            onClick = { selectedResolution = ResolutionFilterService.ResolutionType.CUSTOM }
                        )
                        Spacer(modifier = Modifier.width((8 * uiScale).dp))
                        Text(
                            text = "自定义",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                        )
                    }

                    if (selectedResolution == ResolutionFilterService.ResolutionType.CUSTOM) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { customWidth = it },
                                label = { Text("宽度") },
                                modifier = Modifier.width((120 * uiScale).dp)
                            )
                            Text("×")
                            OutlinedTextField(
                                value = customHeight,
                                onValueChange = { customHeight = it },
                                label = { Text("高度") },
                                modifier = Modifier.width((120 * uiScale).dp)
                            )
                        }
                    }
                }

                // 其他选项
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeUnknown,
                        onCheckedChange = { includeUnknown = it }
                    )
                    Spacer(modifier = Modifier.width((8 * uiScale).dp))
                    Text(
                        text = "包含未知分辨率的电影",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                    )
                }

                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isScanning = true
                                    appState.getApiClient()?.let { client ->
                                        val resolutionService = ResolutionFilterService(client)
                                        statistics = resolutionService.getResolutionStatistics(appState.selectedUserId)
                                        showStatistics = true
                                    }
                                } catch (e: Exception) {
                                    appState.addLog("获取分辨率统计失败: ${e.message}")
                                } finally {
                                    isScanning = false
                                }
                            }
                        },
                        enabled = !isScanning
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((16 * uiScale).dp),
                                strokeWidth = (2 * uiScale).dp
                            )
                        } else {
                            Icon(Icons.Default.BarChart, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width((8 * uiScale).dp))
                        Text("显示统计")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isScanning = true
                                    appState.getApiClient()?.let { client ->
                                        val resolutionService = ResolutionFilterService(client)
                                        
                                        val (filterWidth, filterHeight) = if (selectedResolution == ResolutionFilterService.ResolutionType.CUSTOM) {
                                            try {
                                                Pair(customWidth.toInt(), customHeight.toInt())
                                            } catch (e: NumberFormatException) {
                                                appState.addLog("自定义分辨率格式错误")
                                                return@launch
                                            }
                                        } else {
                                            ResolutionFilterService.ResolutionType.getMaxResolution(selectedResolution)
                                        }

                                        val filter = ResolutionFilterService.ResolutionFilter(
                                            maxWidth = filterWidth,
                                            maxHeight = filterHeight,
                                            includeUnknown = includeUnknown,
                                            resolutionType = selectedResolution
                                        )

                                        appState.addLog("开始筛选分辨率低于 ${filterWidth}x${filterHeight} 的电影...")
                                        filterResult = resolutionService.getMoviesBelowResolution(appState.selectedUserId, filter)
                                        appState.addLog("筛选完成: 找到 ${filterResult?.filteredCount ?: 0} 部低分辨率电影")
                                    }
                                } catch (e: Exception) {
                                    appState.addLog("分辨率筛选失败: ${e.message}")
                                } finally {
                                    isScanning = false
                                }
                            }
                        },
                        enabled = !isScanning
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size((16 * uiScale).dp),
                                strokeWidth = (2 * uiScale).dp
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width((8 * uiScale).dp))
                        Text("开始筛选")
                    }
                }
            }
        }

        // 显示统计信息
        if (showStatistics && statistics != null) {
            Card {
                Column(
                    modifier = Modifier.padding((16 * uiScale).dp),
                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "分辨率统计",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "总电影数: ${statistics!!.totalMovies}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                    )
                    
                    if (statistics!!.unknownCount > 0) {
                        Text(
                            text = "未知分辨率: ${statistics!!.unknownCount}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    statistics!!.resolutionCounts.forEach { (resolution, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = resolution,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                            )
                            Text(
                                text = "$count 部",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 显示筛选结果
        filterResult?.let { result ->
            Card {
                Column(
                    modifier = Modifier.padding((16 * uiScale).dp),
                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "筛选结果",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "总电影数: ${result.totalMovies}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp)
                    )
                    Text(
                        text = "低分辨率电影: ${result.filteredCount}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                        color = if (result.filteredCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )

                    if (result.filteredMovies.isNotEmpty()) {
                        Text(
                            text = "电影列表:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                            fontWeight = FontWeight.Medium
                        )

                        LazyColumn(
                            modifier = Modifier.heightIn(max = (300 * uiScale).dp),
                            verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
                        ) {
                            items(result.filteredMovies) { movieWithRes ->
                                MovieWithResolutionCard(
                                    movieWithRes = movieWithRes,
                                    onOpenInJellyfin = { movie ->
                                        // 构造Jellyfin电影页面URL
                                        val baseUrl = appState.config?.formattedServerUrl ?: ""
                                        val movieUrl = "${baseUrl}web/index.html#!/details?id=${movie.id}"
                                        // 在这里可以添加打开URL的逻辑
                                        appState.addLog("电影链接: $movieUrl")
                                    },
                                    uiScale = uiScale
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "没有找到符合条件的低分辨率电影",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieWithResolutionCard(
    movieWithRes: ResolutionFilterService.MovieWithResolution,
    onOpenInJellyfin: (com.jtools.jellyfin.model.JellyfinMovie) -> Unit,
    uiScale: Float = 1.0f
) {
    val uriHandler = LocalUriHandler.current
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((12 * uiScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
        ) {
            Icon(
                Icons.Default.Movie,
                contentDescription = null,
                modifier = Modifier.size((20 * uiScale).dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = movieWithRes.movie.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${movieWithRes.movie.productionYear ?: "未知年份"} • ${movieWithRes.resolution.displayResolution}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = { onOpenInJellyfin(movieWithRes.movie) },
                modifier = Modifier.size((32 * uiScale).dp)
            ) {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "在Jellyfin中打开",
                    modifier = Modifier.size((16 * uiScale).dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}