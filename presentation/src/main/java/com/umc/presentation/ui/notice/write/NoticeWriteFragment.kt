package com.umc.presentation.ui.notice.write

import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.NoticeChipState
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.adapter.DropDownAdapter
import com.umc.presentation.databinding.FragmentNoticeWriteBinding
import com.umc.presentation.extension.VerticalSpaceItemDecoration
import com.umc.presentation.extension.dp
import com.umc.presentation.ui.notice.detail.bottomsheet.NoticeConfirmBottomSheet
import com.umc.presentation.ui.notice.write.adapter.NoticeClassChipAdapter
import com.umc.presentation.ui.notice.write.adapter.NoticeImageAdapter
import com.umc.presentation.ui.notice.write.adapter.NoticeVoteAdapter
import com.umc.presentation.ui.notice.write.bottomsheet.NoticeVoteBottomSheet
import com.umc.presentation.util.ULog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeWriteFragment : BaseFragment<FragmentNoticeWriteBinding, NoticeWriteUiState, NoticeWriteEvent, NoticeWriteViewModel>(
    FragmentNoticeWriteBinding::inflate,
) {
    override val viewModel: NoticeWriteViewModel by viewModels()

    private companion object {
        const val MAX_PICK_COUNT = 10
    }

    private val dropDownAdapter : DropDownAdapter by lazy {
        DropDownAdapter(object : DropDownAdapter.DropDownDelegate {
            override fun onClickItem(text: String) {
                viewModel.updateCategory(NoticeCategory.find(text))
            }
        })
    }

    private val noticeClassChipAdapter : NoticeClassChipAdapter by lazy {
        NoticeClassChipAdapter(object : NoticeClassChipAdapter.NoticeClassChipDelegate {
            override fun onClickChip(item: NoticeChipState) {
                viewModel.onClickClassChip(item)
            }
        })
    }

    private val noticePartChipAdapter : NoticeClassChipAdapter by lazy {
        NoticeClassChipAdapter(object : NoticeClassChipAdapter.NoticeClassChipDelegate {
            override fun onClickChip(item: NoticeChipState) {
                viewModel.onClickPartChip(item)
            }
        })
    }

    private val noticeImageAdapter : NoticeImageAdapter by lazy {
        NoticeImageAdapter(object : NoticeImageAdapter.NoticeImageDelegate {
            override fun onClickDelete(uri: Uri) {
                viewModel.deleteImage(uri)
            }
        })
    }

    private val pickMultipleImagesLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_PICK_COUNT)) { uris: List<Uri> ->
            if (uris.isEmpty()) return@registerForActivityResult
            viewModel.updateSelectImage(uris)
        }

    override fun initView() {
        binding.apply {
            vm = viewModel

            recyclerDropdown.apply {
                adapter = dropDownAdapter
                layoutManager = LinearLayoutManager(context)
            }

            recyclerClass.apply {
                adapter = noticeClassChipAdapter
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                itemAnimator = null
            }

            recyclerPart.apply {
                adapter = noticePartChipAdapter
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                itemAnimator = null
            }

            recyclerImage.apply {
                adapter = noticeImageAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
                    dropDownAdapter.submitList(it.dropdownList)
                    noticeClassChipAdapter.submitList(it.classList)
                    noticePartChipAdapter.submitList(it.partList)
                    noticeImageAdapter.submitList(it.selectImageList)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeWriteEvent) {
        when(event) {
            NoticeWriteEvent.SelectImageEvent -> openPhotoPicker()
            NoticeWriteEvent.ShowBottomSheetEvent -> showBottomSheet()
        }
    }

    private fun openPhotoPicker() {
        pickMultipleImagesLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun showBottomSheet() {
        val bottomSheet = NoticeVoteBottomSheet()
        bottomSheet.show(childFragmentManager, "")
    }
}
