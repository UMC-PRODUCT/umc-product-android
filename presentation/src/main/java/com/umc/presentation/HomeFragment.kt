package com.umc.presentation

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.calendar.SelectedDecorator
import com.umc.presentation.component.calendar.TodayDecorator
import com.umc.presentation.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeFragmentUiState, HomeFragmentEvent, HomeFragmentViewModel>(
    FragmentHomeBinding::inflate,
) {
    override val viewModel: HomeFragmentViewModel by viewModels()

    //달력 데코레이터
    private lateinit var todayDec : TodayDecorator
    private lateinit var selectedDec : SelectedDecorator

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
        todayDec = TodayDecorator(requireContext())
        selectedDec = SelectedDecorator(requireContext())
        initCalendar()
    }

    override fun initStates() {
        super.initStates()
    }

    //MaterialCalendar 초기화
    fun initCalendar(){
        binding.calendarCalendarView.apply {
            //달력 제목 커스텀
            setTitleFormatter { day ->
                val year = day.year
                val month = String.format("%02d", day.month)
                "$year.$month"
            }

            //주 커스텀
            setWeekDayFormatter { dayOfWeek ->
                val labels = listOf("일", "월", "화", "수", "목", "금", "토")
                val text = labels[dayOfWeek.value % 7] // 일요일이 7(또는 0)이므로 이에 맞춰 인덱싱

                if (dayOfWeek == org.threeten.bp.DayOfWeek.SUNDAY) {
                    SpannableString(text).apply {
                        setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.danger500)),
                            0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else {
                    text
                }
            }

            //화살표 색깔 지정
            setLeftArrow(R.drawable.ic_calendar_arrow_left)
            setRightArrow(R.drawable.ic_calendar_arrow_right)

            //데코레이터 연결
            addDecorators(todayDec, selectedDec)

            //날짜 터치 리스너 달기
            setOnDateChangedListener { widget, date, selected ->
                if(selected){
                    selectedDec.setSelectedDay(date)
                    invalidateDecorators()

                    /**viewModel에 전달**/
                    viewModel.setSelectedDate(date)
                }

            }
        }
    }
}
