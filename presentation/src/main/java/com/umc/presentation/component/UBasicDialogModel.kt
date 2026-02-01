package com.umc.presentation.component

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.umc.presentation.R


sealed class UBasicDialogModel(
    val title: String,
    val content: String?,
    val negativeText: String,
    val positiveText: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBackgroundRes: Int,
    @ColorRes val iconTintRes: Int,
    @ColorRes val positiveTextColorRes: Int,
    @ColorRes val positiveBorderColorRes: Int
) {
    /**
     * 경고 다이얼로그 (빨간색, 에러 아이콘)
     */
    class Warning(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(
        title = title,
        content = content,
        negativeText = negativeText,
        positiveText = positiveText,
        iconRes = R.drawable.ic_error_filled,
        iconBackgroundRes = R.drawable.bg_danger100_radius8,
        iconTintRes = R.color.danger500,
        positiveTextColorRes = R.color.danger500,
        positiveBorderColorRes = R.color.danger500
    )

    /**
     * 반려 다이얼로그 (빨간색, 체크 실패 아이콘)
     */
    class Cancel(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(
        title = title,
        content = content,
        negativeText = negativeText,
        positiveText = positiveText,
        iconRes = R.drawable.ic_check_failed,
        iconBackgroundRes = R.drawable.bg_danger100_radius8,
        iconTintRes = R.color.danger500,
        positiveTextColorRes = R.color.danger500,
        positiveBorderColorRes = R.color.danger500
    )

    /**
     * 성공 다이얼로그 (초록색, 체크 성공 아이콘)
     */
    class Success(
        title: String,
        content: String? = null,
        negativeText: String = "취소",
        positiveText: String
    ) : UBasicDialogModel(
        title = title,
        content = content,
        negativeText = negativeText,
        positiveText = positiveText,
        iconRes = R.drawable.ic_check_success,
        iconBackgroundRes = R.drawable.bg_success100_radius8,
        iconTintRes = R.color.success500,
        positiveTextColorRes = R.color.success500,
        positiveBorderColorRes = R.color.success500
    )
}