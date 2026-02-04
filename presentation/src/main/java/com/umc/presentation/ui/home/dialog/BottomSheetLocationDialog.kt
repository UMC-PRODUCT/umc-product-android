package com.umc.presentation.ui.home.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.home.LocationItem
import com.umc.presentation.R
import com.umc.presentation.databinding.LayoutBottomSheetLocationSelectBinding
import com.umc.presentation.ui.home.PlanAddViewModel
import com.umc.presentation.ui.home.adapter.BottomSheetLocationRecentAdapter
import com.umc.presentation.ui.home.adapter.BottomSheetLocationResultAdapter
import com.umc.presentation.ui.home.adapter.LocationResultDelegate
import com.umc.presentation.ui.home.adapter.RecentLocationDelegate
import kotlinx.coroutines.launch

//onLocationSelected -> 선택 누를 시 부모 프래그먼트로 전송하기 위함.
class BottomSheetLocationDialog (
    private val viewModel: PlanAddViewModel,
    private val onItemSelected: (LocationItem) -> Unit
) : BottomSheetDialogFragment(), RecentLocationDelegate, LocationResultDelegate {

    private lateinit var binding: LayoutBottomSheetLocationSelectBinding

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

                // Mock 데이터: 실제로는 여기서 API 호출 후 submitList 수행
                updateSearchResult(text)
            }
        }
    }

    // 1. 최근 기록 클릭 시: 검색창 텍스트를 바꾸고 검색 모드로 전환
    override fun onRecentClicked(item: String) {
        viewModel.saveRecentPlace(item) //datastore 업데이트
        binding.searchbarLocation.setText(item)
        // 텍스트가 채워지면서 setupSearchLogic의 Listener가 자동으로 결과창을 띄웁니다.
    }

    // 2. 검색 결과의 '선택' 버튼 클릭 시: 최종 데이터 전달 및 닫기
    override fun onLocationSelected(item: LocationItem) {
        viewModel.saveRecentPlace(item.title) //datastore 업데이트
        onItemSelected(item) // 부모 프래그먼트로 전달
        dismiss()
    }

    private fun updateSearchResult(query: String) {
        val mockData = listOf(
            LocationItem("서울역", "서울특별시 어딘가 상세주소 1"),
            LocationItem("중앙대학교", "서울특별시 어딘가 상세주소 2"),
        )
        resultAdapter.submitList(mockData)
    }

    //최근 검색 기록 observe
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                recentAdapter.submitList(state.recentSearchList)
            }
        }
    }

}