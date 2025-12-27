# WIFI建议

一个用于获取和管理WiFi网络建议的Android应用。

## 功能特点

- 📱 获取WiFi网络建议
- ☁️ 与云端服务器同步数据
- 🎨 现代化的Material Design界面
- 🔍 显示当前WiFi网络信息
- 📊 查看WiFi网络详情

## 安装方法

### 从APK安装

1. 下载最新版本的APK文件
2. 允许安装来自未知来源的应用
3. 点击APK文件进行安装

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/xna00/wifi-suggest.git
cd wifi-suggest

# 构建Release版本
./gradlew assembleRelease

# 生成的APK位于
# app/build/outputs/apk/release/app-release.apk
```

## 使用说明

1. 打开应用程序
2. 应用会自动连接到云端服务器获取WiFi建议
3. 查看当前已连接的WiFi网络信息
4. 根据建议优化您的WiFi连接

## 技术栈

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **构建工具**: Gradle
- **SDK版本**: Android 7.0+ (API 24+)
- **目标SDK**: Android 14 (API 34)

## 版本历史

### v1.7 (最新版本)
- 优化了应用界面
- 修复了已知bug

### v1.6
- 移除了URL输入功能，使用固定API地址
- 简化了抽屉菜单内容

### v1.5
- 实现了WiFi网络的备份和恢复功能
- 添加了云端同步功能