package com.umc.presentation.ui.act.study.submit.adapter

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R


@BindingAdapter("markStatusIcon")
fun ImageView.bindMarkStatusIcon(status: String?) {
    val res = when (status) {
        "PASS" -> R.drawable.ic_check_success
        "FAIL" -> R.drawable.ic_check_failed
        else -> null
    }

    if (res == null) {
        setImageDrawable(null)
        return
    }

    setImageResource(res)


    if (status == "FAIL") {
        imageTintList = null
    } else {
        val colorInt = ContextCompat.getColor(context, R.color.primary500)
        imageTintList = android.content.res.ColorStateList.valueOf(colorInt)
    }
}

@BindingAdapter("markStatusStyle")
fun MaterialCardView.bindMarkStatusStyle(status: String?) {
    val bg = when (status) {
        "PASS" -> R.color.primary100

        "FAIL" -> R.color.neutral100
        else -> R.color.neutral000
    }
    setCardBackgroundColor(ContextCompat.getColor(context, bg))
}

@BindingAdapter("slotIcon")
fun ImageView.bindSlotIcon(status: String?) {

//    val (resId, tintColor) = when (status) {
//        "SUBMITTED" -> R.drawable.ic_swipe to R.color.neutral300
//        "PASS" -> R.drawable.ic_check_success to null
//        "BEST" -> R.drawable.ic_check_success to null
//        "FAIL" -> R.drawable.ic_check_failed to null
//        else -> null to null
//    }

    val uiStatus = if (status == "BEST") "PASS" else status

    val (resId, tintColor) = when (uiStatus) {
        "SUBMITTED" -> R.drawable.ic_swipe to R.color.neutral300
        "PASS" -> R.drawable.ic_check_success to null
        "FAIL" -> R.drawable.ic_check_failed to null
        else -> null to null
    }

    if (resId == null) {
        setImageDrawable(null)
        return
    }

    setImageResource(resId)

    imageTintList = tintColor?.let {
        android.content.res.ColorStateList.valueOf(
            ContextCompat.getColor(context, it)
        )
    }
}