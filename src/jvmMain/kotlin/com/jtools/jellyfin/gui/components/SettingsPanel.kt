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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContentPanel(
    appState: AppState,
    uiScale: Float,
    onScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((16 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
    ) {
        // 标题
        Text(
            text = "设置",
            fontSize = (24 * uiScale).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = (8 * uiScale).dp)
        )

        // UI缩放设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = (2 * uiScale).dp)
        ) {
            Column(
                modifier = Modifier.padding((16 * uiScale).dp),
                verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "UI缩放",
                        modifier = Modifier.size((20 * uiScale).dp)
                    )
                    Text(
                        text = "界面缩放",
                        fontSize = (16 * uiScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "适配高分辨率屏幕，调整界面元素大小",
                    fontSize = (14 * uiScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = (28 * uiScale).dp)
                )

                // 缩放滑块
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
                ) {
                    Text(
                        text = "小",
                        fontSize = (12 * uiScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Slider(
                        value = uiScale,
                        onValueChange = onScaleChange,
                        valueRange = 0.8f..2.0f,
                        steps = 11,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "大",
                        fontSize = (12 * uiScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 当前缩放值
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "当前缩放: ${String.format("%.1f", uiScale)}x",
                        fontSize = (14 * uiScale).sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 预设缩放按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    val presets = listOf(
                        "小" to 0.8f,
                        "正常" to 1.0f,
                        "大" to 1.2f,
                        "超大" to 1.5f
                    )
                    
                    presets.forEach { (label, scale) ->
                        OutlinedButton(
                            onClick = { onScaleChange(scale) },
                            modifier = Modifier.weight(1f),
                            colors = if (kotlin.math.abs(uiScale - scale) < 0.1f) {
                                ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            }
                        ) {
                            Text(
                                text = label,
                                fontSize = (12 * uiScale).sp
                            )
                        }
                    }
                }
            }
        }

        // 其他设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = (2 * uiScale).dp)
        ) {
            Column(
                modifier = Modifier.padding((16 * uiScale).dp),
                verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "其他设置",
                        modifier = Modifier.size((20 * uiScale).dp)
                    )
                    Text(
                        text = "其他设置",
                        fontSize = (16 * uiScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 记住连接信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "记住连接信息",
                            fontSize = (14 * uiScale).sp
                        )
                        Text(
                            text = "自动保存服务器地址和API密钥",
                            fontSize = (12 * uiScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = appState.rememberConnection,
                        onCheckedChange = { appState.rememberConnection = it }
                    )
                }

                // 详细日志
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "详细日志",
                            fontSize = (14 * uiScale).sp
                        )
                        Text(
                            text = "显示更多调试信息",
                            fontSize = (12 * uiScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = appState.showDetailedLogs,
                        onCheckedChange = { appState.showDetailedLogs = it }
                    )
                }

                // 实时日志
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "实时日志",
                            fontSize = (14 * uiScale).sp
                        )
                        Text(
                            text = "启用实时日志更新",
                            fontSize = (12 * uiScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Switch(
                        checked = appState.enableRealTimeLogs,
                        onCheckedChange = { appState.enableRealTimeLogs = it }
                    )
                }
            }
        }
    }
}