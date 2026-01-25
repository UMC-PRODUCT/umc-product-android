package com.umc.presentation.ui.notice.search.result

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.Notice
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeSearchResultBinding
import com.umc.presentation.ui.notice.adapter.NoticeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeSearchResultFragment : BaseFragment<FragmentNoticeSearchResultBinding, NoticeSearchResultUiState, NoticeSearchResultEvent, NoticeSearchResultViewModel>(
    FragmentNoticeSearchResultBinding::inflate,
) {
    override val viewModel: NoticeSearchResultViewModel by viewModels()

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

            recyclerRecentSearch.apply {
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

                }
            }
        }
    }

    override fun handleEvent(event: NoticeSearchResultEvent) {
        super.handleEvent(event)
    }
}
