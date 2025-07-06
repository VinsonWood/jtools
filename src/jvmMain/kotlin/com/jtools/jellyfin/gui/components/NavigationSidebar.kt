package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jtools.jellyfin.gui.state.AppState

@Composable
fun NavigationSidebar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    appState: AppState,
    uiScale: Float = 1.0f
) {
    Surface(
        modifier = Modifier
            .width((240 * uiScale).dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = (2 * uiScale).dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((16 * uiScale).dp),
            verticalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            // 应用标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = (16 * uiScale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
            ) {
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    modifier = Modifier.size((32 * uiScale).dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "jtools",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = (18 * uiScale).sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Jellyfin工具箱",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (10 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height((8 * uiScale).dp))
            
            // 连接状态指示
            ConnectionStatusCard(appState, uiScale)
            
            Spacer(modifier = Modifier.height((16 * uiScale).dp))
            
            // 导航菜单
            NavigationItem(
                icon = Icons.Default.Home,
                title = "主界面",
                subtitle = "连接和操作",
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                uiScale = uiScale
            )
            
            NavigationItem(
                icon = Icons.Default.Terminal,
                title = "日志",
                subtitle = "操作记录",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                badge = if (appState.logs.isNotEmpty()) "${appState.logs.size}" else null,
                uiScale = uiScale
            )
            
            NavigationItem(
                icon = Icons.Default.ViewModule,
                title = "内容预览",
                subtitle = "查看导出文件",
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                uiScale = uiScale
            )
            
            NavigationItem(
                icon = Icons.Default.FindInPage,
                title = "重复检测",
                subtitle = "查找重复电影",
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                uiScale = uiScale
            )
            
            NavigationItem(
                icon = Icons.Default.Settings,
                title = "设置",
                subtitle = "配置选项",
                selected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                uiScale = uiScale
            )
            
            NavigationItem(
                icon = Icons.Default.Info,
                title = "关于",
                subtitle = "帮助信息",
                selected = selectedTab == 5,
                onClick = { onTabSelected(5) },
                uiScale = uiScale
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Claude Code 标识
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding((8 * uiScale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((6 * uiScale).dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size((14 * uiScale).dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Created with Claude Code",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (9 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height((8 * uiScale).dp))
            
            // 底部操作按钮
            if (appState.isConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding((12 * uiScale).dp),
                        verticalArrangement = Arrangement.spacedBy((4 * uiScale).dp)
                    ) {
                        Text(
                            text = "已连接",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = appState.users.find { it.id == appState.selectedUserId }?.name ?: "未选择用户",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = (11 * uiScale).sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(appState: AppState, uiScale: Float = 1.0f) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                appState.isConnecting -> MaterialTheme.colorScheme.secondaryContainer
                appState.isConnected -> MaterialTheme.colorScheme.primaryContainer
                appState.connectionError.isNotEmpty() -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((12 * uiScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((8 * uiScale).dp)
        ) {
            when {
                appState.isConnecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size((16 * uiScale).dp),
                        strokeWidth = (2 * uiScale).dp
                    )
                    Text(
                        text = "连接中...",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                appState.isConnected -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size((16 * uiScale).dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "已连接",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                appState.connectionError.isNotEmpty() -> {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size((16 * uiScale).dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "连接失败",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size((16 * uiScale).dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "未连接",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    uiScale: Float = 1.0f
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((12 * uiScale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size((24 * uiScale).dp),
                tint = if (selected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (14 * uiScale).sp),
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * uiScale).sp),
                    color = if (selected) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) 
                    else 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            badge?.let {
                Surface(
                    shape = RoundedCornerShape((12 * uiScale).dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape((12 * uiScale).dp))
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = (8 * uiScale).dp, vertical = (4 * uiScale).dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * uiScale).sp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
