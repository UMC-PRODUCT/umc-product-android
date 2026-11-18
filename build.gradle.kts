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

// 커스텀 Lint(:lint-rules)의 기존 위반은 모듈별 lint-baseline.xml 에 기록해 두고, 새로 생기는 위반만 리포트에 남긴다.
// baseline 에는 UMC 규칙 항목만 담는다(내장 규칙 경고는 지금처럼 리포트에 나온다). 파일이 없는 모듈은 빈 baseline 으로 본다.
// 새 규칙을 추가해 기존 위반을 묶어야 할 때만 python3 lint-rules/update_baseline.py --issue-id <새 규칙 ID> 로 갱신한다.
subprojects {
    val module = this
    // update_baseline.py 는 -PumcLintBaselineDir=<임시 디렉터리> 로 결과를 저장소 밖에 쓰게 한 뒤, 성공했을 때만 반영한다.
    val baselineFile = providers.gradleProperty("umcLintBaselineDir").orNull
        ?.let { dir -> File(dir, "${module.projectDir.relativeTo(rootDir).path}/lint-baseline.xml") }
        ?: module.file("lint-baseline.xml")
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            lint { baseline = baselineFile }
        }
    }
    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.api.dsl.LibraryExtension> {
            lint { baseline = baselineFile }
        }
    }
}
