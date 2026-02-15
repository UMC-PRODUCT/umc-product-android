package com.umc.presentation.ui.notice.detail.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.BottomSheetNoticeConfirmedPeopleBinding
import com.umc.presentation.extension.gone
import com.umc.presentation.extension.visible
import com.umc.presentation.ui.notice.detail.NoticeDetailViewModel
import com.umc.presentation.ui.notice.detail.NoticeFragmentEvent
import com.umc.presentation.ui.notice.detail.adapter.NoticePeopleAdapter
import kotlinx.coroutines.launch

class NoticeConfirmBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: NoticeDetailViewModel by activityViewModels()

    private val noticePeopleAdapter: NoticePeopleAdapter by lazy { NoticePeopleAdapter() }

    private var isShowingRead: Boolean = true

    private var _binding: BottomSheetNoticeConfirmedPeopleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetNoticeConfirmedPeopleBinding.inflate(inflater, container, false)
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

            ubuttonConfirm.setOnClickListener {
                onClickConfirmButton()
            }

            ubuttonUnconfirm.setOnClickListener {
                onClickUnConfirmButton()
            }

            ubuttonSendNotification.setOnClickListener {
                onClickSendReminder()
            }
        }

        observeUiState()
        observeUiEvent()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        // 통계 정보 업데이트
                        state.readStatistics?.let { stats ->
                            ubuttonConfirm.setText("확인 ${stats.readCount}")
                            ubuttonUnconfirm.setText("미확인 ${stats.unreadCount}")
                        }

                        // 리스트 업데이트
                        noticePeopleAdapter.submitList(state.readStatusList)

                        // 로딩 표시
                        if (state.isLoadingReadStatus) {
                            // TODO: 로딩 UI 표시
                        }

                        // 알림 발송 중 표시
                        if (state.isSendingReminder) {
                            ubuttonSendNotification.isEnabled = false
                        } else {
                            ubuttonSendNotification.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun observeUiEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is NoticeFragmentEvent.ShowSuccess -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                        is NoticeFragmentEvent.ShowError -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun onClickUnConfirmButton() {
        isShowingRead = false
        binding.apply {
            ubuttonUnconfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral000))
            ubuttonUnconfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral800))
            ubuttonConfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral100))
            ubuttonConfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral400))
            ubuttonSendNotification.visible()
        }

        viewModel.loadReadStatus("UNREAD")
    }

    private fun onClickConfirmButton() {
        isShowingRead = true
        binding.apply {
            ubuttonConfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral000))
            ubuttonConfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral800))
            ubuttonUnconfirm.setUBackgroundColor(ContextCompat.getColor(root.context, R.color.neutral100))
            ubuttonUnconfirm.setTextColor(ContextCompat.getColor(root.context, R.color.neutral400))
            ubuttonSendNotification.gone()
        }

        viewModel.loadReadStatus("READ")
    }

    private fun onClickSendReminder() {
        // 미확인자에게 알림 발송
        val unreadChallengerIds = viewModel.uiState.value.readStatusList
            .map { it.challengerId }
        
        if (unreadChallengerIds.isNotEmpty()) {
            viewModel.onClickSendReminder(unreadChallengerIds)
        } else {
            Toast.makeText(requireContext(), "알림을 보낼 대상이 없습니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        _binding = null
    }
}