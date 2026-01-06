package com.umc.presentation.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.umc.presentation.R
import com.umc.presentation.databinding.CustomToastBinding
import com.umc.presentation.extension.px
import com.umc.presentation.util.UToast.State.*

object UToast {

    enum class State {
        CHECK, ERROR, NONE
    }

    private const val MARGIN_BOTTOM = 56

    // 필요하다면 message를 String으로 받는 함수 추가
    fun createToast(context: Context, message: Int, length: Int = Toast.LENGTH_SHORT, state: State): Toast {
        val inflater = LayoutInflater.from(context)
        val binding: CustomToastBinding =
            DataBindingUtil.inflate(inflater, R.layout.custom_toast, null, false)

        binding.apply {
            when (state) {
                CHECK -> {
                    imageState.visibility = View.VISIBLE
                    imageState.setImageResource(R.drawable.ic_toast_check)
                }
                ERROR -> {
                    imageState.visibility = View.VISIBLE
                    imageState.setImageResource(R.drawable.ic_toast_error)
                }
                NONE -> {
                    imageState.visibility = View.GONE
                }
            }
            textMessage.setText(message)
        }

        return Toast(context).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER, 0, MARGIN_BOTTOM.px)
            duration = length
            view = binding.root
        }
    }
}