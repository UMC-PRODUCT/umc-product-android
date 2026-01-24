package com.umc.presentation.ui.home

import androidx.fragment.app.viewModels
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeBinding
import com.umc.presentation.databinding.FragmentNoticeDetailBinding
import dagger.hilt.android.AndroidEntryPoint


//공지사항 내용
@AndroidEntryPoint
class NoticeDetailFragment : BaseFragment<FragmentNoticeDetailBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeDetailViewModel>(
    FragmentNoticeDetailBinding::inflate,
) {
    override val viewModel: NoticeDetailViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initStates() {
        super.initStates()
    }

    private fun moveBackPressed(){
        requireActivity().onBackPressedDispatcher.onBackPressed()

    }

    //이벤트 핸들링(얘를 observing 해서 쭈욱 보고 이벤트 처리)
    private fun handleMoveEvent(event: NoticeFragmentEvent){
        when (event){
            is NoticeFragmentEvent.MoveBackPressedEvent -> moveBackPressed()


            else -> {}
        }
    }


}