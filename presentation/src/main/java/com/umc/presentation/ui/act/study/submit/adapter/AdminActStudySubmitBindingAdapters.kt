package com.umc.presentation.ui.act.study.submit.adapter

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.component.UButton


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
fun UButton.bindMarkStatusStyle(status: String?) {
    val bg = when (status) {
        "PASS", "BEST" -> R.color.success100
        "FAIL" -> R.color.danger100
        else -> R.color.neutral000
    }
    setUBackgroundColor(ContextCompat.getColor(context, bg))
}

@BindingAdapter("markStatusText")
fun UButton.bindMarkStatusText(status: String?) {
    val text = when (status) {
        "FAIL" -> "Fail"
        "PASS", "BEST" -> "Pass"
        else -> ""
    }
    setText(text)
}