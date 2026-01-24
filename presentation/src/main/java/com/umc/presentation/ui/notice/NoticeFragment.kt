package com.umc.presentation.ui.notice

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.Notice
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeBinding
import com.umc.presentation.ui.home.NoticeFragmentEvent
import com.umc.presentation.ui.home.NoticeFragmentUiState
import com.umc.presentation.ui.home.NoticeDetailViewModel
import com.umc.presentation.ui.notice.adapter.NoticeAdapter
import com.umc.presentation.ui.notice.adapter.NoticeChipAdapter
import com.umc.presentation.util.ULog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeUiState, NoticeEvent, NoticeViewModel>(
    FragmentNoticeBinding::inflate,
) {
    override val viewModel: NoticeViewModel by viewModels()

    private val noticeChipAdapter : NoticeChipAdapter by lazy {
        NoticeChipAdapter(object : NoticeChipAdapter.NoticeChipDelegate {
            override fun onClickChip(item: NoticeChipState) {
                viewModel.onClickChip(item)
            }
        })
    }

    private val noticeAdapter : NoticeAdapter by lazy {
        NoticeAdapter(object : NoticeAdapter.NoticeDelegate {
            override fun onClickNotice(item: Notice) {
                //TODO 클릭 시 상세 페이지
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            textNoticeTitle.text = getString(R.string.notice_title, 12)

            recyclerTag.apply {
                adapter = noticeChipAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
            }

            recyclerNotice.apply {
                adapter = noticeAdapter
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
                    noticeChipAdapter.submitList(it.chipList)
                    noticeAdapter.submitList(it.noticeList)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeEvent) {
        super.handleEvent(event)
    }
}
