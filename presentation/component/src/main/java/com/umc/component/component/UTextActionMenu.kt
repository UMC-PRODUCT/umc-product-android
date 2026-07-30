package com.umc.component.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey950
import kotlin.math.roundToInt

/**
 * 텍스트 필드 롱클릭 메뉴에 표시할 액션.
 * AI 기능 등 파트별 커스텀 액션을 자유롭게 넣을 수 있다
 */
data class UTextActionItem(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * 텍스트 필드의 기본 롱클릭 툴바(복사/붙여넣기)를 커스텀 액션 메뉴로 교체하는 호스트.
 * [content] 안의 텍스트 필드를 롱클릭하면 해당 위치에 [actions] 메뉴 카드가 뜬다.
 * 텍스트를 선택한 상태에서는 복사/잘라내기 행이 함께 노출된다.
 *
 * [enabled]가 false면 시스템 기본 툴바를 그대로 사용한다 (AI 미지원 기기 등)
 */
@Composable
fun UTextActionMenuHost(
    actions: List<UTextActionItem>,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!enabled || actions.isEmpty()) {
        Box(modifier) { content() }
        return
    }

    val state = remember { UTextActionMenuState() }
    var hostOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            hostOrigin = coordinates.positionInRoot()
        },
    ) {
        CompositionLocalProvider(LocalTextToolbar provides state.toolbar) {
            content()
        }

        state.menuRect?.let { rect ->
            val density = LocalDensity.current
            val offset = with(density) {
                IntOffset(
                    x = (rect.left - hostOrigin.x).roundToInt() + 24.dp.roundToPx(),
                    y = (rect.bottom - hostOrigin.y).roundToInt() + 4.dp.roundToPx(),
                )
            }

            Popup(
                offset = offset,
                onDismissRequest = { state.hideMenu() },
                // 키보드가 내려가지 않도록 포커스를 뺏지 않음 (필드 탭 시 툴바 hide로 닫힘)
                properties = PopupProperties(focusable = false),
            ) {
                UTextActionMenuCard(
                    actions = buildList {
                        addAll(actions)
                        state.onCopy?.let { onCopy ->
                            add(UTextActionItem(AppStrings.COMMON_COPY, onCopy))
                        }
                        state.onCut?.let { onCut ->
                            add(UTextActionItem(AppStrings.COMMON_CUT, onCut))
                        }
                    },
                    onDismiss = { state.hideMenu() },
                )
            }
        }
    }
}

/** 롱클릭 위치에 뜨는 흰색 라운드 메뉴 카드 */
@Composable
private fun UTextActionMenuCard(
    actions: List<UTextActionItem>,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 140.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(grey000()),
    ) {
        actions.forEach { action ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        action.onClick()
                    }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                UText(
                    text = action.label,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey950(),
                )
            }
        }
    }
}

/** 커스텀 툴바 상태. 텍스트 필드가 요청한 메뉴 위치/기본 액션 콜백을 보관 */
@Stable
private class UTextActionMenuState {

    var menuRect by mutableStateOf<Rect?>(null)
        private set

    var onCopy: (() -> Unit)? by mutableStateOf(null)
        private set

    var onCut: (() -> Unit)? by mutableStateOf(null)
        private set

    val toolbar: TextToolbar = object : TextToolbar {
        override val status: TextToolbarStatus
            get() = if (menuRect != null) TextToolbarStatus.Shown else TextToolbarStatus.Hidden

        override fun showMenu(
            rect: Rect,
            onCopyRequested: (() -> Unit)?,
            onPasteRequested: (() -> Unit)?,
            onCutRequested: (() -> Unit)?,
            onSelectAllRequested: (() -> Unit)?,
        ) {
            menuRect = rect
            onCopy = onCopyRequested
            onCut = onCutRequested
        }

        override fun hide() {
            hideMenu()
        }
    }

    fun hideMenu() {
        menuRect = null
        onCopy = null
        onCut = null
    }
}
