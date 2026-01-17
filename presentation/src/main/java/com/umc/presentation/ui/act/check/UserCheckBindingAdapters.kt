package com.umc.presentation.ui.act.check

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.umc.domain.model.act.check.CheckAvailableStatus
import com.umc.domain.model.act.check.CheckHistoryStatus
import com.umc.presentation.R
import com.umc.presentation.component.UButton

object UserCheckBindingAdapters {

    /**
     * UButton의 출석 상태(출석 전, 대기, 완료)에 따른 배경 및 텍스트 스타일 적용
     */
    @JvmStatic
    @BindingAdapter("attendanceStatusStyle")
    fun setAvailableStatusStyle(view: UButton, status: CheckAvailableStatus?) {
        if (status == null) return

        val (bgColorRes, textColorRes) = when (status) {
            CheckAvailableStatus.BEFORE ->
                R.color.neutral100 to R.color.neutral700
            CheckAvailableStatus.PENDING ->
                R.color.warning100 to R.color.warning700
            CheckAvailableStatus.COMPLETED ->
                R.color.success100 to R.color.success700
        }

        view.setCardBackgroundColor(ContextCompat.getColor(view.context, bgColorRes))
        view.setTextColor(ContextCompat.getColor(view.context, textColorRes))
    }

    /**
     * 출석 히스토리 상태에 따른 배경색 적용
     */
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

    /**
     * 리스트 아이템의 위치(처음, 끝)에 따라 카드의 특정 모서리만 둥글게 처리
     */
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

    /**
     * 뷰의 확장 상태(isSelected) 처리 및 텍스트 정렬 로직
     */
    @JvmStatic
    @BindingAdapter("isSelected", "uTextColorRes", requireAll = false)
    fun setIsSelected(view: View, isExpanded: Boolean, textColorRes: Int?) {
        textColorRes?.let { resId ->
            val color = ContextCompat.getColor(view.context, resId)
            when (view) {
                is UButton -> view.setTextColor(color)
                is TextView -> view.setTextColor(color)
            }
        }

        if (view !is TextView && view !is UButton) return

        view.isSelected = isExpanded

        if (isExpanded) {
            view.post {
                if (view is TextView) {
                    val textWidth = view.paint.measureText(view.text.toString())
                    val viewWidth = view.width.toFloat()

                    if (textWidth > viewWidth) {
                        view.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        view.scrollTo(0, 0)
                    } else {
                        view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }
                }
            }
        } else {
            if (view is TextView) {
                view.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
        }
    }

    /**
     * 이미지 리소스 ID를 직접 바인딩
     */
    @JvmStatic
    @BindingAdapter("imageResource")
    fun setImageResource(imageView: ImageView, resId: Int) {
        if (resId != 0) {
            imageView.setImageResource(resId)
        }
    }
}