package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.gui.state.AppState

@Composable
fun MainContentPanel(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 连接配置区域
        ConnectionPanel(appState)
        
        // 操作区域
        OperationsPanel(appState)
    }
}

@Composable
fun LogContentPanel(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LogPanel(appState)
    }
}

@Composable
fun SettingsContentPanel(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "应用设置",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // 日志设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "启用实时日志",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "实时显示操作过程中的日志信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = appState.enableRealTimeLogs,
                        onCheckedChange = { appState.enableRealTimeLogs = it }
                    )
                }
                
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "显示详细日志",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "显示API调用、数据解析等详细调试信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = appState.showDetailedLogs,
                        onCheckedChange = { appState.showDetailedLogs = it }
                    )
                }
                
                Divider()
                
                // 清除日志按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "清除所有日志",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "清空当前会话的所有日志记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Button(
                        onClick = { appState.clearLogs() },
                        enabled = appState.logs.isNotEmpty()
                    ) {
                        Text("清除日志")
                    }
                }
            }
        }
        
        // 连接设置
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "连接设置",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "断开连接",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "断开当前的Jellyfin服务器连接",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Button(
                        onClick = { appState.disconnect() },
                        enabled = appState.isConnected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("断开连接")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutContentPanel(appState: AppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "关于 jtools",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "版本: 1.0.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "一个用于导入导出Jellyfin喜爱电影和演员的跨平台工具。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Divider()
                
                Text(
                    text = "技术栈",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Kotlin Multiplatform", style = MaterialTheme.typography.bodyMedium)
                    Text("• Compose Desktop", style = MaterialTheme.typography.bodyMedium)
                    Text("• Material Design 3", style = MaterialTheme.typography.bodyMedium)
                    Text("• Ktor HTTP Client", style = MaterialTheme.typography.bodyMedium)
                    Text("• kotlinx.serialization", style = MaterialTheme.typography.bodyMedium)
                }
                
                Divider()
                
                Text(
                    text = "功能特性",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• 导出喜爱的电影和演员列表", style = MaterialTheme.typography.bodyMedium)
                    Text("• 智能匹配导入到新服务器", style = MaterialTheme.typography.bodyMedium)
                    Text("• 详细的操作日志和进度显示", style = MaterialTheme.typography.bodyMedium)
                    Text("• 跨平台支持 (Windows, macOS, Linux)", style = MaterialTheme.typography.bodyMedium)
                    Text("• 现代化的图形界面", style = MaterialTheme.typography.bodyMedium)
                }
                
                Divider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            appState.addLog("jtools v1.0.0")
                            appState.addLog("使用 Kotlin Multiplatform 和 Compose Desktop 构建")
                            appState.addLog("支持 Windows、macOS、Linux 平台")
                        }
                    ) {
                        Text("系统信息")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            try {
                                java.awt.Desktop.getDesktop().browse(java.net.URI("https://jellyfin.org"))
                            } catch (e: Exception) {
                                appState.addLog("无法打开浏览器: ${e.message}")
                            }
                        }
                    ) {
                        Text("Jellyfin 官网")
                    }
                }
            }
        }
    }
}
