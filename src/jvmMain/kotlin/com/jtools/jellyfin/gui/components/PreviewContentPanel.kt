package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
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
import com.jtools.jellyfin.model.JellyfinExportData
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewContentPanel(appState: AppState) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var exportData by remember { mutableStateOf<JellyfinExportData?>(null) }
    var error by remember { mutableStateOf("") }
    var selectedView by remember { mutableStateOf(0) } // 0: Movies, 1: People

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
                Icons.Default.ViewModule,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = "内容预览",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择导出文件并预览其中的电影和演员",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 文件选择区域
        FileSelectionCard(
            selectedFile = selectedFile,
            onFileSelected = { file ->
                selectedFile = file
                try {
                    val jsonText = file.readText()
                    val service = com.jtools.jellyfin.service.ExportImportService(
                        com.jtools.jellyfin.api.JellyfinApiClient(
                            com.jtools.jellyfin.config.JellyfinConfig("", "")
                        )
                    )
                    exportData = service.parseFromJson(jsonText)
                    error = ""
                } catch (e: Exception) {
                    error = "读取文件失败: ${e.message}"
                    exportData = null
                }
            },
            error = error
        )

        // 内容预览区域
        exportData?.let { data ->
            // 切换视图的标签页
            TabRow(
                selectedTabIndex = selectedView,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedView == 0,
                    onClick = { selectedView = 0 },
                    text = { Text("电影 (${data.favoriteMovies.size})") }
                )
                Tab(
                    selected = selectedView == 1,
                    onClick = { selectedView = 1 },
                    text = { Text("演员 (${data.favoritePeople.size})") }
                )
            }

            // 内容区域，使用剩余的空间
            when (selectedView) {
                0 -> MovieCardsGrid(data.favoriteMovies, modifier = Modifier.weight(1f))
                1 -> ActorCardsGrid(data.favoritePeople, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FileSelectionCard(
    selectedFile: File?,
    onFileSelected: (File) -> Unit,
    error: String
) {
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
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "选择导出文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "选择一个之前导出的JSON文件来预览其中的内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 选择的文件信息
            selectedFile?.let { file ->
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

            // 错误信息
            if (error.isNotEmpty()) {
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
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 选择文件按钮
            Button(
                onClick = {
                    val fileChooser = JFileChooser().apply {
                        dialogTitle = "选择导出文件"
                        fileFilter = FileNameExtensionFilter("JSON文件", "json")
                    }
                    
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        onFileSelected(fileChooser.selectedFile)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择文件")
            }
        }
    }
}