package com.umc.presentation.ui.notice.search.result

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.NoticeSummary
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeSearchResultBinding
import com.umc.presentation.extension.addInfiniteScrollListener
import com.umc.presentation.ui.notice.adapter.NoticeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class NoticeSearchResultFragment : BaseFragment<FragmentNoticeSearchResultBinding, NoticeSearchResultUiState, NoticeSearchResultEvent, NoticeSearchResultViewModel>(
    FragmentNoticeSearchResultBinding::inflate,
) {
    override val viewModel: NoticeSearchResultViewModel by viewModels()
    private val args: NoticeSearchResultFragmentArgs by navArgs()

    private val noticeAdapter: NoticeAdapter by lazy {
        NoticeAdapter(object : NoticeAdapter.NoticeDelegate {
            override fun onClickNotice(item: NoticeSummary) {
                viewModel.onClickNotice(item.id)
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            recyclerSearchResult.apply {
                adapter = noticeAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null

                addInfiniteScrollListener {
                    viewModel.loadNextPage()
                }
            }
        }

        viewModel.initSearch(args.search, args.gisuId)
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
                    noticeAdapter.submitList(state.noticeList)
                    noticeAdapter.setReadNoticeIds(state.readNoticeIds)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeSearchResultEvent) {
        when (event) {
            NoticeSearchResultEvent.MoveToBack -> findNavController().popBackStack()
            is NoticeSearchResultEvent.MoveToNoticeDetail -> {
                // 공지사항 상세 페이지로 이동
                val action = NoticeSearchResultFragmentDirections
                    .actionNoticeSearchResultFragmentToNoticeDetailFragment(event.noticeId)
                findNavController().navigate(action)
            }
        }
    }
}
