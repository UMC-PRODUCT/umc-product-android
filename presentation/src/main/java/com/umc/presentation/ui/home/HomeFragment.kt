package com.umc.presentation.ui.home

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.home.SchedulePlan
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.calendar.SelectedDecorator
import com.umc.presentation.component.calendar.TodayDecorator
import com.umc.presentation.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeFragmentUiState, HomeFragmentEvent, HomeFragmentViewModel>
    (
    FragmentHomeBinding::inflate,
), ScheduleItemDelegate {
    override val viewModel: HomeFragmentViewModel by viewModels()

    //달력 데코레이터
    private lateinit var todayDec : TodayDecorator
    private lateinit var selectedDec : SelectedDecorator

    //리사이클러뷰 어댑터
    private val dailyAdapter by lazy { ScheduleAdapter(this) }
    private val allAdapter by lazy { ScheduleAdapter(this) }

    override fun initView() {
        binding.apply {
            vm = viewModel

        }
        todayDec = TodayDecorator(requireContext())
        selectedDec = SelectedDecorator(requireContext())
        initCalendar()

        binding.homeRcvDailyPlan.adapter = dailyAdapter
        binding.homeRcvAllPlanList.adapter = allAdapter
    }

    override fun initStates() {
        super.initStates()

        //임시로 뷰모델에서 가져와서 연결시키기
        repeatOnStarted(viewLifecycleOwner) {

            launch {
                viewModel.uiState.collect { state ->
                    // 변수명 수정: Diffiui가 자동으로 해준대요 그래서 submitList 사용
                    // notifiydatasetChanged() 쓸 필요가 없대요.
                    if (state.viewMode == HomeViewMode.CALENDAR) {
                        dailyAdapter.submitList(state.dailyPlans)
                    } else {
                        allAdapter.submitList(state.allPlans)
                    }
                }
            }

            //여기서 이자식이 event 수신
            launch {
                viewModel.uiEvent.collect {
                    handleMoveEvent(it)
                }
            }
        }
    }

    //얻배터에 delegate 정의하기!
    override fun onItemClicked(item: SchedulePlan) {
        //여기서 뷰모델의 onClick을 정의 -> 그럼 아이템 터치한 리스너를 viewModel에 전달하고
        //이벤트 발생을 보내고, 이를 다시 Fragment에서 수신한다.
        viewModel.onClickPlanDetail(item)
    }


    //이벤트 핸들링(얘를 observing 해서 쭈욱 보고 이벤트 처리)
    private fun handleMoveEvent(event: HomeFragmentEvent){
        when (event){
            is HomeFragmentEvent.MoveNoticeEvent -> moveToNotice()
            is HomeFragmentEvent.MoveNotificationEvent -> moveToNotification()
            is HomeFragmentEvent.MovePlanDetailEvent -> moveToPlanDetail(event.plan)
            is HomeFragmentEvent.MovePlanAddEvent -> moveToPlanAdd()

            else -> {}
        }
    }

    //이동 함수들
    private fun moveToNotice(){
        val action = HomeFragmentDirections.actionHomeToNotice()
        findNavController().navigate(action)

    }

    private fun moveToNotification(){
        val action = HomeFragmentDirections.actionHomeToNotification()
        findNavController().navigate(action)
    }

    private fun moveToPlanAdd(){
        val action = HomeFragmentDirections.actionHomeToPlanAdd()
        findNavController().navigate(action)
    }

    private fun moveToPlanDetail(plan : SchedulePlan){
        val action = HomeFragmentDirections.actionHomeToPlanDetail()
        findNavController().navigate(action)
    }


    //MaterialCalendar 초기화
    private fun initCalendar(){
        binding.homeCalendarView.apply {
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

                if (dayOfWeek == DayOfWeek.SUNDAY) {
                    SpannableString(text).apply {
                        setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.danger500
                                )
                            ),
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
