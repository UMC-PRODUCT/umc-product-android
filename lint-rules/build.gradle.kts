plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.lint.api)
    testImplementation(libs.lint.checks)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}

tasks.jar {
    manifest {
        attributes("Lint-Registry-v2" to "com.umc.lint.UmcIssueRegistry")
    }
}

// UmcIssueRegistryTest 가 CodeRabbit 지시문의 자동 검사 목록과 레지스트리를 비교한다. 지시문만 바꿨을 때도 다시 돌도록 입력으로 등록한다.
tasks.withType<Test>().configureEach {
    inputs.file(rootProject.file(".coderabbit.yaml")).withPathSensitivity(PathSensitivity.RELATIVE)
}
