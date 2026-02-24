package com.umc.presentation.ui.community.top

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.LayoutBottomSheetCategoryBinding
import com.umc.presentation.ui.community.adapter.BottomSheetTrophyCategoryAdapter

class TrophyBottomSheetDialog (
    private val title: String,          // 바텀시트 상단 제목
    private val content: String?,       // 하단 설명 (유무)
    private val itemList: List<String>, // 보여줄 데이터 리스트
    private val onSelected: (String) -> Unit
) : BottomSheetDialogFragment() {
    private lateinit var binding: LayoutBottomSheetCategoryBinding

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun onStart() {
        super.onStart()

        // 다이얼로그의 내부 뷰(design_bottom_sheet)를 찾아 높이를 설정
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // 1. 레이아웃 파라미터의 높이를 화면 전체의 80%로 설정
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.7).toInt()
            bottomSheet.layoutParams = layoutParams

            // 2. 초기 상태를 확장 상태(EXPANDED)로 고정
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // 3. 드래그해서 절반으로 접히는 현상 방지 (선택 사항)
            behavior.skipCollapsed = true
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutBottomSheetCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ui 연결
        binding.tvTitle.text = title
        if(content != null) {
            binding.tvContent.visibility = View.VISIBLE
            binding.tvContent.text = content
        }

        //어댑터 초기화 및 클릭 리스너 연결
        val categoryAdapter = BottomSheetTrophyCategoryAdapter { selectedItem ->
            onSelected(selectedItem)
            dismiss() //다이얼로그 닫기
        }

        //리사이클러뷰 설정
        binding.rvCategories.adapter = categoryAdapter

        //데이터 주입 (Enum의 모든 항목)
        categoryAdapter.submitList(itemList)
    }
}