# MDMobile APK 构建指南

## ✅ 当前状态
- ✅ 项目代码：阶段6全部完成
- ✅ Gradle Wrapper：已配置（gradle-wrapper.jar 已下载）
- ⚠️ 需要：Java JDK 17

## 🚀 快速构建步骤

### 步骤1：安装Java JDK 17
1. **下载**：https://www.oracle.com/java/technologies/downloads/#java17
   - 选择 "Windows x64 Installer" (约 160MB)
2. **安装**：运行下载的安装程序，使用默认设置
3. **验证安装**：
   ```cmd
   java -version
   ```
   应该显示类似：
   ```
   java version "17.0.x"
   ```

### 步骤2：设置环境变量（可选但推荐）
1. **设置 `JAVA_HOME`**：
   ```cmd
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   ```
   （根据实际安装路径调整）

2. **添加到 `PATH`**（如果Java命令不可用）：
   ```cmd
   setx PATH "%PATH%;%JAVA_HOME%\bin"
   ```

### 步骤3：构建APK
在项目目录中运行：
```cmd
cd D:\mytools\MDMobile
gradlew.bat assembleDebug
```

**构建时间**：首次构建需要下载依赖，可能需要5-15分钟。

### 步骤4：找到APK文件
构建成功后，APK文件位于：
```
D:\mytools\MDMobile\app\build\outputs\apk\debug\app-debug.apk
```

## 📱 安装到手机

### 方法A：USB传输
1. 通过USB连接手机到电脑
2. 复制 `app-debug.apk` 到手机存储
3. 在手机上找到文件并安装

### 方法B：无线传输
1. 使用微信/QQ文件传输
2. 或上传到网盘（百度网盘、Google Drive等）
3. 在手机上下载并安装

### 安装注意事项
- **允许未知来源**：第一次安装时，需要允许"安装未知应用"
- **安全警告**：可能会显示"此应用可能包含恶意软件"，点击"继续安装"
- **权限请求**：应用需要存储权限来访问文件

## 🔧 故障排除

### 问题1：`java -version` 不工作
- **原因**：Java未安装或PATH未设置
- **解决**：重新安装Java，确保安装时选择"添加到PATH"

### 问题2：`gradlew.bat` 报错
- **常见错误**：`JAVA_HOME not set`
  ```cmd
  set JAVA_HOME=C:\Program Files\Java\jdk-17
  gradlew.bat assembleDebug
  ```

- **内存不足**：
  ```cmd
  set GRADLE_OPTS=-Xmx2048m
  gradlew.bat assembleDebug
  ```

### 问题3：构建失败
- **网络问题**：Gradle需要下载依赖，确保网络连接
- **代理设置**（如果需要）：
  ```cmd
  set GRADLE_OPTS="-Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=8080"
  ```

- **重新构建**：
  ```cmd
  gradlew.bat clean
  gradlew.bat assembleDebug
  ```

## 📋 新功能测试清单

安装APK后，请测试：

### ✅ 1. 应用图标
- 查看手机桌面上的紫色"MD"字母图标
- 图标在各种屏幕密度下显示正常

### ✅ 2. 启动画面
- 冷启动应用（完全退出后重新打开）
- 观察1.5秒的渐变动画
- 自动跳转到主界面或权限请求

### ✅ 3. 设置页面
- 点击底部导航最右侧的**齿轮图标**（设置）
- 页面应包含：
  - 主题设置
  - 默认文件夹设置
  - 隐私政策
  - 关于

### ✅ 4. 隐私政策页面
- 从设置页面点击"隐私政策"
- 显示完整的Markdown格式隐私政策
- 返回按钮正常工作

### ✅ 5. 原有功能
- 文件浏览、Markdown阅读、书签管理
- 主题切换、字体大小调整

## ⚡ 备选方案

### 方案A：使用Android Studio（推荐）
1. 安装Android Studio
2. 打开项目 `D:\mytools\MDMobile`
3. 点击绿色"Run"按钮
4. Android Studio会自动处理所有依赖

### 方案B：在线构建服务
- **GitHub Actions**：如果项目在GitHub上，可以设置自动构建
- **第三方服务**：但需要注意代码安全

## 📊 构建配置详情

项目已配置发布优化：
- **代码混淆**：启用（ProGuard）
- **资源压缩**：启用
- **目标SDK**：35（Android 14）
- **最低SDK**：24（Android 7.0）

## 📞 支持

如果构建失败，请提供：
1. 完整的错误信息
2. `java -version` 输出
3. 运行 `gradlew.bat assembleDebug --stacktrace` 的结果

**项目已完全准备好构建**！所有阶段6的新功能都已实现，等待测试验证。

---

**构建命令总结**：
```cmd
# 在项目目录中
java -version  # 验证Java
gradlew.bat assembleDebug  # 构建APK
# APK位置：app/build/outputs/apk/debug/app-debug.apk
```