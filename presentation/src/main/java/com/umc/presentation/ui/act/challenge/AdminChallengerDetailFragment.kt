package com.umc.presentation.ui.act.challenge

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UBasicDialog
import com.umc.presentation.component.UBasicDialogModel
import com.umc.presentation.databinding.FragmentAdminChallengerDetailBinding
import com.umc.presentation.ui.act.adapter.ChallengerHistoryAdapter
import com.umc.presentation.util.UToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminChallengerDetailFragment : BaseFragment<
        FragmentAdminChallengerDetailBinding,
        AdminChallengerDetailUiState,
        AdminChallengerDetailEvent,
        AdminChallengerDetailViewModel>(
    FragmentAdminChallengerDetailBinding::inflate
) {
    override val viewModel: AdminChallengerDetailViewModel by viewModels()

    private lateinit var historyAdapter: ChallengerHistoryAdapter

    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.btnBack.setOnClickListener {
            moveToChallengerTab()
        }

        setupHistoryRecycler()
        setupButtons()
    }

    private fun setupHistoryRecycler() {
        historyAdapter = ChallengerHistoryAdapter(
            onDeleteClick = { point ->
                val warningDialog = UBasicDialog(
                    model = UBasicDialogModel.Warning(
                        title = "해당 기록을 삭제하시겠습니까?",
                        content = "삭제된 기록은 복구가 어렵습니다.",
                        positiveText = "삭제하기"
                    ),
                    onConfirm = {
                        viewModel.deletePoint(point.id)
                        viewModel.exitEditMode()
                    }
                )

                warningDialog.show(childFragmentManager, "DeleteWarningDialog")

                childFragmentManager.executePendingTransactions()

                (childFragmentManager.findFragmentByTag("DeleteWarningDialog") as? UBasicDialog)
                    ?.dialog
                    ?.setOnDismissListener {
                        viewModel.exitEditMode()
                    }
            }
        )

        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        }
    }

    private fun setupButtons() {
        binding.layoutAddPositive.setOnClickListener {
            UToast.createToast(
                context = requireContext(),
                message = "어헛차!!",
                state = UToast.State.CHECK
            ).show()
        }

        binding.layoutAddNegative.setOnClickListener {
            UToast.createToast(
                context = requireContext(),
                message = "어헛차! 화이팅",
                state = UToast.State.CHECK
            ).show()
        }

        binding.layoutAddEtc.setOnClickListener {
            UToast.createToast(
                context = requireContext(),
                message = "어헛차! 아잣스!",
                state = UToast.State.CHECK
            ).show()
        }

        binding.btnEditHistoryToggle.setOnClickListener {
            viewModel.toggleEditMode()
        }
    }



    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    binding.state = state
                    binding.model = state.model
                    historyAdapter.setEditMode(state.isEditMode)
                    historyAdapter.submitList(state.model?.history ?: emptyList())

                    binding.btnEditHistoryToggle.visibility =
                        if (state.isEditMode) View.GONE else View.VISIBLE
                }
            }

            launch {
                viewModel.uiEvent.collect { handleEvent(it) }
            }
        }
    }

    override fun handleEvent(event: AdminChallengerDetailEvent) {
        when (event) {
            is AdminChallengerDetailEvent.ShowToast -> {
                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = if (event.isError) UToast.State.ERROR else UToast.State.CHECK
                ).show()
            }

            AdminChallengerDetailEvent.NavigateBack -> {
                moveToChallengerTab()
            }
        }
    }

    private fun moveToChallengerTab() {
        val nav = findNavController()
        val actEntry = nav.getBackStackEntry(R.id.activityManagementFragment)

        actEntry.savedStateHandle["ACT_TARGET_TAB"] = 2

        nav.popBackStack(R.id.activityManagementFragment, false)
    }
}