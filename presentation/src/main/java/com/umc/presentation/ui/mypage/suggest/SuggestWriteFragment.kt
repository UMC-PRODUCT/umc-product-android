package com.umc.presentation.ui.mypage.suggest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentSuggestWriteBinding
import com.umc.presentation.ui.home.adapter.ShowCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SuggestWriteFragment : BaseFragment<FragmentSuggestWriteBinding, SuggestWriteFragmentUiState, SuggestWriteFragmentEvent, SuggestWriteViewModel>(
    FragmentSuggestWriteBinding::inflate,
) {

    override val viewModel : SuggestWriteViewModel by viewModels()
    private lateinit var categoryAdapter: ShowCategoryAdapter


    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner

            // 제목 입력 리스너
            sugwriteTextfieldTitle.addTextChangedListener { text ->
                viewModel.updateTitle(text.toString())
            }

            // 본문 입력 리스너 (ID가 planadd_...로 되어 있으니 확인 필요!)
            planaddTextfieldPlanDetail.addTextChangedListener { text ->
                viewModel.updateContent(text.toString())
            }

            // 등록 버튼 클릭
            sugwriteTvComplete.setOnClickListener {
                viewModel.onClickRegister()
            }

        }

        //여기서 바뀔 때 호출
        binding.sugwriteSwitchAnomy.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAnomy(isChecked)
        }

        //카테고리 어댑터 -> 클릭 시 변경하기
        categoryAdapter = ShowCategoryAdapter{ categoryItem ->
            viewModel.setCategory(categoryItem)
        }
        binding.sugwriteRcv.adapter = categoryAdapter

    }


    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    categoryAdapter.submitList(state.categories)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: SuggestWriteFragmentEvent) {
        super.handleEvent(event)

        when(event) {
            is SuggestWriteFragmentEvent.ClickBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            else -> {}
        }
    }
}