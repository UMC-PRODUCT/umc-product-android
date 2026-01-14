package com.umc.presentation.component

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomTextFieldBinding
import com.umc.presentation.extension.px

class UTextField @JvmOverloads constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : MaterialCardView(mContext, attrs, defStyle) {

    companion object {
        @JvmStatic
        @BindingAdapter("text")
        fun bindText(view: UTextField, value: String?) {
            view.setText(value)
        }

        @JvmStatic
        @BindingAdapter("onTextChanged")
        fun bindOnTextChanged(view: UTextField, listener: OnUTextChanged?) {
            view.setOnTextChangedListener(
                if (listener == null) null else { text -> listener.onChanged(text) }
            )
        }
    }

    fun interface OnUTextChanged {
        fun onChanged(text: String)
    }

    private val binding = CustomTextFieldBinding.inflate(LayoutInflater.from(context), this)
    private var suppressCallback = false
    private var onTextChangedListener: ((String) -> Unit)? = null

    private var focusStrokeColor: Int = ContextCompat.getColor(context, R.color.primary500)
    private var defaultStrokeColor: Int = ContextCompat.getColor(context, R.color.neutral300)
    private var isFocus: Boolean = false

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
                        imagePrevIcon.imageTintList =
                            a.getColorStateList(R.styleable.UTextField_prevIconTint)
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

                // 텍스트(초기 attrs)
                editText.setText(a.getString(R.styleable.UTextField_text) ?: editText.text)
                editText.setTextColor(
                    a.getColor(R.styleable.UTextField_textColor, editText.currentTextColor),
                )
                editText.setTextAppearance(
                    a.getResourceId(R.styleable.UTextField_textAppearance, R.style.Callout),
                )
                editText.hint = a.getText(R.styleable.UTextField_placeholderText)
                editText.setHintTextColor(
                    a.getColor(
                        R.styleable.UTextField_placeholderTextColor,
                        ContextCompat.getColor(context, R.color.neutral400),
                    ),
                )

                editText.doOnTextChanged { text, _, _, _ ->
                    if (!suppressCallback) {
                        onTextChangedListener?.invoke(text?.toString().orEmpty())
                    }
                }

                editText.setOnFocusChangeListener { _, hasFocus ->
                    isFocus = hasFocus
                    updateStrokeColor()
                }

                // 코너
                radius = a.getDimension(R.styleable.UTextField_cornerRadius, 8.px.toFloat())

                focusStrokeColor = a.getColor(
                    R.styleable.UTextField_focusStrokeColor,
                    ContextCompat.getColor(context, R.color.primary500)
                )
                elevation = 0f
            }
        } finally {
            a.recycle()
        }
    }

    fun getText(): String = binding.editText.text?.toString().orEmpty()

    fun setText(value: String?) {
        val newValue = value.orEmpty()
        if (getText() == newValue) return

        suppressCallback = true
        binding.editText.setText(newValue)
        binding.editText.setSelection(newValue.length)
        suppressCallback = false
    }

    fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        onTextChangedListener = listener
    }

    override fun setBackgroundColor(color: Int) {
        setCardBackgroundColor(color)
    }

    private fun updateStrokeColor() {
        strokeColor = if (isFocus) {
            focusStrokeColor
        } else {
            defaultStrokeColor
        }
    }

    override fun setStrokeColor(strokeColor: ColorStateList?) {
        super.setStrokeColor(if(isFocus) ColorStateList.valueOf(focusStrokeColor) else strokeColor)
    }
}
