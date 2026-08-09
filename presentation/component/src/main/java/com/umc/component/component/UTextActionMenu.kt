package com.umc.component.component

import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.ui.Modifier

/**
 * 텍스트 롱클릭 메뉴에 추가할 커스텀 액션.
 * AI 기능 등 파트별 액션을 자유롭게 넣을 수 있다
 */
data class UTextActionItem(
    val label: String,
    val onClick: () -> Unit,
)

/**
 * 텍스트 필드의 롱클릭 컨텍스트 메뉴에 [actions]를 추가한다.
 * 복사/붙여넣기 등 기본 항목은 그대로 유지되고 그 뒤에 우리 항목이 붙는다.
 *
 * Compose 1.9부터 텍스트 컨텍스트 메뉴는 LocalTextToolbar가 아니라
 * foundation의 text context menu 시스템으로 표시되므로 이 확장 지점을 쓴다.
 * (LocalTextToolbar를 교체하는 예전 방식은 더 이상 동작하지 않는다)
 */
fun Modifier.uTextActionMenu(actions: List<UTextActionItem>): Modifier {
    if (actions.isEmpty()) return this

    return appendTextContextMenuComponents {
        separator()
        actions.forEach { action ->
            item(key = action.label, label = action.label) {
                close()
                action.onClick()
            }
        }
    }
}
