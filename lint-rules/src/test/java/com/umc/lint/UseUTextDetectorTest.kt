package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class UseUTextDetectorTest {

    @Test
    fun `화면 모듈에서 material3 Text를 쓰면 오류`() {
        lintWith(
            UseUTextDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.material3.Text

                fun HomeTitle() {
                    Text("홈")
                }
                """,
            ).indented(),
        ).expectErrorCount(1).expectContains("UText를 사용하세요")
    }

    @Test
    fun `화면 모듈에서 BasicText를 쓰면 오류`() {
        lintWith(
            UseUTextDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                import androidx.compose.foundation.text.BasicText

                fun HomeTitle() {
                    BasicText("홈")
                }
                """,
            ).indented(),
        ).expectErrorCount(1)
    }

    @Test
    fun `component 모듈에서는 허용`() {
        lintWith(
            UseUTextDetector.ISSUE,
            Stubs.componentModuleManifest,
            kotlin(
                """
                package com.umc.component.component

                import androidx.compose.material3.Text

                fun UText(text: String) {
                    Text(text)
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `다른 모듈이 component 패키지를 선언해도 검사`() {
        lintWith(
            UseUTextDetector.ISSUE,
            Stubs.screenModuleManifest,
            kotlin(
                """
                package com.umc.component.component

                import androidx.compose.material3.Text

                fun HomeTitle() {
                    Text("홈")
                }
                """,
            ).indented(),
        ).expectErrorCount(1)
    }

    @Test
    fun `같은 이름의 다른 함수는 무시`() {
        lintWith(
            UseUTextDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.home

                fun Text(value: String) {}

                fun HomeTitle() {
                    Text("홈")
                }
                """,
            ).indented(),
        ).expectClean()
    }
}
