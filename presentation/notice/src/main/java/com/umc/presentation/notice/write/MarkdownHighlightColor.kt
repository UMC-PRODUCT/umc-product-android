package com.umc.presentation.notice.write

import androidx.compose.ui.graphics.Color
import com.umc.component.theme.AppStrings
import java.util.Locale

/**
 * 형광펜 색상. iOS 스펙(공식 5색, opacity 0.35)과 동일하게 `<mark color="R,G,B,A">`로 직렬화한다.
 *
 * [swatch]는 메뉴에 보이는 iOS 라이트 모드 기준 원본 색이고, 본문에는 글자가 읽히도록
 * 같은 RGB에 [MARK_ALPHA]를 적용해 덧칠한다. 기존 7색으로 저장된 공지는 읽기 파서가
 * RGBA 값을 그대로 해석해 하위 호환을 유지한다.
 */
enum class MarkdownHighlightColor(val label: String, val swatch: Color) {
    PURPLE(AppStrings.HIGHLIGHT_PURPLE, Color(0xFFAF52DE)),
    PINK(AppStrings.HIGHLIGHT_PINK, Color(0xFFFF2D55)),
    ORANGE(AppStrings.HIGHLIGHT_ORANGE, Color(0xFFFF9500)),
    MINT(AppStrings.HIGHLIGHT_MINT, Color(0xFF00C7BE)),
    BLUE(AppStrings.HIGHLIGHT_BLUE, Color(0xFF007AFF));

    /** `<mark color="...">`에 들어가는 값. 각 성분 0~1 실수 */
    val markColorCode: String
        get() = listOf(swatch.red, swatch.green, swatch.blue, MARK_ALPHA)
            // 로케일에 따라 소수점이 쉼표가 되면 파싱이 깨지므로 US 고정
            .joinToString(",") { String.format(Locale.US, "%.3f", it) }

    companion object {
        /** 본문 위에 덧칠되므로 글자가 읽히도록 낮춘다 (iOS 기준 0.35) */
        private const val MARK_ALPHA = 0.35f
    }
}
