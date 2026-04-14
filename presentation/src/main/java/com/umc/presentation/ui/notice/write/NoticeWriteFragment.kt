package com.umc.presentation.ui.notice.write

import android.net.Uri
import android.widget.Toast
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar
import com.skydoves.colorpickerview.ColorPickerDialog
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.organization.Chapter
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNoticeWriteBinding
import com.umc.presentation.ui.notice.write.adapter.NoticeClassChipAdapter
import com.umc.presentation.ui.notice.write.adapter.NoticeImageAdapter
import com.umc.presentation.ui.notice.write.bottomsheet.ChapterSelectBottomSheet
import com.umc.presentation.ui.notice.write.bottomsheet.NoticeVoteBottomSheet
import com.umc.presentation.ui.notice.write.model.NoticeImageItem
import com.umc.presentation.ui.signUp.bottomSheet.SchoolSelectBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeWriteFragment : BaseFragment<FragmentNoticeWriteBinding, NoticeWriteUiState, NoticeWriteEvent, NoticeWriteViewModel>(
    FragmentNoticeWriteBinding::inflate,
) {
    override val viewModel: NoticeWriteViewModel by viewModels()

    private val args: NoticeWriteFragmentArgs by navArgs()

    private companion object {
        const val MAX_PICK_COUNT = 10
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
            override fun onClickDelete(item: NoticeImageItem) {
                viewModel.deleteImage(item)
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

            if (args.isEditMode) {
                viewModel.initEditMode(noticeId = args.noticeId)
            } else {
                viewModel.setSelectedGisu(args.gisuId, args.gisuName)
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

            editTextContent.apply {
                setEditorHeight(220)
                setEditorFontSize(16)
                setEditorFontColor(resources.getColor(com.umc.presentation.R.color.neutral800, null))
                setPadding(16, 16, 16, 16)
                setPlaceholder("내용을 입력해주세요")
                setHtml(viewModel.uiState.value.content)
                setOnTextChangeListener { html ->
                    viewModel.updateContent(html)
                }
            }

            btnRichBold.setOnClickListener { editTextContent.setBold() }
            btnRichItalic.setOnClickListener { editTextContent.setItalic() }
            btnRichUnderline.setOnClickListener { editTextContent.setUnderline() }
            btnRichStrike.setOnClickListener { editTextContent.setStrikeThrough() }
            btnRichBullet.setOnClickListener { editTextContent.setBullets() }
            btnRichNumber.setOnClickListener { editTextContent.setNumbers() }

            // Heading 조정
            btnRichTitle.setOnClickListener { editTextContent.setHeading(1) }      // 머리말
            btnRichHeading.setOnClickListener { editTextContent.setHeading(2) }      // 머리말
            btnRichSubheading.setOnClickListener { editTextContent.setHeading(3) }   // 부머리말
            btnRichBody.setOnClickListener { editTextContent.setHeading(4) }          // 본문
            btnRichMono.setOnClickListener { editTextContent.setHeading(5) }

            // 텍스트 색 변경 (컬러피커)
            btnRichColor.setOnClickListener {
                showTextColorPicker { selectedColor ->
                    editTextContent.setTextColor(selectedColor)
                }
            }

            // 텍스트 정렬
            btnRichAlignLeft.setOnClickListener { editTextContent.setAlignLeft() }
            btnRichAlignCenter.setOnClickListener { editTextContent.setAlignCenter() }
            btnRichAlignRight.setOnClickListener { editTextContent.setAlignRight() }

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

    private fun showTextColorPicker(onColorSelected: (Int) -> Unit) {
        val colorPickerView = com.skydoves.colorpickerview.ColorPickerView(requireContext())
        val alphaSlideBar = AlphaSlideBar(requireContext())
        val brightnessSlideBar = BrightnessSlideBar(requireContext())

        colorPickerView.attachAlphaSlider(alphaSlideBar)
        colorPickerView.attachBrightnessSlider(brightnessSlideBar)

        ColorPickerDialog.Builder(requireContext())
            .setTitle("텍스트 색상")
            .setPreferenceName("notice_write_text_color_picker")
            .setPositiveButton(
                "선택",
                ColorEnvelopeListener { envelope: ColorEnvelope, _: Boolean ->
                    onColorSelected(envelope.color)
                },
            )
            .setNegativeButton("취소") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setColorPickerView(colorPickerView)
            .setBottomSpace(12)
            .show()
    }

    private fun showChapterBottomSheet() {
        val chapters = viewModel.uiState.value.chapterList
        val selectedChapterId = viewModel.uiState.value.classList
            .find { it.chapterId != null }?.chapterId
        if (chapters.isNotEmpty()) {
            val bottomSheet = ChapterSelectBottomSheet.newInstance(chapters, selectedChapterId)
            bottomSheet.show(childFragmentManager, "")
        } else {
            Toast.makeText(requireContext(), "지부 목록을 불러오는 중입니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSchoolBottomSheet() {
        val schoolList = viewModel.uiState.value.schoolList
        val selectedSchoolId = viewModel.uiState.value.classList
            .find { it.schoolId != null }?.schoolId
        if (schoolList.isNotEmpty()) {
            val bottomSheet = SchoolSelectBottomSheet.newInstance(schoolList, selectedSchoolId)
            bottomSheet.show(childFragmentManager, "")
        } else {
            Toast.makeText(requireContext(), "학교 목록을 불러오는 중입니다", Toast.LENGTH_SHORT).show()
        }
    }
}
