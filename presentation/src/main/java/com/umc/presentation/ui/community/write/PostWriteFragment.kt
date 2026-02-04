package com.umc.presentation.ui.community.write

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPostWriteBinding
import com.umc.presentation.ui.community.adapter.BottomSheetCategoryAdapter
import com.umc.presentation.ui.home.adapter.ShowCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostWriteFragment : BaseFragment<FragmentPostWriteBinding, PostWriteFragmentUiState, PostWriteFragmentEvent, PostWriteViewModel>(
    FragmentPostWriteBinding::inflate
) {
    override val viewModel: PostWriteViewModel by viewModels()

    //위치 정하는 지역 카테고리 어댑터
    private lateinit var categoryAdapter: ShowCategoryAdapter



    override fun initView() {
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
            writeTextfieldTime.setOnTextChangedListener { text ->
                viewModel.updateLightTime(text)
            }

            // 번개 장소 입력 리스너
            writeTextfieldPlace.setOnTextChangedListener { text ->
                viewModel.updateLightPlace(text)
            }

            //번개 장소 입력 리스너
            writeTextfieldPeople.setOnTextChangedListener { text ->
                viewModel.updateLightPeople(text)
            }

            //번개 옵챗 입력 리스너
            writeTextfieldOpenchat.setOnTextChangedListener { text ->
                viewModel.updateLightOpenChat(text)
            }




            // 등록 버튼 클릭
            writeTvComplete.setOnClickListener {
                viewModel.onClickRegister()
            }

        }

        //카테고리 어댑터 -> 클릭 시 변경하기
        /*
        categoryAdapter = ShowCategoryAdapter{ categoryItem ->
            viewModel.setCategory(categoryItem)
        }
        binding.writeRcv.adapter = categoryAdapter

         */


    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    /**카테고리 관련은 OUT**/
                //categoryAdapter.submitList(state.regionCategories)
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