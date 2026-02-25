package com.umc.presentation.ui.notice.write

import android.net.Uri
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.adapter.DropDownAdapter
import com.umc.presentation.component.adapter.DropDownItem
import com.umc.presentation.databinding.FragmentNoticeWriteBinding
import com.umc.presentation.ui.notice.write.adapter.NoticeClassChipAdapter
import com.umc.presentation.ui.notice.write.adapter.NoticeImageAdapter
import com.umc.presentation.ui.notice.write.bottomsheet.ChapterSelectBottomSheet
import com.umc.presentation.ui.notice.write.bottomsheet.NoticeVoteBottomSheet
import com.umc.presentation.ui.signUp.bottomSheet.SchoolSelectBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private class StringDropDownItem(val text: String) : DropDownItem {
    override val displayText: String get() = text
}

@AndroidEntryPoint
class NoticeWriteFragment : BaseFragment<FragmentNoticeWriteBinding, NoticeWriteUiState, NoticeWriteEvent, NoticeWriteViewModel>(
    FragmentNoticeWriteBinding::inflate,
) {
    override val viewModel: NoticeWriteViewModel by viewModels()

    private companion object {
        const val MAX_PICK_COUNT = 10
    }

    private val dropDownAdapter: DropDownAdapter<StringDropDownItem> by lazy {
        DropDownAdapter(object : DropDownAdapter.DropDownDelegate<StringDropDownItem> {
            override fun onClickItem(item: StringDropDownItem) {
                viewModel.updateCategory(NoticeCategory.find(item.text))
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

            editTextTitle.doAfterTextChanged { text ->
                viewModel.updateTitle(text?.toString() ?: "")
            }

            editTextContent.doAfterTextChanged { text ->
                viewModel.updateContent(text?.toString() ?: "")
            }

            imageBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        // Chapter 선택 결과 리스너 등록
        childFragmentManager.setFragmentResultListener(
            ChapterSelectBottomSheet.CHAPTER_SELECT,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedChapter = bundle.getSerializable(ChapterSelectBottomSheet.BUNDLE_KEY_CHAPTER) as? Chapter
            selectedChapter?.let {
                viewModel.onChapterSelected(it)
            }
        }

        childFragmentManager.setFragmentResultListener(
            SchoolSelectBottomSheet.SCHOOL_SELECT,
            viewLifecycleOwner
        ) { _, bundle ->
            val selectedSchool = bundle.getSerializable(SchoolSelectBottomSheet.BUNDLE_KEY_SELECT) as? SchoolInfo
            selectedSchool?.let {
                viewModel.onSchoolSelected(it)
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
                    dropDownAdapter.submitList(state.dropdownList.map { text -> StringDropDownItem(text) })
                    noticeClassChipAdapter.submitList(state.classList)
                    noticePartChipAdapter.submitList(state.partList)
                    noticeImageAdapter.submitList(state.selectImageList)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeWriteEvent) {
        when(event) {
            NoticeWriteEvent.SelectImageEvent -> openPhotoPicker()
            NoticeWriteEvent.ShowBottomSheetEvent -> showBottomSheet()
            NoticeWriteEvent.ShowChapterBottomSheetEvent -> showChapterBottomSheet()
            NoticeWriteEvent.ShowSchoolBottomSheetEvent -> showSchoolBottomSheet()
            NoticeWriteEvent.SubmitSuccess -> {
                Toast.makeText(requireContext(), "공지사항이 작성되었습니다", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            is NoticeWriteEvent.ShowError -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
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

    private fun showChapterBottomSheet() {
        val chapters = viewModel.uiState.value.chapterList
        if (chapters.isNotEmpty()) {
            val bottomSheet = ChapterSelectBottomSheet.newInstance(chapters)
            bottomSheet.show(childFragmentManager, "")
        } else {
            Toast.makeText(requireContext(), "지부 목록을 불러오는 중입니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSchoolBottomSheet() {
        val schoolList = viewModel.uiState.value.schoolList
        if (schoolList.isNotEmpty()) {
            val bottomSheet = SchoolSelectBottomSheet.newInstance(schoolList)
            bottomSheet.show(childFragmentManager, "")
        } else {
            Toast.makeText(requireContext(), "학교 목록을 불러오는 중입니다", Toast.LENGTH_SHORT).show()
        }
    }
}
