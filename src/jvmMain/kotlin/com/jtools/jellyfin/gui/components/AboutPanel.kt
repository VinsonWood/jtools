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

@Composable
fun AboutContentPanel(appState: AppState, uiScale: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding((16 * uiScale).dp),
        verticalArrangement = Arrangement.spacedBy((12 * uiScale).dp)
    ) {
        // 标题
        Text(
            text = "关于",
            fontSize = (24 * uiScale).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = (8 * uiScale).dp)
        )

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
                        imageVector = Icons.Default.Info,
                        contentDescription = "应用信息",
                        modifier = Modifier.size((20 * uiScale).dp)
                    )
                    Text(
                        text = "jtools",
                        fontSize = (20 * uiScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "Jellyfin喜爱内容导入导出工具",
                    fontSize = (16 * uiScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "版本: 1.0.0",
                    fontSize = (14 * uiScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Divider(modifier = Modifier.padding(vertical = (8 * uiScale).dp))
                
                Text(
                    text = "功能特性:",
                    fontSize = (16 * uiScale).sp,
                    fontWeight = FontWeight.Medium
                )
                
                val features = listOf(
                    "• 导出和导入Jellyfin收藏的电影",
                    "• 导出和导入收藏的演员",
                    "• 检测重复电影",
                    "• 跨服务器数据迁移",
                    "• 实时日志查看",
                    "• 高分辨率屏幕适配"
                )
                
                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = (14 * uiScale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = (8 * uiScale).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LogContentPanel(appState: AppState, uiScale: Float) {
    Text("日志面板 - 缩放: ${String.format("%.1f", uiScale)}x", fontSize = (14 * uiScale).sp)
}