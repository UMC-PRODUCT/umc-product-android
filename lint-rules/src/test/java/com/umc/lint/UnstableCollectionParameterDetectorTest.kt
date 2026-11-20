package com.umc.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import org.junit.Test

class UnstableCollectionParameterDetectorTest {

    @Test
    fun `Composable 의 List·Set·Map 파라미터는 경고, Immutable 타입·람다·미리보기·일반 함수는 무시`() {
        lintWith(
            UnstableCollectionParameterDetector.ISSUE,
            kotlin(
                "src/com/umc/presentation/home/Grid.kt",
                """
                package com.umc.presentation.home

                import androidx.compose.runtime.Composable
                import androidx.compose.runtime.Stable
                import androidx.compose.ui.tooling.preview.Preview
                import kotlinx.collections.immutable.ImmutableList

                @Stable
                interface StableItems<T> : List<T>

                @Composable
                fun Grid(
                    items: List<String>,
                    selected: Set<Int>,
                    labels: Map<String, String>,
                    pending: MutableList<String>,
                    fixed: ImmutableList<String>,
                    stable: StableItems<String>,
                    onItems: (List<String>) -> Unit,
                    title: String,
                ) {}

                @Preview
                @Composable
                fun GridPreview(items: List<String> = emptyList()) {}

                fun format(items: List<String>): String = items.joinToString()
                """,
            ).indented(),
        ).expect(
            """
            src/com/umc/presentation/home/Grid.kt:13: Warning: items: List<String> 는 내용이 같아도 새 인스턴스면 다시 그립니다. ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 받으세요. [UnstableCollectionParameter]
                items: List<String>,
                ~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/Grid.kt:14: Warning: selected: Set<Int> 는 내용이 같아도 새 인스턴스면 다시 그립니다. ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 받으세요. [UnstableCollectionParameter]
                selected: Set<Int>,
                ~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/Grid.kt:15: Warning: labels: Map<String, String> 는 내용이 같아도 새 인스턴스면 다시 그립니다. ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 받으세요. [UnstableCollectionParameter]
                labels: Map<String, String>,
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~
            src/com/umc/presentation/home/Grid.kt:16: Warning: pending: MutableList<String> 는 내용이 같아도 새 인스턴스면 다시 그립니다. ImmutableList·ImmutableSet·ImmutableMap(kotlinx.collections.immutable)으로 받으세요. [UnstableCollectionParameter]
                pending: MutableList<String>,
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            0 errors, 4 warnings
            """,
        )
    }
}
