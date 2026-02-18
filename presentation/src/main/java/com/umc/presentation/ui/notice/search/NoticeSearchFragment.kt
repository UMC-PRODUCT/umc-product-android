package com.umc.presentation.ui.notice.search

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeSearchBinding
import com.umc.presentation.ui.notice.search.adapter.RecentSearchAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class NoticeSearchFragment : BaseFragment<FragmentNoticeSearchBinding, NoticeSearchUiState, NoticeSearchEvent, NoticeSearchViewModel>(
    FragmentNoticeSearchBinding::inflate,
) {
    override val viewModel: NoticeSearchViewModel by viewModels()

    private val args: NoticeSearchFragmentArgs by navArgs()

    private val recentSearchAdapter : RecentSearchAdapter by lazy {
        RecentSearchAdapter(object : RecentSearchAdapter.RecentSearchDelegate {
            override fun onClickItem(text: String) {
                viewModel.selectRecentSearch(text)
            }

            override fun onClickDelete(text: String) {
                viewModel.deleteRecentSearch(text)
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            recyclerRecentSearch.apply {
                adapter = recentSearchAdapter
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
                    recentSearchAdapter.submitList(it.recentSearchList)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeSearchEvent) {
        when (event) {
            NoticeSearchEvent.MoveToBack -> findNavController().popBackStack()
            is NoticeSearchEvent.MoveToSearchResult -> moveToSearchResult(event.search)
        }
    }

    private fun moveToSearchResult(search: String) {
        val action = NoticeSearchFragmentDirections.actionNoticeSearchFragmentToNoticeSearchResultFragment(search, args.gisuId)
        findNavController().navigate(action)
    }
}
