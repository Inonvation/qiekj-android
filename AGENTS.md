# 瘦刁生活 - Agent 指南

## 首要原则
- **在你执行任何发布、构建、提交、推送等操作前，必须先向用户阐述方案，等待用户明确同意后再执行**
- 不要自作主张做任何用户没有要求的事情

## 构建
- `scripts/build.bat`（Windows）— 自动管理版本号，构建并归档 APK
- `scripts/build.ps1`（Windows PowerShell）— 同上
- 直接构建：`gradlew :app:archiveDebugApk -PbuildVersionName="x.y.z"`
- APK 归档至 `archive/app-debug-v{版本号}.apk`

## 版本管理
- 版本号在 `app/build.gradle.kts` 的 `defaultConfig` 中
- `versionCode`: 每次发布 +1（整数）
- `versionName`: 语义化版本，如 "0.0.13"
- 构建脚本 `build.bat` 自动从版本文件读取并递增 patch

## 发布新版本
当用户要求"发布新版本"时，AI 应自动执行以下步骤：
1. 读取当前版本号（`app/build.gradle.kts` 中 `defaultConfig.versionName` 的默认值）
2. 按用户指定的版本号更新 `versionName` 默认值
3. 更新 `archive/` 中对应版本的 APK（运行 `archiveDebugApk` 任务构建）
4. 提交并推送代码：`git add -A && git commit -m "Bump version to x.y.z" && git push`
5. 打 tag 并推送：`git tag vx.y.z main && git push --tags`
   - **注意**：必须指定 `main` 分支，否则 tag 可能指向旧 commit
6. 告知用户 GitHub Actions 正在自动构建中（可在 Actions 页面查看进度）
7. 如果 Actions 报错 `Resource not accessible`，检查 workflow 是否有 `permissions: contents: write`
- 注意：无需手动调用 `gh release create`，GitHub Actions 工作流会自动处理 Release 创建和 APK 上传

## 签名
- 使用项目内的 `app/debug.keystore`（自定义固定 debug 签名）
- 已配置在 `signingConfigs.fixedDebug`
- `app/debug.keystore` 已提交到仓库，CI 与本地签名一致
- 如果 CI 构建的 APK 签名仍不一致，检查 workflow 中是否有 `keytool -genkey` 步骤（应该没有）

## 代码规范
- 不要在 Compose 函数外使用 `remember`
- 所有 UI 间距/颜色优先使用 `AppStyles.kt` 中的常量
- Card 布局统一使用 `CardShapes.cardCorner`
- 修改 Kotlin 文件后优先用 Node.js（`fs.writeFileSync`）而非 PowerShell 写入，避免中文乱码
- 所有涉及颜色、间距的改动尽量走 `AppStyles.kt` 统一管理

## 注意事项
- Token 随手机号重新登录而变化，App 自动保存最新 Token
- 接口返回结构变化时需同步更新数据层代码
- 不要将个人 Token、抓包文件、签名密钥上传到公开仓库
- `PointsTaskRunner.kt` 中的 `ANDROID_SECRET` / `ALIPAY_SECRET` 是接口签名密钥，属于已知暴露项（反编译 APK 同样能获取），如有安全需求可迁移至 BuildConfig
