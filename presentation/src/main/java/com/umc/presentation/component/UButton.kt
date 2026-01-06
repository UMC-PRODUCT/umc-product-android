package com.umc.presentation.component

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomButtonBinding
import com.umc.presentation.util.ULog

class UButton @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(mContext, attrs, defStyle) {

    private val binding = CustomButtonBinding.inflate(LayoutInflater.from(context), this)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UButton, defStyle, 0)

        try {
            isClickable = true
            isFocusable = true
            binding.apply {

                elevation = a.getInt(R.styleable.UButton_buttonElevation, 0).toFloat()

                // 텍스트
                textView.text = a.getString(R.styleable.UButton_text) ?: textView.text
                textView.setTextColor(
                    a.getColor(R.styleable.UButton_textColor, ContextCompat.getColor(context,R.color.white))
                )
                textView.setTextAppearance(
                    a.getResourceId(R.styleable.UButton_textAppearance, R.style.Callout) // TODO 확정 아님
                )

                // 코너
                radius = a.getDimension(R.styleable.UButton_cornerRadius, 14.toFloat())

                // Border
                strokeWidth = a.getDimensionPixelSize(R.styleable.UButton_borderWidth, 0)
                strokeColor = a.getColor(R.styleable.UButton_borderColor, Color.TRANSPARENT)

                // 배경색
                val backgroundColor = a.getColor(R.styleable.UButton_backgroundColor, ContextCompat.getColor(context, R.color.black))
                val pressedColor = a.getColor(R.styleable.UButton_pressedColor, backgroundColor)
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)

                val bg = ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_pressed),
                        intArrayOf()
                    ),
                    intArrayOf(
                        pressedColor,
                        a.getColor(R.styleable.UButton_backgroundColor, ContextCompat.getColor(context, R.color.black))
                    )
                )
                setCardBackgroundColor(bg)
            }
        } finally {
            a.recycle()
        }
    }
}