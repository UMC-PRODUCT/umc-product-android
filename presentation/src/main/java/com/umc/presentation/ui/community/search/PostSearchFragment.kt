package com.umc.presentation.ui.community.search

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.domain.model.enums.SearchMode
import com.umc.domain.model.community.ContentItem
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPostSearchBinding
import com.umc.presentation.ui.community.adapter.RecentSearchAdapter
import com.umc.presentation.ui.community.adapter.RecentSearchDelegate
import com.umc.presentation.ui.community.adapter.ContentAdapter
import com.umc.presentation.ui.community.adapter.ContentItemDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostSearchFragment : BaseFragment<FragmentPostSearchBinding, PostSearchFragmentUiState, PostSearchFragmentEvent, PostSearchViewModel>(
    FragmentPostSearchBinding::inflate
), ContentItemDelegate, RecentSearchDelegate {
    override val viewModel: PostSearchViewModel by viewModels()

    // 검색 결과용 recycler 어댑터
    private lateinit var searchContentAdapter : ContentAdapter
    // 최근 검색 기록용 recycler 어댑터
    private lateinit var recentSearchAdapter : RecentSearchAdapter
    
    // 최근 기록 검색(누를 시) 로직
    override fun onRecentSearchClicked(keyword: String) {
        viewModel.selectRecentSearch(keyword)
    }
    
    // 최근 기록 X 버튼 누를 시 로직
    override fun onDeleteClicked(keyword: String) {
        viewModel.deleteRecentSearch(keyword)
    }
    
    // 검색 결과 item 터치 시 이동 로직
    override fun onItemClicked(item: ContentItem) {
        val action = PostSearchFragmentDirections.actionPostSearchToPostDetail()
        findNavController().navigate(action)
    }

    
    
    override fun initView() {
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        
        // 텍스트 필드 정의
        binding.searchTextfield.apply {
            setOnTextChangedListener { text ->
                //바뀔때마다 비교
                /**여기서 아이콘 + ViewMode 바꾸기**/
                if (text.isNotBlank()) {
                    // 글자가 입력될 때마다 실시간으로 검색 내용 변경
                    viewModel.onQueryChanged(text)
                    viewModel.onChangeViewMode(SearchMode.TYPING)

                } else {
                    /**여기도 바꾸기**/
                    viewModel.onQueryChanged(text)
                    viewModel.onChangeViewMode(SearchMode.EMPTY)
                }
            }
        }

        //어댑터 정의
        searchContentAdapter = ContentAdapter(this)
        binding.searchRcvResult.apply{
            adapter = searchContentAdapter
        }

        recentSearchAdapter = RecentSearchAdapter(this)
        binding.searchRcvRecent.apply{
            adapter = recentSearchAdapter
        }




    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner){
            launch {
                viewModel.uiState.collect { state ->
                    //UISTATE의 모드 최신화
                    handleSearchMode(state.mode)

                    //어댑터
                    searchContentAdapter.submitList(state.searchResults)
                    recentSearchAdapter.submitList(state.recentSearches)
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }

    }

    override fun handleEvent(event: PostSearchFragmentEvent) {
        super.handleEvent(event)

        when(event){
            
            //검색 결과를 눌렀을 때 메소드
            is PostSearchFragmentEvent.ShowSearchResult -> {
            /**TODO 서버에서 검색 결과를 받아와서 uistate에 넣어주기**/
                binding.searchTextfield.setText(event.query)
                Toast.makeText(requireContext(), "${event.query} 검색 결과", Toast.LENGTH_SHORT).show()
            }

            is PostSearchFragmentEvent.MoveBackPressedEvent -> {
                findNavController().popBackStack()
            }

            else -> {}
        }
    }

    //타입에 따라 다르게 보이게 하기
    private fun handleSearchMode(mode: SearchMode) {
        binding.apply {
            when (mode) {
                SearchMode.EMPTY -> {
                    searchLayoutRecent.visibility = View.VISIBLE
                    searchLayoutResult.visibility = View.GONE
                }

                SearchMode.TYPING -> {
                    searchLayoutRecent.visibility = View.GONE
                    searchLayoutResult.visibility = View.GONE
                }

                SearchMode.RESULT -> {
                    searchLayoutRecent.visibility = View.GONE
                    searchLayoutResult.visibility = View.VISIBLE
                }
            }
        }
    }


}