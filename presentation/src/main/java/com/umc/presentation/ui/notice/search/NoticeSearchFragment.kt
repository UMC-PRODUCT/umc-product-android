package com.umc.presentation.ui.notice.search

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.Notice
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeBinding
import com.umc.presentation.databinding.FragmentNoticeSearchBinding
import com.umc.presentation.ui.notice.adapter.NoticeAdapter
import com.umc.presentation.ui.notice.adapter.NoticeChipAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeSearchFragment : BaseFragment<FragmentNoticeSearchBinding, NoticeSearchUiState, NoticeSearchEvent, NoticeSearchViewModel>(
    FragmentNoticeSearchBinding::inflate,
) {
    override val viewModel: NoticeSearchViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect {
                    handleEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                }
            }
        }
    }

    override fun handleEvent(event: NoticeSearchEvent) {
        super.handleEvent(event)
    }
}
