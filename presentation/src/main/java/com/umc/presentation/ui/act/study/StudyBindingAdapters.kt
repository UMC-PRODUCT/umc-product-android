package com.umc.presentation.ui.act.study


import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.presentation.R
import com.umc.presentation.component.UButton
import android.view.View


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
