# DeviceControl - Agent 指南

## 版本管理
- 版本号在 app/build.gradle.kts 的 defaultConfig 中
- versionCode: 每次发布 +1（整型）
- versionName: 语义化版本，如 "0.0.1"
- 每次构建新 APK 时，必须更新这两个值（versionCode +1，versionName 升 patch）
- 构建命令: gradlew :app:assembleDebug

## 签名
- 使用项目内的 app/debug.keystore（自定义固定 debug 签名）
- 已配置在 signingConfigs.fixedDebug
- 首次安装需先卸载旧版（签名不同）

## 构建脚本
- build.ps1: 自动管理版本号的构建脚本
- build.bat: 同上（批处理版）
