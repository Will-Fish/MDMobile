# MDMobile - Android Markdown Reader

一个简洁的Android Markdown阅读器应用，专为小米15等现代Android设备设计。

## 功能特性

### 已实现
- ✅ 项目基础架构搭建
- ✅ 基本的文件浏览界面
- ✅ Typora风格的简洁UI设计
- ✅ 导航系统（文件浏览 -> 阅读器）
- ✅ 存储权限请求系统
- ✅ 文件浏览组件（支持目录导航）
- ✅ 文件列表UI（卡片视图）
- ✅ 文件排序（名称/日期，升序/降序）
- ✅ 文件搜索功能
- ✅ Markdown文件渲染（CommonMark集成）
- ✅ 主题切换（亮色/暗色）
- ✅ 字体大小调整
- ✅ 书签管理系统（添加/删除/列表）

### 开发中
- 🔄 文件内书签（章节标记）
- 🔄 导出/分享功能

### 计划中（第六阶段 - 已完成）
- ✅ 应用图标和启动画面
- ✅ 隐私政策页面
- ✅ 构建发布版本APK
- 📅 小米应用商店上架（暂不实现）

## 技术栈

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM + Repository
- **Markdown渲染**: CommonMark-java
- **数据存储**: Room + DataStore
- **导航**: Navigation Compose

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/mdmobile/
│   │   ├── data/                    # 数据层
│   │   │   ├── model/              # 数据模型
│   │   │   │   ├── MarkdownFile.kt
│   │   │   │   └── Bookmark.kt
│   │   │   └── repository/         # 数据仓库
│   │   ├── ui/                     # UI层
│   │   │   ├── screens/           # 主要界面
│   │   │   │   ├── FileBrowserScreen.kt
│   │   │   │   └── ReaderScreen.kt
│   │   │   ├── components/        # 可复用组件
│   │   │   │   └── FileItem.kt
│   │   │   └── theme/             # 主题定义
│   │   │       ├── Theme.kt
│   │   │       ├── Typography.kt
│   │   │       └── Package.kt
│   │   └── MainActivity.kt        # 主活动
│   └── res/                       # 资源文件
```

## 开发计划

### 第一阶段：项目搭建与基础UI ✅
- [x] 创建Android项目结构
- [x] 配置Gradle依赖
- [x] 实现基础主题和导航
- [x] 创建主界面框架

### 第二阶段：文件浏览功能 ✅
- [x] 实现存储权限请求
- [x] 创建文件浏览组件
- [x] 实现文件列表UI
- [x] 添加文件排序和过滤

### 第三阶段：Markdown渲染器 ✅
- [x] 集成CommonMark库
- [x] 创建Markdown阅读界面
- [x] 实现主题切换
- [x] 添加字体大小调整
- [ ] 阅读进度保存（移至下一阶段）

### 第四阶段：书签系统 ✅
- [x] 设计书签数据模型
- [x] 实现书签DAO和Repository
- [x] 创建书签列表界面
- [x] 添加/删除书签功能
- [x] 书签搜索功能
- [x] 书签排序优化

### 第五阶段：优化与测试 ✅
- [x] 阅读进度保存基础功能（文件访问记录）
- [x] 最近文件记录功能
- [x] 性能优化（异步文件加载，数据库优化）
- [x] UI/UX细节打磨
- [x] Markdown兼容性测试套件
- [x] 小米15屏幕特性适配

### 第六阶段：发布准备
- [x] 应用图标和启动画面
- [x] 隐私政策页面
- [x] 构建发布版本APK
- [ ] 小米应用商店上架（暂不实现）

## 快速开始

### 选项A：使用Android Studio（推荐）
1. **安装Android Studio**（如果尚未安装）
2. **打开项目**：在Android Studio中打开项目目录
3. **Android Studio会自动**配置Gradle和下载依赖
4. **运行应用**：点击绿色的"Run"按钮

### 选项B：使用Gradle命令行
1. **设置Gradle Wrapper**：
   ```bash
   # 如果缺少gradle-wrapper.jar，先安装Gradle然后运行：
   gradle wrapper
   # 或参考 SETUP_GRADLE.md 文件
   ```

2. **构建项目**
   ```bash
   ./gradlew build
   ```

3. **运行应用**
   - 连接Android设备（小米15）
   - 启用USB调试
   - 运行 `./gradlew installDebug`

## 系统要求

- Android SDK 24+ (Android 7.0)
- 推荐：Android 14+ (小米15)
- Kotlin 1.9.24
- Gradle 8.7

## 贡献

项目处于早期开发阶段，欢迎反馈和建议。

## 测试新功能（阶段6）

阶段6（发布准备）已全部完成，包括：
- ✅ 应用图标和启动画面
- ✅ 隐私政策页面
- ✅ 构建发布版本APK配置

要测试新功能，请参考 `TEST_NEW_FEATURES.md` 文件。

推荐使用Android Studio打开项目并运行应用，这是最简单的测试方法。

## 许可证

MIT License