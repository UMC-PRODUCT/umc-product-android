package com.umc.presentation.ui.notice.detail

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.VoteItem
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeDetailBinding
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailVoteAdapter
import com.umc.presentation.ui.notice.detail.bottomsheet.NoticeConfirmBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeDetailFragment :
    BaseFragment<FragmentNoticeDetailBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeDetailViewModel>(
        FragmentNoticeDetailBinding::inflate,
    ) {
    override val viewModel: NoticeDetailViewModel by viewModels()

    private val noticeDetailVoteAdapter: NoticeDetailVoteAdapter by lazy {
        NoticeDetailVoteAdapter(object : NoticeDetailVoteAdapter.NoticeDetailVoteDelegate {
            override fun onClickVote(item: VoteItem) {
                viewModel.onClickVoteItem(item)
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            recyclerVote.apply {
                adapter = noticeDetailVoteAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }
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
                    noticeDetailVoteAdapter.submitList(it.detail.vote.item)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeFragmentEvent) {
        when (event) {
            NoticeFragmentEvent.MoveBackPressedEvent -> findNavController().popBackStack()
            NoticeFragmentEvent.ShowBottomSheetEvent -> showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val bottomSheet = NoticeConfirmBottomSheet()
        bottomSheet.show(parentFragmentManager, "")
    }


}