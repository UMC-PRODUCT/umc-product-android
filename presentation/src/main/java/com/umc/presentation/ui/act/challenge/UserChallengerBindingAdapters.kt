package com.umc.presentation.ui.act.challenge

import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.presentation.R
import com.umc.presentation.component.UButton
import com.umc.presentation.extension.px

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

        // 색상 정의
        val (bgColorRes, textColorRes, borderColorRes) = when (role) {
            UserChallengerRole.LEADER -> Triple(R.color.warning100, R.color.warning700, R.color.warning700)
            UserChallengerRole.SUB_LEADER -> Triple(R.color.accent100, R.color.accent700, R.color.accent700)
            UserChallengerRole.PART_LEADER -> Triple(R.color.success100, R.color.success700, R.color.success700)
            else -> Triple(R.color.neutral100, R.color.neutral700, R.color.neutral200)
        }

        // 색상 적용
        view.setUBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
        view.strokeColor = ContextCompat.getColor(view.context, borderColorRes)
        view.strokeWidth = 1.px
    }
}