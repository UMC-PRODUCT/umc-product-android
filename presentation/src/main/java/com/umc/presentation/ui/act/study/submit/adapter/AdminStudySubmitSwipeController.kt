package com.umc.presentation.ui.act.study.submit.adapter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.R
import java.util.WeakHashMap
import kotlin.math.max
import kotlin.math.min
import com.google.android.material.card.MaterialCardView
import androidx.core.view.ViewCompat

class AdminStudySubmitSwipeController(
    private val recyclerView: RecyclerView,
    private val isBestEnabled: (position: Int) -> Boolean,
    private val isReviewEnabled: (position: Int) -> Boolean,
    private val onClickBest: (position: Int) -> Unit,
    private val onClickReview: (position: Int) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {



    private val originalCardElevation = WeakHashMap<View, Float>()
    private val originalStrokeWidth = WeakHashMap<View, Int>()
    private val originalStrokeColor = WeakHashMap<View, Int>()
    private val originalZ = WeakHashMap<View, Float>()

    private var swipedPosition: Int = RecyclerView.NO_POSITION
    private var bestRect: RectF? = null
    private var reviewRect: RectF? = null

    private val originalElevation = WeakHashMap<View, Float>()
    private val originalTranslationZ = WeakHashMap<View, Float>()

    private val panelRadius = dp(16f)
    private val buttonRadius = dp(12f)

    private val buttonSize = dp(56f)
    private val buttonGap = dp(8f)
    private val buttonInsetEnd = dp(12f)
    private val panelInsetEnd = 0f
    private val iconSize = dp(18f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = sp(12f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    init {
        attachTouchInterceptor()
    }



    private fun attachTouchInterceptor() {
        recyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (swipedPosition == RecyclerView.NO_POSITION) return false

                val best = bestRect
                val review = reviewRect

                if (e.action == MotionEvent.ACTION_UP && best != null && review != null) {
                    val x = e.x
                    val y = e.y
                    when {
                        best.contains(x, y) -> {
                            if (isBestEnabled(swipedPosition)) onClickBest(swipedPosition)
                            resetSwipe()
                            return true
                        }
                        review.contains(x, y) -> {
                            if (isReviewEnabled(swipedPosition)) onClickReview(swipedPosition)
                            resetSwipe()
                            return true
                        }
                        else -> {
                            resetSwipe()
                            return false
                        }
                    }
                }
                return false
            }
        })
    }

    fun resetSwipe() {
        if (swipedPosition != RecyclerView.NO_POSITION) {
            val prev = swipedPosition
            swipedPosition = RecyclerView.NO_POSITION
            bestRect = null
            reviewRect = null
            recyclerView.adapter?.notifyItemChanged(prev)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipedPosition = viewHolder.bindingAdapterPosition
        recyclerView.invalidate()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.2f
    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 4
    override fun getSwipeVelocityThreshold(defaultValue: Float) = defaultValue * 4

    override fun onChildDraw(
        c: Canvas,
        rv: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val pos = viewHolder.bindingAdapterPosition

        if (pos == RecyclerView.NO_POSITION) {
            super.onChildDraw(c, rv, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        if (swipedPosition != RecyclerView.NO_POSITION && swipedPosition != pos && isCurrentlyActive) {
            resetSwipe()
        }

        val revealBuffer = dp(12f)
        val maxReveal = (buttonSize * 2) + buttonGap + buttonInsetEnd + panelInsetEnd + revealBuffer
        val clampedDx = max(-maxReveal, min(0f, dX))

        if (clampedDx < 0f || swipedPosition == pos) {


            val card = itemView as? MaterialCardView

            if (!originalZ.containsKey(itemView)) {
                originalZ[itemView] = ViewCompat.getTranslationZ(itemView)
            }


            ViewCompat.setTranslationZ(itemView, dp(6f))


            if (card != null) {
                if (!originalCardElevation.containsKey(card)) {
                    originalCardElevation[card] = card.cardElevation
                    originalStrokeWidth[card] = card.strokeWidth
                    originalStrokeColor[card] = card.strokeColor
                }

                card.cardElevation = dp(2f)
                card.strokeWidth = dp(1f).toInt()
                card.strokeColor = ContextCompat.getColor(recyclerView.context, R.color.neutral200)
            }
        }

        val panelTop = itemView.top.toFloat()
        val panelBottom = itemView.bottom.toFloat()
        val panelRight = itemView.right.toFloat() - panelInsetEnd

        val itemHeight = panelBottom - panelTop
        val btnTop = panelTop + (itemHeight - buttonSize) / 2f
        val btnBottom = btnTop + buttonSize
        val btnRight = panelRight - buttonInsetEnd

        val reviewLeft = btnRight - buttonSize
        val review = RectF(reviewLeft, btnTop, btnRight, btnBottom)

        val bestRight = reviewLeft - buttonGap
        val bestLeft = bestRight - buttonSize
        val best = RectF(bestLeft, btnTop, bestRight, btnBottom)

        val panelLeft = panelRight - maxReveal
        val panelRect = RectF(panelLeft, panelTop, panelRight, panelBottom)

        val reveal = -clampedDx + dp(2f)
        val visibleRight = itemView.right.toFloat() + dp(2f)
        val clipLeft = visibleRight - reveal

        val neutralBg = ContextCompat.getColor(recyclerView.context, R.color.neutral000)

        c.save()
        c.clipRect(clipLeft, panelTop, visibleRight, panelBottom)

        paint.color = neutralBg
        paint.alpha = 255
        c.drawRoundRect(panelRect, panelRadius, panelRadius, paint)
        c.drawRect(panelRect.left, panelRect.top, panelRect.left + panelRadius, panelRect.bottom, paint)

        val bestEnabled = isBestEnabled(pos)
        val reviewEnabled = isReviewEnabled(pos)


        val bestBg = ContextCompat.getColor(recyclerView.context, if (bestEnabled) R.color.warning100 else R.color.neutral100)
        val bestText = ContextCompat.getColor(recyclerView.context, if (bestEnabled) R.color.warning700 else R.color.neutral400)
        val bestIcon = ContextCompat.getColor(recyclerView.context, if (bestEnabled) R.color.warning500 else R.color.neutral400)

        val reviewBg = ContextCompat.getColor(recyclerView.context, if (reviewEnabled) R.color.primary100 else R.color.neutral100)
        val reviewText = ContextCompat.getColor(recyclerView.context, if (reviewEnabled) R.color.primary500 else R.color.neutral400)
        val reviewIcon = ContextCompat.getColor(recyclerView.context, if (reviewEnabled) R.color.primary500 else R.color.neutral400)

        drawActionButton(
            canvas = c,
            rect = best,
            bgColorInt = bestBg,
            iconRes = R.drawable.ic_best,
            iconTintInt = bestIcon,
            text = "베스트",
            textColorInt = bestText,
            enabled = bestEnabled
        )

        drawActionButton(
            canvas = c,
            rect = review,
            bgColorInt = reviewBg,
            iconRes = R.drawable.ic_check_success,
            iconTintInt = reviewIcon,
            text = "검토",
            textColorInt = reviewText,
            enabled = reviewEnabled
        )


        if (clampedDx < 0f) {
            paint.color = neutralBg
            paint.alpha = 255
            val movedRight = itemView.right.toFloat() + clampedDx
            val coverLeft = movedRight - panelRadius - dp(14f)
            val coverRight = movedRight + dp(14f)
            c.drawRect(coverLeft, panelTop, coverRight, panelBottom, paint)
        }

        c.restore()

        bestRect = best
        reviewRect = review

        super.onChildDraw(c, rv, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        val v = viewHolder.itemView
        val card = v as? MaterialCardView

        // 기존 elevation/translationZ 원복
        originalZ[v]?.let { ViewCompat.setTranslationZ(v, it) }
        originalZ.remove(v)

        card?.let {
            originalCardElevation[it]?.let { e -> it.cardElevation = e }
            originalStrokeWidth[it]?.let { w -> it.strokeWidth = w }
            originalStrokeColor[it]?.let { c -> it.strokeColor = c }

            originalCardElevation.remove(it)
            originalStrokeWidth.remove(it)
            originalStrokeColor.remove(it)
        }
    }

    private fun drawActionButton(
        canvas: Canvas,
        rect: RectF,
        bgColorInt: Int,
        iconRes: Int,
        iconTintInt: Int,
        text: String,
        textColorInt: Int,
        enabled: Boolean
    ) {
        val alpha = if (enabled) 255 else 110

        paint.color = bgColorInt
        paint.alpha = alpha
        canvas.drawRoundRect(rect, buttonRadius, buttonRadius, paint)

        val drawable = AppCompatResources.getDrawable(recyclerView.context, iconRes)?.mutate()
        drawable?.setTint(iconTintInt)
        drawable?.alpha = alpha
        val iconBmp = drawable?.toBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)

        textPaint.color = textColorInt
        textPaint.alpha = alpha

        val gap = dp(4f)
        val totalHeight = (if (iconBmp != null) iconSize else 0f) + gap + textPaint.textSize
        val startY = rect.centerY() - totalHeight / 2f

        if (iconBmp != null) {
            val iconLeft = rect.centerX() - iconSize / 2f
            val iconTop = startY
            canvas.drawBitmap(iconBmp, iconLeft, iconTop, null)
        }

        val textY = startY + (if (iconBmp != null) iconSize + gap else 0f) + textPaint.textSize
        canvas.drawText(text, rect.centerX(), textY, textPaint)
    }

    private fun dp(v: Float): Float = v * recyclerView.resources.displayMetrics.density
    private fun sp(v: Float): Float = v * recyclerView.resources.displayMetrics.scaledDensity
}