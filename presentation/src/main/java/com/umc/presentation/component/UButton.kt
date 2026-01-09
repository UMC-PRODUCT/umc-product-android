package com.umc.presentation.component

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomButtonBinding
import com.umc.presentation.extension.px

class UButton
@JvmOverloads
constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : MaterialCardView(mContext, attrs, defStyle) {

    private val binding = CustomButtonBinding.inflate(LayoutInflater.from(context), this)
    private var normalColor: Int = ContextCompat.getColor(context, R.color.black)
    private var pressedColor: Int = normalColor

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UButton, defStyle, 0)

        try {
            binding.apply {

                elevation = a.getInt(R.styleable.UButton_buttonElevation, 0).toFloat()

                // 텍스트
                textView.text = a.getString(R.styleable.UButton_text) ?: textView.text
                textView.setTextColor(
                    a.getColor(
                        R.styleable.UButton_textColor,
                        ContextCompat.getColor(context, R.color.white)
                    ),
                )
                textView.setTextAppearance(
                    a.getResourceId(R.styleable.UButton_textAppearance, R.style.SubheadlineBold),
                )

                // 코너
                radius = a.getDimension(R.styleable.UButton_cornerRadius, 8.px.toFloat())

                // Border
                strokeWidth = a.getDimensionPixelSize(R.styleable.UButton_borderWidth, 0.px)
                strokeColor = a.getColor(R.styleable.UButton_borderColor, Color.TRANSPARENT)

                // 배경색
                pressedColor = a.getColor(R.styleable.UButton_pressedColor, normalColor)
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
            }
        } finally {
            a.recycle()
        }

        //setter
        fun setText(text: String?) {
            binding.textView.text = text ?: ""
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
    }

    override fun setBackgroundColor(color: Int) {
        normalColor = color
        updateBackgroundColors()
    }

    private fun updateBackgroundColors() {
        if (!isEnabled) {
            setCardBackgroundColor(normalColor)
        } else {
            val bg = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_pressed),
                    intArrayOf(),
                ),
                intArrayOf(
                    pressedColor,
                    normalColor,
                ),
            )
            setCardBackgroundColor(bg)
        }
    }

}
