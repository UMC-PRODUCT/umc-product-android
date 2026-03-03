package com.umc.presentation.ui.act.study.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudyScheduleAddBinding
import com.umc.presentation.ui.act.study.schedule.model.AdminActStudyScheduleAddEvent
import com.umc.presentation.ui.act.study.schedule.model.AdminActStudyScheduleAddState
import com.umc.presentation.ui.act.study.schedule.model.AdminActStudyScheduleAddViewModel
import com.umc.presentation.ui.home.dialog.BottomSheetLocationDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.umc.domain.model.home.LocationItem
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R

@AndroidEntryPoint
class AdminActStudyScheduleAddFragment :
    BaseFragment<
            FragmentAdminStudyScheduleAddBinding,
            AdminActStudyScheduleAddState,
            AdminActStudyScheduleAddEvent,
            AdminActStudyScheduleAddViewModel
            >(FragmentAdminStudyScheduleAddBinding::inflate) {

    override val viewModel: AdminActStudyScheduleAddViewModel by viewModels()
    private val args: AdminActStudyScheduleAddFragmentArgs by navArgs()

    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.handleEvent(AdminActStudyScheduleAddEvent.Init(groupId = args.groupId))

        binding.etStudyName.setOnTextChangedListener { text ->
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.UpdateStudyName(text))
        }

        binding.cardLocation.setOnClickListener {
            BottomSheetLocationDialog(
                onItemSelected = { item: LocationItem ->
                    viewModel.handleEvent(
                        AdminActStudyScheduleAddEvent.UpdateLocation(
                            name = item.title,
                            lat = item.latitude,
                            lng = item.longitude
                        )
                    )
                }
            ).show(parentFragmentManager, "location")
        }

        binding.chipStartDate.setOnClickListener { showDatePicker(true) }
        binding.chipStartTime.setOnClickListener { showTimePicker(true) }
        binding.chipEndDate.setOnClickListener { showDatePicker(false) }
        binding.chipEndTime.setOnClickListener { showTimePicker(false) }

        binding.btnBack.setOnClickListener { moveToStudyGroupTab() }
        binding.btnCancel.setOnClickListener { moveToStudyGroupTab() }

        // 여기서 바로 뒤로가지 말기
        binding.btnRegister.setOnClickListener {
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.ClickRegister)
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->
                binding.state = state
            }
        }

        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is AdminActStudyScheduleAddEvent.ShowToast -> {
                        android.widget.Toast.makeText(requireContext(), event.message, android.widget.Toast.LENGTH_SHORT).show()
                        if (event.message.contains("등록되었습니다")) {
                            moveToStudyGroupTab()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun moveToStudyGroupTab() {
        val nav = findNavController()

        val actEntry = nav.getBackStackEntry(R.id.activityManagementFragment)

        actEntry.savedStateHandle["ACT_TARGET_TAB"] = 1
        actEntry.savedStateHandle["ADMIN_STUDY_TARGET_TAB"] = "GROUP"
        nav.popBackStack(R.id.activityManagementFragment, false)
    }

    private fun showDatePicker(isStart: Boolean) {
        val cal = if (isStart) viewModel.uiState.value.startDate else viewModel.uiState.value.endDate
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                viewModel.handleEvent(
                    if (isStart) AdminActStudyScheduleAddEvent.UpdateStartDate(year, month, day)
                    else AdminActStudyScheduleAddEvent.UpdateEndDate(year, month, day)
                )
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
                viewModel.handleEvent(
                    if (isStart) AdminActStudyScheduleAddEvent.UpdateStartTime(hour, minute)
                    else AdminActStudyScheduleAddEvent.UpdateEndTime(hour, minute)
                )
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