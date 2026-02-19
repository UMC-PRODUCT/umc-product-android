package com.umc.presentation.ui.act.study


import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.StudyStatus
import com.umc.domain.model.enums.SubmitState
import com.umc.presentation.R
import com.umc.presentation.component.UButton


@BindingAdapter("studyStatusStyle")
fun UButton.bindStudyStatusStyle(status: StudyStatus?) {
    if (status == null) return
    android.util.Log.d("BA", "studyStatusStyle called = $status")

    setText(status.text)

    val (bgRes, textRes) = when (status) {
        StudyStatus.PASS -> R.color.success100 to R.color.success700
        StudyStatus.FAIL -> R.color.danger100 to R.color.danger700
        StudyStatus.IN_PROGRESS -> R.color.primary100 to R.color.primary600
    }

    setTextColor(ContextCompat.getColor(context, textRes))
    setUBackgroundColor(ContextCompat.getColor(context, bgRes))
}


@BindingAdapter("confirmButtonStyle")
fun UButton.bindConfirmButtonStyle(isConfirming: Boolean) {
    if (!isConfirming) return

    setUBackgroundColor(ContextCompat.getColor(context, R.color.primary500))
    setTextColor(ContextCompat.getColor(context, R.color.neutral000))

}


@BindingAdapter("visibleWhenInput")
fun View.visibleWhenInput(item: ActStudyItemUiModel?) {
    visibility =
        if (item != null && !item.isLocked && item.submitState == SubmitState.READY)
            View.VISIBLE
        else
            View.GONE
}

@BindingAdapter("visibleWhenConfirming")
fun View.visibleWhenConfirming(item: ActStudyItemUiModel?) {
    visibility =
        if (item != null && !item.isLocked && item.submitState == SubmitState.CONFIRMING)
            View.VISIBLE
        else
            View.GONE
}

@BindingAdapter("visibleWhenWaiting")
fun View.visibleWhenWaiting(item: ActStudyItemUiModel?) {
    visibility =
        if (item != null && !item.isLocked && item.submitState == SubmitState.REQUESTED)
            View.VISIBLE
        else
            View.GONE
}

@BindingAdapter("visibleWhenResultPass")
fun View.visibleWhenResultPass(item: ActStudyItemUiModel?) {
    visibility =
        if (item != null && !item.isLocked && item.status == StudyStatus.PASS)
            View.VISIBLE
        else
            View.GONE
}

@BindingAdapter("visibleWhenResultFail")
fun View.visibleWhenResultFail(item: ActStudyItemUiModel?) {
    visibility =
        if (item != null && !item.isLocked && item.status == StudyStatus.FAIL)
            View.VISIBLE
        else
            View.GONE
}

@BindingAdapter("enabledWhenConfirmReady")
fun View.enabledWhenConfirmReady(item: ActStudyItemUiModel?) {
    val enabled = item != null &&
            !item.isLocked &&
            item.submitState == SubmitState.CONFIRMING

    isEnabled = enabled
    alpha = if (enabled) 1f else 0.4f
}



@BindingAdapter(
    value = ["timelineStatus", "timelineWeek", "timelineLocked"],
    requireAll = true
)
fun ConstraintLayout.bindTimeline(
    status: StudyStatus?,
    week: Int?,
    locked: Boolean?
) {
    if (status == null || week == null || locked == null) return

    val icon = findViewById<ImageView>(R.id.iv_timeline_status)
    val number = findViewById<TextView>(R.id.tv_timeline_number)

    if (locked) {
        icon.setImageResource(R.drawable.ic_locked)
        icon.visibility = View.VISIBLE
        number.visibility = View.GONE
        return
    }


    number.visibility = View.VISIBLE
    number.text = week.toString()


    icon.visibility = View.VISIBLE
    icon.setImageResource(R.drawable.bg_primary500_circle)

    when (status) {
        StudyStatus.PASS -> {
            icon.setImageResource(R.drawable.ic_check_success)
            number.visibility = View.GONE
        }

        StudyStatus.FAIL -> {
            icon.setImageResource(R.drawable.ic_check_failed)
            number.visibility = View.GONE
        }

        StudyStatus.IN_PROGRESS -> {

        }
    }
}
