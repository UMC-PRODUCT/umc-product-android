package com.umc.presentation.ui.notice.detail

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeDetailBinding
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailImageAdapter
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailVoteAdapter
import com.umc.presentation.ui.notice.detail.bottomsheet.NoticeConfirmBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeDetailFragment :
    BaseFragment<FragmentNoticeDetailBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeDetailViewModel>(
        FragmentNoticeDetailBinding::inflate,
    ) {
    override val viewModel: NoticeDetailViewModel by activityViewModels()

    private val args: NoticeDetailFragmentArgs by navArgs()

    private val noticeDetailVoteAdapter: NoticeDetailVoteAdapter by lazy {
        NoticeDetailVoteAdapter(object : NoticeDetailVoteAdapter.NoticeDetailVoteDelegate {
            override fun onClickVote(item: NoticeVoteOption) {
                viewModel.onClickVoteItem(item)
            }
        })
    }

    private val noticeDetailImageAdapter: NoticeDetailImageAdapter by lazy {
        NoticeDetailImageAdapter()
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            viewModel.init(args.noticeId)

            recyclerVote.apply {
                adapter = noticeDetailVoteAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }

            recyclerImages.apply {
                adapter = noticeDetailImageAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
                viewModel.uiState.collect { state ->
                    state.detail.vote?.options?.let { options ->
                        noticeDetailVoteAdapter.submitList(options)
                    }
                    noticeDetailVoteAdapter.setSelectedOptionIds(state.selectedVoteOptionIds.toSet())
                    val isVoted = state.detail.vote?.mySelectedOptionIds?.isNotEmpty() == true
                    noticeDetailVoteAdapter.setVotedState(isVoted, state.detail.vote)
                    noticeDetailImageAdapter.submitList(state.detail.images)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeFragmentEvent) {
        when (event) {
            NoticeFragmentEvent.MoveBackPressedEvent -> findNavController().popBackStack()
            NoticeFragmentEvent.ShowBottomSheetEvent -> showBottomSheet()
            is NoticeFragmentEvent.MoveToEditPostEvent -> {
                moveToEditPost(event.noticeId)
            }
            is NoticeFragmentEvent.ShowError -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
            is NoticeFragmentEvent.ShowSuccess -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheet = NoticeConfirmBottomSheet()
        bottomSheet.show(parentFragmentManager, "")
    }

    private fun moveToEditPost(noticeId: Long) {
        val action = NoticeDetailFragmentDirections
            .actionNoticeDetailFragmentToNoticeWriteFragment(
                noticeId = noticeId,
                isEditMode = true
            )
        findNavController().navigate(action)
    }


}