package com.jtools.jellyfin.gui

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jtools.jellyfin.gui.state.AppState

/**
 * GUI版本的主入口函数
 */
fun main() {
    // 设置系统属性以获得更好的渲染效果
    System.setProperty("skiko.renderApi", "SOFTWARE")

    // 启动GUI应用
    application {
        val appState = remember { AppState() }
        val windowState = rememberWindowState(width = 800.dp, height = 900.dp)

        Window(
            onCloseRequest = {
                appState.disconnect()
                exitApplication()
            },
            title = "jtools - Jellyfin工具箱",
            state = windowState
        ) {
            MainAppWithMenuBar(appState) {
                appState.disconnect()
                exitApplication()
            }
        }
    }
}
