plugins {
    // 应用 Android 应用程序插件（把这个项目定义为一个可安装的 App，而不是库）
    alias(libs.plugins.android.application)
    // 应用 Kotlin 的 Compose 编译器插件（让 Kotlin 支持 Jetpack Compose 语法）
    alias(libs.plugins.kotlin.compose)
    // 应用序列化插件
    alias(libs.plugins.kotlin.serialization)
}

android {
    // 项目的命名空间，通常对应代码的包名，用于生成 R 文件和 BuildConfig
    namespace = "love.moonc.sparklink"
    // 编译时使用的 SDK 版本。这里使用了 API 36 (Android 16) 的预览版配置
    compileSdk {
        // 指定发布版本号为 36
        version = release(36) {
            // 次要 API 级别（Android 16 引入的新机制，用于季度更新）
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // App 的唯一标识符（就像身份证号，发布到商店后不能更改）
        applicationId = "love.moonc.sparklink"
        // 最低支持的 Android 版本。24 对应 Android 7.0（低于此版本的手机无法安装）
        minSdk = 24
        // 目标 SDK 版本。告诉系统你的 App 是针对 Android 16 开发并测试过的
        // 系统会根据这个值决定是否开启某些新特性的兼容模式
        targetSdk = 36
        // 内部版本号（整数），每次发版都要递增，应用商店靠它判断是否是新版
        versionCode = 1
        // 展示给用户的版本号（字符串），比如 "1.0.0"
        versionName = "1.0.0"
        // 指定进行自动化测试时使用的运行器
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // 发布模式的配置
        release {
            // 是否开启代码混淆和压缩（设为 false 表示不开启，通常为了减小包体积会设为 true）
            isMinifyEnabled = true
            // 指定混淆规则文件
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // 指定编译 Java 代码时使用的兼容版本（目前主流推荐 11 或 17）
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // 显式开启 Jetpack Compose 功能
        compose = true
    }
}

dependencies {
    // 提供了大量的 Kotlin 扩展函数，让 Android 原生 API 写起来更简洁
    implementation(libs.androidx.core.ktx)
    // 处理 Activity 和 Fragment 的生命周期（比如在后台自动停止协程）
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // 让 Activity 支持 Jetpack Compose，是 Compose 项目的入口
    implementation(libs.androidx.activity.compose)
    // BOM (Bill of Materials) 类似于一个版本清单，确保下面所有 Compose 库版本兼容，不用一个个写版本号
    implementation(platform(libs.androidx.compose.bom))
    // Compose 的核心 UI 库（布局、测量、绘制等）
    implementation(libs.androidx.compose.ui)
    // Compose 的图形库（处理颜色、矢量图等）
    implementation(libs.androidx.compose.ui.graphics)
    // 在 Android Studio 编辑器中实时预览 UI 的工具
    implementation(libs.androidx.compose.ui.tooling.preview)
    // Material Design 3 UI 组件库（按钮、文本框、卡片等最新设计规范）
    implementation(libs.androidx.compose.material3)
    // 纯 Java/Kotlin 逻辑的测试工具
    testImplementation(libs.junit)
    // Android 环境下的 JUnit 扩展
    androidTestImplementation(libs.androidx.junit)
    // 传统的 Android UI 测试工具（模拟点击、检查视图）
    androidTestImplementation(libs.androidx.espresso.core)
    // 确保测试环境的 Compose 版本与正式版一致
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // 专门用于测试 Compose 界面的工具
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // 调试专用：在运行时查看布局边界等工具
    debugImplementation(libs.androidx.compose.ui.tooling)
    // 调试专用：用于在测试中启动空的 Activity 容器
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 新增
    // 导航库：处理 Compose 页面之间的跳转（类似路由）
    implementation(libs.androidx.navigation.compose)
    // 轻量级存储：用来保存简单的配置或 Token（取代旧的 SharedPreference）
    implementation(libs.androidx.datastore.preferences)
    // 扩展图标库：提供了 Material Design 官方最全的图标集合
    implementation(libs.androidx.compose.material.icons.extended)
    // OkHttp 核心：Android 最基础的网络请求底层框架
    implementation(libs.okhttp.core)
    // OkHttp 日志拦截器：在控制台打印网络请求的详细日志（非常利于调试接口）
    implementation(libs.okhttp.logging)
    // Retrofit 核心：基于 OkHttp 的类型安全网络请求库，能把 API 变成 Kotlin 接口
    implementation(libs.retrofit.core)
    // Retrofit 解析器：自动把服务器返回的 JSON 字符串转成 Kotlin 对象（实体类）
    implementation(libs.retrofit.gson)
    // Coil：Compose 最常用的异步图片加载库（从 URL 加载图片到界面上）
    implementation(libs.coil.compose)
    // 引入 JSON 序列化库
    implementation(libs.kotlinx.serialization.json)
}