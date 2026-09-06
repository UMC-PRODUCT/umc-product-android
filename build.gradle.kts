// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.androidx.navigation.safeargs) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
// Compose 컴파일러 계측 — 어떤 Composable이 skippable인지, 어떤 파라미터가 unstable인지 리포트로 뽑는다.
subprojects {
    plugins.withId("org.jetbrains.kotlin.plugin.compose") {
        extensions.configure<org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension> {
            metricsDestination.set(rootProject.layout.buildDirectory.dir("compose_metrics"))
            reportsDestination.set(rootProject.layout.buildDirectory.dir("compose_metrics"))
            // 실제로 불변인데 컴파일러가 알 수 없는 외부 타입을 stable 로 선언한다.
            stabilityConfigurationFiles.add(
                rootProject.layout.projectDirectory.file("compose_stability_config.conf"),
            )
        }
    }
}
