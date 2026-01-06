package com.umc.presentation

import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.umc.presentation.base.BaseActivity
import com.umc.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityUiState, MainActivityEvent, MainActivityViewModel>(
    ActivityMainBinding::inflate
) {
    override val viewModel: MainActivityViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
    }

    override fun initState() {
        repeatOnStarted {
            launch {
                viewModel.uiEvent.collect{
                    // TODO 이벤트 처리
                    val navController = findNavController(R.id.main_fragmentContainer)
                    binding.mainBnv.setupWithNavController(navController)

                }
            }

            launch {
                viewModel.uiState.collect {
                    // TODO 상태 관리
                }
            }
        }
    }
}