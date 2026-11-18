package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class UseUTextFieldDetectorTest {

    @Test
    fun `화면 모듈에서 기본 입력창을 쓰면 오류`() {
        lintWith(
            UseUTextFieldDetector.ISSUE,
            kotlin(
                """
                package com.umc.presentation.notice

                import androidx.compose.foundation.text.BasicTextField
                import androidx.compose.material3.OutlinedTextField
                import androidx.compose.material3.TextField

                fun NoticeWriteForm(title: String, onTitleChange: (String) -> Unit) {
                    TextField(title, onTitleChange)
                    OutlinedTextField(title, onTitleChange)
                    BasicTextField(title, onTitleChange)
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/notice/test.kt:8: Error: TextField를 화면에서 직접 쓰면 입력 동작이 화면마다 갈라집니다. com.umc.component.component.UTextField를 사용하세요. [UseUTextField]
                TextField(title, onTitleChange)
                ~~~~~~~~~
            src/com/umc/presentation/notice/test.kt:9: Error: OutlinedTextField를 화면에서 직접 쓰면 입력 동작이 화면마다 갈라집니다. com.umc.component.component.UTextField를 사용하세요. [UseUTextField]
                OutlinedTextField(title, onTitleChange)
                ~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/notice/test.kt:10: Error: BasicTextField를 화면에서 직접 쓰면 입력 동작이 화면마다 갈라집니다. com.umc.component.component.UTextField를 사용하세요. [UseUTextField]
                BasicTextField(title, onTitleChange)
                ~~~~~~~~~~~~~~
            3 errors
            """,
        )
    }

    @Test
    fun `component 모듈에서는 허용`() {
        lintWith(
            UseUTextFieldDetector.ISSUE,
            Stubs.componentModuleManifest,
            kotlin(
                """
                package com.umc.component.component

                import androidx.compose.foundation.text.BasicTextField

                fun UTextField(value: String, onValueChange: (String) -> Unit) {
                    BasicTextField(value, onValueChange)
                }
                """,
            ).indented(),
        ).expectClean()
    }

    @Test
    fun `다른 모듈이 component 패키지를 선언해도 검사`() {
        lintWith(
            UseUTextFieldDetector.ISSUE,
            Stubs.screenModuleManifest,
            kotlin(
                """
                package com.umc.component.component

                import androidx.compose.foundation.text.BasicTextField

                fun SearchBar(value: String, onValueChange: (String) -> Unit) {
                    BasicTextField(value, onValueChange)
                }
                """,
            ).indented(),
        ).expectErrorCount(1)
    }
}
