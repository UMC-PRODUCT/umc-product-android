package com.umc.presentation.ui.act.challenge

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object UserChallengerBindingAdapters {

    @JvmStatic
    @BindingAdapter("userChallengerRoleStyle")
    fun setUserChallengerRoleStyle(view: UButton, role: UserChallengerRole?) {
        if (role == null || role == UserChallengerRole.MEMBER) {
            view.visibility = View.GONE
            return
        }

        view.visibility = View.VISIBLE
        view.setText(role.label)

        // 역할별 색상 리소스 정의
        val (bgColorRes, textColorRes, borderColorRes) = when (role) {
            UserChallengerRole.LEADER -> Triple(R.color.warning100, R.color.warning700, R.color.warning300)
            UserChallengerRole.SUB_LEADER -> Triple(R.color.accent100, R.color.accent700, R.color.accent300)
            UserChallengerRole.PART_LEADER -> Triple(R.color.success100, R.color.success700, R.color.success300)
            else -> Triple(R.color.neutral100, R.color.neutral700, R.color.neutral200)
        }

        // 배경색, 텍스트색, 테두리색 적용
        view.setUBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
        view.strokeColor = ContextCompat.getColor(view.context, borderColorRes)
    }
}