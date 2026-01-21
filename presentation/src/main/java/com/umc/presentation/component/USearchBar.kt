package com.umc.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomSearchBarBinding
import com.umc.presentation.extension.px

class USearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : MaterialCardView(context, attrs, defStyle) {

    companion object {
        @JvmStatic
        @BindingAdapter("onTextChanged")
        fun bindOnTextChanged(view: USearchBar, listener: OnSearchTextChanged?) {
            view.setOnTextChangedListener(
                if (listener == null) null else { text -> listener.onChanged(text) }
            )
        }
    }

    fun interface OnSearchTextChanged {
        fun onChanged(text: String)
    }

    private val binding = CustomSearchBarBinding.inflate(LayoutInflater.from(context), this)
    private var onTextChangedListener: ((String) -> Unit)? = null
    private var isFocus: Boolean = false
    private var placeholderText: String? = null

    init {
        elevation = 0f
        radius = 8.px.toFloat()

        val a = context.obtainStyledAttributes(attrs, R.styleable.USearchBar, defStyle, 0)
        try {
            placeholderText = a.getString(R.styleable.USearchBar_placeholderText) ?: ""

            binding.apply {
                imageClear.setOnClickListener { editText.setText("") }

                editText.apply {
                    hint = placeholderText

                    doOnTextChanged { text, _, _, _ ->
                        imageClear.visibility = if (text.isNullOrEmpty()) GONE else VISIBLE
                        onTextChangedListener?.invoke(text.toString())
                        updateStyle()
                    }

                    setOnFocusChangeListener { _, hasFocus ->
                        isFocus = hasFocus
                        hint = if (hasFocus) "" else placeholderText
                        updateStyle()
                    }
                }
            }
        } finally { a.recycle() }
        updateStyle()
    }

    private fun updateStyle() {
        val hasText = binding.editText.text?.isNotEmpty() == true

        // 돋보기 아이콘 제어
        binding.imagePrevIcon.visibility = if (!isFocus && !hasText) VISIBLE else GONE

        // 배경색 제어
        val bgColor = if (!isFocus && !hasText) {
            ContextCompat.getColor(context, R.color.neutral100)
        } else {
            ContextCompat.getColor(context, R.color.neutral000)
        }
        setCardBackgroundColor(bgColor)

        // 테두리 제어
        when {
            isFocus -> {
                strokeColor = ContextCompat.getColor(context, R.color.primary500)
                strokeWidth = 1.px
            }
            hasText -> {
                strokeColor = ContextCompat.getColor(context, R.color.neutral200)
                strokeWidth = 1.px
            }
            else -> {
                strokeWidth = 0
            }
        }
    }

    fun setOnTextChangedListener(listener: ((String) -> Unit)?) { onTextChangedListener = listener }
}