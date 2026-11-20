package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class UseUToastDetectorTest {

    private val toastStub = java(
        """
        package android.widget;

        public class Toast {
            public static final int LENGTH_SHORT = 0;
            public static Toast makeText(android.content.Context context, CharSequence text, int duration) { return null; }
            public void show() {}
        }
        """,
    ).indented()

    private val contextStub = java(
        """
        package android.content;

        public abstract class Context {}
        """,
    ).indented()

    @Test
    fun `시스템 Toast makeText 는 오류, 같은 이름의 다른 함수는 무시`() {
        lintWith(
            UseUToastDetector.ISSUE,
            toastStub,
            contextStub,
            kotlin(
                """
                package com.umc.presentation.home

                import android.content.Context
                import android.widget.Toast

                object Other {
                    fun makeText(message: String): String = message
                }

                fun notify(context: Context) {
                    Toast.makeText(context, "저장했어요", Toast.LENGTH_SHORT).show()
                    Other.makeText("괜찮다")
                }
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/Other.kt:11: Error: 시스템 Toast 대신 UToast를 쓰세요. 화면에서는 LocalUToast.current.show(…), 바텀시트·다이얼로그 안에서는 그 안에 둔 UToastHost로 띄웁니다. [UseUToast]
                Toast.makeText(context, "저장했어요", Toast.LENGTH_SHORT).show()
                      ~~~~~~~~
            1 error
            """,
        )
    }
}
