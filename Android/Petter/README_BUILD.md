# Android Studio 编译配置指南

## 问题解决

当前遇到的编译错误是由于Java版本不兼容导致的。Gradle 7.6.4与Java 25不兼容。

## 解决方案

### 方案一：降级Java版本（推荐）

1. **安装Java 17**：
   ```bash
   # 使用sdkman安装Java 17
   sdk install java 17.0.8-zulu

   # 切换到Java 17
   sdk use java 17.0.8-zulu
   ```

2. **验证Java版本**：
   ```bash
   java -version
   # 应该显示Java 17
   ```

3. **编译项目**：
   ```bash
   ./gradlew clean assembleDebug
   ```

### 方案二：升级Gradle版本

1. **修改项目级build.gradle**，升级到支持Java 25的版本：
   ```gradle
   plugins {
       id 'com.android.application' version '8.1.0' apply false
       id 'com.android.library' version '8.1.0' apply false
       id 'org.jetbrains.kotlin.android' version '1.8.20' apply false
   }
   ```

2. **修改gradle-wrapper.properties**：
   ```
   distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
   ```

## 当前配置状态

### ✅ 已完成配置

1. **Gradle版本配置**：
   - 项目级：AGP 7.4.2 + Kotlin 1.8.20
   - Gradle Wrapper：7.6.4
   - 镜像源：腾讯云镜像加速

2. **应用级build.gradle**：
   - compileSdk: 33
   - targetSdk: 33
   - minSdk: 21
   - 启用ViewBinding和DataBinding
   - 完整的依赖库配置

3. **AndroidManifest.xml**：
   - 完整的权限配置（网络、存储、前台服务）
   - 所有Activity注册
   - Application类配置

4. **资源文件**：
   - 完整的图标资源
   - 主题样式配置
   - 布局文件完整

### 📁 项目结构

```
app/src/main/
├── java/hi/petter/
│   ├── PetterApplication.kt          # Application类
│   ├── MainActivity.kt               # 主Activity
│   ├── domain/                     # 领域层
│   │   ├── model/                  # 数据模型
│   │   ├── usecase/                # 用例类
│   │   └── repository/             # 仓储接口
│   ├── data/                       # 数据层
│   │   ├── local/                  # 本地存储
│   │   ├── remote/                 # 网络请求
│   │   └── repository/             # 仓储实现
│   └── presentation/               # 表现层
│       ├── ui/                     # UI界面
│       └── service/                # 后台服务
├── res/                          # 资源文件
│   ├── layout/                    # 布局文件
│   ├── drawable/                  # 图标资源
│   ├── menu/                      # 菜单资源
│   ├── values/                    # 值资源
│   └── xml/                       # XML配置
└── AndroidManifest.xml             # 清单文件
```

## 编译命令

```bash
# 清理项目
./gradlew clean

# 编译Debug版本
./gradlew assembleDebug

# 编译Release版本
./gradlew assembleRelease

# 安装Debug版本到设备
./gradlew installDebug

# 运行单元测试
./gradlew test

# 运行UI测试
./gradlew connectedAndroidTest
```

## Android Studio 配置

1. **打开项目**：
   - File → Open → 选择`Petter`目录

2. **配置Gradle**：
   - File → Settings → Build, Execution, Deployment → Build Tools → Gradle
   - 设置Gradle JDK为Java 17

3. **同步项目**：
   - 点击"Sync Project with Gradle Files"

4. **构建APK**：
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

## 依赖库说明

- **AndroidX**: 现代Android支持库
- **Material Design**: Material Design组件
- **Room**: 本地数据库
- **Lifecycle**: 生命周期管理
- **Navigation**: 导航组件
- **Coroutines**: 协程支持
- **Retrofit**: 网络请求
- **Gson**: JSON解析
- **MQTT**: 消息队列客户端
- **Glide**: 图片加载（可选）

## 注意事项

1. **最低要求**：
   - Android Studio 2022.3.1+
   - Java 17
   - Android SDK API 21+

2. **权限说明**：
   - INTERNET: 网络访问
   - ACCESS_NETWORK_STATE: 网络状态检查
   - FOREGROUND_SERVICE: 前台服务
   - READ_EXTERNAL_STORAGE: 读取文件（可选）

3. **目标设备**：
   - Android 5.0+ (API 21+)
   - 支持ARM64和ARMv7架构