package com.umc.presentation.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.opencsv.CSVReader
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPlanAddBinding
import java.io.InputStreamReader
import java.util.Calendar

class PlanAddFragment : BaseFragment<FragmentPlanAddBinding, PlanAddFragmentUiState, PlanAddFragmentEvent, PlanAddViewModel>(
    FragmentPlanAddBinding::inflate,
) {
    override val viewModel: PlanAddViewModel by viewModels()

    //csv 파일 처리를 위한 런처
    private val csvPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        uri: Uri? -> uri?.let {
            //돌아오고 난 뒤, 아래의 처리로직을 수행
            parseCsvFile(it)
        }
    }

    override fun initView() {
        binding.apply {
            //onclick 달기
            //시작 날짜/시간
            plandetailCdvStartDate.setOnClickListener { showDatePicker(true) }
            plandetailCdvStartTime.setOnClickListener { showTimePicker(true) }
            //종료 날짜/시간
            plandetailCdvEndDate.setOnClickListener { showDatePicker(false) }
            plandetailCdvEndTime.setOnClickListener { showTimePicker(false) }

            //csv 선택
            planaddBtnUploadCsv.setOnClickListener {

                // 파일 타입을 설정
                val mimeTypes = arrayOf(
                    "text/csv", //CSV 형식
                    "text/comma-separated-values", //쉼표로 분리된 형식
                    "text/plain",               // 일반 텍스트
                    "application/vnd.ms-excel", // 엑셀에서 만든 CSV
                )
                csvPickerLauncher.launch(mimeTypes)
            }

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

    //CSV 파일 파싱
    private fun parseCsvFile(uri: Uri){
        //이름 정보를 임시로 담을 곳
        val names = mutableListOf<String>()

        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = InputStreamReader(inputStream, "UTF-8")
                val csvReader = CSVReader(reader)

                /**TODO CSV 파일 형식은 추후 물어봐서 수정해야 할수도**/
                // 첫 줄은 건너뛰기 (목차 부분)
                val header = csvReader.readNext()

                //1. 그냥 특정 번째 열을 뒤진다.
                /*
                var nextLine: Array<String>?
                while (csvReader.readNext().also { nextLine = it } != null) {
                    // 첫 번째 열에 이름이 있다고 가정하고 추출한다. (후에 따라 다르게 변경)
                    nextLine?.getOrNull(0)?.let { name ->
                        if (name.isNotBlank()) {
                            names.add(name.trim())
                        }
                    }
                    }
                }
                */

                //2. header에서 뜯어봐서 '이름'이 있는지 확인한다.
                Log.d("log_home", header.contentToString())
                val nameColumnIndex = header?.indexOfFirst { it.trim().contains("이름") } ?: -1
                if(nameColumnIndex == -1){
                    Toast.makeText(requireContext(), "이름 정보가 없는 파일입니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                //그 후, 파싱한다.
                var nextLine: Array<String>?
                while (csvReader.readNext().also { nextLine = it } != null) {
                    nextLine?.getOrNull(nameColumnIndex)?.let { name ->
                        if (name.isNotBlank()) {
                            names.add(name.trim())
                        }
                    }
                }


            }
            // 추출된 이름 리스트를 뷰모델로 전송
            val event = PlanAddFragmentEvent.UpdateParticipants(names)
            Log.d("log_home", "이름 리스트: $names")
            viewModel.processParticipants(event)
            
            // 임시 토스트
            Toast.makeText(requireContext(), "${names.size}명의 명단을 불러왔습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            
            // 임시 토스트
            Toast.makeText(requireContext(), "CSV 파일을 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    //날짜 다이얼로그 호출 (날짜)
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
            val event = if (isStart) PlanAddFragmentEvent.UpdateStartTime(hour, minute)
            else PlanAddFragmentEvent.UpdateEndTime(hour, minute)
            viewModel.processDateTime(event)
        },
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


}