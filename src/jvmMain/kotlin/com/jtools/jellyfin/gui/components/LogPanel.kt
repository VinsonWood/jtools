package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.gui.state.AppState

@Composable
fun LogPanel(appState: AppState) {
    val listState = rememberLazyListState()
    
    // 自动滚动到最新日志
    LaunchedEffect(appState.logs.size) {
        if (appState.logs.isNotEmpty()) {
            listState.animateScrollToItem(appState.logs.size - 1)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "操作日志",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (appState.logs.isNotEmpty()) {
                        AssistChip(
                            onClick = { },
                            label = { Text("${appState.logs.size}") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 日志状态指示
                    if (!appState.enableRealTimeLogs) {
                        AssistChip(
                            onClick = { },
                            label = { Text("实时日志已关闭", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }

                    if (appState.showDetailedLogs) {
                        AssistChip(
                            onClick = { },
                            label = { Text("详细模式", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 清除日志按钮
                    IconButton(
                        onClick = { appState.clearLogs() },
                        enabled = appState.logs.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "清除日志",
                            tint = if (appState.logs.isNotEmpty())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (appState.logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "暂无日志",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "操作日志将在这里显示",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(appState.logs) { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogItem(log: String) {
    val (icon, color) = when {
        log.contains("✅") || log.contains("✓") -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        log.contains("❌") || log.contains("✗") -> Icons.Default.Error to MaterialTheme.colorScheme.error
        log.contains("⚠️") -> Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
        log.contains("🚀") -> Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
        log.contains("📊") -> Icons.Default.Assessment to MaterialTheme.colorScheme.secondary
        log.contains("🎬") -> Icons.Default.Movie to MaterialTheme.colorScheme.primary
        log.contains("👥") -> Icons.Default.People to MaterialTheme.colorScheme.primary
        log.contains("📖") -> Icons.Default.Description to MaterialTheme.colorScheme.secondary
        log.contains("📦") -> Icons.Default.Archive to MaterialTheme.colorScheme.secondary
        log.contains("🔍") -> Icons.Default.Search to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        log.contains("💡") -> Icons.Default.Lightbulb to MaterialTheme.colorScheme.tertiary
        log.contains("正在") || log.contains("进度") -> Icons.Default.Sync to MaterialTheme.colorScheme.secondary
        else -> Icons.Default.Info to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        
        SelectionContainer {
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
