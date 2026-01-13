package com.umc.presentation.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanAddBinding
import java.util.Calendar

class PlanAddFragment : BaseFragment<FragmentPlanAddBinding, PlanAddFragmentUiState, PlanAddFragmentEvent, PlanAddViewModel>(
    FragmentPlanAddBinding::inflate,
) {
    override val viewModel: PlanAddViewModel by viewModels()

    override fun initView() {
        binding.apply {
            //onclick 달기
            //시작 날짜/시간
            plandetailCdvStartDate.setOnClickListener { showDatePicker(true) }
            plandetailCdvStartTime.setOnClickListener { showTimePicker(true) }
            //종료 날짜/시간
            plandetailCdvEndDate.setOnClickListener { showDatePicker(false) }
            plandetailCdvEndTime.setOnClickListener { showTimePicker(false) }

        }
    }


    override fun initStates() {
        super.initStates()
        repeatOnStarted(viewLifecycleOwner){
            viewModel.uiState.collect{ state ->
                binding.apply {
                    plandetailCdvStartDate.setText(state.startDateText)
                    plandetailCdvStartTime.setText(state.startTimeText)
                    plandetailCdvEndDate.setText(state.endDateText)
                    plandetailCdvEndTime.setText(state.endTimeText)

                }
            }
        }
    }

    //날짜 다이얼로그 호출
    private fun showDatePicker(isStart: Boolean) {
        // 시작/종료 여부에 따라 현재 설정된 날짜 가져오기
        val cal = if (isStart) viewModel.uiState.value.startDate else viewModel.uiState.value.endDate

        DatePickerDialog(requireContext(), { _, year, month, day ->
            // ViewModel 이벤트 호출 리스너 달기
            val event = if (isStart) PlanAddFragmentEvent.UpdateStartDate(year, month, day)
            else PlanAddFragmentEvent.UpdateEndDate(year, month, day)

            viewModel.processDateTime(event)
        },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val cal = if (isStart) viewModel.uiState.value.startTime else viewModel.uiState.value.endTime

        TimePickerDialog(requireContext(), { _, hour, minute ->
            val event = if (isStart) PlanAddFragmentEvent.UpdateStartTime(hour, minute)
            else PlanAddFragmentEvent.UpdateEndTime(hour, minute)
            viewModel.processDateTime(event)
        },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }


}