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

        fun newInstance(chapterList: List<Chapter>): ChapterSelectBottomSheet {
            return ChapterSelectBottomSheet().apply {
                arguments = bundleOf(
                    ARG_CHAPTER_LIST to ArrayList(chapterList)
                )
            }
        }
    }

    private val chapterList: List<Chapter> by lazy {
        @Suppress("UNCHECKED_CAST")
        (arguments?.getSerializable(ARG_CHAPTER_LIST) as? ArrayList<Chapter>)?.toList() ?: emptyList()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
