package com.umc.presentation.ui.notice.write.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.organization.Chapter
import com.umc.presentation.databinding.BottomSheetChapterListBinding
import com.umc.presentation.ui.notice.write.adapter.ChapterListAdapter

class ChapterSelectBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val CHAPTER_SELECT = "chapterSelect"
        const val BUNDLE_KEY_CHAPTER = "chapter_select_key"
        private const val ARG_CHAPTER_LIST = "arg_chapter_list"
        private const val ARG_SELECTED_CHAPTER_ID = "arg_selected_chapter_id"

        fun newInstance(chapterList: List<Chapter>, selectedChapterId: Long? = null): ChapterSelectBottomSheet {
            return ChapterSelectBottomSheet().apply {
                arguments = bundleOf(
                    ARG_CHAPTER_LIST to ArrayList(chapterList),
                    ARG_SELECTED_CHAPTER_ID to selectedChapterId
                )
            }
        }
    }

    private val chapterList: List<Chapter> by lazy {
        @Suppress("UNCHECKED_CAST")
        (arguments?.getSerializable(ARG_CHAPTER_LIST) as? ArrayList<Chapter>)?.toList() ?: emptyList()
    }

    private val selectedChapterId: Long? by lazy {
        arguments?.getLong(ARG_SELECTED_CHAPTER_ID)?.takeIf { it != 0L }
    }

    private val chapterListAdapter: ChapterListAdapter by lazy {
        ChapterListAdapter(object : ChapterListAdapter.ChapterListDelegate {
            override fun onClickChapter(item: Chapter) {
                setFragmentResult(CHAPTER_SELECT, bundleOf(BUNDLE_KEY_CHAPTER to item))
                dismiss()
            }
        })
    }

    private var _binding: BottomSheetChapterListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetChapterListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerChapterList.apply {
            adapter = chapterListAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }

        chapterListAdapter.submitList(chapterList)
        chapterListAdapter.setSelectedChapterId(selectedChapterId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
