package com.umc.presentation.component

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomChipBinding
import com.umc.presentation.extension.px

class UChip @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0) : MaterialCardView(mContext, attrs, defStyle)

{
    private val binding = CustomChipBinding.inflate(LayoutInflater.from(context), this)
    // 리스너 설정
    private var onCloseClickListener: ((String) -> Unit)? = null
    
    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.UChip, defStyle, 0)
        try {
            binding.apply {
                // 클릭 여부
                isClickable = true
                isFocusable = true

                // 모양 고정 (둥글게)
                radius = 100.px.toFloat()
                preventCornerOverlap = true

                // 그림자 제거
                cardElevation = 0f
                maxCardElevation = 0f

                // 텍스트 설정
                val textColor = a.getColor(R.styleable.UChip_textColor,
                    ContextCompat.getColor(context, R.color.neutral000))

                tvChipText.text = a.getString(R.styleable.UChip_text) ?: ""
                tvChipText.setTextColor(textColor)
                tvChipText.setTextAppearance(a.getResourceId(R.styleable.UChip_textAppearance,
                    R.style.SubheadlineBold))

                // 외곽선 설정
                strokeWidth = a.getDimensionPixelSize(R.styleable.UChip_borderWidth,
                    0.px)
                strokeColor = a.getColor(R.styleable.UChip_borderColor,
                    Color.TRANSPARENT)

                // 배경색 설정
                val backgroundColor = a.getColor(R.styleable.UChip_backgroundColor,
                    ContextCompat.getColor(context, R.color.primary500))
                val pressedColor = a.getColor(R.styleable.UChip_pressedColor,
                     backgroundColor)
                rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)

                val bg =
                    ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_pressed),
                            intArrayOf(),
                        ),
                        intArrayOf(
                            pressedColor,
                            backgroundColor,
                        ),
                    )
                setCardBackgroundColor(bg)

                // X 버튼 디자인
                imvClose.imageTintList = ColorStateList.valueOf(textColor)

                // X 버튼 로직
                val showClose = a.getBoolean(R.styleable.UChip_showCloseIcon, false)
                imvClose.visibility = if (showClose) View.VISIBLE else View.GONE

                imvClose.setOnClickListener {
                    // 아이템이 사라지는 기본 로직 수행 후 콜백 호출
                    this@UChip.visibility = View.GONE
                    onCloseClickListener?.invoke(binding.tvChipText.text.toString())
                }


            }
        } finally {
            a.recycle()
        }
    }

    // 텍스트 Setter
    fun setText(text: String?) {
        binding.tvChipText.text = text ?: ""
    }

    // X 버튼 클릭 리스너 등록 (데이터 삭제나 기타 로직 수행)
    fun setOnCloseClickListener(listener: (String) -> Unit) {
        this.onCloseClickListener = listener
    }

    // 텍스트 색상 변경
    fun setUChipTextColor(color: Int) {
        binding.tvChipText.setTextColor(color)
        // 텍스트 색상과 X 버튼 색상을 같이 변경
        binding.imvClose.imageTintList = ColorStateList.valueOf(color)
    }

    // 배경색 변경 (단일 색상)
    fun setUChipBackgroundColor(color: Int) {
        setCardBackgroundColor(ColorStateList.valueOf(color))
    }

    /** 배경색 변경 (눌림 효과 유지 버전)
     * @param backgroundColor 기본 배경색
     * @param pressedColor 눌렸을 때 배경색
     */
    fun setUChipBackgroundStateList(backgroundColor: Int, pressedColor: Int = backgroundColor) {
        val bg = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(),
            ),
            intArrayOf(
                pressedColor,
                backgroundColor,
            ),
        )
        setCardBackgroundColor(bg)
    }

    /** 외곽선 설정 변경
     * @param color 외곽선 색상
     * @param width 외곽선 두께 (기본값은 현재 두께 유지)
     */
    fun setUChipBorder(color: Int, width: Int = strokeWidth) {
        strokeColor = color
        strokeWidth = width
    }

}