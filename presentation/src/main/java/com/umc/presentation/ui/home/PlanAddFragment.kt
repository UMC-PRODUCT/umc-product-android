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
import com.umc.presentation.ui.home.dialog.AddAttendanceDialog
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
            planaddBtnCancelPlan.setOnClickListener { moveBackPressed() }

            //최종 확인 버튼
            planaddBtnRegisterPlan.setOnClickListener {
                /**TODO 이벤트를 통해 해당 정보를 서버에 넘겨야 한다.**/

                //운영진 여부에 따라 분기 처리
                if(viewModel.uiState.value.isManager){
                    //다이얼로그 생성
                    val dialog = AddAttendanceDialog(
                        onReject = {
                            viewModel.submitPlan(false)
                        },
                        onConfirm = {
                            viewModel.submitPlan(true)

                        }

                    )
                    dialog.show(childFragmentManager, "AddAttendance")
                    
                }
                else{
                    viewModel.submitPlan(false)
                }
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

                //수정 모드에 따른 UI 및 터치 로직
                val isEditMode = viewModel.uiState.value.updateScheduleId != -1L
                isEnabled = !isEditMode //수정 모드이면 터치 못하게 막기

                val bgColor = if (isEditMode) {
                    context.getColor(R.color.neutral100)
                } else {
                    context.getColor(R.color.neutral000)
                }
                setCardBackgroundColor(bgColor)



                setOnClickListener {
                    // 뷰모델을 생성자로 전달하여 상태를 공유합니다.
                    val participantDialog = BottomSheetParticipantDialog { selectedParticipant, selectedParticipantString ->
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


    //뒤로가기
    private fun moveBackPressed(){
        requireActivity().onBackPressedDispatcher.onBackPressed()

    }


}