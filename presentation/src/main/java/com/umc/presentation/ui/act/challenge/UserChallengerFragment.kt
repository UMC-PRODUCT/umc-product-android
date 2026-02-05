package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.domain.model.enums.CheckHistoryStatus
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserChallengerBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.ChallengerHeaderAdapter
import com.umc.presentation.ui.act.adapter.UserChallengerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserChallengerFragment : BaseFragment<FragmentUserChallengerBinding, UserChallengerUiState, UserChallengerEvent, UserChallengerViewModel>(
    FragmentUserChallengerBinding::inflate
) {
    override val viewModel: UserChallengerViewModel by viewModels()
    private val partOrder = UserPart.entries
    private val headerAdapters = mutableMapOf<UserPart, ChallengerHeaderAdapter>()
    private val itemAdapters = mutableMapOf<UserPart, UserChallengerAdapter>()

    override fun initView() {
        setupRecyclerView()
        setupSearch()

        // 배경 클릭 시 포커스 해제 및 키보드 숨기기 로직
        binding.root.setOnClickListener {
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
            binding.searchBar.clearFocus()
        }
    }

    private fun setupRecyclerView() {
        val mainConcatAdapter = ConcatAdapter()

        partOrder.forEach { part ->
            val headerAdapter = ChallengerHeaderAdapter(part.label)
            val itemAdapter = UserChallengerAdapter { id -> viewModel.navigateToDetail(id) }

            headerAdapters[part] = headerAdapter
            itemAdapters[part] = itemAdapter

            mainConcatAdapter.addAdapter(headerAdapter)
            mainConcatAdapter.addAdapter(itemAdapter)
        }

        val footerAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_challenger_list_footer, parent, false)
                ) {}
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun getItemCount() = 1
        }
        mainConcatAdapter.addAdapter(footerAdapter)

        binding.rvChallengerList.apply {
            adapter = mainConcatAdapter
            clipToPadding = false
            setPadding(0, 0, 0, 64.px)
        }
    }

    private fun setupSearch() {
        binding.searchBar.setOnTextChangedListener { text -> viewModel.filterList(text) }
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    val grouped = state.filteredChallengers.groupBy { it.part }
                    partOrder.forEach { part ->
                        val list = grouped[part] ?: emptyList()
                        headerAdapters[part]?.updateCount(list.size)
                        itemAdapters[part]?.submitList(list)
                    }
                }
            }
            launch { viewModel.uiEvent.collect { handleEvent(it) } }
        }
    }

    /**
     * ViewModel에서 발생한 이벤트를 처리
     */
    override fun handleEvent(event: UserChallengerEvent) {
        when (event) {
            is UserChallengerEvent.NavigateToDetail -> {
                ChallengerInfoDialog(event.model).show(childFragmentManager, "ChallengerInfoDialog")
            }
            is UserChallengerEvent.ShowErrorToast -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}