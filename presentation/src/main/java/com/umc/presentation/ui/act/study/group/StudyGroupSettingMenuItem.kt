package com.umc.presentation.ui.act.study.group

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.umc.presentation.R
import com.umc.domain.model.enums.EditDeleteAction

data class StudyGroupSettingMenuItem(
    val action: EditDeleteAction,
    val title: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val titleColorRes: Int = R.color.neutral800,
    @ColorRes val iconTintRes: Int = R.color.neutral600,
    @ColorRes val arrowTintRes: Int = R.color.neutral400,
)