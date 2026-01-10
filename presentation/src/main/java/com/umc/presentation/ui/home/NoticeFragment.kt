package com.umc.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeBinding


//공지사항 내용
class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeViewModel>(
    FragmentNoticeBinding::inflate,
) {
    override val viewModel: NoticeViewModel by viewModels()

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