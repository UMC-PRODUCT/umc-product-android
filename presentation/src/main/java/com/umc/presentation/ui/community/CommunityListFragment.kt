package com.umc.presentation.ui.community

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.community.ContentItem
import com.umc.domain.model.enums.ContentType
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentCommunityListBinding
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CommunityListFragment : BaseFragment<FragmentCommunityListBinding, CommunityListFragmentUiState, CommunityListFragmentEvent, CommunityListViewModel>(
    FragmentCommunityListBinding::inflate
), ContentItemDelegate {

    override val viewModel: CommunityListViewModel by viewModels()
    private lateinit var contentAdapter: ContentAdapter

    override fun onItemClicked(item: ContentItem) {
        /**TODO. 이동 로직 작성하기**/

        val action = CommunityFragmentDirections.actionCommunityToPostDetail(
            postId = item.postId
        )
        findNavController().navigate(action)
    }

    override fun initView() {
        binding.apply {
            vm = viewModel
        }

        contentAdapter = ContentAdapter(this)

        val type = arguments?.getSerializable("TYPE") as? ContentType ?: ContentType.ALL
        viewModel.setNowTab(type)

        binding.communityRcv.apply {
            adapter = contentAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (!viewModel.uiState.value.isPageLoading &&
                        layoutManager.findLastCompletelyVisibleItemPosition() >= layoutManager.itemCount - 2) {
                        viewModel.fetchPosts(isRefresh = false)
                    }
                }
            })
        }

    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    contentAdapter.submitList(state.nowContents)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: CommunityListFragmentEvent) {
        super.handleEvent(event)
        when (event) {
            is CommunityListFragmentEvent.NavigateWrite -> {
                //전역 action을 이동
                val action = CommunityFragmentDirections.actionCommunityToPostWrite(
                    postId = -1L
                )
                findNavController().navigate(action)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // 다른 화면에서 돌아올 때마다 최신 데이터로 새로고침
        viewModel.fetchPosts(isRefresh = true)
    }

    //탭 이동 시 체크를 위한 인자
    companion object {
        fun newInstance(type: ContentType) = CommunityListFragment().apply {
            arguments = Bundle().apply { putSerializable("TYPE", type) }
        }
    }

}