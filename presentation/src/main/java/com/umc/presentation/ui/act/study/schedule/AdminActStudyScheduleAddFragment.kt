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
import com.umc.presentation.ui.home.dialog.BottomSheetParticipantDialog
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

        viewModel.handleEvent(
            AdminActStudyScheduleAddEvent.Init(
                groupId = args.groupId,
                groupTitle = args.groupTitle,
                groupPart = args.groupPart,
            )
        )

        binding.etStudyName.setOnTextChangedListener { text ->
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.UpdateStudyName(text))
        }

        binding.cardLocation.setOnClickListener {
            if (viewModel.uiState.value.isOnline) return@setOnClickListener
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

        binding.layoutOnlineCheck.setOnClickListener {
            viewModel.handleEvent(AdminActStudyScheduleAddEvent.ToggleOnline)
        }

        binding.cardParticipant.setOnClickListener {
            BottomSheetParticipantDialog(
                viewModel.uiState.value.selectedParticipants
            ) { participants, summaryString ->
                viewModel.handleEvent(AdminActStudyScheduleAddEvent.UpdateParticipants(participants, summaryString))
            }.show(childFragmentManager, "ParticipantSelect")
        }

        binding.chipStartDate.setOnClickListener { showDatePicker(true) }
        binding.chipStartTime.setOnClickListener { showTimePicker(true) }
        binding.chipEndDate.setOnClickListener { showDatePicker(false) }
        binding.chipEndTime.setOnClickListener { showTimePicker(false) }

        binding.chipCheckInStartDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.CHECK_IN_START_DATE) }
        binding.chipCheckInStartTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.CHECK_IN_START_TIME) }
        binding.chipOnTimeEndDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.ON_TIME_END_DATE) }
        binding.chipOnTimeEndTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.ON_TIME_END_TIME) }
        binding.chipLateEndDate.setOnClickListener { showAttendanceDatePicker(AttendanceField.LATE_END_DATE) }
        binding.chipLateEndTime.setOnClickListener { showAttendanceTimePicker(AttendanceField.LATE_END_TIME) }

        binding.cardWeek.setOnClickListener {
            BottomSheetWeekPickerDialog(
                selectedWeek = viewModel.uiState.value.selectedWeek,
                onSelect = { week ->
                    viewModel.handleEvent(AdminActStudyScheduleAddEvent.SelectWeek(week))
                }
            ).show(childFragmentManager, "WeekPicker")
        }

        binding.btnBack.setOnClickListener { moveToStudyGroupTab() }

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
        showTimePickerDialog(cal) { hour, minute ->
            viewModel.handleEvent(
                if (isStart) AdminActStudyScheduleAddEvent.UpdateStartTime(hour, minute)
                else AdminActStudyScheduleAddEvent.UpdateEndTime(hour, minute)
            )
        }
    }

    private enum class AttendanceField {
        CHECK_IN_START_DATE, CHECK_IN_START_TIME,
        ON_TIME_END_DATE, ON_TIME_END_TIME,
        LATE_END_DATE, LATE_END_TIME,
    }

    private fun showAttendanceDatePicker(field: AttendanceField) {
        val s = viewModel.uiState.value
        val cal = when (field) {
            AttendanceField.CHECK_IN_START_DATE -> s.checkInStartDate
            AttendanceField.ON_TIME_END_DATE -> s.onTimeEndDate
            AttendanceField.LATE_END_DATE -> s.lateEndDate
            else -> return
        }
        DatePickerDialog(requireContext(), { _, year, month, day ->
            viewModel.handleEvent(when (field) {
                AttendanceField.CHECK_IN_START_DATE -> AdminActStudyScheduleAddEvent.UpdateCheckInStartDate(year, month, day)
                AttendanceField.ON_TIME_END_DATE -> AdminActStudyScheduleAddEvent.UpdateOnTimeEndDate(year, month, day)
                AttendanceField.LATE_END_DATE -> AdminActStudyScheduleAddEvent.UpdateLateEndDate(year, month, day)
                else -> return@DatePickerDialog
            })
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showAttendanceTimePicker(field: AttendanceField) {
        val s = viewModel.uiState.value
        val cal = when (field) {
            AttendanceField.CHECK_IN_START_TIME -> s.checkInStartTime
            AttendanceField.ON_TIME_END_TIME -> s.onTimeEndTime
            AttendanceField.LATE_END_TIME -> s.lateEndTime
            else -> return
        }
        showTimePickerDialog(cal) { hour, minute ->
            viewModel.handleEvent(when (field) {
                AttendanceField.CHECK_IN_START_TIME -> AdminActStudyScheduleAddEvent.UpdateCheckInStartTime(hour, minute)
                AttendanceField.ON_TIME_END_TIME -> AdminActStudyScheduleAddEvent.UpdateOnTimeEndTime(hour, minute)
                AttendanceField.LATE_END_TIME -> AdminActStudyScheduleAddEvent.UpdateLateEndTime(hour, minute)
                else -> return@showTimePickerDialog
            })
        }
    }

    private fun showTimePickerDialog(cal: Calendar, onSet: (Int, Int) -> Unit) {
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val themeResId = if (isDarkMode) android.R.style.Theme_Holo_Dialog_NoActionBar
        else android.R.style.Theme_Holo_Light_Dialog_NoActionBar

        val dialog = TimePickerDialog(requireContext(), themeResId,
            { _, hour, minute -> onSet(hour, minute) },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

}