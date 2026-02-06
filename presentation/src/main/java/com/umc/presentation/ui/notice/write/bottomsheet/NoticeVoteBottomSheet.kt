package com.umc.presentation.ui.notice.write.bottomsheet

import android.content.DialogInterface
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.R
import com.umc.presentation.base.BaseBottomSheetFragment
import com.umc.presentation.databinding.BottomSheetNoticeVoteBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.ui.notice.write.NoticeWriteEvent
import com.umc.presentation.ui.notice.write.NoticeWriteUiState
import com.umc.presentation.ui.notice.write.NoticeWriteViewModel
import com.umc.presentation.ui.notice.write.adapter.NoticeVoteAdapter
import com.umc.presentation.util.ULog
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.text.isNotEmpty

class NoticeVoteBottomSheet: BaseBottomSheetFragment<BottomSheetNoticeVoteBinding, NoticeWriteUiState, NoticeWriteEvent, NoticeWriteViewModel>(
    BottomSheetNoticeVoteBinding::inflate,
) {

    private val currentVoteList = MutableList(5) { "" }

    override val viewModel: NoticeWriteViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private val noticeVoteAdapter : NoticeVoteAdapter by lazy {
        NoticeVoteAdapter(object : NoticeVoteAdapter.NoticeVoteDelegate {
            override fun onTextChanged(position: Int, text: String) {
                currentVoteList[position] = text
                checkEnable()
            }

            override fun onClickDelete(position: Int) {
                currentVoteList[position] = ""
                viewModel.onVoteDelete(position)
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            updateCurrentList(viewModel.uiState.value.voteTextList)
            checkEnable()

            recyclerVote.apply {
                adapter = noticeVoteAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }

            ubuttonComplete.setOnClickListener {
                val resultList = currentVoteList.filter { it.isNotEmpty() }.toMutableList()
                while (resultList.size < 2) {
                    resultList.add("")
                }
                viewModel.updateVoteList(resultList)
                dismiss()
            }
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect {
                    if (it.voteTextList.size == 5) binding.ubuttonAddVote.gone()
                    noticeVoteAdapter.submitList(it.voteTextList)
                }
            }
        }
    }

    private fun updateCurrentList(list: List<String>) {
        list.forEachIndexed { index, vote ->
            currentVoteList[index] = vote
        }
    }

    private fun checkEnable() {
        val validItems = currentVoteList.filter { it.isNotBlank() }
        val isEnabled = validItems.size >= 2
        val enableColor = ContextCompat.getColor(requireActivity(), R.color.primary500)
        val disableColor = ContextCompat.getColor(requireActivity(), R.color.neutral300)
        binding.ubuttonComplete.isEnabled = isEnabled
        binding.ubuttonComplete.setUBackgroundColor(if (isEnabled) enableColor else disableColor)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}