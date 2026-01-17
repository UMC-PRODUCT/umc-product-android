package com.umc.presentation.ui.act.check

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.presentation.component.UButton

object UCheckBindingAdapter {
    @JvmStatic
    @BindingAdapter("isSelected", "uTextColorRes", requireAll = false)
    fun setIsSelected(view: View, isExpanded: Boolean, textColorRes: Int?) {
        textColorRes?.let { resId ->
            val color = ContextCompat.getColor(view.context, resId)
            when (view) {
                is UButton -> view.setTextColor(color)
                is TextView -> view.setTextColor(color)
            }
        }

        if (view !is TextView && view !is UButton) return

        view.isSelected = isExpanded

        if (isExpanded) {
            view.post {
                val paint = if (view is TextView) view.paint else (view as UButton).let { /* UButton 내부 TextView 접근 로직 필요시 추가 */ null }
                val text = if (view is TextView) view.text.toString() else ""

                if (view is TextView) {
                    val textWidth = view.paint.measureText(view.text.toString())
                    val viewWidth = view.width.toFloat()

                    if (textWidth > viewWidth) {
                        view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        view.scrollTo(0, 0)
                    } else {
                        view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }
                }
            }
        } else {
            if (view is TextView) {
                view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
        }
    }
}