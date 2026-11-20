package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestFiles.xml
import org.junit.Test

class UntrackedTodoDetectorTest {

    @Test
    fun `번호 없는 TODO·FIXME·임시 표시는 오류, 번호가 붙은 것과 낱말 일부는 무시`() {
        lintWith(
            UntrackedTodoDetector.ISSUE,
            kotlin(
                """
                package test.pkg

                // TODO 공통 에러 처리
                // TODO: 서버 응답이 바뀌면 교체
                // TODO(#123): 명함 교환 차단을 한곳으로 모은다
                // FIXME(#12) 경계값
                // [임시] 명함 교환 차단
                // TODOS, MY_TODO_LIST, FIXMEs 는 표시가 아니다

                /**
                 * 카드 UI.
                 *
                 * FIXME 태그 색
                 */
                fun card(): String {
                    /* TODO 회원 탈퇴 연결 */
                    return "TODO 문자열은 주석이 아니다"
                }
                """,
            ).indented(),
            java(
                """
                package test.pkg;

                class Legacy {
                    // TODO(#123) 이동 연결
                    // TODO 이동 연결
                    void run() {}
                }
                """,
            ).indented(),
        ).expect(
            """
            src/test/pkg/Legacy.java:5: Error: 이슈 번호 없는 TODO 는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
                // TODO 이동 연결
                   ~~~~
            src/test/pkg/test.kt:3: Error: 이슈 번호 없는 TODO 는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
            // TODO 공통 에러 처리
               ~~~~
            src/test/pkg/test.kt:4: Error: 이슈 번호 없는 TODO 는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
            // TODO: 서버 응답이 바뀌면 교체
               ~~~~
            src/test/pkg/test.kt:7: Error: [임시] 표시는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호) 형식으로 바꾸세요. [UntrackedTodo]
            // [임시] 명함 교환 차단
               ~~~~
            src/test/pkg/test.kt:13: Error: 이슈 번호 없는 FIXME 는 추적되지 않습니다. 추적 이슈를 만들고 FIXME(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
             * FIXME 태그 색
               ~~~~~
            src/test/pkg/test.kt:16: Error: 이슈 번호 없는 TODO 는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
                /* TODO 회원 탈퇴 연결 */
                   ~~~~
            6 errors
            """,
        )
    }

    @Test
    fun `리소스와 매니페스트의 XML 주석도 본다`() {
        lintWith(
            UntrackedTodoDetector.ISSUE,
            xml(
                "res/values/strings.xml",
                """
                <resources>
                    <!-- TODO: Remove or change this placeholder text -->
                    <string name="hello">Hello</string>
                    <!-- TODO(#123) 이모지 문구 -->
                    <string name="emoji">이모지</string>
                </resources>
                """,
            ).indented(),
            manifest(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.umc.product">
                    <!-- [임시] 권한 -->
                    <application />
                </manifest>
                """,
            ).indented(),
        ).expect(
            """
            AndroidManifest.xml:2: Error: [임시] 표시는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호) 형식으로 바꾸세요. [UntrackedTodo]
                <!-- [임시] 권한 -->
                ~~~~~~~~~~~~~~~~
            res/values/strings.xml:2: Error: 이슈 번호 없는 TODO 는 추적되지 않습니다. 추적 이슈를 만들고 TODO(#이슈번호)처럼 번호를 붙이세요. [UntrackedTodo]
                <!-- TODO: Remove or change this placeholder text -->
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }
}
