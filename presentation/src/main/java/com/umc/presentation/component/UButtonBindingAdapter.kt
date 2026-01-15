package com.umc.presentation.component

import android.content.res.ColorStateList
import androidx.databinding.BindingAdapter

object UButtonBindingAdapter {
    @JvmStatic
    @BindingAdapter("uBackgroundColor")
    fun setUBackgroundColor(view: UButton, color: Int) {
        view.setCardBackgroundColor(ColorStateList.valueOf(color))
    }
}