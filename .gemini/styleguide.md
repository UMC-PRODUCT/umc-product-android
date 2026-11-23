# UMC Android 리뷰 지침

- 모든 리뷰, 요약, 설명은 한국어로 작성한다.
- Kotlin Coroutines 사용 시 예외 처리가 누락되지 않았는지 확인한다.
- MVVM 패턴에 따라 비즈니스 로직이 ViewModel에 분리되었는지 검토한다.
- Jetpack Compose에서 불필요한 리컴포지션이 생길 코드가 있는지 확인한다.
- `:lint-rules` 커스텀 Lint, `gradle/module-rules.gradle.kts`(모듈 의존 방향), `:data`의 `GsonDtoKeepPackageTest`(R8 keep 패키지)가 CI에서 막는 항목은 지적하지 않는다. 규칙 목록은 `lint-rules/src/main/java/com/umc/lint/UmcIssueRegistry.kt`에 있다. 단, `domain/`은 Android Lint 대상이 아니므로 이슈 번호 없는 TODO·FIXME·`[임시]`는 리뷰에서 지적한다.
