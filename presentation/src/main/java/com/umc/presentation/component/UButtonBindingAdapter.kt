package com.umc.presentation.component

import androidx.databinding.BindingAdapter

object UButtonBindingAdapter {
    @JvmStatic
    @BindingAdapter("uBackgroundColor")
    fun setUBackgroundColor(view: UButton, color: Int) {
        view.setUBackgroundColor(color)
    }

    @JvmStatic
    @BindingAdapter("uTextColor")
    fun setUTextColor(view: UButton, color: Int) {
        view.setTextColor(color)
    }
}