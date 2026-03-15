package com.umc.presentation.ui.act.study.submit

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.CustomDialogBestBinding
import com.umc.presentation.databinding.CustomDialogReviewBinding
import com.umc.presentation.databinding.FragmentAdminStudySubmitBinding
import com.umc.presentation.ui.act.study.submit.adapter.AdminActStudySubmitAdapter
import com.umc.presentation.ui.act.study.submit.adapter.AdminStudySubmitSwipeController
import com.umc.presentation.ui.act.study.submit.bottomsheet.AdminActStudySubmitGroupSelectBottomSheet
import com.umc.presentation.ui.act.study.submit.bottomsheet.AdminActStudySubmitWeekSelectBottomSheet
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitAction
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitEvent
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitItemUiModel
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitState
import com.umc.presentation.ui.act.study.submit.model.AdminActStudySubmitViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActStudySubmitFragment :
    BaseFragment<
            FragmentAdminStudySubmitBinding,
            AdminActStudySubmitState,
            AdminActStudySubmitEvent,
            AdminActStudySubmitViewModel
            >(FragmentAdminStudySubmitBinding::inflate) {

    override val viewModel: AdminActStudySubmitViewModel by viewModels()

    private lateinit var adapter: AdminActStudySubmitAdapter
    private lateinit var swipeController: AdminStudySubmitSwipeController

    private var bestDialog: androidx.appcompat.app.AlertDialog? = null
    private var reviewDialog: androidx.appcompat.app.AlertDialog? = null


    private var latestState: AdminActStudySubmitState = AdminActStudySubmitState()

    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.state = AdminActStudySubmitState()


        adapter = AdminActStudySubmitAdapter(
            onClickBest = { item -> viewModel.onAction(AdminActStudySubmitAction.ClickBest(item)) },
            onClickReview = { item -> viewModel.onAction(AdminActStudySubmitAction.ClickReview(item)) },
        )
        binding.rvSubmit.adapter = adapter

        swipeController = AdminStudySubmitSwipeController(
            recyclerView = binding.rvSubmit,
            isBestEnabled = { pos -> adapter.currentList.getOrNull(pos)?.isBestEnabled == true },
            isReviewEnabled = { pos -> adapter.currentList.getOrNull(pos)?.isReviewEnabled == true },
            onClickBest = { position ->
                adapter.currentList.getOrNull(position)?.let { item ->
                    viewModel.onAction(AdminActStudySubmitAction.ClickBest(item))
                }
            },
            onClickReview = { position ->
                adapter.currentList.getOrNull(position)?.let { item ->
                    viewModel.onAction(AdminActStudySubmitAction.ClickReview(item))
                }
            }
        )
        ItemTouchHelper(swipeController).attachToRecyclerView(binding.rvSubmit)


        binding.clWeekDropdown.setOnClickListener {
            AdminActStudySubmitWeekSelectBottomSheet(
                weeks = latestState.availableWeeks,
                onSelect = { week ->
                    viewModel.onAction(AdminActStudySubmitAction.SelectWeek(week))
                }
            ).show(parentFragmentManager, "admin_act_study_submit_week")
        }



        binding.clGroupDropdown.setOnClickListener {
            AdminActStudySubmitGroupSelectBottomSheet(
                groups = viewModel.getGroupNames(latestState),
                onSelect = { name ->
                    viewModel.onAction(AdminActStudySubmitAction.SelectGroupName(name))
                }
            ).show(parentFragmentManager, "admin_act_study_submit_group")
        }


    }

    override fun initStates() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                latestState = state
                binding.state = state
                adapter.submitList(state.items)
                val loading = viewModel.isLoading.value
                binding.tvEmpty.visibility =
                    if (!loading && state.items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is AdminActStudySubmitEvent.ShowBestDialog -> showBestDialog(event.item)
                    is AdminActStudySubmitEvent.ShowReviewDialog -> showReviewDialog(event.item)
                    is AdminActStudySubmitEvent.ShowToast -> showToast(event.message)
                }
            }
        }
    }

    private fun showBestDialog(item: AdminActStudySubmitItemUiModel) {
        bestDialog?.dismiss()

        val dialogBinding = CustomDialogBestBinding.inflate(layoutInflater)
        dialogBinding.item = item
        dialogBinding.lifecycleOwner = viewLifecycleOwner

        bestDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        bestDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bestDialog?.show()



        dialogBinding.btnCancel.setOnClickListener {
            viewModel.onAction(AdminActStudySubmitAction.DismissBestDialog)
            bestDialog?.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            val reason = dialogBinding.etReason.getText()?.toString()?.trim().orEmpty()
            viewModel.onAction(
                AdminActStudySubmitAction.ConfirmBest(
                    reason = reason.ifBlank { null }
                )
            )
            bestDialog?.dismiss()
        }
    }

    private fun showReviewDialog(item: AdminActStudySubmitItemUiModel) {
        reviewDialog?.dismiss()

        val dialogBinding = CustomDialogReviewBinding.inflate(layoutInflater)
        dialogBinding.item = item
        dialogBinding.lifecycleOwner = viewLifecycleOwner


        dialogBinding.etUrl.setText(item.submitUrl.orEmpty())
        dialogBinding.etUrl.setReadOnly(true)
        dialogBinding.etUrl.setReadOnlyStyle()

        dialogBinding.btnGo.setOnClickListener {
            val raw = item.submitUrl.orEmpty().trim()
            if (raw.isBlank()) return@setOnClickListener

            val url =
                if (raw.startsWith("http://") || raw.startsWith("https://")) raw
                else "https://$raw"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            dialogBinding.root.context.startActivity(intent)
        }

        reviewDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        reviewDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        reviewDialog?.show()

        dialogBinding.ivClose.setOnClickListener {
            viewModel.onAction(AdminActStudySubmitAction.DismissReviewDialog)
            reviewDialog?.dismiss()
        }

        dialogBinding.btnReject.setOnClickListener {
            val url = dialogBinding.etUrl.getText()?.toString().orEmpty()
            val feedback = dialogBinding.etFeedback.getText()?.toString().orEmpty()

            viewModel.onAction(AdminActStudySubmitAction.SubmitReview(false, url, feedback))
            reviewDialog?.dismiss()
        }

        dialogBinding.btnApprove.setOnClickListener {
            val url = dialogBinding.etUrl.getText()?.toString().orEmpty()
            val feedback = dialogBinding.etFeedback.getText()?.toString().orEmpty()

            viewModel.onAction(AdminActStudySubmitAction.SubmitReview(true, url, feedback))
            reviewDialog?.dismiss()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
