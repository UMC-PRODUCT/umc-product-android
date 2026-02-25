package com.umc.presentation.ui.community.detail

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.umc.domain.model.community.CommentItem
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import com.umc.presentation.databinding.FragmentPostDetailBinding
import com.umc.presentation.databinding.LayoutMenuCommentBinding
import com.umc.presentation.ui.community.CommunityFragmentDirections
import com.umc.presentation.ui.community.adapter.PostDetailAdapter
import com.umc.presentation.ui.community.adapter.PostItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostDetailFragment : BaseFragment<FragmentPostDetailBinding, PostDetailFragmentUiState, PostDetailFragmentEvent, PostDetailViewModel>(
    FragmentPostDetailBinding::inflate
), PostItemDelegate {

    override val viewModel: PostDetailViewModel by viewModels()

    private val args: PostDetailFragmentArgs by navArgs()
    private var postId : Long = -1L
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
        //권한 조회하러 가기
        viewModel.onCommentMenuClicked(item)
    }

    //번개글 옵챗 터치 시
    override fun onOpenChatClicked(url: String) {
        openWebpage(url)
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

            //애니메이션 끄기
            (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false

        }

        //일정 화면에서 게시글 id 가져오기
        postId = args.postId
        if (postId != -1L) {
            viewModel.initPostDetailData(postId)
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
                val reportModel = UBasicDialogModel.Warning(
                    title = "해당 글을 신고하시겠습니까?",
                    positiveText = "신고하기"
                )

                UBasicDialog(
                    model = reportModel,
                    onConfirm = {
                        //서버에 신고
                        viewModel.reportPost()
                    }
                ).show(childFragmentManager, "ReportDialog")
            }

            //게시글 삭제를 누른 경우
            is PostDetailFragmentEvent.DeletePost -> {
                val reportModel = UBasicDialogModel.Warning(
                    title = "해당 글을 삭제하시겠습니까",
                    content = "삭제된 글은 복구할 수 없습니다.",
                    positiveText = "삭제하기"
                )

                UBasicDialog(
                    model = reportModel,
                    onConfirm = {
                        viewModel.deletePost()
                    }
                ).show(childFragmentManager, "ReportDialog")
            }

            //게시글 수정을 누른 경우
            is PostDetailFragmentEvent.EditPost -> {
                //게시글 작성 페이지로 이동하되, id를 주기
                val action = PostDetailFragmentDirections.actionPostDetailToPostWrite(
                    postId = postId
                )
                findNavController().navigate(action)
            }

            //댓글 신고
            is PostDetailFragmentEvent.ReportComment -> {
                val reportModel = UBasicDialogModel.Warning(
                    title = "해당 댓글을 신고하시겠습니까?",
                    positiveText = "신고하기"
                )

                UBasicDialog(
                    model = reportModel,
                    onConfirm = {
                        // 서버에 신고
                        viewModel.reportComment(event.commentId)
                    }
                ).show(childFragmentManager, "ReportDialog")
            }

            //댓글 메뉴 보여주기(권한 바탕으로)
            is PostDetailFragmentEvent.ShowCommentMenu -> {
                showCommentPopupMenu(event.item, event.canDelete)
            }


            //에러 토스트 출력
            is PostDetailFragmentEvent.ShowErrorToast -> {
                Toast.makeText(requireContext(), event.errorMessage, Toast.LENGTH_SHORT).show()
            }

            //뒤로가기
            is PostDetailFragmentEvent.MoveBackPressed -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }


            else -> {}
        }
    }


    //댓글 메뉴 눌렀을 때 처리하기 (실제 권한을 바탕을 로직 분리)
    private fun showCommentPopupMenu(item: CommentItem, canDelete: Boolean) {
        val inflater = LayoutInflater.from(requireContext())
        val menuBinding = LayoutMenuCommentBinding.inflate(inflater)

        val popup = PopupWindow(
            menuBinding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply { elevation = 20f }

        // 서버에서 받은 canDelete 결과에 따라 UI 가시성 조절
        menuBinding.layoutMenuReport.visibility = if (canDelete) View.GONE else View.VISIBLE
        menuBinding.layoutMenuDelete.visibility = if (canDelete) View.VISIBLE else View.GONE

        // 메뉴 클릭 이벤트
        menuBinding.layoutMenuReport.setOnClickListener {
            popup.dismiss()
            handleEvent(PostDetailFragmentEvent.ReportComment(item.commentId))
        }

        menuBinding.layoutMenuDelete.setOnClickListener {
            popup.dismiss()
            viewModel.onClickeDeleteComment(item) // 삭제 로직 실행
        }

        // 앵커 뷰 찾기 및 팝업 노출
        val anchor = binding.postdetailRcv.findViewHolderForAdapterPosition(
            postDetailAdapter.currentList.indexOf(PostDetailItem.Comment(item))
        )?.itemView?.findViewById<View>(R.id.item_btn_menu)

        anchor?.let { popup.showAsDropDown(it, -250, +25) }
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

    private fun openWebpage(url: String) {
        try {
            val webpage: Uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, webpage)
            startActivity(intent)
            //브라우저를 실행할 수 있는 앱이 있는지 확인
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // 브라우저조차 없는 특수한 상황
                val webIntent = Intent(Intent.ACTION_VIEW, webpage)
                startActivity(webIntent)
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

}