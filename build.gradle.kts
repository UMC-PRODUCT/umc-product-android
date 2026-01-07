// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
}

subprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    }
    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
            android.set(true)
            ignoreFailures.set(false)
            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }
    }
}