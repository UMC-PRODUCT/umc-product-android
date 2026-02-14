package com.umc.presentation.ui.notice.search.result

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.Notice
import com.umc.domain.model.notice.NoticeSummary
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeSearchResultBinding
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
    private val noticeAdapter : NoticeAdapter by lazy {
        NoticeAdapter(object : NoticeAdapter.NoticeDelegate {
            override fun onClickNotice(item: NoticeSummary) {
                //TODO 클릭 시 상세 페이지
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            viewModel.updateSearchText(args.search)

            recyclerSearchResult.apply {
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
                    //noticeAdapter.submitList(it.noticeList)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeSearchResultEvent) {
        when (event) {
            NoticeSearchResultEvent.MoveToBack -> findNavController().popBackStack()
        }
    }
}
