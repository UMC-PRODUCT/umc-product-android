package com.umc.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R

class UButton @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(mContext, attrs, defStyle) {

    private val card: MaterialCardView
    private val buttonText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_button, this, true)
        card = findViewById(R.id.card_view)
        buttonText = findViewById(R.id.text_view)

        val a = context.obtainStyledAttributes(attrs, R.styleable.UButton, defStyle, 0)

        try {
            // 텍스트
            buttonText.text = a.getString(R.styleable.UButton_text) ?: buttonText.text
            buttonText.setTextColor(
                a.getColor(R.styleable.UButton_textColor, resources.getColor(R.color.white))
            )
            buttonText.setTextAppearance(
                a.getResourceId(R.styleable.UButton_textAppearance, R.style.Callout) // TODO 확정 아님
            )

            // 배경색
            card.setCardBackgroundColor(
                a.getColor(R.styleable.UButton_backgroundColor, resources.getColor(R.color.black))
            )

            // 코너
            card.radius = a.getDimension(R.styleable.UButton_cornerRadius, 14.toFloat())

            // Border
            card.strokeWidth = a.getDimensionPixelSize(R.styleable.UButton_borderWidth, 0)
            card.strokeColor = a.getColor(R.styleable.UButton_borderColor, Color.TRANSPARENT)

        } finally {
            a.recycle()
        }
    }
}