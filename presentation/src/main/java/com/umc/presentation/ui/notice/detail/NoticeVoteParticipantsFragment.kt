package com.umc.presentation.ui.notice.detail

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeVoteParticipantsBinding
import com.umc.presentation.ui.notice.detail.adapter.NoticeVoteParticipantsSectionAdapter
import com.umc.presentation.ui.notice.detail.adapter.VoteParticipantListItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoticeVoteParticipantsFragment :
    BaseFragment<FragmentNoticeVoteParticipantsBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeDetailViewModel>(
        FragmentNoticeVoteParticipantsBinding::inflate
    ) {

    override val viewModel: NoticeDetailViewModel by activityViewModels()

    private val adapter: NoticeVoteParticipantsSectionAdapter by lazy {
        NoticeVoteParticipantsSectionAdapter()
    }

    override fun initView() {
        binding.imageBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == NoticeVoteParticipantsSectionAdapter.TYPE_HEADER) 2 else 1
                }
            }
        }

        binding.recyclerParticipants.layoutManager = gridLayoutManager
        binding.recyclerParticipants.adapter = adapter

        submitSections(viewModel.uiState.value.voteParticipantSections)
    }

    override fun initStates() {
        super.initStates()
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.uiState.collect { state ->
                submitSections(state.voteParticipantSections)
            }
        }
    }

    private fun submitSections(sections: List<VoteOptionParticipants>) {
        val items = sections.flatMap { section ->
            listOf(
                VoteParticipantListItem.Header(
                    optionId = section.optionId,
                    title = section.optionTitle,
                    count = section.participants.size
                )
            ) + section.participants.map {
                VoteParticipantListItem.Participant(
                    optionId = section.optionId,
                    member = it
                )
            }
        }
        adapter.submitList(items)
    }
}
