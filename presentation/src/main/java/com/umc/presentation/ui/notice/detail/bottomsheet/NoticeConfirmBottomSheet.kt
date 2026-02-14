package com.umc.presentation.ui.notice.detail.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetNoticeConfirmedPeopleBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible
import com.umc.presentation.ui.notice.detail.NoticeDetailViewModel
import com.umc.presentation.ui.notice.detail.adapter.NoticePeopleAdapter

class NoticeConfirmBottomSheet: BottomSheetDialogFragment() {

    private val viewModel: NoticeDetailViewModel by activityViewModels()

    private val noticePeopleAdapter : NoticePeopleAdapter by lazy { NoticePeopleAdapter() }

    private val binding: BottomSheetNoticeConfirmedPeopleBinding by lazy {
        BottomSheetNoticeConfirmedPeopleBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            recyclerPeople.apply {
                adapter = noticePeopleAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }

            //noticePeopleAdapter.submitList(viewModel.uiState.value.detail.userList.filter { !it.isCheck } )

            //ubuttonConfirm.setText("확인 ${viewModel.uiState.value.detail.nowReceiverCount}")
            ubuttonConfirm.setOnClickListener {
                onClickConfirmButton()
            }

            //ubuttonUnconfirm.setText("미확인 ${viewModel.uiState.value.detail.allReceiverCount - viewModel.uiState.value.detail.nowReceiverCount}")
            ubuttonUnconfirm.setOnClickListener {
                onClickUnConfirmButton()
            }

            ubuttonSendNotification.setOnClickListener {
                //TODO
            }
        }
    }

    private fun onClickUnConfirmButton() {
        binding.apply {
            ubuttonUnconfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral000))
            ubuttonUnconfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral800))
            ubuttonConfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral100))
            ubuttonConfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral400))
            ubuttonSendNotification.visible()
        }

        //noticePeopleAdapter.submitList(viewModel.uiState.value.detail.userList.filter { !it.isCheck } )
    }


    private fun onClickConfirmButton() {
        binding.apply {
            ubuttonConfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral000))
            ubuttonConfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral800))
            ubuttonUnconfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral100))
            ubuttonUnconfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral400))

            ubuttonSendNotification.gone()
        }

        //noticePeopleAdapter.submitList(viewModel.uiState.value.detail.userList.filter { it.isCheck } )
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}