package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

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
            // 어댑터 생성 시 클릭 리스너의 id 인자가 이제 Long이므로 navigateToDetail과 일치합니다.
            val itemAdapter = UserChallengerAdapter { id ->
                viewModel.navigateToDetail(id)
            }

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

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    val grouped = state.filteredChallengers.groupBy { it.part }

                    partOrder.forEach { part ->
                        val list = grouped[part] ?: emptyList()

                        headerAdapters[part]?.updateCount(list.size)

                        // UI 모델 리스트 생성 - 어댑터 타입과 일치하여 에러가 해결됩니다.
                        val uiList = list.mapIndexed { index, challenger ->
                            UserChallengerUIModel(
                                challenger = challenger,
                                isLastInPart = index == list.size - 1
                            )
                        }

                        itemAdapters[part]?.submitList(uiList)
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