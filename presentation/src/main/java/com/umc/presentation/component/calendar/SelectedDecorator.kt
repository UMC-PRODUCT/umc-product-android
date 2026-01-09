package com.umc.presentation.component.calendar

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.umc.presentation.R

class SelectedDecorator(context: Context) : DayViewDecorator {
    private var selectedDay: CalendarDay? = null
    private val drawable = ContextCompat.getDrawable(context, R.drawable.bg_calendar_selected)

    fun setSelectedDay(day: CalendarDay) { selectedDay = day }

    override fun shouldDecorate(day: CalendarDay): Boolean = day == selectedDay

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setBackgroundDrawable(it) }
        // 글자색 설정
        view.addSpan(ForegroundColorSpan(R.color.primary600))
    }
}