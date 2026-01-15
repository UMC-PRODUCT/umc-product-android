package com.umc.presentation.ui.act.check

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("isSelected")
    fun setIsSelected(view: View, isExpanded: Boolean) {
        if (view !is TextView) return

        view.isSelected = isExpanded

        if (isExpanded) {
            view.post {
                val paint = view.paint
                val textWidth = paint.measureText(view.text.toString())
                val viewWidth = view.width.toFloat()

                if (textWidth > viewWidth) {
                    view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    view.scrollTo(0, 0)
                } else {
                    view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }
        } else {
            view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }
    }
}