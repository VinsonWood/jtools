plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.jtools"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("io.ktor:ktor-client-core:2.3.5")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
                implementation("io.ktor:ktor-client-auth:2.3.5")
                implementation("io.ktor:ktor-client-logging:2.3.5")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.5")
                implementation("ch.qos.logback:logback-classic:1.4.11")
                implementation("com.github.ajalt.clikt:clikt:4.2.1")
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
            }
        }
        
        val jvmTest by getting
        
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:2.3.5")
            }
        }
        
        val jsTest by getting
        
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:2.3.5")
            }
        }
        
        val nativeTest by getting
    }
}

// Application configuration moved to Compose Desktop

// 重新配置jvmJar任务以正确包含所有依赖
tasks.named<Jar>("jvmJar") {
    manifest {
        attributes["Main-Class"] = "com.jtools.jellyfin.MainKt"
    }
    
    val jvmMain = kotlin.jvm().compilations.getByName("main")
    from(jvmMain.output)
    
    dependsOn(jvmMain.compileTaskProvider)
    
    from({
        jvmMain.runtimeDependencyFiles.filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

compose.desktop {
    application {
        mainClass = "com.jtools.jellyfin.gui.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "jtools"
            packageVersion = "1.0.0"
            description = "Jellyfin工具箱 - 内容管理和迁移工具"
            copyright = "© 2024 jtools"
            vendor = "jtools"

            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                menuGroup = "jtools"
                upgradeUuid = "18159995-d967-4CD2-8885-77BFA97CFA9F"
            }

            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
                bundleID = "com.jtools.jellyfin"
            }

            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }
        }
    }
}

// 注释掉JS相关的资源处理，因为我们主要关注JVM平台
// tasks.named<Copy>("jvmProcessResources") {
//     val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
//     from(jsBrowserDistribution)
// }
