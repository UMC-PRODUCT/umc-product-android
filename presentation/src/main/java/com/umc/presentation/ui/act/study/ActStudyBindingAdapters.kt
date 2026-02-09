package com.umc.presentation.ui.act.study

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.StudyStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

@BindingAdapter("submitButtonStyle")
fun UButton.bindSubmitButtonStyle(status: StudyStatus?) {
    if (status == null) return


    val (bgRes, textRes) = when (status) {
        StudyStatus.PASS -> R.color.neutral100 to R.color.neutral500
        StudyStatus.FAIL -> R.color.warning500 to R.color.neutral000
        StudyStatus.IN_PROGRESS -> R.color.primary500 to R.color.neutral000
    }


    setUBackgroundColor(ContextCompat.getColor(context, bgRes))
    setTextColor(ContextCompat.getColor(context, textRes))


    isEnabled = status != StudyStatus.PASS
    alpha = if (status == StudyStatus.PASS) 0.6f else 1f
}

@BindingAdapter("confirmButtonStyle")
fun UButton.bindConfirmButtonStyle(isConfirming: Boolean) {
    if (!isConfirming) return

    setUBackgroundColor(
        ContextCompat.getColor(context, R.color.primary500)
    )
    setTextColor(
        ContextCompat.getColor(context, R.color.neutral000)
    )
    isEnabled = true
    alpha = 1f
}

@BindingAdapter("timelineStatus", "timelineWeek", requireAll = false)
fun bindTimelineStatus(
    view: View,
    status: StudyStatus?,
    week: Int?
) {
    val root = view as? androidx.constraintlayout.widget.ConstraintLayout ?: return

    val iv = root.findViewById<ImageView>(R.id.iv_timeline_status)
    val tv = root.findViewById<TextView>(R.id.tv_timeline_number)

    if (status == null) return

    when (status) {
        StudyStatus.PASS -> {
            iv.visibility = View.VISIBLE
            tv.visibility = View.GONE
            iv.setImageResource(R.drawable.ic_check_success)
        }

        StudyStatus.FAIL -> {
            iv.visibility = View.VISIBLE
            tv.visibility = View.GONE
            iv.setImageResource(R.drawable.ic_check_failed)
        }

        StudyStatus.IN_PROGRESS -> {
            iv.visibility = View.GONE
            tv.visibility = View.VISIBLE
            tv.text = (week ?: 0).toString()
            tv.background = ContextCompat.getDrawable(root.context, R.drawable.bg_primary500_circle)
        }
    }
}

