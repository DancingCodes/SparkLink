// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // 声明 Android 应用程序插件（来自 libs.versions.toml 的定义）
    // apply false 表示：在根目录只加载这个插件的版本信息，但不直接安装到根目录。
    // 具体的安装（应用）操作是在 app/build.gradle.kts 里完成的。
    alias(libs.plugins.android.application) apply false

    // 声明 Kotlin Compose 编译器插件
    // 同样使用 apply false，确保所有子模块都能共享同一个确定的 Kotlin 版本，
    // 避免不同模块之间因为 Kotlin 版本不一致导致编译冲突。
    alias(libs.plugins.kotlin.compose) apply false
}