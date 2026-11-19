// 모듈 의존 방향과 Kotlin·KSP 버전 맞춤을 Gradle 구성 단계에서 검사한다.
// 리뷰 지시문에만 두면 지켜지지 않으므로, 어기면 빌드·CI·IDE sync 등 어떤 Gradle 실행이든 바로 실패하게 한다.
// 루트 build.gradle.kts 가 apply(from = ...) 로 적용한다.

import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.VersionCatalogsExtension

/** lint 규칙 모듈은 어느 모듈이든 lintChecks 로 가져다 쓸 수 있다. */
val lintConfigurations = setOf("lintChecks", "lintPublish")

/**
 * 모듈이 의존해도 되는 프로젝트. null 이면 제한하지 않는다.
 * 새 모듈은 여기에 규칙을 정하기 전까지 다른 프로젝트에 의존할 수 없다.
 */
fun allowedDependencies(path: String, allPaths: Set<String>): Set<String>? = when {
    path == ":app" || path == ":benchmark" -> null
    path == ":domain" || path == ":lint-rules" -> emptySet()
    path == ":data" -> setOf(":domain")
    // 화면을 모아 내비게이션을 조립하는 모듈이라 presentation 하위 모듈 전부에 의존할 수 있다.
    path == ":presentation" -> allPaths.filter { it.startsWith(":presentation:") }.toSet() + ":domain"
    path == ":presentation:component" -> setOf(":domain")
    // 잎 모듈(화면 기능)은 서로 의존하지 않는다. 공통은 :presentation:component 로 올린다.
    path.startsWith(":presentation:") -> setOf(":domain", ":presentation:component")
    else -> emptySet()
}

// configure-on-demand 는 요청에 필요한 모듈만 구성하므로, 구성되지 않은 모듈의 의존 선언은 검사할 수 없다.
// 위반을 놓친 채 통과하지 않도록 이 모드에서는 바로 실패한다.
if (gradle.startParameter.isConfigureOnDemand) {
    throw GradleException(
        "모듈 의존 규칙은 모든 모듈을 구성해야 검사할 수 있습니다. configure-on-demand 를 끄고 실행하세요 " +
            "(--no-configure-on-demand, 또는 gradle.properties 의 org.gradle.configureondemand=false). 규칙: gradle/module-rules.gradle.kts",
    )
}

gradle.projectsEvaluated {
    val allPaths = rootProject.allprojects.map { it.path }.toSet()
    // 모든 configuration 을 본다. annotationProcessor·kapt 처럼 선언하면서 resolvable 이기도 한 것도 빠지지 않게 한다.
    // AGP 가 만드는 *Classpath 에도 같은 의존이 들어 있으므로, 의존 하나를 한 줄로 묶고 선언한 쪽 이름을 앞세운다.
    val edges = rootProject.allprojects.flatMap { module ->
        val allowed = allowedDependencies(module.path, allPaths) ?: return@flatMap emptyList()
        module.configurations
            .filter { it.name !in lintConfigurations }
            .flatMap { configuration ->
                configuration.dependencies
                    .withType(ProjectDependency::class.java)
                    .filter { it.path != module.path && it.path !in allowed }
                    .map { Triple(module.path, it.path, configuration.name) }
            }
    }
    val violations = edges
        .groupBy({ it.first to it.second }, { it.third })
        .map { (edge, configurations) ->
            val declared = configurations.filterNot { it.endsWith("Classpath") }.ifEmpty { configurations }
            "${edge.first} -> ${edge.second} (${declared.distinct().sorted().joinToString()})"
        }
        .sorted()

    if (violations.isNotEmpty()) {
        throw GradleException(
            buildString {
                appendLine("모듈 의존 규칙 위반:")
                violations.forEach { appendLine("  $it") }
                appendLine()
                appendLine("presentation 잎 모듈은 :domain 과 :presentation:component 에만 의존합니다.")
                appendLine("다른 잎 모듈의 화면이 필요하면 :presentation(MainNavHost)에서 조립해 슬롯(@Composable 파라미터)으로 넘기고,")
                appendLine("여러 모듈이 쓰는 코드는 :presentation:component 로 옮기세요. :domain 은 다른 프로젝트에 의존하지 않습니다.")
                append("규칙: gradle/module-rules.gradle.kts")
            },
        )
    }
}

// KSP 는 2.3.0 전까지 "<Kotlin 버전>-<KSP 버전>" 형식이라 Kotlin 과 접두사가 같아야 한다.
// 접두사가 없는 새 형식(2.3.0 이후)은 Kotlin 버전과 독립이라 검사하지 않는다.
gradle.projectsEvaluated {
    val libs = rootProject.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    val kotlin = libs.findVersion("kotlin").get().requiredVersion
    val ksp = libs.findVersion("ksp").get().requiredVersion
    val kotlinPrefix = Regex("""^(\d+\.\d+\.\d+(?:-[A-Za-z0-9]+)?)-\d+\.\d+\.\d+$""").matchEntire(ksp)?.groupValues?.get(1)
    if (kotlinPrefix != null && kotlinPrefix != kotlin) {
        throw GradleException(
            "gradle/libs.versions.toml 의 ksp=$ksp 는 Kotlin $kotlinPrefix 용인데 kotlin=$kotlin 입니다. " +
                "Kotlin 을 올릴 때는 같은 Kotlin 버전용 KSP 로 함께 올리세요.",
        )
    }
}
