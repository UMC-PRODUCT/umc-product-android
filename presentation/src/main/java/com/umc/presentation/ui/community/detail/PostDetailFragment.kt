package com.umc.presentation.ui.community.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.domain.model.mypage.CommentItem
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPostDetailBinding
import com.umc.presentation.ui.community.adapter.PostDetailAdapter
import com.umc.presentation.ui.community.adapter.PostItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostDetailFragment : BaseFragment<FragmentPostDetailBinding, PostDetailFragmentUiState, PostDetailFragmentEvent, PostDetailViewModel>(
    FragmentPostDetailBinding::inflate
), PostItemDelegate {

    override val viewModel: PostDetailViewModel by viewModels()

    private lateinit var postDetailAdapter : PostDetailAdapter

    override fun onLikeClicked(item: ContentItem) {
        viewModel.toggleLike()
    }

    override fun onScrapClicked(item: ContentItem) {
       viewModel.toggleScrap()
    }

    override fun onCommentMenuClicked(item: CommentItem) {
        /**TODO 일단 임시로**/
    }


    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        //어댑터 정의
        postDetailAdapter = PostDetailAdapter(this)
        binding.postdetailRcv.apply {
            adapter = postDetailAdapter
        }


    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    //어댑터에 새 값 연결
                    val isNewCommentAdded = state.nowDetailList.size > postDetailAdapter.itemCount
                    postDetailAdapter.submitList(state.nowDetailList) {
                        // 리스트가 업데이트된 후, 새 댓글이 추가된 상황이라면 맨 아래로 스크롤
                        if (isNewCommentAdded) {
                            binding.postdetailRcv.smoothScrollToPosition(postDetailAdapter.itemCount - 1)
                        }
                    }


                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }



    }

    override fun handleEvent(event: PostDetailFragmentEvent) {
        super.handleEvent(event)
    }

}