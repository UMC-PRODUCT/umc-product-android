# UMC product Android

Compose 전환 중인 멀티모듈 앱(`:app`, `:data`, `:domain`, `:presentation/*`, `:lint-rules`, `:benchmark`).
코딩 컨벤션은 `README.md`의 「📋 컨벤션」 절을 함께 본다. 이 문서는 그 위에 **AI 작업용 규칙**만 적는다.

## 코드 규칙

- 텍스트는 `Text` 대신 **`UText`**(`com.umc.component.component`)를 쓴다. fontScale 고정이 들어간 공용 컴포넌트다.
- 텍스트 입력은 `UTextField`를 거친다. 입력 지점 대부분이 이 컴포넌트 하나를 경유하므로, 입력 동작을 바꿀 일은 호출부가 아니라 이 파일에서 한다.
- 레이아웃은 기기 종류가 아니라 창 크기를 기준으로 생각한다. 매니페스트에 `screenOrientation` 고정이나 `resizeableActivity="false"`를 새로 추가하지 않는다(현재 16개 매니페스트 모두 고정이 없다). 화면 폭을 `width(350.dp)`처럼 못 박지 말고 `widthIn(max = …)`를 쓴다.

## 검증: 초록불이 보장하지 않는 것

- **테스트가 있는 모듈은 일부뿐이다.** 단위 테스트는 `:data`, `:domain`, `:presentation`, `:presentation:community`·`component`·`act`·`home`·`mypage`, `:lint-rules`에만 있다. `:app`과 나머지 presentation 모듈은 `testDebugUnitTest`가 NO-SOURCE로 **항상 통과**하므로, 이 모듈들을 바꿨을 때는 "테스트 통과"를 완료 근거로 보고하지 않는다.
- **`ktlintCheck`는 존재하지 않는 태스크다.** ktlint는 `gradle/libs.versions.toml`에 선언만 있고 어느 모듈에도 적용돼 있지 않다(README의 "Ktlint로 자동 점검" 설명과 실제 설정이 어긋나 있다). 정적 분석은 Android Lint와 `:lint-rules` 커스텀 규칙으로만 돈다.
- **lint 중에서 CI를 막는 건 커스텀 규칙의 ERROR뿐이다.** 게이트는 lint 리포트에서 category가 `UMC`인 항목만 보고, 내장 규칙은 ERROR여도 막지 않는다. 커스텀 규칙의 기존 위반은 모듈별 `lint-baseline.xml`에 묶여 있다. baseline은 규칙을 새로 추가할 때만 `python3 lint-rules/update_baseline.py --issue-id <새 규칙 ID>`로 갱신하고(다른 규칙의 항목은 그대로 둔다), 새로 생긴 위반을 숨기려고 갱신하지 않는다.
- **DI·네트워크를 바꿨으면 실행까지 확인한다.** `app/src/main/java/com/umc/product/di/`나 `Qualifier.kt`를 건드린 변경은 컴파일 통과로 끝내지 말고, 앱을 띄워 해당 화면의 네트워크 호출을 확인한 뒤 보고한다. qualifier 7종(`@NormalRetrofit`·`@AuthRetrofit`·Kakao·RemoteConfig 등)이 뒤바뀌어도 컴파일은 통과하고, 런타임에 401이나 엉뚱한 호스트로만 드러난다.
- **UI를 바꾸면 그 화면의 `@Preview`를 같이 갱신한다.** 여러 폭을 봐야 하는 화면에는 `@PreviewScreenSizes`를 붙인다. 스크린샷 테스트(Compose Preview Screenshot Testing, AGP test suite)는 `:presentation:component`와 `:presentation:act`에서만 시범 운영 중이고 CI는 결과를 보고만 한다. 그 밖의 화면은 CI가 UI 회귀를 잡지 못한다.
  - 시범 모듈의 UI를 바꾸면 `./gradlew :presentation:<모듈>:updateDebugScreenshotTestDefaultTestSuite`로 기준 이미지(`src/screenshotTestDefaultDebug/reference`)를 갱신해 함께 커밋한다. 비교는 `testDebugScreenshotTestDefaultTestSuite`, 리포트는 `build/reports/tests/testDebugScreenshotTestDefaultTestSuite/`.
  - 테스트는 `src/screenshotTest`에 `@PreviewTest`로 쓴다. 미리보기 이름에 `.`을 넣지 않는다(파일 이름이 잘려 이미지가 덮어써진다).

## 스킬

- `AndroidManifest.xml`의 `uses-permission`, `exported` 컴포넌트, `intent-filter`를 추가·변경하면 **android-permissions-security**와 **android-intent-security** 스킬로 점검한 뒤 보고한다.
- 안드로이드 주제 작업(navigation-3, adaptive, camerax, agp-9-upgrade, r8-analyzer, testing-setup 등)은 해당 `android/skills` 스킬을 먼저 읽고 그 지침을 따른다.
- 단, 그 스킬들은 **개발자 각자 머신의 전역 스킬**(`~/.claude/skills`)이라 저장소에는 없다. 없으면 `/plugin marketplace add android/skills` → `/plugin install android-skills@android-skills`로 설치한다. 저장소에 포함하는 프로젝트 스킬은 `.claude/skills/`에만 둔다.

## 라이브러리 버전

- 현재: AGP 9.5.1(Gradle 9.6.0), Kotlin 2.2.21, KSP 2.3.14, Compose BOM 2025.09.00(foundation 1.9.2 / **material3 1.3.2**), Navigation 2.9.6, minSdk 24, targetSdk 36.
- AGP 9는 Kotlin을 내장한다(built-in Kotlin). 모듈에 `org.jetbrains.kotlin.android` 플러그인이나 `android { kotlinOptions {} }`를 다시 넣지 않는다. Kotlin `jvmTarget`은 `compileOptions.targetCompatibility`를 따른다. AGP 내부 API(`com.android.build.gradle.internal.*`)는 build 스크립트에서 쓰지 않는다.
- `:lint-rules`의 lint 버전은 AGP 버전 + 23.0.0이다(AGP 9.5.1 → lint 32.5.1). AGP를 올리면 함께 올리고 `:lint-rules:test`로 확인한다.
- 새로 나왔거나 알파·베타인 API는 추측해서 쓰지 말고 릴리스 노트나 소스로 시그니처를 확인한다. 특히 material3가 1.3.2라 material3 1.4+나 Compose 1.13 알파의 API(M3 `TextFieldState` 오버로드 등)는 **이 저장소에 없다**.
- 버전을 올릴 때는 릴리스 노트에서 `minSdk` 변경을 먼저 확인한다.
- Kotlin을 올릴 때는 `gradle/libs.versions.toml`의 `kotlin` ref를 올린다. Kotlin JVM 플러그인(`:domain`, `:lint-rules`)도 같은 ref를 쓴다. AGP가 요구하는 KGP 최소 버전(AGP 9.5는 2.2.10)보다 낮추지 않는다. KSP는 2.3부터 Kotlin 버전과 독립(`2.3.x` 형식)이라 함께 올릴 필요가 없다. 예전 `<Kotlin>-<KSP>` 형식으로 되돌리면 접두사가 `kotlin`과 다를 때 Gradle 구성 단계에서 실패한다(`gradle/module-rules.gradle.kts`).

## 보고

- 작업 결과 보고는 한국어로 쓴다.
- 커밋은 사용자가 명시적으로 요청할 때만 한다.
