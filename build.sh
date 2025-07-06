#!/bin/bash

# jtools 构建脚本

echo "🔨 开始构建 jtools..."

# 清理之前的构建
echo "🧹 清理之前的构建..."
./gradlew clean

# 运行测试
echo "🧪 运行测试..."
./gradlew jvmTest

if [ $? -ne 0 ]; then
    echo "❌ 测试失败，构建中止"
    exit 1
fi

# 构建JAR
echo "📦 构建JAR文件..."
./gradlew jvmJar

if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

# 创建可执行脚本
echo "📝 创建可执行脚本..."
cat > jellyfin-tools << 'EOF'
#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
java -jar "$DIR/build/libs/jtools-jvm-1.0.0.jar" "$@"
EOF

chmod +x jellyfin-tools

echo "✅ 构建完成！"
echo ""
echo "📋 使用方法："
echo "  ./jellyfin-tools --help"
echo "  ./jellyfin-tools test -s http://your-server:8096 -t your-token"
echo "  ./jellyfin-tools export -s http://your-server:8096 -t your-token -o favorites.json"
echo "  ./jellyfin-tools import -s http://your-server:8096 -t your-token -i favorites.json"
echo ""
echo "📁 生成的文件："
echo "  - build/libs/jtools-jvm-1.0.0.jar (JAR文件)"
echo "  - jellyfin-tools (可执行脚本)"
