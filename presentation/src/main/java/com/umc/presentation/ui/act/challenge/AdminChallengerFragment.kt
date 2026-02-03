package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.domain.model.act.challenger.HistoryItem
import com.umc.domain.model.act.challenger.HistoryType
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import com.umc.domain.model.act.challenger.UChallengerManageDialogModel
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

            val itemAdapter = AdminChallengerAdapter { challenger ->
                showManageDialog(challenger)
            }

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
            layoutManager = LinearLayoutManager(requireContext())
            clipToPadding = false
            setPadding(0, 0, 0, 64.px)
        }
    }

    /**
     * 다이얼로그에 더미 데이터 추가 (연동할 때 변경 예정)
     */
    private fun showManageDialog(challenger: AdminChallenger) {
        val dialogModel = UChallengerManageDialogModel(
            name = challenger.name,
            university = "중앙대학교",
            part = challenger.part.label,
            absenceCount = challenger.outCount,
            warningCount = challenger.warningCount,
            hasNewAbsence = false,
            history = listOf(
                HistoryItem("2024.01.01", "스터디 미제출", HistoryType.OUT, 1.0),
                HistoryItem("2024.01.01", "세션 지각", HistoryType.WARNING, 0.5)
            )
        )

        val dialog = ChallengerManageDialog(
            model = dialogModel,
            onAbsenceSubmit = { reason -> /* TODO: API 연동 */ },
            onWarningSubmit = { reason -> /* TODO: API 연동 */ },
            onDeleteHistory = { item ->
                val warningDialog = UBasicDialog(
                    model = UBasicDialogModel.Warning(
                        title = "해당 기록을 삭제하시겠습니까?",
                        content = "삭제된 기록은 복구가 어렵습니다.",
                        positiveText = "삭제하기"
                    ),
                    onConfirm = {
                        /* TODO: API 연동 */
                    }
                )
                warningDialog.show(childFragmentManager, "DeleteWarningDialog")
            }
        )

        dialog.show(childFragmentManager, "UChallengerManageDialog")
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