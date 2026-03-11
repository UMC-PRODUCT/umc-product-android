package com.umc.presentation.ui.act.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminChallengerBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.AdminChallengerGroupAdapter
import com.umc.presentation.util.UFormat
import com.umc.presentation.util.UToast
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.enums.PointType
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminChallengerFragment : BaseFragment<FragmentAdminChallengerBinding, AdminChallengerUiState, AdminChallengerEvent, AdminChallengerViewModel>(
    FragmentAdminChallengerBinding::inflate
) {
    override val viewModel: AdminChallengerViewModel by viewModels()
    private lateinit var groupAdapter: AdminChallengerGroupAdapter

    override fun initView() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()

        binding.root.setOnClickListener {
            binding.searchBar.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        groupAdapter = AdminChallengerGroupAdapter { id ->
            viewModel.onChallengerClicked(id)
        }

        binding.rvAdminChallengerList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (lastVisibleItem >= totalItemCount - 5) {
                    viewModel.fetchNextPage()
                }
            }
        })
    }

    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    groupAdapter.submitList(state.filteredGroups)
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
                showManageDialog(formattedModel, formattedModel.challengerId.toInt())
            }
            is AdminChallengerEvent.ShowToast -> {
                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = if (event.isError) UToast.State.ERROR else UToast.State.CHECK
                ).show()
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