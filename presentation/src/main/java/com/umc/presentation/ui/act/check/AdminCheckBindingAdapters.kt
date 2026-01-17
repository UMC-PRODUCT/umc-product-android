package com.umc.presentation.ui.act.check

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object AdminCheckBindingAdapters {

    /**
     * 어드민 세션 카드의 상단 뱃지 스타일 (진행 중 / 종료됨)
     */
    @JvmStatic
    @BindingAdapter("adminStatusStyle")
    fun setAdminStatusStyle(view: UButton, status: AdminSessionStatus?) {
        if (status == null) return

        val (bgColorRes, textColorRes) = when (status) {
            AdminSessionStatus.IN_PROGRESS -> // 진행 중: 연한 파랑
                R.color.primary100 to R.color.primary600
            AdminSessionStatus.COMPLETED -> // 종료됨: 연한 회색
                R.color.neutral200 to R.color.neutral600
        }

        view.setCardBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
    }
}