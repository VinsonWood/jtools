# jtools - Jellyfin工具箱

一个用于Jellyfin内容管理、迁移和维护的Kotlin Multiplatform工具箱，提供命令行和图形界面两种版本。

> 🤖 **本项目由 [Claude Code](https://claude.ai/code) 创建**  
> 使用 Anthropic 的 AI 助手进行完整开发，展示了 AI 辅助编程的强大能力。

## 功能特性

- 🎬 **导入导出** - 喜爱的电影和演员列表
- 🔄 **智能迁移** - 跨服务器数据迁移，智能匹配内容
- 🔍 **重复检测** - 发现并管理重复的电影内容
- 📺 **分辨率筛选** - 筛选指定分辨率以下的电影，支持统计和导出
- 📋 **内容预览** - 可视化浏览导出的数据
- 📊 **详细日志** - 完整的操作过程追踪
- 📱 **高DPI适配** - 完美支持高分辨率屏幕
- 🌐 **跨平台** - 支持 Windows、macOS、Linux
- 🖥️ **双界面** - 图形界面（GUI）和命令行（CLI）

## 技术亮点

- **Kotlin Multiplatform** - 现代化的跨平台架构
- **Compose Desktop** - 原生的桌面UI体验
- **Material Design 3** - 现代化的设计语言
- **Ktor Client** - 高性能的HTTP客户端
- **kotlinx.serialization** - 类型安全的数据序列化
- **Claude Code AI** - AI 辅助开发，提升代码质量

## 快速开始

### 前置要求

- Java 11 或更高版本
- Jellyfin服务器和有效的API Token

### 获取API Token

1. 登录Jellyfin Web界面
2. 进入 设置 > API密钥
3. 点击 "+" 创建新的API密钥
4. 复制生成的Token

### 构建项目

```bash
# 构建所有版本
./build-gui.sh

# 或者只构建JAR文件
./gradlew jvmJar
```

## 使用方法

### 🖥️ GUI版本（推荐）

图形界面版本提供直观的操作体验：

```bash
# 运行GUI版本
./jellyfin-tools-gui

# 或直接运行JAR
java -cp build/libs/jtools-jvm-1.0.0.jar com.jtools.jellyfin.gui.MainKt
```

GUI版本功能：
- 🎨 **现代界面设计** - 左侧导航栏布局，直观易用
- 📱 **多功能页面** - 主界面/日志/预览/重复检测/设置/关于六大功能页面
- 📊 **智能状态栏** - 实时显示操作状态和进度指示
- 📝 **专业日志系统** - 独立日志页面，支持实时和详细模式切换
- 🔧 **可视化配置** - 图形化的连接配置和偏好设置
- 📁 **文件管理** - 便捷的文件导入导出对话框
- 📱 **UI缩放** - 完美适配高分辨率屏幕，缩放设置自动保存
- 🔍 **重复检测** - 可视化显示重复电影，一键打开Jellyfin页面
- 📺 **分辨率筛选** - 可视化分辨率统计，支持预设和自定义分辨率筛选
- 👁️ **内容预览** - 导出文件的可视化预览，电影和演员分类展示
- ⚡ **响应式设计** - Material Design 3，完美适配不同屏幕尺寸
- 🤖 **AI驱动** - 全程由Claude Code AI辅助开发，代码质量优异

### 📋 CLI版本

命令行版本适合自动化和脚本使用：

#### 测试连接

```bash
./jellyfin-tools-cli test -s http://your-jellyfin-server:8096 -t your-api-token
```

#### 导出喜爱内容

```bash
./jellyfin-tools-cli export -s http://your-jellyfin-server:8096 -t your-api-token -o favorites.json
```

可选参数：
- `-u, --user`: 指定用户ID（默认使用第一个用户）

#### 导入喜爱内容

```bash
./jellyfin-tools-cli import -s http://your-jellyfin-server:8096 -t your-api-token -i favorites.json
```

#### 分辨率筛选

筛选指定分辨率以下的电影：

```bash
# 显示分辨率统计
./jellyfin-tools-cli resolution -s http://your-jellyfin-server:8096 -t your-api-token --stats-only

# 筛选1080p以下的电影
./jellyfin-tools-cli resolution -s http://your-jellyfin-server:8096 -t your-api-token -p 1080p

# 自定义分辨率筛选
./jellyfin-tools-cli resolution -s http://your-jellyfin-server:8096 -t your-api-token -w 1280 -h 720

# 保存结果到文件
./jellyfin-tools-cli resolution -s http://your-jellyfin-server:8096 -t your-api-token -p 720p -o low_res_movies.json
```

## 命令行选项

### 通用选项

- `-s, --server`: Jellyfin服务器URL（必需）
- `-t, --token`: API Token（必需）
- `-u, --user`: 用户ID（可选，默认使用第一个用户）

### export命令

- `-o, --output`: 输出文件路径（必需）

### import命令

- `-i, --input`: 输入文件路径（必需）

### resolution命令

- `-w, --width`: 最大宽度
- `-h, --height`: 最大高度  
- `-p, --preset`: 预设分辨率 (480p, 720p, 1080p, 4k)
- `-o, --output`: 输出文件路径（可选）
- `--include-unknown`: 包含未知分辨率的电影
- `--stats-only`: 只显示统计信息

## 导出文件格式

导出的JSON文件包含以下信息：

```json
{
  "exportDate": "2024-01-01T12:00:00",
  "serverUrl": "http://jellyfin-server:8096",
  "userId": "user-id",
  "favoriteMovies": [
    {
      "id": "movie-id",
      "name": "电影名称",
      "originalTitle": "原始标题",
      "overview": "简介",
      "productionYear": 2023,
      "genres": ["动作", "科幻"],
      "communityRating": 8.5,
      "people": [...]
    }
  ],
  "favoritePeople": [
    {
      "id": "person-id",
      "name": "演员姓名",
      "type": "Actor",
      "role": "角色名"
    }
  ]
}
```

## 原生安装包

可以构建适用于不同操作系统的原生安装包：

### Windows (.msi)
```bash
./gradlew packageMsi
```

### macOS (.dmg)
```bash
./gradlew packageDmg
```

### Linux (.deb)
```bash
./gradlew packageDeb
```

### 所有平台
```bash
./gradlew packageDistributionForCurrentOS
```

## 开发

### 项目结构

```
src/
├── commonMain/kotlin/          # 共享代码
│   ├── config/                 # 配置类
│   ├── model/                  # 数据模型
│   ├── api/                    # API客户端
│   └── service/                # 业务逻辑
├── jvmMain/kotlin/             # JVM平台代码
│   ├── cli/                    # 命令行界面
│   └── gui/                    # 图形界面
│       ├── components/         # UI组件
│       ├── state/              # 状态管理
│       └── theme/              # 主题样式
├── jsMain/kotlin/              # JavaScript平台代码
└── nativeMain/kotlin/          # Native平台代码
```

### 运行测试

```bash
./gradlew jvmTest
```

### 构建所有平台

```bash
./gradlew build
```

### 开发GUI版本

```bash
# 运行GUI开发版本
./gradlew runDistributable
```

## 注意事项

1. **API限制**: 工具在导入时会添加延迟以避免触发API限制
2. **匹配策略**: 优先使用ID匹配，失败时回退到名称搜索
3. **错误处理**: 导入过程中的错误会被记录但不会中断整个过程
4. **备份建议**: 在导入前建议备份目标Jellyfin实例

## 故障排除

### 连接失败

- 检查服务器URL是否正确（包含协议 http:// 或 https://）
- 确认API Token有效
- 检查网络连接和防火墙设置

### 导入失败

- 确保目标服务器有相同的媒体文件
- 检查用户权限
- 查看错误日志了解具体失败原因

## 关于 Claude Code

本项目完全由 [Claude Code](https://claude.ai/code) 创建，这是 Anthropic 推出的 AI 编程助手。Claude Code 具备以下能力：

- 🔧 **全栈开发** - 从架构设计到功能实现的完整开发流程
- 🎨 **UI/UX设计** - 现代化的用户界面设计和用户体验
- 📚 **技术选型** - 合理选择和组合现代化技术栈
- 🐛 **调试能力** - 快速定位和解决复杂的技术问题
- 📖 **文档编写** - 详细的代码文档和用户指南

这个项目展示了 AI 在软件开发领域的巨大潜力，从零开始构建了一个功能完整、用户友好的桌面应用程序。

### 开发特色

- ✨ **零人工干预** - 完全由 AI 独立设计和开发
- 🏗️ **现代架构** - 采用最新的 Kotlin Multiplatform 技术
- 🎯 **用户导向** - 注重用户体验和界面美观
- 🧪 **质量保证** - 包含完整的错误处理和边界情况考虑
- 📈 **持续优化** - 根据用户反馈不断改进功能

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！
