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
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.gui.state.AppState
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun OperationsPanel(appState: AppState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "导入导出操作",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 导出部分
            ExportSection(appState)
            
            Divider()
            
            // 导入部分
            ImportSection(appState)
        }
    }
}

@Composable
private fun ExportSection(appState: AppState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FileUpload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "导出喜爱内容",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "将当前用户的喜爱电影和演员导出为JSON文件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!appState.enableRealTimeLogs) {
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
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "实时日志已关闭，操作过程将不显示详细信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 导出进度
        if (appState.isExporting) {
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
                        text = appState.exportProgress,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // 导出错误
        if (appState.exportError.isNotEmpty()) {
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
                            text = appState.exportError,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // 导出结果
        appState.exportedData?.let { data ->
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
                        text = "导出完成！",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "电影: ${data.favoriteMovies.size} 部",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "演员: ${data.favoritePeople.size} 位",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // 导出按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { appState.exportFavorites() },
                enabled = appState.isConnected && !appState.isExporting && appState.selectedUserId.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始导出")
            }
            
            if (appState.exportedData != null) {
                Button(
                    onClick = {
                        val fileChooser = JFileChooser().apply {
                            dialogTitle = "保存导出文件"
                            fileFilter = FileNameExtensionFilter("JSON文件", "json")
                            selectedFile = File("jellyfin-favorites-${System.currentTimeMillis()}.json")
                        }
                        
                        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                            appState.saveExportData(fileChooser.selectedFile)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存文件")
                }
            }
        }
    }
}

@Composable
private fun ImportSection(appState: AppState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.FileDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "导入喜爱内容",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "从JSON文件导入喜爱的电影和演员到当前用户",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!appState.enableRealTimeLogs) {
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
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "建议启用详细日志以查看导入过程和错误信息",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 选择的文件
        appState.selectedImportFile?.let { file ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.InsertDriveFile, contentDescription = null)
                    Column {
                        Text(
                            text = file.name,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "大小: ${file.length() / 1024} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // 导入进度
        if (appState.isImporting) {
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
                        text = appState.importProgress,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        // 导入错误
        if (appState.importError.isNotEmpty()) {
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
                            text = appState.importError,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // 导入结果
        appState.importResult?.let { result ->
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
                        text = "导入完成！",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "电影: ${result.importedMovies}/${result.totalMovies} 成功",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "演员: ${result.importedPeople}/${result.totalPeople} 成功",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (result.errors.isNotEmpty()) {
                        Text(
                            text = "错误: ${result.errors.size} 个",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        // 导入按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val fileChooser = JFileChooser().apply {
                        dialogTitle = "选择导入文件"
                        fileFilter = FileNameExtensionFilter("JSON文件", "json")
                    }
                    
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        appState.selectedImportFile = fileChooser.selectedFile
                    }
                },
                enabled = !appState.isImporting,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择文件")
            }
            
            Button(
                onClick = {
                    appState.selectedImportFile?.let { file ->
                        appState.importFavorites(file)
                    }
                },
                enabled = appState.isConnected && !appState.isImporting && 
                         appState.selectedUserId.isNotEmpty() && appState.selectedImportFile != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始导入")
            }
        }
    }
}
