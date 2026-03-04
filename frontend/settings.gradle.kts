// pluginManagement 块：负责配置“Gradle 插件”的下载策略
pluginManagement {
    // 定义插件的下载仓库源
    repositories {
        // 1. Google 官方仓库（包含 Android、Firebase 等插件）
        google {
            // 内容过滤：优化搜索性能，告诉 Gradle 只有以下开头的插件才去 google 仓库找
            content {
                includeGroupByRegex("com\\.android.*") // Android 官方插件
                includeGroupByRegex("com\\.google.*")  // Google 相关服务插件
                includeGroupByRegex("androidx.*")     // AndroidX 扩展库插件
            }
        }
        // 2. Maven 中央仓库（全球最大的 Java/Kotlin 库托管中心）
        mavenCentral()
        // 3. Gradle 官方插件门户（专门存放各种第三方 Gradle 插件）
        gradlePluginPortal()
    }
}

// plugins 块：应用一些全局性的工具插件
plugins {
    // foojay-resolver：这是一个自动化工具，如果你的电脑没装正确的 JDK，
    // 它能根据项目的 toolchain 设置，自动帮你下载并配置合适的 Java 环境。
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// dependencyResolutionManagement 块：负责配置“第三方依赖库”（如 Retrofit, Coil 等）的下载策略
dependencyResolutionManagement {
    // 设置仓库模式为 FAIL_ON_PROJECT_REPOS（强制模式）
    // 意思是为了统一管理，所有子模块（如 :app）禁止自己定义仓库，必须统一使用下面列出的仓库。
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    // 定义代码依赖库的下载来源
    repositories {
        google()       // 优先从 Google 仓库找（Android 官方库）
        mavenCentral() // 其次从 Maven 中央仓库找（大部分开源库都在这）
    }
}

// 定义整个项目的根名称（在 IDE 和构建日志中显示的名称）
rootProject.name = "sparklink"

// 包含子模块：告诉 Gradle 这个项目里有一个名为 "app" 的文件夹是需要编译的模块
include(":app")