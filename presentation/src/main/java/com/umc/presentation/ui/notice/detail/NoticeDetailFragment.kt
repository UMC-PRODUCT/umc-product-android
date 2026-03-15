package com.umc.presentation.ui.notice.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.umc.domain.model.notice.NoticeImage
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeDetailBinding
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailFullScreenImageAdapter
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailImageAdapter
import com.umc.presentation.ui.notice.detail.adapter.NoticeDetailVoteAdapter
import com.umc.presentation.ui.notice.detail.bottomsheet.NoticeConfirmBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeDetailFragment :
    BaseFragment<FragmentNoticeDetailBinding, NoticeFragmentUiState, NoticeFragmentEvent, NoticeDetailViewModel>(
        FragmentNoticeDetailBinding::inflate,
    ) {
    override val viewModel: NoticeDetailViewModel by activityViewModels()

    private val args: NoticeDetailFragmentArgs by navArgs()

    private val noticeDetailVoteAdapter: NoticeDetailVoteAdapter by lazy {
        NoticeDetailVoteAdapter(object : NoticeDetailVoteAdapter.NoticeDetailVoteDelegate {
            override fun onClickVote(item: NoticeVoteOption) {
                viewModel.onClickVoteItem(item)
            }
        })
    }

    private val noticeDetailImageAdapter: NoticeDetailImageAdapter by lazy {
        NoticeDetailImageAdapter(object : NoticeDetailImageAdapter.NoticeDetailImageDelegate {
            override fun onClickImage(position: Int) {
                showFullScreenImages(position)
            }
        })
    }

    private val noticeDetailFullScreenImageAdapter: NoticeDetailFullScreenImageAdapter by lazy {
        NoticeDetailFullScreenImageAdapter()
    }

    private var imageList: List<NoticeImage> = emptyList()

    private val fullScreenPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            updateImagePageIndicator(position)
        }
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            viewModel.init(args.noticeId)

            recyclerVote.apply {
                adapter = noticeDetailVoteAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }

            recyclerImages.apply {
                adapter = noticeDetailImageAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
            }

            viewPagerFullImage.apply {
                adapter = noticeDetailFullScreenImageAdapter
                registerOnPageChangeCallback(fullScreenPageChangeCallback)
            }

            imageCloseFullScreen.setOnClickListener {
                hideFullScreenImages()
            }

            layoutImageFullScreen.setOnClickListener {
                hideFullScreenImages()
            }

            layoutLink.setOnClickListener {
                moveToLinkUrl(viewModel.uiState.value.detail.links.firstOrNull()?.url)
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
                viewModel.uiState.collect { state ->
                    state.detail.vote?.options?.let { options ->
                        noticeDetailVoteAdapter.submitList(options)
                    }
                    noticeDetailVoteAdapter.setSelectedOptionIds(state.selectedVoteOptionIds.toSet())
                    val isVoted = state.detail.vote?.mySelectedOptionIds?.isNotEmpty() == true
                    noticeDetailVoteAdapter.setVotedState(isVoted, state.detail.vote)
                    imageList = state.detail.images
                    noticeDetailImageAdapter.submitList(imageList)
                    noticeDetailFullScreenImageAdapter.submitList(imageList)
                    updateImagePageIndicator(binding.viewPagerFullImage.currentItem)
                }
            }
        }
}

    override fun handleEvent(event: NoticeFragmentEvent) {
        when (event) {
            NoticeFragmentEvent.MoveBackPressedEvent -> findNavController().popBackStack()
            NoticeFragmentEvent.ShowBottomSheetEvent -> showBottomSheet()
            is NoticeFragmentEvent.MoveToEditPostEvent -> {
                moveToEditPost(event.noticeId)
            }
            is NoticeFragmentEvent.ShowError -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
            is NoticeFragmentEvent.ShowSuccess -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBottomSheet() {
        val bottomSheet = NoticeConfirmBottomSheet()
        bottomSheet.show(parentFragmentManager, "")
    }

    private fun moveToEditPost(noticeId: Long) {
        val action = NoticeDetailFragmentDirections
            .actionNoticeDetailFragmentToNoticeWriteFragment(
                noticeId = noticeId,
                isEditMode = true
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        binding.viewPagerFullImage.unregisterOnPageChangeCallback(fullScreenPageChangeCallback)
        super.onDestroyView()
    }

    private fun showFullScreenImages(startPosition: Int) {
        if (imageList.isEmpty()) return

        binding.layoutImageFullScreen.isVisible = true
        binding.viewPagerFullImage.setCurrentItem(startPosition, false)
        updateImagePageIndicator(startPosition)
    }

    private fun hideFullScreenImages() {
        binding.layoutImageFullScreen.isVisible = false
    }

    private fun updateImagePageIndicator(position: Int) {
        val totalCount = imageList.size
        binding.textImagePageIndicator.text = if (totalCount > 0) {
            "${position + 1} / $totalCount"
        } else {
            "0 / 0"
        }
    }

    private fun moveToLinkUrl(rawUrl: String?) {
        if (rawUrl.isNullOrBlank()) {
            Toast.makeText(requireContext(), "유효한 링크가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        val normalizedUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "https://$rawUrl"
        }

        val intent = Intent(Intent.ACTION_VIEW, normalizedUrl.toUri())
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "링크를 열 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
}
