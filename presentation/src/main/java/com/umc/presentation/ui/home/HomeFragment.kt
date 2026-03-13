package com.umc.presentation.ui.home

import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.umc.domain.model.enums.HomeViewMode
import com.umc.domain.model.home.SchedulePlanItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UMypageDialog
import com.umc.presentation.component.UMypageDialogModel
import com.umc.presentation.component.calendar.EventDecorator
import com.umc.presentation.component.calendar.SelectedDecorator
import com.umc.presentation.component.calendar.TodayDecorator
import com.umc.presentation.databinding.FragmentHomeBinding
import com.umc.presentation.ui.act.dialog.BottomSheetRewardEtcDialog
import com.umc.presentation.ui.act.dialog.BottomSheetRewardPunishDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeFragmentUiState, HomeFragmentEvent, HomeViewModel>
    (
    FragmentHomeBinding::inflate,
), ScheduleItemDelegate {
    override val viewModel: HomeViewModel by viewModels()

    //달력 데코레이터
    private lateinit var todayDec : TodayDecorator
    private lateinit var selectedDec : SelectedDecorator
    private lateinit var eventDec : EventDecorator

    //리사이클러뷰 어댑터
    private val dailyAdapter by lazy { ScheduleAdapter(this) }
    private val allAdapter by lazy { ScheduleAdapter(this) }
    
    //앱 종료 다이얼로그
    val finishAppDialogModel = UMypageDialogModel(
        title = "애플리케이션 종료",
        content = "애플리케이션을 종료하시겠습니까?",
        isTwoButton = true,
        positiveText = "종료",
        negativeText = "취소"
    )

    override fun initView() {
        binding.apply {
            vm = viewModel

        }
        todayDec = TodayDecorator(requireContext())
        selectedDec = SelectedDecorator(requireContext())
        eventDec = EventDecorator(requireContext(), viewModel.uiState.value.eventDates)

        initCalendar()

        binding.homeRcvDailyPlan.adapter = dailyAdapter
        binding.homeRcvAllPlanList.adapter = allAdapter

        //뒤로가기 백스택 막기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            val dialog = UMypageDialog(finishAppDialogModel) {
                requireActivity().finish()
            }
            dialog.show(parentFragmentManager, "HomeDialog")
        }

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

                    //달력 이벤트 데코레이터 갱신
                    //로직상 기존 데코레이터를 지우고 새로 입력
                    binding.homeCalendarView.removeDecorators()
                    binding.homeCalendarView.addDecorators(
                        //TodayDecorator
                        todayDec,
                        //SelectedDecoator
                        selectedDec.apply {
                            //데코레이터에 셀렉트 넣고
                            setSelectedDay(state.selectedDate) },
                            //EventDecorator
                            EventDecorator(requireContext(), state.eventDates)
                    )

                    //커스텀 텍스트뷰
                    updateGrowthDaysText(state.growDay)
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
    override fun onItemClicked(item: SchedulePlanItem) {
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
            is HomeFragmentEvent.OpenDatePickerEvent -> openDatePicker()

            else -> {}
        }
    }

    //이동 함수들
    /**TODO 조나단이 작성한 공지 상세 페이지로 이동하도록 로직 수정 필요
     *
     * **/
    private fun moveToNotice(){
        //val action = HomeFragmentDirections.actionHomeToNotice()
        //findNavController().navigate(action)

    }

    /**테스트 용동**/
    private fun moveToNotification(){
        //val action = HomeFragmentDirections.actionHomeToNotification()
        //findNavController().navigate(action)

        val dilog = BottomSheetRewardEtcDialog()
        dilog.show(parentFragmentManager, "BottomSheetRewardEtc")

        //val dialog = BottomSheetRewardPunishDialog(false)
        // childFragmentManager를 사용하여 생명주기를 안전하게 관리합니다.
        //dialog.show(childFragmentManager, "BottomSheetRewardPunish")

    }

    private fun moveToPlanAdd(){
        val action = HomeFragmentDirections.actionHomeToPlanAdd()
        findNavController().navigate(action)
    }

    private fun moveToPlanDetail(plan : SchedulePlanItem){
        val action = HomeFragmentDirections.actionHomeToPlanDetail(
            scheduleId = plan.id,
            plusDay = plan.plusDay
        )
        findNavController().navigate(action)
    }

    //텍스트 뷰 색깔 변경 (000일째 성장하고 있어요)
    private fun updateGrowthDaysText(days: Int) {
        val highlight = "${days}일째"
        val fullText = "$highlight 성장하고 있어요"
        val color = ContextCompat.getColor(requireContext(), R.color.primary600) // 다크모드 대응

        //몇일째 성장하고 있어요 텍스트
        binding.homeTvUserstatus.text = SpannableStringBuilder(fullText).apply {
            setSpan(ForegroundColorSpan(color), 0, highlight.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    //DatePicker 달력 열기
    private fun openDatePicker(){
        // 현재 선택된 날짜 가져오기
        val currentDay = viewModel.uiState.value.selectedDate

        // DatePickerDialog 인스턴스 생성
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // DatePicker의 month는 0부터 시작하므로 1 더하기
                val selectedDate = CalendarDay.from(year, month + 1, dayOfMonth)

                // 뷰모델에 선택한 날짜 전달
                viewModel.setSelectedDate(selectedDate)

                // 달력 UI 업데이트 (강제로 해당 날짜를 선택하고 월 페이지를 이동시킴)
                binding.homeCalendarView.selectedDate = selectedDate // 선택 하이라이트 이동
                binding.homeCalendarView.setCurrentDate(selectedDate) // 해당 월로 달력 스크롤
            },
            currentDay.year,     // 다이얼로그 처음 켰을 때 보여줄 연도
            currentDay.month - 1, // 다이얼로그 처음 켰을 때 보여줄 월 (0~11)
            currentDay.day        // 다이얼로그 처음 켰을 때 보여줄 일
        )

        datePickerDialog.show()
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

            //제목 터치 시 로직
            setOnTitleClickListener {
                viewModel.onClickCalendarHeader()
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
            /**initState에서 관리**/

            //날짜 터치 리스너 달기
            setOnDateChangedListener { widget, date, selected ->
                if(selected){
                    //데코레이터에 전달
                    selectedDec.setSelectedDay(date)
                    invalidateDecorators()

                    /**viewModel에 전달**/
                    viewModel.setSelectedDate(date)
                }
            }

            //월이 바뀔 때 일정 다시 가져오기 리스너
            setOnMonthChangedListener { widget, date ->
                viewModel.getScheduleMonth(date.year, date.month)
            }

        }
    }

    //API 재호출
    override fun onResume() {

        val current = viewModel.uiState.value.selectedDate
        viewModel.getScheduleMonth(current.year, current.month)
        viewModel.getUserInfo()

        super.onResume()
    }


}
