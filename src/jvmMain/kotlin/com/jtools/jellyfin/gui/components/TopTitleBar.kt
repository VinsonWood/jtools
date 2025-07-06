package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jtools.jellyfin.gui.state.AppState
import java.awt.Desktop
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTitleBar(appState: AppState, uiScale: Float = 1.0f) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = (1 * uiScale).dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (24 * uiScale).dp, vertical = (16 * uiScale).dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：当前操作状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
            ) {
                when {
                    appState.isExporting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size((20 * uiScale).dp),
                            strokeWidth = (2 * uiScale).dp
                        )
                        Column {
                            Text(
                                text = "正在导出",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = appState.exportProgress,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    appState.isImporting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size((20 * uiScale).dp),
                            strokeWidth = (2 * uiScale).dp
                        )
                        Column {
                            Text(
                                text = "正在导入",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = appState.importProgress,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    appState.isConnecting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size((20 * uiScale).dp),
                            strokeWidth = (2 * uiScale).dp
                        )
                        Column {
                            Text(
                                text = "正在连接",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "测试服务器连接...",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    appState.isConnected -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size((20 * uiScale).dp)
                        )
                        Column {
                            Text(
                                text = "就绪",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "可以开始导入导出操作",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    else -> {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size((20 * uiScale).dp)
                        )
                        Column {
                            Text(
                                text = "未连接",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "请先配置并连接到Jellyfin服务器",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // 右侧：操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日志控制开关
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Text(
                        text = "实时日志",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (11 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Switch(
                        checked = appState.enableRealTimeLogs,
                        onCheckedChange = { appState.enableRealTimeLogs = it },
                        modifier = Modifier.size((32 * uiScale).dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Text(
                        text = "详细日志",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (11 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Switch(
                        checked = appState.showDetailedLogs,
                        onCheckedChange = { appState.showDetailedLogs = it },
                        modifier = Modifier.size((32 * uiScale).dp)
                    )
                }
                
                Divider(
                    modifier = Modifier
                        .height((32 * uiScale).dp)
                        .width((1 * uiScale).dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                // 帮助按钮
                IconButton(
                    onClick = {
                        try {
                            Desktop.getDesktop().browse(URI("https://jellyfin.org"))
                        } catch (e: Exception) {
                            appState.addLog("无法打开浏览器: ${e.message}")
                        }
                    },
                    modifier = Modifier.size((48 * uiScale).dp)
                ) {
                    Icon(
                        Icons.Default.Help,
                        contentDescription = "帮助",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size((20 * uiScale).dp)
                    )
                }
                
                // 设置按钮
                IconButton(
                    onClick = {
                        // 可以添加快速设置功能
                    },
                    modifier = Modifier.size((48 * uiScale).dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size((20 * uiScale).dp)
                    )
                }
            }
        }
    }
}
