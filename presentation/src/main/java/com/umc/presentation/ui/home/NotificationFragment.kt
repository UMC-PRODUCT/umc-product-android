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
class NotificationFragment : BaseFragment<FragmentNotificationBinding, NotificationFragmentUiState, NotificationFragmentEvent, NotificationViewModel>(
    FragmentNotificationBinding::inflate,
){
    override val viewModel: NotificationViewModel by viewModels()

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
            
            launch { 
                viewModel.uiEvent.collect {
                    handleMoveEvent(it)
                }
            }
        }
    }

    //이벤트 핸들링(얘를 observing 해서 쭈욱 보고 이벤트 처리)
    private fun handleMoveEvent(event: NotificationFragmentEvent){
        when (event){
            is NotificationFragmentEvent.MoveBackPressedEvent -> moveBackPressed()

            else -> {}
        }
    }
    
    //뒤로 가기 고우
    private fun moveBackPressed(){
        requireActivity().onBackPressedDispatcher.onBackPressed()
    
    }


}