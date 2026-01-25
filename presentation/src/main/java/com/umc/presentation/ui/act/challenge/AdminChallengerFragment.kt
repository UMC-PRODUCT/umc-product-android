package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminChallengerBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.ChallengerHeaderAdapter
import com.umc.presentation.ui.act.adapter.AdminChallengerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminChallengerFragment : BaseFragment<FragmentAdminChallengerBinding, AdminChallengerUiState, AdminChallengerEvent, AdminChallengerViewModel>(
    FragmentAdminChallengerBinding::inflate
) {
    override val viewModel: AdminChallengerViewModel by viewModels()
    private val partOrder = UserPart.entries
    private val headerAdapters = mutableMapOf<UserPart, ChallengerHeaderAdapter>()
    private val itemAdapters = mutableMapOf<UserPart, AdminChallengerAdapter>()

    override fun initView() {
        setupRecyclerView()

        // 검색바 텍스트 변경 리스너 연결
        binding.searchBar.setOnTextChangedListener { text ->
            viewModel.filterList(text)
        }

        // 배경 클릭 시 포커스 해제
        binding.root.setOnClickListener {
            binding.searchBar.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        val mainConcatAdapter = ConcatAdapter()

        partOrder.forEach { part ->
            val headerAdapter = ChallengerHeaderAdapter(part.label)
            val itemAdapter = AdminChallengerAdapter { id -> /* 상세 이동 로직 */ }

            headerAdapters[part] = headerAdapter
            itemAdapters[part] = itemAdapter

            mainConcatAdapter.addAdapter(headerAdapter)
            mainConcatAdapter.addAdapter(itemAdapter)
        }

        // 하단 여백용 푸터
        val footerAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                object : RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_challenger_list_footer, parent, false)
                ) {}
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
            override fun getItemCount() = 1
        }
        mainConcatAdapter.addAdapter(footerAdapter)

        binding.rvAdminChallengerList.apply {
            adapter = mainConcatAdapter
            clipToPadding = false
            setPadding(0, 0, 0, 64.px)
        }
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
        }
    }
}