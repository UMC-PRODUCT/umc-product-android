package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserChallengerBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.UserChallengerGroupAdapter
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserChallengerFragment : BaseFragment<FragmentUserChallengerBinding, UserChallengerUiState, UserChallengerEvent, UserChallengerViewModel>(
    FragmentUserChallengerBinding::inflate
) {
    override val viewModel: UserChallengerViewModel by viewModels()

    private lateinit var groupAdapter: UserChallengerGroupAdapter

    override fun initView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

        // 배경 터치 시 키보드 내리기
        binding.root.setOnClickListener {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
            binding.searchBar.clearFocus()
        }
    }

    private fun setupRecyclerView() {
        groupAdapter = UserChallengerGroupAdapter { id ->
            viewModel.navigateToDetail(id)
        }

        binding.rvChallengerList.apply {
            adapter = groupAdapter
            clipToPadding = false
            setPadding(0, 0, 0, 64.px)

            // 하단 스크롤 리스너: 바닥 감지 및 페이징 호출
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) { // 아래로 스크롤 중일 때만 체크
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                        val totalItemCount = layoutManager.itemCount

                        // 마지막 아이템 근처에 도달하면 다음 페이지 요청
                        if (lastVisibleItem >= totalItemCount - 2) {
                            viewModel.fetchNextPage()
                        }
                    }
                }
            })
        }
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                // UI 상태 관찰 및 어댑터 업데이트
                viewModel.uiState.collect { state ->
                    groupAdapter.submitList(state.filteredGroups)
                }
            }
            launch {
                // 일회성 이벤트(토스트, 화면 이동) 처리
                viewModel.uiEvent.collect { handleEvent(it) }
            }
        }
    }

    override fun handleEvent(event: UserChallengerEvent) {
        when (event) {
            is UserChallengerEvent.NavigateToDetail -> {
                ChallengerInfoDialog(event.model).show(childFragmentManager, "ChallengerInfoDialog")
            }
            is UserChallengerEvent.ShowToast -> {
                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = if (event.isError) UToast.State.ERROR else UToast.State.CHECK
                ).show()
            }
        }
    }
}