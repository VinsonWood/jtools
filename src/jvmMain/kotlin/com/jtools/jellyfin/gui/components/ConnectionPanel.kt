package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.gui.state.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionPanel(appState: AppState) {
    var showPassword by remember { mutableStateOf(false) }
    
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
                    imageVector = if (appState.isConnected) Icons.Default.CheckCircle else Icons.Default.Settings,
                    contentDescription = null,
                    tint = if (appState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Jellyfin 服务器连接",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (appState.isConnected) {
                    AssistChip(
                        onClick = { },
                        label = { Text("已连接") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            
            // 服务器URL输入
            OutlinedTextField(
                value = appState.serverUrl,
                onValueChange = { appState.serverUrl = it },
                label = { Text("服务器URL") },
                placeholder = { Text("http://localhost:8096") },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !appState.isConnecting && !appState.isConnected,
                singleLine = true
            )
            
            // API Token输入
            OutlinedTextField(
                value = appState.apiToken,
                onValueChange = { appState.apiToken = it },
                label = { Text("API Token") },
                placeholder = { Text("在Jellyfin设置中生成API密钥") },
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "隐藏" else "显示"
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !appState.isConnecting && !appState.isConnected,
                singleLine = true
            )
            
            // 记住连接信息选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = appState.rememberConnection,
                        onCheckedChange = { appState.rememberConnection = it }
                    )
                    Text(
                        text = "记住连接信息",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 配置管理按钮
                if (appState.hasSavedConfig()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { appState.loadConfig() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "重新加载配置",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { appState.deleteConfig() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除保存的配置",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // 配置状态提示
            if (appState.hasSavedConfig() && appState.rememberConnection) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "已保存连接信息，下次启动时自动加载",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 错误信息
            if (appState.connectionError.isNotEmpty()) {
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
                        SelectionContainer {
                            Text(
                                text = appState.connectionError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // 用户选择
            if (appState.users.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = appState.users.find { it.id == appState.selectedUserId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("选择用户") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        appState.users.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(user.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            "ID: ${user.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    appState.selectedUserId = user.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 连接按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!appState.isConnected) {
                    Button(
                        onClick = { appState.testConnection() },
                        enabled = !appState.isConnecting && appState.serverUrl.isNotBlank() && appState.apiToken.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (appState.isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (appState.isConnecting) "连接中..." else "测试连接")
                    }
                } else {
                    Button(
                        onClick = { appState.disconnect() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LinkOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("断开连接")
                    }
                }
            }
        }
    }
}
