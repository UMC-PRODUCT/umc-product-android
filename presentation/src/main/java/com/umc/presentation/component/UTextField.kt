package com.umc.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomTextFieldBinding

class UTextField @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MaterialCardView(mContext, attrs, defStyle) {

    private val binding = CustomTextFieldBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UTextField, defStyle, 0)

        try {
            binding.apply {
                // 선행 아이콘
                val icon = a.getResourceId(R.styleable.UTextField_prevIcon, 0)
                if (icon != 0) {
                    imagePrevIcon.visibility = View.VISIBLE
                    imagePrevIcon.setImageResource(icon)

                    if (a.hasValue(R.styleable.UTextField_prevIconTint)) {
                        val tint = a.getColorStateList(R.styleable.UTextField_prevIconTint)
                        imagePrevIcon.imageTintList = tint
                    } else {
                        imagePrevIcon.imageTintList = null
                    }

                    val iconSize = a.getDimensionPixelSize(R.styleable.UTextField_prevIconSize, 0)
                    imagePrevIcon.layoutParams = imagePrevIcon.layoutParams.apply {
                        width = iconSize
                        height = iconSize
                    }

                    val iconPadding =
                        a.getDimensionPixelSize(R.styleable.UTextField_prevIconPadding, 0)
                    imagePrevIcon.setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
                } else {
                    imagePrevIcon.visibility = View.GONE
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
                cardView.setCardBackgroundColor(
                    a.getColor(
                        R.styleable.UTextField_backgroundColor,
                        resources.getColor(R.color.neutral500)
                    ) //TODO 확정 아님
                )

                // 코너
                cardView.radius = a.getDimension(R.styleable.UTextField_cornerRadius, 14.toFloat())

                // Border
                cardView.strokeWidth = a.getDimensionPixelSize(R.styleable.UTextField_borderWidth, 0)
                cardView.strokeColor = a.getColor(R.styleable.UTextField_borderColor, Color.TRANSPARENT)
            }
        } finally {
            a.recycle()
        }
    }
}