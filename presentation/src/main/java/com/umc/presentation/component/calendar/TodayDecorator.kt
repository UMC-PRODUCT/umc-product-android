package com.umc.presentation.component.calendar

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.umc.presentation.R

class TodayDecorator(context: Context) : DayViewDecorator{
    private val drawable = ContextCompat.getDrawable(context, R.drawable.bg_primary500_circle)
    private val today = CalendarDay.today()

    override fun shouldDecorate(day: CalendarDay): Boolean = day == today

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setBackgroundDrawable(it) }
        // 글자색 흰색으로 설정
        view.addSpan(ForegroundColorSpan(R.color.neutral000))
    }
}