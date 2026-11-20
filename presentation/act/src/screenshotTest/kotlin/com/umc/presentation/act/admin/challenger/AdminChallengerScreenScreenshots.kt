package com.umc.presentation.act.admin.challenger

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme
import kotlinx.collections.immutable.persistentListOf

/**
 * 운영진 챌린저 관리 화면을 창 크기 9가지(폭 400·610·900 × 높이 400·500·1000dp)로 찍고,
 * 400×500 폰 크기에서 다크 테마·글자 1.5배와 주요 상태(불러오는 중·오류·권한 없음)를 함께 찍는다.
 */
@Preview(name = "400x400", widthDp = 400, heightDp = 400, showBackground = true)
@Preview(name = "400x500", widthDp = 400, heightDp = 500, showBackground = true)
@Preview(name = "400x1000", widthDp = 400, heightDp = 1000, showBackground = true)
@Preview(name = "610x400", widthDp = 610, heightDp = 400, showBackground = true)
@Preview(name = "610x500", widthDp = 610, heightDp = 500, showBackground = true)
@Preview(name = "610x1000", widthDp = 610, heightDp = 1000, showBackground = true)
@Preview(name = "900x400", widthDp = 900, heightDp = 400, showBackground = true)
@Preview(name = "900x500", widthDp = 900, heightDp = 500, showBackground = true)
@Preview(name = "900x1000", widthDp = 900, heightDp = 1000, showBackground = true)
annotation class WindowSizePreviews

private val loadedState = AdminChallengerUiState(
    manageAccess = AdminChallengerAccess.ALLOWED,
    loadState = AdminChallengerLoadState.SUCCESS,
    sections = persistentListOf(
        AdminChallengerSectionUi(
            partName = "Android",
            members = persistentListOf(
                AdminChallengerMemberUi(id = 1L, nicknameWithName = "홍길동(길동)", generation = "11기", totalScore = 3),
                AdminChallengerMemberUi(id = 2L, nicknameWithName = "김챌린저(챌린저)", generation = "10기", totalScore = -1),
            ),
        ),
        AdminChallengerSectionUi(
            partName = "Web PE",
            members = persistentListOf(
                AdminChallengerMemberUi(id = 3L, nicknameWithName = "이웹(웹)", generation = "11기", totalScore = 0),
            ),
        ),
    ),
)

@PreviewTest
@WindowSizePreviews
@Composable
fun AdminChallengerScreenSizes() {
    UmcTheme { AdminChallengerScreen(uiState = loadedState) }
}

@PreviewTest
@Preview(name = "다크", widthDp = 400, heightDp = 500, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(name = "큰글자", widthDp = 400, heightDp = 500, showBackground = true, fontScale = 1.5f)
@Composable
fun AdminChallengerScreenVariants() {
    UmcTheme { AdminChallengerScreen(uiState = loadedState) }
}

@PreviewTest
@Preview(name = "불러오는 중", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun AdminChallengerScreenLoading() {
    UmcTheme {
        AdminChallengerScreen(
            uiState = AdminChallengerUiState(manageAccess = AdminChallengerAccess.ALLOWED, loadState = AdminChallengerLoadState.LOADING),
        )
    }
}

@PreviewTest
@Preview(name = "오류", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun AdminChallengerScreenError() {
    UmcTheme {
        AdminChallengerScreen(
            uiState = AdminChallengerUiState(manageAccess = AdminChallengerAccess.ALLOWED, loadState = AdminChallengerLoadState.ERROR),
        )
    }
}

@PreviewTest
@Preview(name = "권한 없음", widthDp = 400, heightDp = 500, showBackground = true)
@Composable
fun AdminChallengerScreenNoAccess() {
    UmcTheme { AdminChallengerScreen(uiState = AdminChallengerUiState(manageAccess = AdminChallengerAccess.DENIED)) }
}
