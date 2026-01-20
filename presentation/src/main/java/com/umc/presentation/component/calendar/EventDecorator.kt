package com.umc.presentation.component.calendar

import android.content.Context
import androidx.core.R
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDecorator(private val context: Context, dates: Collection<CalendarDay>) : DayViewDecorator {

    private val dates = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean = dates.contains(day)

    override fun decorate(view: DayViewFacade) {
        val dotColor = ContextCompat.getColor(context, com.umc.presentation.R.color.accent600)
        view.addSpan(DotSpan(5f, dotColor))
    }
}