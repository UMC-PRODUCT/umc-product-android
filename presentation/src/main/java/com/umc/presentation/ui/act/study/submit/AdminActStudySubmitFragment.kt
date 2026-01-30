package com.umc.presentation.ui.act.study.submit

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentAdminStudySubmitBinding
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.umc.presentation.databinding.CustomDialogBestBinding
import com.umc.presentation.databinding.CustomDialogReviewBinding



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


    private var bestDialog: AlertDialog? = null
    private var reviewDialog: AlertDialog? = null


    override fun initView() {
        binding.vm = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        adapter = AdminActStudySubmitAdapter(
            onClickBest = { item -> viewModel.onAction(AdminActStudySubmitAction.ClickBest(item)) },
            onClickReview = { item -> viewModel.onAction(AdminActStudySubmitAction.ClickReview(item)) },
        )
        binding.rvSubmit.adapter = adapter

        swipeController = AdminStudySubmitSwipeController(
            recyclerView = binding.rvSubmit,
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

        viewModel.loadDummy()
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

        // 닫기
        dialogBinding.ivClose.setOnClickListener {
            viewModel.onAction(AdminActStudySubmitAction.DismissBestDialog)
            bestDialog?.dismiss()
        }

        dialogBinding.btnCancel.setOnClickListener {
            viewModel.onAction(AdminActStudySubmitAction.DismissBestDialog)
            bestDialog?.dismiss()
        }


        dialogBinding.btnConfirm.setOnClickListener {
            val reason = dialogBinding.etReason.getText()?.toString().orEmpty()

            viewModel.onAction(AdminActStudySubmitAction.ConfirmBest(reason))
            bestDialog?.dismiss()
        }
    }

    private fun showReviewDialog(item: AdminActStudySubmitItemUiModel) {
        reviewDialog?.dismiss()

        val dialogBinding = CustomDialogReviewBinding.inflate(layoutInflater)
        dialogBinding.item = item
        dialogBinding.lifecycleOwner = viewLifecycleOwner


        dialogBinding.etUrl.setText(item.submitUrl)
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
            val url = dialogBinding.etUrl.getText()
            val feedback = dialogBinding.etFeedback.getText()

            viewModel.onAction(AdminActStudySubmitAction.SubmitReview(false, url, feedback))
            reviewDialog?.dismiss()
        }

        dialogBinding.btnApprove.setOnClickListener {
            val url = dialogBinding.etUrl.getText()
            val feedback = dialogBinding.etFeedback.getText()

            viewModel.onAction(AdminActStudySubmitAction.SubmitReview(true, url, feedback))
            reviewDialog?.dismiss()
        }
    }



    override fun initStates() {


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                binding.state = state
                adapter.submitList(state.items)
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is AdminActStudySubmitEvent.ShowBestDialog -> showBestDialog(event.item)
                    is AdminActStudySubmitEvent.ShowReviewDialog -> showReviewDialog(event.item)
                    is AdminActStudySubmitEvent.ShowToast -> {

                    }
                }
            }
        }
    }
}
