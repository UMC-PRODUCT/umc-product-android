package com.umc.presentation.ui.community.write

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPostWriteBinding
import com.umc.presentation.ui.community.adapter.BottomSheetCategoryAdapter
import com.umc.presentation.ui.community.detail.PostDetailFragmentArgs
import com.umc.presentation.ui.home.adapter.ShowCategoryAdapter
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class PostWriteFragment : BaseFragment<FragmentPostWriteBinding, PostWriteFragmentUiState, PostWriteFragmentEvent, PostWriteViewModel>(
    FragmentPostWriteBinding::inflate
) {
    override val viewModel: PostWriteViewModel by viewModels()

    private val args: PostWriteFragmentArgs by navArgs()

    private var postId : Long = -1L

    override fun initView() {

        //postId를 비교하는 로직 (만약 유효하면 이는 게시글 작성이 아닌 수정으로 판단)
        postId = args.postId
        if(postId != -1L){
            viewModel.settingUpdatePost(postId)
        }

        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            // 제목 입력 리스너
            writeTextfieldTitle.addTextChangedListener { text ->
                viewModel.updateTitle(text.toString())
            }

            // 본문 입력 리스너 (ID가 planadd_...로 되어 있으니 확인 필요!)
            writeTextfieldContent.addTextChangedListener { text ->
                viewModel.updateContent(text.toString())
            }

            // 번개 날짜 입력 리스너
            writeTextfieldTime.setOnClickListener {
                val cal = Calendar.getInstance()

                // 다크모드에 따른 테마 설정
                val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                val themeResId = if (isDarkMode) {
                    android.R.style.Theme_Holo_Dialog_NoActionBar
                } else {
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar
                }

                // 날짜 선택 다이얼로그 (DatePickerDialog)
                val dateDialog = DatePickerDialog(requireContext(),{ _, y, m, d ->


                    val displayDate = String.format("%02d.%02d.%02d", y % 100, m, d) //UI 보여주기용 date
                    val selectedDate = String.format("%d-%02d-%02d", y, m + 1, d)

                    // 날짜 선택 완료 후 바로 시간 선택 다이얼로그 띄우기
                    val timeDialog = TimePickerDialog(requireContext(), themeResId, { _, hour, minute ->
                        val selectedTime = String.format("%02d:%02d", hour, minute)

                        // UI 표시 (사용자용)
                        binding.writeTextfieldTime.setText("$displayDate $selectedTime")

                        // 서버 전송용 ISO 8601 조립 (예: 2026-02-08T10:02:00.000Z)
                        val isoDateTime = "${selectedDate}T${selectedTime}:00.000Z"

                        // 뷰모델 업데이트
                        viewModel.updateLightTime(isoDateTime)

                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)

                    timeDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    timeDialog.show()

                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                dateDialog.show()
            }

            // 번개 장소 입력 리스너
            writeTextfieldPlace.setOnTextChangedListener { text ->
                viewModel.updateLightPlace(text)
            }

            //번개 인원 수 입력 리스너
            writeTextfieldPeople.setOnTextChangedListener { text ->
                viewModel.updateLightPeople(text)
            }

            //번개 옵챗 입력 리스너
            writeTextfieldOpenchat.setOnTextChangedListener { text ->
                viewModel.updateLightOpenChat(text)
            }


        }



    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    /**카테고리 관련은 OUT**/
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: PostWriteFragmentEvent) {
        super.handleEvent(event)

        when(event) {
            is PostWriteFragmentEvent.ClickBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            //카테고리 선택 시
            is PostWriteFragmentEvent.ClickCategorySelect -> {
                // 카테고리 선택 다이얼로그 띄우기
                settingBottomSheetDialog()
            }

            //textfield 채우기일때
            is PostWriteFragmentEvent.SetTextfields -> {
                binding.writeTextfieldTitle.setText(viewModel.uiState.value.title)
                binding.writeTextfieldContent.setText(viewModel.uiState.value.content)
                binding.writeTextfieldTime.setText(viewModel.uiState.value.lightTime)
                binding.writeTextfieldPlace.setText(viewModel.uiState.value.lightPlace)
                binding.writeTextfieldPeople.setText(viewModel.uiState.value.lightPeople)
                binding.writeTextfieldOpenchat.setText(viewModel.uiState.value.lightOpenChat)
            }

            //Error toast make
            is PostWriteFragmentEvent.MakeErrorTaost -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    private fun settingBottomSheetDialog(){
        // BottomSheet 생성 및 클릭 리스너 연결
        val bottomSheet = CategoryBottomSheetDialog { selectedCategory ->
            // BottomSheet 어댑터에서 클릭된 CategoryType을 뷰모델로 전달
            viewModel.updateContentCategory(selectedCategory)
        }
        // 3. 화면에 표시
        bottomSheet.show(childFragmentManager, "CategoryBottomSheet")
    }

}