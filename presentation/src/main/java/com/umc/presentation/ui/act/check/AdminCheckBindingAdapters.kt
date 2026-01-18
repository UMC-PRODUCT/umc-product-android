package com.umc.presentation.ui.act.check

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.AdminSessionStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object AdminCheckBindingAdapters {

    /**
     * 세션 상태(진행 중/종료)에 따른 UButton 배경 및 텍스트 색상 설정
     */
    @JvmStatic
    @BindingAdapter("adminStatusStyle")
    fun setAdminStatusStyle(view: UButton, status: AdminSessionStatus?) {
        if (status == null) return

        val (bgColorRes, textColorRes) = when (status) {
            AdminSessionStatus.IN_PROGRESS ->
                R.color.primary100 to R.color.primary600
            AdminSessionStatus.COMPLETED ->
                R.color.neutral200 to R.color.neutral600
        }

        view.setCardBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
    }
}