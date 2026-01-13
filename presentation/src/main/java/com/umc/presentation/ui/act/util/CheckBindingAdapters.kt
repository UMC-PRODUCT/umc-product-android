package com.umc.presentation.ui.util

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.umc.domain.model.act.check.AttendanceStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object CheckBindingAdapters {
    @JvmStatic
    @BindingAdapter("attendanceStatusColor")
    fun setAttendanceStatusColor(view: UButton, status: AttendanceStatus?) {
        if (status == null) return
        val colorRes = when (status) {
            AttendanceStatus.BEFORE -> R.color.neutral100
            AttendanceStatus.SUCCESS -> R.color.success500
            AttendanceStatus.LATE -> R.color.warning500
            AttendanceStatus.ABSENT -> R.color.danger500
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