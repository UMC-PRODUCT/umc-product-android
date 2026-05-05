package com.umc.presentation.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanAddBinding
import com.umc.presentation.extension.dp
import com.umc.presentation.ui.home.dialog.BottomSheetCategoryPlanDialog
import com.umc.presentation.ui.home.dialog.BottomSheetLocationDialog
import com.umc.presentation.ui.home.dialog.BottomSheetParticipantDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class PlanAddFragment : BaseFragment<FragmentPlanAddBinding, PlanAddFragmentUiState, PlanAddFragmentEvent, PlanAddViewModel>(
    FragmentPlanAddBinding::inflate,
) {
    override val viewModel: PlanAddViewModel by viewModels()

    private val args: PlanAddFragmentArgs by navArgs()
    private var scheduleId : Long = -1L


    override fun initView() {

        scheduleId = args.scheduleId
        if(scheduleId != -1L){

            viewModel.settingUpdateSchedule(scheduleId)
        }

        binding.apply {
            vm = viewModel
            //onclick 달기
            
            //시작 날짜/시간 chip 설정
            planaddCdvStartDate.setOnClickListener { showDatePicker(true) }
            planaddCdvStartTime.setOnClickListener { showTimePicker(true) }
            //종료 날짜/시간 chip 설정
            planaddCdvEndDate.setOnClickListener { showDatePicker(false) }
            planaddCdvEndTime.setOnClickListener { showTimePicker(false) }

            //하루종일 스위치 로직
            binding.planaddSwitchAllday.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAllday(isChecked)
            }

            //일정 제목 title 설정
            planaddTextfieldPlanTitleName.apply {
                setOnTextChangedListener { text ->
                    viewModel.updatePlanTitle(text)
                }
            }

            //일정 상세 내용 설정
            planaddTextfieldPlanDetail.apply{
                setOnTextChangedListener { text ->
                    viewModel.updatePlanDetail(text)
                }
            }

            planaddBtnBack.setOnClickListener { moveBackPressed() }

            //출석부 함께 생성 체크박스
            planaddLayoutAttendanceCheck.setOnClickListener {
                viewModel.toggleAttendanceCheck()
            }

            //출석 시작 날짜/시간 chip
            planaddChipCheckInStartDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.CHECK_IN_START_DATE) }
            planaddChipCheckInStartTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.CHECK_IN_START_TIME) }

            //출석 종료 날짜/시간 chip
            planaddChipOnTimeEndDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.ON_TIME_END_DATE) }
            planaddChipOnTimeEndTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.ON_TIME_END_TIME) }

            //지각 인정 날짜/시간 chip
            planaddChipLateEndDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.LATE_END_DATE) }
            planaddChipLateEndTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.LATE_END_TIME) }

            //최종 확인 버튼
            planaddBtnRegisterPlan.setOnClickListener {
                viewModel.submitPlan(viewModel.uiState.value.isAttendanceChecked)
            }

            //장소 선택 부분 터치시 다이얼로그 로직
            binding.planaddCdvPlanLocation.setOnClickListener {
                // 앞서 만든 BottomSheetDialog 생성
                val locationDialog = BottomSheetLocationDialog { selectedItem ->
                    // 선택된 장소(LocationItem)의 제목을 뷰모델 이벤트로 전달
                    viewModel.updatePlanLocation(selectedItem)
                }
                // 다이얼로그 표시
                locationDialog.show(childFragmentManager, "LocationSelect")
            }

            //카테고리 선택 처피 시 다이얼로그 로직
            binding.planaddCdvSearchCategory.setOnClickListener {
                val categoryDialog = BottomSheetCategoryPlanDialog(viewModel)
                categoryDialog.show(childFragmentManager, "CategorySelect")
            }

            //인원 관련 터치 시 다이얼로그 로직
            binding.planaddCdvSearchParticipant.apply{


                setOnClickListener {
                    // 뷰모델을 생성자로 전달하여 상태를 공유합니다.
                    val participantDialog = BottomSheetParticipantDialog(
                        viewModel.uiState.value.selectedParticipants
                    ) { selectedParticipant, selectedParticipantString ->
                        viewModel.updateParticipants(selectedParticipant, selectedParticipantString)
                    }

                    // childFragmentManager를 사용하여 프래그먼트 계층 구조를 유지합니다.
                    participantDialog.show(childFragmentManager, "ParticipantSelect")
                }
            }
            

        }


    }


    override fun initStates() {
        super.initStates()
        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        planaddCdvStartDate.setText(state.startDateText)
                        planaddCdvStartTime.setText(state.startTimeText)
                        planaddCdvEndDate.setText(state.endDateText)
                        planaddCdvEndTime.setText(state.endTimeText)

                    }

                }
            }
            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    override fun handleEvent(event: PlanAddFragmentEvent) {
        super.handleEvent(event)
        when(event){
            is PlanAddFragmentEvent.MoveBackPressedEvent -> {
                moveBackPressed()
            }
            else -> {}
        }
    }



    //날짜 다이얼로그 호출 (날짜)
    private fun showDatePicker(isStart: Boolean) {
        // 시작/종료 여부에 따라 현재 설정된 날짜 가져오기
        val cal = if (isStart) viewModel.uiState.value.startDate else viewModel.uiState.value.endDate

        DatePickerDialog(requireContext(), { _, year, month, day ->
            // ViewModel 이벤트 호출 리스너 달기

            if(isStart){
                viewModel.updateStartDate(year, month, day)
            }
            else{
                viewModel.updateEndDate(year, month, day)
            }
        },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    //날짜 다이얼로그 호출 (시간)
    private fun showTimePicker(isStart: Boolean) {
        val cal = if (isStart) viewModel.uiState.value.startTime else viewModel.uiState.value.endTime

        // 다크모드 라이드 모드 확인
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val themeResId = if (isDarkMode) {
            android.R.style.Theme_Holo_Dialog_NoActionBar
        } else {
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar
        }

        val dialog = TimePickerDialog(requireContext(),
            themeResId,
            { _, hour, minute ->

                if(isStart){
                    viewModel.updateStartTime(hour, minute)
                }
                else{
                    viewModel.updateEndTime(hour, minute)
                }
        },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    //출석 정책 날짜 다이얼로그
    private fun showAttendanceDatePicker(field: AttendanceField) {
        val cal = field.getDateCal(viewModel.uiState.value)
        DatePickerDialog(requireContext(), { _, year, month, day ->
            when (field) {
                AttendanceField.CHECK_IN_START_DATE -> viewModel.updateCheckInStartDate(year, month, day)
                AttendanceField.ON_TIME_END_DATE    -> viewModel.updateOnTimeEndDate(year, month, day)
                AttendanceField.LATE_END_DATE       -> viewModel.updateLateEndDate(year, month, day)
                else -> {}
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    //출석 정책 시간 다이얼로그
    private fun showAttendanceTimePicker(field: AttendanceField) {
        val cal = field.getTimeCal(viewModel.uiState.value)
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val themeResId = if (isDarkMode) android.R.style.Theme_Holo_Dialog_NoActionBar
                         else android.R.style.Theme_Holo_Light_Dialog_NoActionBar
        val dialog = TimePickerDialog(requireContext(), themeResId, { _, hour, minute ->
            when (field) {
                AttendanceField.CHECK_IN_START_TIME -> viewModel.updateCheckInStartTime(hour, minute)
                AttendanceField.ON_TIME_END_TIME    -> viewModel.updateOnTimeEndTime(hour, minute)
                AttendanceField.LATE_END_TIME       -> viewModel.updateLateEndTime(hour, minute)
                else -> {}
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private enum class AttendanceField {
        CHECK_IN_START_DATE, CHECK_IN_START_TIME,
        ON_TIME_END_DATE, ON_TIME_END_TIME,
        LATE_END_DATE, LATE_END_TIME;

        fun getDateCal(state: PlanAddFragmentUiState): Calendar = when (this) {
            CHECK_IN_START_DATE, CHECK_IN_START_TIME -> state.checkInStartDate
            ON_TIME_END_DATE, ON_TIME_END_TIME       -> state.onTimeEndDate
            LATE_END_DATE, LATE_END_TIME             -> state.lateEndDate
        }

        fun getTimeCal(state: PlanAddFragmentUiState): Calendar = when (this) {
            CHECK_IN_START_DATE, CHECK_IN_START_TIME -> state.checkInStartTime
            ON_TIME_END_DATE, ON_TIME_END_TIME       -> state.onTimeEndTime
            LATE_END_DATE, LATE_END_TIME             -> state.lateEndTime
        }
    }

    //뒤로가기
    private fun moveBackPressed(){
        requireActivity().onBackPressedDispatcher.onBackPressed()

    }


}