package com.umc.presentation.ui.act.challenge

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object AdminChallengerBindingAdapters {

    @JvmStatic
    @BindingAdapter("adminStatusStyle")
    fun setAdminStatusStyle(view: UButton, statusType: String?) {
        val (bgColorRes, textColorRes, borderColorRes) = when (statusType) {
            "OUT" -> Triple(R.color.danger100, R.color.danger700, R.color.danger300)
            "WARNING" -> Triple(R.color.warning100, R.color.warning700, R.color.warning300)
            else -> Triple(R.color.neutral100, R.color.neutral700, R.color.neutral200)
        }

        val context = view.context

        view.setUBackgroundColor(ContextCompat.getColor(context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(context, textColorRes))
        view.strokeColor = ContextCompat.getColor(context, borderColorRes)
    }
}