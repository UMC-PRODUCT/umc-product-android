package com.umc.presentation.ui.act.study.submit

import android.graphics.*
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

class AdminStudySubmitSwipeController(
    private val recyclerView: RecyclerView,
    private val onClickBest: (position: Int) -> Unit,
    private val onClickReview: (position: Int) -> Unit,
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {



    private var swipedPosition: Int = RecyclerView.NO_POSITION
    private var bestRect: RectF? = null
    private var reviewRect: RectF? = null




    private val originalElevation = WeakHashMap<View, Float>()
    private val originalTranslationZ = WeakHashMap<View, Float>()


    private val panelRadius = dp(16f)
    private val buttonRadius = dp(12f)

    private val buttonWidth = dp(78f)
    private val buttonGap = dp(8f)


    private val buttonInsetV = dp(10f)
    private val buttonInsetEnd = dp(12f)

    private val panelInsetEnd = dp(0f)
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
                            onClickBest(swipedPosition)
                            resetSwipe()
                            return true
                        }
                        review.contains(x, y) -> {
                            onClickReview(swipedPosition)
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

        val revealBuffer = dp(12f) // 4~10dp 사이로 취향 조절
        val maxReveal = (buttonWidth * 2) + buttonGap + buttonInsetEnd + panelInsetEnd + revealBuffer
        val clampedDx = max(-maxReveal, min(0f, dX))



        if (clampedDx < 0f || swipedPosition == pos) {
            if (!originalElevation.containsKey(itemView)) {
                originalElevation[itemView] = itemView.elevation
                originalTranslationZ[itemView] = itemView.translationZ
            }
            itemView.elevation = 0f
            itemView.translationZ = 0f
        }


        val panelTop = itemView.top.toFloat()
        val panelBottom = itemView.bottom.toFloat()
        val panelRight = itemView.right.toFloat() - panelInsetEnd


        val btnTop = panelTop + buttonInsetV
        val btnBottom = panelBottom - buttonInsetV
        val btnRight = panelRight - buttonInsetEnd

        val reviewLeft = btnRight - buttonWidth
        val review = RectF(reviewLeft, btnTop, btnRight, btnBottom)

        val bestRight = reviewLeft - buttonGap
        val bestLeft = bestRight - buttonWidth
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
        c.drawRoundRect(panelRect, panelRadius, panelRadius, paint)


        c.drawRect(panelRect.left, panelRect.top, panelRect.left + panelRadius, panelRect.bottom, paint)

        // 버튼 컬러
        val bestBg = ContextCompat.getColor(recyclerView.context, R.color.warning100)
        val bestText = ContextCompat.getColor(recyclerView.context, R.color.warning700)
        val bestIconTint = ContextCompat.getColor(recyclerView.context, R.color.warning700)

        val reviewBg = ContextCompat.getColor(recyclerView.context, R.color.success100)
        val reviewText = ContextCompat.getColor(recyclerView.context, R.color.success700)
        val reviewIconTint = ContextCompat.getColor(recyclerView.context, R.color.success700)


        drawActionButton(
            canvas = c,
            rect = best,
            bgColor = bestBg,
            iconRes = R.drawable.ic_act_hourglass,
            iconTint = bestIconTint,
            text = "베스트",
            textColor = bestText
        )

        drawActionButton(
            canvas = c,
            rect = review,
            bgColor = reviewBg,
            iconRes = R.drawable.ic_check_success,
            iconTint = reviewIconTint,
            text = "검토",
            textColor = reviewText
        )


        if (clampedDx < 0f) {
            paint.color = neutralBg
            val movedRight = itemView.right.toFloat() + clampedDx
            val coverLeft = movedRight - panelRadius - dp(14f)
            val coverRight = movedRight + dp(14f)
            c.drawRect(coverLeft, panelTop, coverRight, panelBottom, paint)
        }


        c.restore()

        bestRect = best
        reviewRect = review


        super.onChildDraw(c, rv, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)


        if (clampedDx < 0f) {
            paint.color = neutralBg

            val movedRight = itemView.right.toFloat() + clampedDx


            c.save()
            c.clipRect(
                itemView.left.toFloat(),
                panelTop,
                movedRight,
                panelBottom
            )


            c.drawRect(
                movedRight - panelRadius - dp(2f),
                panelTop,
                movedRight + dp(2f),
                panelBottom,
                paint
            )

            c.restore()
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        val v = viewHolder.itemView
        originalElevation[v]?.let { v.elevation = it }
        originalTranslationZ[v]?.let { v.translationZ = it }

        originalElevation.remove(v)
        originalTranslationZ.remove(v)
    }

    private fun drawActionButton(
        canvas: Canvas,
        rect: RectF,
        bgColor: Int,
        iconRes: Int,
        iconTint: Int,
        text: String,
        textColor: Int,
    ) {

        paint.color = bgColor
        canvas.drawRoundRect(rect, buttonRadius, buttonRadius, paint)


        val drawable = AppCompatResources.getDrawable(recyclerView.context, iconRes)?.mutate()
        drawable?.setTint(iconTint)
        val iconBmp = drawable?.toBitmap(
            iconSize.toInt(),
            iconSize.toInt(),
            Bitmap.Config.ARGB_8888
        )


        textPaint.color = textColor

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

    private fun dp(v: Float): Float =
        v * recyclerView.resources.displayMetrics.density

    private fun sp(v: Float): Float =
        v * recyclerView.resources.displayMetrics.scaledDensity
}
