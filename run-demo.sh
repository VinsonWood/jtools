#!/bin/bash

# jtools 演示脚本

echo "🎬 jtools 功能演示"
echo "=========================="
echo ""

# 检查Java环境
echo "🔍 检查运行环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java运行环境"
    echo "请安装Java 11或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ 错误: Java版本过低 (当前: $JAVA_VERSION, 需要: 11+)"
    exit 1
fi

echo "✅ Java环境正常 (版本: $JAVA_VERSION)"
echo ""

# 检查构建文件
echo "🔍 检查构建文件..."
if [ ! -f "build/libs/jtools-jvm-1.0.0.jar" ]; then
    echo "❌ 错误: 未找到JAR文件"
    echo "请先运行构建脚本: ./build-gui.sh"
    exit 1
fi

if [ ! -f "jellyfin-tools-cli" ] || [ ! -f "jellyfin-tools-gui" ]; then
    echo "❌ 错误: 未找到可执行脚本"
    echo "请先运行构建脚本: ./build-gui.sh"
    exit 1
fi

echo "✅ 构建文件完整"
echo ""

# 显示菜单
while true; do
    echo "📋 请选择要演示的功能:"
    echo "1. CLI版本 - 显示帮助信息"
    echo "2. CLI版本 - 测试连接（演示错误处理）"
    echo "3. CLI版本 - 解析示例JSON文件"
    echo "4. GUI版本 - 启动图形界面（需要图形环境）"
    echo "5. 查看项目文档"
    echo "6. 运行单元测试"
    echo "0. 退出"
    echo ""
    read -p "请输入选项 (0-6): " choice

    case $choice in
        1)
            echo ""
            echo "🚀 演示: CLI帮助信息"
            echo "========================"
            ./jellyfin-tools-cli --help
            echo ""
            echo "📋 子命令帮助:"
            ./jellyfin-tools-cli export --help
            echo ""
            ;;
        2)
            echo ""
            echo "🚀 演示: CLI连接测试（错误处理）"
            echo "================================"
            echo "正在测试无效服务器连接..."
            ./jellyfin-tools-cli test -s http://invalid-server:8096 -t demo-token
            echo ""
            echo "💡 这演示了网络错误的处理机制"
            echo ""
            ;;
        3)
            echo ""
            echo "🚀 演示: JSON文件解析"
            echo "====================="
            if [ -f "test-favorites.json" ]; then
                echo "📄 示例JSON文件内容预览:"
                head -20 test-favorites.json
                echo "..."
                echo ""
                echo "📊 文件统计:"
                echo "文件大小: $(wc -c < test-favorites.json) 字节"
                echo "行数: $(wc -l < test-favorites.json) 行"
                echo "电影数量: $(grep -o '"name"' test-favorites.json | wc -l) 个条目"
                echo ""
                echo "🧪 测试导入解析（会因连接失败而停止，但会显示文件解析过程）:"
                ./jellyfin-tools-cli import -s http://demo:8096 -t demo -i test-favorites.json
            else
                echo "❌ 未找到示例JSON文件"
            fi
            echo ""
            ;;
        4)
            echo ""
            echo "🚀 演示: GUI版本启动"
            echo "===================="
            if [ -n "$DISPLAY" ] || [ -n "$WAYLAND_DISPLAY" ]; then
                echo "✅ 检测到图形环境，启动GUI..."
                echo "💡 GUI将在新窗口中打开"
                echo "💡 关闭GUI窗口返回此菜单"
                ./jellyfin-tools-gui &
                GUI_PID=$!
                echo "GUI已启动 (PID: $GUI_PID)"
                echo "按Enter键继续..."
                read
            else
                echo "⚠️  未检测到图形环境"
                echo "GUI版本需要X11或Wayland显示服务器"
                echo ""
                echo "🔧 在有图形环境的系统中，您可以:"
                echo "1. 双击 jellyfin-tools-gui 文件"
                echo "2. 在终端运行: ./jellyfin-tools-gui"
                echo "3. 运行: java -cp build/libs/jtools-jvm-1.0.0.jar com.jtools.jellyfin.gui.MainKt"
                echo ""
                echo "📱 GUI功能特性:"
                echo "- 全新的左侧导航栏设计（参考mdcx项目）"
                echo "- 分页式内容组织（主界面/日志/设置/关于）"
                echo "- 顶部实时状态栏和进度指示"
                echo "- 现代化Material Design 3界面"
                echo "- 智能的日志显示和控制"
                echo "- 详细的操作进度反馈"
                echo "- 专业的错误处理和用户提示"
                echo "- 响应式布局和优雅的视觉层次"
            fi
            echo ""
            ;;
        5)
            echo ""
            echo "📚 项目文档"
            echo "==========="
            echo "📄 可用文档文件:"
            ls -la *.md | awk '{print "  " $9 " (" $5 " 字节)"}'
            echo ""
            echo "📖 推荐阅读顺序:"
            echo "1. README.md - 项目概述和基本使用"
            echo "2. GUI_GUIDE.md - GUI版本详细使用指南"
            echo "3. UI_OPTIMIZATION_REPORT.md - 界面优化报告（新增）"
            echo "4. demo-detailed-logs.md - 详细日志功能说明"
            echo "5. example-config.md - 配置示例"
            echo "6. TEST_REPORT.md - 测试报告"
            echo "7. PROJECT_SUMMARY.md - 技术总结"
            echo ""
            read -p "输入文档名称查看内容 (或按Enter跳过): " doc_name
            if [ -n "$doc_name" ] && [ -f "$doc_name" ]; then
                echo ""
                echo "📄 $doc_name 内容:"
                echo "=================="
                head -50 "$doc_name"
                echo ""
                echo "💡 使用 'less $doc_name' 查看完整内容"
            fi
            echo ""
            ;;
        6)
            echo ""
            echo "🧪 运行单元测试"
            echo "==============="
            ./gradlew jvmTest
            echo ""
            echo "📊 测试报告位置: build/test-results/"
            echo ""
            ;;
        0)
            echo ""
            echo "👋 感谢使用jtools!"
            echo "========================"
            echo ""
            echo "🚀 快速开始:"
            echo "  GUI版本: ./jellyfin-tools-gui"
            echo "  CLI版本: ./jellyfin-tools-cli --help"
            echo ""
            echo "📚 文档: 查看 README.md 和 GUI_GUIDE.md"
            echo "🐛 问题: 查看 TEST_REPORT.md 故障排除部分"
            echo ""
            exit 0
            ;;
        *)
            echo "❌ 无效选项，请输入0-6之间的数字"
            echo ""
            ;;
    esac
done
