package com.jtools.jellyfin.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.jtools.jellyfin.gui.components.*
import com.jtools.jellyfin.gui.state.AppState
import com.jtools.jellyfin.gui.theme.JToolsTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(appState: AppState) {
    var selectedTab by remember { mutableStateOf(0) }
    var uiScale by remember { mutableStateOf(1.0f) }
    
    // 加载保存的UI缩放设置
    LaunchedEffect(Unit) {
        try {
            val appConfig = com.jtools.jellyfin.config.ConfigManager.loadAppConfig()
            if (appConfig != null && appConfig.isValidUiScale()) {
                uiScale = appConfig.uiScale
            }
        } catch (e: Exception) {
            println("加载UI缩放设置失败: ${e.message}")
        }
    }

    JToolsTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧导航栏
            NavigationSidebar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                appState = appState,
                uiScale = uiScale
            )

            // 主内容区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // 顶部标题栏
                TopTitleBar(appState, uiScale)

                // 主要内容
                when (selectedTab) {
                    0 -> MainContentPanelWithScale(appState, uiScale)
                    1 -> LogContentPanelWithScale(appState, uiScale)
                    2 -> PreviewContentPanel(appState, uiScale)
                    3 -> DuplicateMoviePanel(appState, uiScale)
                    4 -> ResolutionFilterPanel(appState, uiScale)
                    5 -> SettingsContentPanel(appState, uiScale) { newScale ->
                        uiScale = newScale
                        // 保存UI缩放设置
                        try {
                            val currentConfig = com.jtools.jellyfin.config.ConfigManager.loadAppConfig() 
                                ?: com.jtools.jellyfin.config.AppConfig.default()
                            val updatedConfig = currentConfig.copy(uiScale = newScale)
                            com.jtools.jellyfin.config.ConfigManager.saveAppConfig(updatedConfig)
                        } catch (e: Exception) {
                            println("保存UI缩放设置失败: ${e.message}")
                        }
                    }
                    6 -> AboutContentPanelWithScale(appState, uiScale)
                }
            }
        }
    }
}

// 带缩放的MainContentPanel包装器
@Composable
fun MainContentPanelWithScale(appState: AppState, uiScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((24 * uiScale).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy((24 * uiScale).dp)
    ) {
        // 连接配置区域
        ConnectionPanel(appState)
        
        // 操作区域
        OperationsPanel(appState)
    }
}

