package com.umc.presentation.ui.act.study


import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.presentation.R
import com.umc.presentation.component.UButton
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.ImageView
import android.widget.TextView



@BindingAdapter("studyStatusStyle")
fun UButton.bindStudyStatusStyle(status: StudyStatus?) {
    if (status == null) return


    setText(status.text)


    val (bgRes, textRes) = when (status) {
        StudyStatus.PASS -> R.color.success100 to R.color.success700
        StudyStatus.FAIL -> R.color.danger100 to R.color.danger700
        StudyStatus.IN_PROGRESS -> R.color.primary100 to R.color.primary600
    }

    setTextColor(ContextCompat.getColor(context, textRes))
    setUBackgroundColor(ContextCompat.getColor(context, bgRes))
}

@BindingAdapter("visibleWhenInput")
fun View.visibleWhenInput(item: ActStudyItemUiModel?) {
    if (item == null) return
    visibility =
        if (
            item.status == StudyStatus.IN_PROGRESS &&
            (item.submitState == SubmitState.IDLE || item.submitState == SubmitState.READY)
        ) View.VISIBLE else View.GONE
}


@BindingAdapter("visibleWhenConfirming")
fun View.visibleWhenConfirming(item: ActStudyItemUiModel?) {
    if (item == null) return
    visibility = if (item.submitState == SubmitState.CONFIRMING) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleWhenWaiting")
fun View.visibleWhenWaiting(item: ActStudyItemUiModel?) {
    if (item == null) return
    visibility =
        if (item.status == StudyStatus.IN_PROGRESS && item.submitState == SubmitState.REQUESTED)
            View.VISIBLE else View.GONE
}

@BindingAdapter("visibleWhenResultPass")
fun View.visibleWhenResultPass(item: ActStudyItemUiModel?) {
    if (item == null) return
    visibility = if (item.status == StudyStatus.PASS) View.VISIBLE else View.GONE
}

@BindingAdapter("visibleWhenResultFail")
fun View.visibleWhenResultFail(item: ActStudyItemUiModel?) {
    if (item == null) return
    visibility = if (item.status == StudyStatus.FAIL) View.VISIBLE else View.GONE
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
        number.visibility = View.GONE
        return
    }


    number.visibility = View.VISIBLE
    number.text = week.toString()
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
