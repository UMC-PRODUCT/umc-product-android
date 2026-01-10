package com.umc.presentation.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentNotificationBinding
import kotlinx.coroutines.launch


//알람 내용
class NotificationFragment : BaseFragment<FragmentNotificationBinding, NotificationFragmentUiState, NotificationFragmentEvent, NotificationFragmentViewModel>(
    FragmentNotificationBinding::inflate,
){
    override val viewModel: NotificationFragmentViewModel by viewModels()

    //리사이클러 어댑터
    private val adapter by lazy { NotificationAdapter() }


    override fun initView() {
        binding.apply {
            vm = viewModel
        }
        binding.notificationRecyclerview.adapter = adapter

    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.notifications)
                }

            }
        }
    }


}