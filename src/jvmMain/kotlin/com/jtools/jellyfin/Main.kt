package com.jtools.jellyfin

import com.github.ajalt.clikt.core.subcommands
import com.jtools.jellyfin.cli.*

/**
 * 主入口函数
 */
fun main(args: Array<String>) {
    JellyfinCli()
        .subcommands(
            ExportCommand(),
            ImportCommand(),
            TestCommand(),
            ViewCommand(),
            ConfigCommand()
        )
        .main(args)
}
