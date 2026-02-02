package com.umc.presentation.ui.act.study.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminActStudyScheduleAddBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class AdminActStudyScheduleAddFragment :
    BaseFragment<
            FragmentAdminActStudyScheduleAddBinding,
            AdminActStudyScheduleAddState,
            AdminActStudyScheduleAddEvent,
            AdminActStudyScheduleAddViewModel
            >(FragmentAdminActStudyScheduleAddBinding::inflate) {

    override val viewModel: AdminActStudyScheduleAddViewModel by viewModels()

    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.etStudyName.setOnTextChangedListener { text ->
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.UpdateStudyName(text))
        }
        binding.etLocation.setOnTextChangedListener { text ->
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.UpdateLocation(text))
        }


        binding.chipStartDate.setOnClickListener { showDatePicker(isStart = true) }
        binding.chipStartTime.setOnClickListener { showTimePicker(isStart = true) }
        binding.chipEndDate.setOnClickListener { showDatePicker(isStart = false) }
        binding.chipEndTime.setOnClickListener { showTimePicker(isStart = false) }


        binding.btnBack.setOnClickListener { moveBackPressed() }
        binding.btnCancel.setOnClickListener { moveBackPressed() }


        binding.btnRegister.setOnClickListener {
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.ClickRegister)
            moveBackPressed()
        }
    }

    override fun initStates() {
        super.initStates()
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->
                binding.state = state
            }
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = if (isStart) viewModel.uiState.value.startDate else viewModel.uiState.value.endDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val event =
                    if (isStart) AdminActStudyScheduleAddEvent.UpdateStartDate(year, month, day)
                    else AdminActStudyScheduleAddEvent.UpdateEndDate(year, month, day)

                viewModel.handleEvent(event)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val cal = if (isStart) viewModel.uiState.value.startTime else viewModel.uiState.value.endTime

        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

        val themeResId = if (isDarkMode) {
            android.R.style.Theme_Holo_Dialog_NoActionBar
        } else {
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar
        }

        TimePickerDialog(
            requireContext(),
            themeResId,
            { _, hour, minute ->
                val event =
                    if (isStart) AdminActStudyScheduleAddEvent.UpdateStartTime(hour, minute)
                    else AdminActStudyScheduleAddEvent.UpdateEndTime(hour, minute)

                viewModel.handleEvent(event)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun moveBackPressed() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