// 带缩放的LogContentPanel包装器
@Composable
fun LogContentPanelWithScale(appState: AppState, uiScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((24 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
    ) {
        LogPanel(appState)
    }
}

// 带缩放的AboutContentPanel包装器
@Composable
fun AboutContentPanelWithScale(appState: AppState, uiScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((24 * uiScale).dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy((24 * uiScale).dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = (4 * uiScale).dp)
        ) {
            Column(
                modifier = Modifier.padding((24 * uiScale).dp),
                verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
            ) {
                Text(
                    text = "关于 jtools",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = (20 * uiScale).sp)
                )
                
                Text(
                    text = "Jellyfin工具箱 v1.0.0",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = (16 * uiScale).sp)
                )
                
                Text(
                    text = "一个用于Jellyfin内容管理、迁移和维护的跨平台工具箱。",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                // Claude Code 创建说明
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding((12 * uiScale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size((20 * uiScale).dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "🤖 本项目由 Claude Code 创建",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "使用 Anthropic 的 AI 助手进行开发",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Text(
                    text = "技术栈",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = (18 * uiScale).sp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy((6 * uiScale).dp)
                ) {
                    Text("• Kotlin Multiplatform", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• Compose Desktop", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• Material Design 3", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• Ktor HTTP Client", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• kotlinx.serialization", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• Claude Code AI 开发", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp), color = MaterialTheme.colorScheme.primary)
                }
                
                Divider()
                
                Text(
                    text = "功能特性",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = (18 * uiScale).sp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Text("• 导入导出喜爱的电影和演员", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 智能匹配和跨服务器迁移", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 重复内容检测和清理", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 内容预览和批量管理", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 详细日志和进度跟踪", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 高分辨率屏幕完美适配", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                    Text("• 跨平台支持 (Windows, macOS, Linux)", style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp))
                }
            }
        }
    }
}

// 带缩放的PreviewContentPanel包装器
@Composable
fun PreviewContentPanel(appState: AppState, uiScale: Float) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var exportData by remember { mutableStateOf<com.jtools.jellyfin.model.JellyfinExportData?>(null) }
    var error by remember { mutableStateOf("") }
    var selectedView by remember { mutableStateOf(0) } // 0: Movies, 1: People

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((16 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
    ) {
        // 标题区域
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
        ) {
            Icon(
                Icons.Default.ViewModule,
                contentDescription = null,
                modifier = Modifier.size((32 * uiScale).dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = "内容预览",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = (24 * uiScale).sp),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "选择导出文件并预览其中的电影和演员",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 文件选择区域
        FileSelectionCardWithScale(
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
            error = error,
            uiScale = uiScale
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
                    text = { 
                        Text(
                            "电影 (${data.favoriteMovies.size})",
                            fontSize = (14 * uiScale).sp
                        ) 
                    }
                )
                Tab(
                    selected = selectedView == 1,
                    onClick = { selectedView = 1 },
                    text = { 
                        Text(
                            "演员 (${data.favoritePeople.size})",
                            fontSize = (14 * uiScale).sp
                        ) 
                    }
                )
            }

            // 内容区域，使用剩余的空间
            when (selectedView) {
                0 -> MovieCardsGridWithScale(data.favoriteMovies, uiScale, modifier = Modifier.weight(1f))
                1 -> ActorCardsGridWithScale(data.favoritePeople, uiScale, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FileSelectionCardWithScale(
    selectedFile: java.io.File?,
    onFileSelected: (java.io.File) -> Unit,
    error: String,
    uiScale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = (4 * uiScale).dp)
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
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size((20 * uiScale).dp)
                )
                Text(
                    text = "选择导出文件",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = (16 * uiScale).sp),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "选择一个之前导出的JSON文件来预览其中的内容",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
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
                        modifier = Modifier.padding((12 * uiScale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.InsertDriveFile, 
                            contentDescription = null,
                            modifier = Modifier.size((20 * uiScale).dp)
                        )
                        Column {
                            Text(
                                text = file.name,
                                fontWeight = FontWeight.Medium,
                                fontSize = (14 * uiScale).sp
                            )
                            Text(
                                text = "大小: ${file.length() / 1024} KB",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
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
                        modifier = Modifier.padding((12 * uiScale).dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size((20 * uiScale).dp)
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = (14 * uiScale).sp
                        )
                    }
                }
            }

            // 选择文件按钮
            Button(
                onClick = {
                    val fileChooser = javax.swing.JFileChooser().apply {
                        dialogTitle = "选择导出文件"
                        fileFilter = javax.swing.filechooser.FileNameExtensionFilter("JSON文件", "json")
                    }
                    
                    if (fileChooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                        onFileSelected(fileChooser.selectedFile)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.FolderOpen, 
                    contentDescription = null,
                    modifier = Modifier.size((16 * uiScale).dp)
                )
                Spacer(modifier = Modifier.width((8 * uiScale).dp))
                Text(
                    "选择文件",
                    fontSize = (14 * uiScale).sp
                )
            }
        }
    }
}

// 占位符组件，需要检查CardComponents.kt中的实现
@Composable
private fun MovieCardsGridWithScale(
    movies: List<com.jtools.jellyfin.model.JellyfinMovie>,
    uiScale: Float,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive((200 * uiScale).dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues((8 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
    ) {
        items(movies) { movie ->
            MovieCardWithScale(movie = movie, uiScale = uiScale)
        }
    }
}

@Composable
private fun ActorCardsGridWithScale(
    people: List<com.jtools.jellyfin.model.JellyfinPerson>,
    uiScale: Float,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive((150 * uiScale).dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues((8 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp),
        horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
    ) {
        items(people) { person ->
            ActorCardWithScale(person = person, uiScale = uiScale)
        }
    }
}

@Composable
private fun MovieCardWithScale(movie: com.jtools.jellyfin.model.JellyfinMovie, uiScale: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height((280 * uiScale).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (2 * uiScale).dp)
    ) {
        Column(
            modifier = Modifier.padding((12 * uiScale).dp),
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            // 电影海报占位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((160 * uiScale).dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size((48 * uiScale).dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // 电影信息
            Column {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = (14 * uiScale).sp),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                movie.productionYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                movie.communityRating?.let { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size((12 * uiScale).dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActorCardWithScale(person: com.jtools.jellyfin.model.JellyfinPerson, uiScale: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height((200 * uiScale).dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (2 * uiScale).dp)
    ) {
        Column(
            modifier = Modifier.padding((12 * uiScale).dp),
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            // 演员头像占位符
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((120 * uiScale).dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size((48 * uiScale).dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // 演员信息
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = (14 * uiScale).sp),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                person.type?.let { type ->
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// 带缩放的DuplicateMoviePanel占位符
@Composable
fun DuplicateMoviePanel(appState: AppState, uiScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((24 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((16 * uiScale).dp)
    ) {
        Text(
            text = "重复电影检测",
            fontSize = (24 * uiScale).sp,
            fontWeight = FontWeight.Bold
        )
        
        // 扫描按钮
        Button(
            onClick = { appState.scanForDuplicateMovies() },
            enabled = appState.isConnected && !appState.isDuplicateScanning,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (appState.isDuplicateScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size((16 * uiScale).dp),
                    strokeWidth = (2 * uiScale).dp
                )
                Spacer(modifier = Modifier.width((8 * uiScale).dp))
            }
            Text(
                text = if (appState.isDuplicateScanning) "扫描中..." else "开始扫描重复电影",
                fontSize = (14 * uiScale).sp
            )
        }
        
        // 结果显示
        appState.duplicateMovieResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = (2 * uiScale).dp)
            ) {
                Column(
                    modifier = Modifier.padding((16 * uiScale).dp),
                    verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
                ) {
                    Text(
                        text = "扫描结果",
                        fontSize = (18 * uiScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "找到 ${result.duplicateGroups.size} 组重复电影，共 ${result.totalDuplicates} 个重复项",
                        fontSize = (14 * uiScale).sp
                    )
                    
                    result.duplicateGroups.take(5).forEach { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = (4 * uiScale).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    fontSize = (14 * uiScale).sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${group.duplicateCount} 个版本",
                                    fontSize = (12 * uiScale).sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // 为每个重复电影添加打开链接按钮
                            group.movies.forEach { movie ->
                                IconButton(
                                    onClick = { appState.openMovieInJellyfin(movie.id) },
                                    modifier = Modifier.size((32 * uiScale).dp)
                                ) {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = "打开 ${movie.name}",
                                        modifier = Modifier.size((16 * uiScale).dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    if (result.duplicateGroups.size > 5) {
                        Text(
                            text = "还有 ${result.duplicateGroups.size - 5} 组未显示...",
                            fontSize = (12 * uiScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        if (appState.duplicateScanError.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "扫描错误: ${appState.duplicateScanError}",
                    fontSize = (14 * uiScale).sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding((16 * uiScale).dp)
                )
            }
        }
    }
}

@Composable
fun MainAppWithMenuBar(appState: AppState, onCloseRequest: () -> Unit) {
    // 简化版本，暂时移除MenuBar
    MainApp(appState)
}
