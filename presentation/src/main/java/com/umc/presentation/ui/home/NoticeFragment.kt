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
class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeFragmentViewModel>(
    FragmentNoticeBinding::inflate,
) {
    override val viewModel: NoticeFragmentViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initStates() {
        super.initStates()
    }


}