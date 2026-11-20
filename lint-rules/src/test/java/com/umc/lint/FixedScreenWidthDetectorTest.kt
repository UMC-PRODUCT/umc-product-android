package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

class FixedScreenWidthDetectorTest {

    private val issues: Array<Issue> = arrayOf(FixedScreenWidthDetector.ISSUE, FixedScreenWidthDetector.ORDER_ISSUE)

    /**
     * lint 32 부터 기본으로 도는 JVM_OVERLOADS 모드는 스텁 함수에 @JvmOverloads 와 가짜 파라미터를 끼워 넣어 인자 매핑을 바꾼다.
     * Compose 레이아웃 modifier(widthIn·size 등)는 @JvmOverloads 가 아니고, 이 검사는 실제 파라미터 이름(min·max·width)으로
     * 인자를 고르므로 그 모드는 뺀다.
     */
    private fun lintWidth(vararg files: TestFile): TestLintResult =
        lint()
            .allowMissingSdk()
            .files(*Stubs.ALL, *files)
            .issues(*issues)
            .skipTestModes(TestMode.JVM_OVERLOADS)
            .run()

    @Test
    fun `300dp 이상 고정 폭은 오류, 상한과 300dp 미만과 파라미터와 미리보기는 무시`() {
        lintWidth(
            kotlin(
                """
                package com.umc.presentation.login

                import androidx.compose.foundation.layout.requiredWidth
                import androidx.compose.foundation.layout.size
                import androidx.compose.foundation.layout.sizeIn
                import androidx.compose.foundation.layout.width
                import androidx.compose.foundation.layout.widthIn
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.Dp
                import androidx.compose.ui.unit.dp

                private val ButtonWidth = 320.dp

                fun Screen(width: Dp) {
                    Modifier.width(350.dp)
                    Modifier.size(width = 364.dp, height = 52.dp)
                    Modifier.requiredWidth(ButtonWidth)
                    Modifier.widthIn(min = 300.dp)
                    Modifier.size(Dp(310f))
                    Modifier.widthIn(max = 350.dp)
                    Modifier.sizeIn(maxWidth = 600.dp)
                    Modifier.width(280.dp)
                    Modifier.width(width)
                }

                @Preview
                fun ScreenPreview() {
                    Modifier.width(400.dp)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/login/test.kt:16: Error: 350dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 350.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.width(350.dp)
                ~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/login/test.kt:17: Error: 364dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 364.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.size(width = 364.dp, height = 52.dp)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/login/test.kt:18: Error: 320dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 320.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.requiredWidth(ButtonWidth)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/login/test.kt:19: Error: 300dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 300.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.widthIn(min = 300.dp)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/login/test.kt:20: Error: 310dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 310.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.size(Dp(310f))
                ~~~~~~~~~~~~~~~~~~~~~~~
            5 errors
            """,
        )
    }

    @Test
    fun `fillMaxWidth 뒤의 widthIn 은 오류, 앞에 두면 무시`() {
        lintWidth(
            kotlin(
                """
                package com.umc.presentation.community

                import androidx.compose.foundation.layout.fillMaxWidth
                import androidx.compose.foundation.layout.padding
                import androidx.compose.foundation.layout.requiredWidthIn
                import androidx.compose.foundation.layout.widthIn
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.unit.dp

                fun Dialog() {
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 370.dp)
                    Modifier.padding(horizontal = 24.dp).widthIn(max = 370.dp).fillMaxWidth()
                    Modifier.fillMaxWidth().requiredWidthIn(max = 370.dp)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/community/test.kt:11: Error: fillMaxWidth() 가 폭을 먼저 고정해 widthIn 의 상한이 적용되지 않습니다. widthIn 을 fillMaxWidth() 앞에 두세요. [WidthInAfterFillMaxWidth]
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 370.dp)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `DpSize 폭도 보고, 팀 multipreview 함수는 무시`() {
        lintWidth(
            kotlin(
                "src/com/umc/presentation/home/Sized.kt",
                """
                package com.umc.presentation.home

                import androidx.compose.foundation.layout.size
                import androidx.compose.foundation.layout.width
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.tooling.preview.Preview
                import androidx.compose.ui.unit.DpSize
                import androidx.compose.ui.unit.dp

                @Preview
                annotation class DevicePreviews

                fun Banner() {
                    Modifier.size(DpSize(360.dp, 48.dp))
                }

                @DevicePreviews
                fun BannerPreview() {
                    Modifier.width(400.dp)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/Sized.kt:14: Error: 360dp 고정 폭은 좁은 화면에서 좌우 여백을 없앱니다. padding(horizontal = …).widthIn(max = 360.dp).fillMaxWidth() 처럼 상한으로 바꾸세요. [FixedScreenWidth]
                Modifier.size(DpSize(360.dp, 48.dp))
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            1 error
            """,
        )
    }

    @Test
    fun `높이만 거는 sizeIn 과 wrapContentWidth 뒤의 widthIn 은 무시, then 과 val 로 나눈 체인은 보고`() {
        lintWidth(
            kotlin(
                "src/com/umc/presentation/community/Chains.kt",
                """
                package com.umc.presentation.community

                import androidx.compose.foundation.layout.fillMaxWidth
                import androidx.compose.foundation.layout.sizeIn
                import androidx.compose.foundation.layout.widthIn
                import androidx.compose.foundation.layout.wrapContentWidth
                import androidx.compose.ui.Modifier
                import androidx.compose.ui.unit.dp

                fun Chains() {
                    Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp)
                    Modifier.fillMaxWidth().wrapContentWidth().widthIn(max = 600.dp)
                    Modifier.fillMaxWidth().then(Modifier.widthIn(max = 600.dp))
                    val base = Modifier.fillMaxWidth()
                    base.widthIn(max = 370.dp)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/community/Chains.kt:13: Error: fillMaxWidth() 가 폭을 먼저 고정해 widthIn 의 상한이 적용되지 않습니다. widthIn 을 fillMaxWidth() 앞에 두세요. [WidthInAfterFillMaxWidth]
                Modifier.fillMaxWidth().then(Modifier.widthIn(max = 600.dp))
                                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/community/Chains.kt:15: Error: fillMaxWidth() 가 폭을 먼저 고정해 widthIn 의 상한이 적용되지 않습니다. widthIn 을 fillMaxWidth() 앞에 두세요. [WidthInAfterFillMaxWidth]
                base.widthIn(max = 370.dp)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~
            2 errors
            """,
        )
    }
}
