package com.umc.presentation.ui.home.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.home.LocationItem
import com.umc.presentation.R
import com.umc.presentation.databinding.LayoutBottomSheetLocationSelectBinding
import com.umc.presentation.ui.home.PlanAddViewModel
import com.umc.presentation.ui.home.adapter.BottomSheetLocationRecentAdapter
import com.umc.presentation.ui.home.adapter.BottomSheetLocationResultAdapter
import com.umc.presentation.ui.home.adapter.LocationResultDelegate
import com.umc.presentation.ui.home.adapter.RecentLocationDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

//onLocationSelected -> 선택 누를 시 부모 프래그먼트로 전송하기 위함.
@AndroidEntryPoint
class BottomSheetLocationDialog(
    private val title: String? = null,       // 커스텀 타이틀
    private val description: String? = null, // 추가 설명
    private val onItemSelected: (LocationItem) -> Unit
) : BottomSheetDialogFragment(), RecentLocationDelegate, LocationResultDelegate {
    private lateinit var binding: LayoutBottomSheetLocationSelectBinding

    //다이얼로그 전용 뷰모델 (이젠 뷰모델에 의존X)
    private val viewModel: BottomSheetLocationViewModel by viewModels()

    // 두 종류의 어댑터 선언
    private lateinit var recentAdapter: BottomSheetLocationRecentAdapter
    private lateinit var resultAdapter: BottomSheetLocationResultAdapter

    // XML의 둥근 모서리를 보여주기 위해 투명 테마를 적용
    override fun getTheme(): Int = R.style.TransparentBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutBottomSheetLocationSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecyclerView()
        setupSearchLogic()
        observeData()
    }

    private fun initRecyclerView() {
        // 1. 최근 검색 기록 어댑터 연결 (터치 시 검색창 입력)
        recentAdapter = BottomSheetLocationRecentAdapter(this)
        binding.rcvRecent.adapter = recentAdapter

        // 2. 검색 결과 어댑터 연결 (선택 버튼 클릭 시 최종 적용)
        resultAdapter = BottomSheetLocationResultAdapter(this)
        binding.rcvResult.adapter = resultAdapter

    }

    // searchbar에 따라 아래 보여주기 diff
    private fun setupSearchLogic() {
        binding.searchbarLocation.setOnTextChangedListener { text ->
            if (text.isEmpty()) {
                // 텍스트가 비면 최근 검색 기록 표시
                binding.layoutRecent.visibility = View.VISIBLE
                binding.layoutResult.visibility = View.GONE
            } else {
                // 텍스트가 있으면 최근 검색 기록 숨기고 결과 리스트 표시
                binding.layoutRecent.visibility = View.GONE
                binding.layoutResult.visibility = View.VISIBLE

                // 카카오 API를 이용한 장소 검색
                viewModel.searchLocation(text)
                
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // 다이얼로그의 내부 뷰(design_bottom_sheet)를 찾아 높이를 설정
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // 1. 레이아웃 파라미터의 높이를 화면 전체의 80%로 설정
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            bottomSheet.layoutParams = layoutParams

            // 2. 초기 상태를 확장 상태(EXPANDED)로 고정
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            // 3. 드래그해서 절반으로 접히는 현상 방지 (선택 사항)
            behavior.skipCollapsed = true
        }
    }

    // 1. 최근 기록 클릭 시: 검색창 텍스트를 바꾸고 검색 모드로 전환
    override fun onRecentClicked(item: String) {
        viewModel.saveRecentPlace(item) //datastore 업데이트
        binding.searchbarLocation.setText(item)
    }

    // 2. 검색 결과의 '선택' 버튼 클릭 시: 최종 데이터 전달 및 닫기
    override fun onLocationSelected(item: LocationItem) {
        viewModel.saveRecentPlace(item.title) //datastore 업데이트
        onItemSelected(item) // 부모 프래그먼트로 전달
        dismiss()
    }

    //최근 검색 기록 observe
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                //최근 검색 기록 observe
                recentAdapter.submitList(state.recentSearchList)
                //카카오맵 검색 기록 observe
                resultAdapter.submitList(state.searchResultList)
            }
        }
    }

    private fun initView() {
        // 타이틀이 전달되었다면 변경 (없으면 XML 기본값 유지)
        title?.let { binding.tvTitle.text = it }

        // 설명이 전달되었다면 보이기
        description?.let {
            binding.tvDescription.text = it
            binding.tvDescription.visibility = View.VISIBLE
        }
    }

}