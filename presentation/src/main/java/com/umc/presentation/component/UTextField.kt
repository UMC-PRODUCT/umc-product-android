package com.umc.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomTextFieldBinding
import com.umc.presentation.extension.px

class UTextField
@JvmOverloads
constructor(
    mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : MaterialCardView(mContext, attrs, defStyle) {

    companion object {
        @JvmStatic
        @BindingAdapter("text")
        fun setText(view: UTextField, value: String?) {
            val newValue = value ?: ""
            if (view.getText() != newValue) {
                view.setText(newValue)
            }
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "text", event = "textAttrChanged")
        fun getText(view: UTextField): String {
            return view.getText()
        }

        @JvmStatic
        @BindingAdapter("textAttrChanged")
        fun setTextAttrChanged(view: UTextField, listener: InverseBindingListener?) {
            if (listener == null) {
                view.setOnTextChangedListener(null)
            } else {
                view.setOnTextChangedListener {
                    listener.onChange()
                }
            }
        }
    }

    private val binding = CustomTextFieldBinding.inflate(LayoutInflater.from(context), this)
    private var suppressCallback = false
    private var onTextChangedListener: ((String) -> Unit)? = null

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
                    imagePrevIcon.layoutParams =
                        imagePrevIcon.layoutParams.apply {
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
                    a.getColor(R.styleable.UTextField_textColor, editText.currentTextColor),
                )
                editText.setTextAppearance(
                    a.getResourceId(R.styleable.UTextField_textAppearance, R.style.Callout),
                )
                editText.hint = a.getText(R.styleable.UTextField_placeholderText)
                editText.setHintTextColor(
                    a.getColor(
                        R.styleable.UTextField_placeholderTextColor,
                        ContextCompat.getColor(context, R.color.neutral400)
                    )
                )
                editText.doOnTextChanged { text, _, _, _ ->
                    if (!suppressCallback) {
                        onTextChangedListener?.invoke(text?.toString().orEmpty())
                    }
                }

                // 배경색
                setCardBackgroundColor(
                    a.getColor(
                        R.styleable.UTextField_backgroundColor,
                        ContextCompat.getColor(context, R.color.neutral000),
                    ),
                )

                // 코너
                radius = a.getDimension(R.styleable.UTextField_cornerRadius, 8.px.toFloat())

                // Border
                strokeWidth = a.getDimensionPixelSize(R.styleable.UTextField_borderWidth, 1.px)
                strokeColor = a.getColor(
                    R.styleable.UTextField_borderColor,
                    ContextCompat.getColor(context, R.color.neutral300)
                )

                elevation = 0.toFloat()
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
}
