package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.domain.model.enums.PointType
import com.umc.presentation.databinding.FragmentAdminChallengerBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.ChallengerHeaderAdapter
import com.umc.presentation.ui.act.adapter.AdminChallengerAdapter
import com.umc.presentation.util.UFormat
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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

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
                viewModel.onChallengerClicked(challenger.id)
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

    override fun handleEvent(event: AdminChallengerEvent) {
        when (event) {
            is AdminChallengerEvent.ShowManageDialog -> {
                val formattedHistory = event.model.history.map { point ->
                    point.copy(date = UFormat.parseDateTime(point.date).first)
                }
                val formattedModel = event.model.copy(history = formattedHistory)

                val existingDialog = childFragmentManager.findFragmentByTag("UChallengerManageDialog") as? ChallengerManageDialog

                if (existingDialog != null && existingDialog.isAdded) {
                    existingDialog.updateData(formattedModel)
                } else {
                    showManageDialog(formattedModel, formattedModel.challengerId.toInt())
                }
            }
            is AdminChallengerEvent.ShowErrorToast -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showManageDialog(model: ChallengerManageDialogModel, challengerId: Int) {
        val dialog = ChallengerManageDialog(
            model = model,
            onAbsenceSubmit = { reason ->
                // 아웃 부여 시 POINT_TYPE을 OUT으로 전송
                viewModel.grantPoint(challengerId, PointType.OUT, reason)
            },
            onWarningSubmit = { reason ->
                // 경고 부여 시 POINT_TYPE을 WARNING으로 전송
                viewModel.grantPoint(challengerId, PointType.WARNING, reason)
            },
            onDeleteHistory = { point ->
                val warningDialog = UBasicDialog(
                    model = UBasicDialogModel.Warning(
                        title = "해당 기록을 삭제하시겠습니까?",
                        content = "삭제된 기록은 복구가 어렵습니다.",
                        positiveText = "삭제하기"
                    ),
                    onConfirm = {
                        viewModel.deletePoint(challengerId, point.id)
                    }
                )
                warningDialog.show(childFragmentManager, "DeleteWarningDialog")
            }
        )
        dialog.show(childFragmentManager, "UChallengerManageDialog")
    }
}