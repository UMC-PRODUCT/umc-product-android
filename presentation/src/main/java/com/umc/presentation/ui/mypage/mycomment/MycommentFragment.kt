package com.umc.presentation.ui.mypage.mycomment

import androidx.fragment.app.viewModels
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentMycommentBinding
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class MycommentFragment : BaseFragment<FragmentMycommentBinding, MycommentFragmentUiState, MycommentFragmentEvent, MycommentViewModel>(
FragmentMycommentBinding::inflate,
), ContentItemDelegate {

    override val viewModel : MycommentViewModel by viewModels()

    private lateinit var myContentAdapter : ContentAdapter

    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //어댑터 정의 및 연결
        myContentAdapter = ContentAdapter(this)
        binding.mycommentRcv.apply {
            adapter = myContentAdapter
        }

    }


    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    myContentAdapter.submitList(state.tmpData)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: MycommentFragmentEvent) {
        super.handleEvent(event)

        when(event){
            is MycommentFragmentEvent.ClickBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

    }


}