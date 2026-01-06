package com.umc.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R

class UTextField @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(mContext, attrs, defStyle) {

    private val card: MaterialCardView
    private val prevIcon: ImageView
    private val editText: AppCompatEditText

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_text_field, this, true)
        card = findViewById(R.id.card_view)
        prevIcon = findViewById(R.id.image_prev_icon)
        editText = findViewById(R.id.edit_text)

        val a = context.obtainStyledAttributes(attrs, R.styleable.UTextField, defStyle, 0)

        try {

            // 선행 아이콘
            val icon = a.getResourceId(R.styleable.UTextField_prevIcon, 0)
            if (icon != 0) {
                prevIcon.visibility = View.VISIBLE
                prevIcon.setImageResource(icon)

                if (a.hasValue(R.styleable.UTextField_prevIconTint)) {
                    val tint = a.getColorStateList(R.styleable.UTextField_prevIconTint)
                    prevIcon.imageTintList = tint
                } else {
                    prevIcon.imageTintList = null
                }

                val iconSize = a.getDimensionPixelSize(R.styleable.UTextField_prevIconSize, 0)
                prevIcon.layoutParams = prevIcon.layoutParams.apply {
                    width = iconSize
                    height = iconSize
                }

                val iconPadding = a.getDimensionPixelSize(R.styleable.UTextField_prevIconPadding, 0)
                prevIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            } else {
                prevIcon.visibility = View.GONE
            }

            // 텍스트
            editText.setText(a.getString(R.styleable.UTextField_text) ?: editText.text)
            editText.setTextColor(
                a.getColor(R.styleable.UTextField_textColor, editText.currentTextColor)
            )
            editText.setTextAppearance(
                a.getResourceId(R.styleable.UTextField_textAppearance, R.style.Callout)
            )

            // 배경색
            card.setCardBackgroundColor(
                a.getColor(R.styleable.UTextField_backgroundColor, resources.getColor(R.color.neutral500)) //TODO 확정 아님
            )

            // 코너
            card.radius = a.getDimension(R.styleable.UTextField_cornerRadius, 14.toFloat())

            // Border
            card.strokeWidth = a.getDimensionPixelSize(R.styleable.UTextField_borderWidth, 0)
            card.strokeColor = a.getColor(R.styleable.UTextField_borderColor, Color.TRANSPARENT)

        } finally {
            a.recycle()
        }
    }
}