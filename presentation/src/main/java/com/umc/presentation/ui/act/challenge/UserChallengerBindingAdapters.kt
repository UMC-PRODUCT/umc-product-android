package com.umc.presentation.ui.act.challenge

import android.view.View
import android.widget.ImageView
import coil.load
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.CheckHistoryStatus
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
        view.setText(role.displayName)

        // 역할별 색상 리소스 정의
        val (bgColorRes, textColorRes, borderColorRes) = when (role) {
            UserChallengerRole.SCHOOL_PRESIDENT -> Triple(R.color.warning100, R.color.warning700, R.color.warning300)
            UserChallengerRole.SCHOOL_VICE_PRESIDENT -> Triple(R.color.accent100, R.color.accent700, R.color.accent300)
            UserChallengerRole.SCHOOL_PART_LEADER -> Triple(R.color.success100, R.color.success700, R.color.success300)
            else -> Triple(R.color.neutral100, R.color.neutral700, R.color.neutral200)
        }

        // 배경색, 텍스트색, 테두리색 적용
        view.setUBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
        view.strokeColor = ContextCompat.getColor(view.context, borderColorRes)
    }

    @JvmStatic
    @BindingAdapter("attendanceStatus")
    fun setAttendanceStatus(view: UButton, status: CheckHistoryStatus?) {
        if (status == null) return

        // Enum에 정의된 text 설정
        view.setText(status.text)

        // 상태별 색상 매핑
        val (textColorRes, bgColorRes) = when (status) {
            CheckHistoryStatus.PRESENT -> R.color.success500 to R.color.success100
            CheckHistoryStatus.LATE -> R.color.warning500 to R.color.warning100
            CheckHistoryStatus.ABSENT -> R.color.danger500 to R.color.danger100
            CheckHistoryStatus.EXCUSED -> R.color.success500 to R.color.success100
            CheckHistoryStatus.PRESENT_PENDING -> R.color.neutral600 to R.color.neutral100
        }

        val textColor = ContextCompat.getColor(view.context, textColorRes)
        val bgColor = ContextCompat.getColor(view.context, bgColorRes)

        view.setTextColor(textColor)
        view.setUBackgroundColor(bgColor)
    }

    @JvmStatic
    @BindingAdapter("profileImageUrl")
    fun setProfileImageUrl(view: ImageView, url: String?) {
        view.load(url) {
            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
        }
    }
}