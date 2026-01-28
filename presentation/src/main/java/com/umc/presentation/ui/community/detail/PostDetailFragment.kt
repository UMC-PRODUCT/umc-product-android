package com.umc.presentation.ui.community.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.viewModels
import com.umc.domain.model.mypage.CommentItem
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UWarningDialog
import com.umc.presentation.component.UWarningDialogModel
import com.umc.presentation.databinding.FragmentPostDetailBinding
import com.umc.presentation.databinding.LayoutMenuCommentBinding
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

    //RecyclerView에서 정의한 위임 내용
    //좋아요 버튼 클릭
    override fun onLikeClicked(item: ContentItem) {
        viewModel.toggleLike()
    }

    //스크랩 버튼 클릭
    override fun onScrapClicked(item: ContentItem) {
       viewModel.toggleScrap()
    }

    //댓글의 메뉴 클릭
    override fun onCommentMenuClicked(item: CommentItem) {
        val inflater = LayoutInflater.from(requireContext())
        val menuBinding = LayoutMenuCommentBinding.inflate(inflater)

        // PopupWindow 설정
        val popup = PopupWindow(
            menuBinding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true // 바깥쪽 터치 시 닫힘
        ).apply {
            elevation = 20f
        }

        // 내 댓글인지 여부에 따른 가시성 조절 (임시 로직)
        val isMyComment = item.username == "새 유저"
        menuBinding.layoutMenuReport.visibility = if (isMyComment) View.GONE else View.VISIBLE
        menuBinding.layoutMenuDelete.visibility = if (isMyComment) View.VISIBLE else View.GONE

        // 메뉴 클릭 이벤트
        // 신고
        menuBinding.layoutMenuReport.setOnClickListener {
            popup.dismiss()
            // 아까 만든 신고 다이얼로그 띄우기
            handleEvent(PostDetailFragmentEvent.ReportComment)
        }

        // 삭제
        menuBinding.layoutMenuDelete.setOnClickListener {
            popup.dismiss()
            // 삭제 로직
            viewModel.onClickeDeleteComment(item)

        }

        // 버튼 위치를 기준으로 띄우기 (약간의 오프셋 조정 가능)
        val anchor = binding.postdetailRcv.findViewHolderForAdapterPosition(
            postDetailAdapter.currentList.indexOf(PostDetailItem.Comment(item))
        )?.itemView?.findViewById<View>(R.id.item_btn_menu)

        anchor?.let {
            popup.showAsDropDown(it, -250, +25) // 버튼 왼쪽 아래로 정렬
        }
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
                    
                    //애니메이션
                    handleKebabAnimation(state.isMenuVisible)


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

        when(event){
            is PostDetailFragmentEvent.OnClickCommentAdd -> {
                //String을 받아서 뷰모델에 넘기기
                val commentText = binding.postdetailEdtComment.getText()
                binding.postdetailEdtComment.setText("")
                viewModel.addComment(commentText)

            }

            //게시글 신고 누를 경우
            is PostDetailFragmentEvent.ReportPost -> {
                //1. 게시글에 넣을 data class 정의
                val reportModel = UWarningDialogModel(
                    title = "해당 글을 신고하시겠습니까?",
                    positiveText = "신고하기",
                    negativeText = "취소"
                )
                //2. dialog inflate
                UWarningDialog(
                    model = reportModel,
                    onConfirm = {
                        // TODO: 서버에 신고 API 호출하는 뷰모델 함수 연결


                    }
                ).show(childFragmentManager, "ReportDialog")
            }

            //댓글 신고
            is PostDetailFragmentEvent.ReportComment -> {
                //1. 게시글에 넣을 data class 정의
                val reportModel = UWarningDialogModel(
                    title = "해당 댓글을 신고하시겠습니까?",
                    positiveText = "신고하기",
                    negativeText = "취소"
                )
                //2. dialog inflate
                UWarningDialog(
                    model = reportModel,
                    onConfirm = {
                        // TODO: 서버에 신고 API 호출하는 뷰모델 함수 연결


                    }
                ).show(childFragmentManager, "ReportDialog")
            }

            //뒤로가기
            is PostDetailFragmentEvent.MoveBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }


            else -> {}
        }
    }




    private fun handleKebabAnimation(isVisible: Boolean) {
        binding.postdetailLayoutKebabMenu.apply {
            if (isVisible) {
                //위에서 아래로 내려오며 나타남
                visibility = View.VISIBLE
                alpha = 0f
                translationY = -30f
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(250L)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            } else {
                //위로 올라가며 사라짐
                animate()
                    .alpha(0f)
                    .translationY(-30f)
                    .setDuration(200L)
                    .withEndAction { visibility = View.GONE }
                    .start()
            }
        }
    }

}