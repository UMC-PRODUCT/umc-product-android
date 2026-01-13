package com.umc.presentation.ui.act.util

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.umc.domain.model.act.check.CheckAvailableStatus
import com.umc.domain.model.act.check.CheckHistoryStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object CheckBindingAdapters {

    @JvmStatic
    @BindingAdapter("attendanceStatusStyle")
    fun setAvailableStatusStyle(view: UButton, status: CheckAvailableStatus?) {
        if (status == null) return

        // 이미지 기반 색상 매핑
        val (bgColorRes, textColorRes) = when (status) {
            CheckAvailableStatus.BEFORE ->
                R.color.neutral100 to R.color.neutral700
            CheckAvailableStatus.PENDING ->
                R.color.warning100 to R.color.warning700
            CheckAvailableStatus.COMPLETED ->
                R.color.success100 to R.color.success700 // 연한 녹색 배경 / 진한 녹색 글자
        }

        view.setCardBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes)) // UButton에 해당 메서드가 있다고 가정
    }

    @JvmStatic
    @BindingAdapter("attendanceStatusColor")
    fun setHistoryStatusColor(view: UButton, status: CheckHistoryStatus?) {
        if (status == null) return
        val colorRes = when (status) {
            CheckHistoryStatus.SUCCESS -> R.color.success500
            CheckHistoryStatus.LATE -> R.color.warning500
            CheckHistoryStatus.ABSENT -> R.color.danger500
        }
        view.setCardBackgroundColor(ContextCompat.getColor(view.context, colorRes))
    }

    @JvmStatic
    @BindingAdapter("isFirst", "isLast", "cornerRadius")
    fun setJoinedCardCorners(view: MaterialCardView, isFirst: Boolean, isLast: Boolean, radiusDp: Float) {
        val radius = radiusDp * view.context.resources.displayMetrics.density
        val builder = view.shapeAppearanceModel.toBuilder()

        val topRadius = if (isFirst) radius else 0f
        val bottomRadius = if (isLast) radius else 0f

        view.shapeAppearanceModel = builder
            .setTopLeftCorner(CornerFamily.ROUNDED, topRadius)
            .setTopRightCorner(CornerFamily.ROUNDED, topRadius)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomRadius)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRadius)
            .build()
    }
}

@BindingAdapter("imageResource")
fun setImageResource(imageView: ImageView, resId: Int) {
    if (resId != 0) {
        imageView.setImageResource(resId)
    }
}