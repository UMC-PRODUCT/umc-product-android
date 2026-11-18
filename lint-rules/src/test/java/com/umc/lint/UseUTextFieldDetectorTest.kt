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
        ).expectErrorCount(3).expectContains("UTextField를 사용하세요")
    }

    @Test
    fun `component 모듈 안에서는 허용`() {
        lintWith(
            UseUTextFieldDetector.ISSUE,
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
}
