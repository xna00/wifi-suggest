# WiFi 备份助手

一个用于备份和同步WiFi网络配置的Android应用。

## 功能特点

- 📱 自动备份WiFi网络配置
- ☁️ 同步到云端服务器
- 🔒 安全存储WiFi密码
- 📦 一键恢复网络配置
- 🎨 现代化的Material Design界面

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
./gradlew.sh assembleRelease

# 生成的APK位于
# app/build/outputs/apk/release/app-release.apk
```

## 使用说明

1. 打开应用程序
2. 应用会自动扫描并显示当前已连接的WiFi网络
3. 点击备份按钮将WiFi配置上传到云端
4. 在新设备上，点击恢复按钮即可恢复所有备份的WiFi网络

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

## 贡献指南

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个Pull Request

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情

## 联系方式

- GitHub: [https://github.com/xna00/wifi-suggest](https://github.com/xna00/wifi-suggest)
- 作者: XNA