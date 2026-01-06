package com.umc.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomButtonBinding

class UButton @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(mContext, attrs, defStyle) {

    private val binding = CustomButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UButton, defStyle, 0)

        try {
            binding.apply {
                // 텍스트
                textView.text = a.getString(R.styleable.UButton_text) ?: textView.text
                textView.setTextColor(
                    a.getColor(R.styleable.UButton_textColor, resources.getColor(R.color.white))
                )
                textView.setTextAppearance(
                    a.getResourceId(R.styleable.UButton_textAppearance, R.style.Callout) // TODO 확정 아님
                )

                // 배경색
                cardView.setCardBackgroundColor(
                    a.getColor(R.styleable.UButton_backgroundColor, resources.getColor(R.color.black))
                )

                // 코너
                cardView.radius = a.getDimension(R.styleable.UButton_cornerRadius, 14.toFloat())

                // Border
                cardView.strokeWidth = a.getDimensionPixelSize(R.styleable.UButton_borderWidth, 0)
                cardView.strokeColor = a.getColor(R.styleable.UButton_borderColor, Color.TRANSPARENT)

            }
        } finally {
            a.recycle()
        }
    }
}