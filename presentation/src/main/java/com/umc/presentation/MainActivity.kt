package com.umc.presentation

import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.umc.presentation.base.BaseActivity
import com.umc.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityUiState, MainActivityEvent, MainActivityViewModel>(
    ActivityMainBinding::inflate,
) {
    override val viewModel: MainActivityViewModel by viewModels()

    override fun initView() {
        binding.apply {
            vm = viewModel
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        binding.mainBnv.setupWithNavController(navController)

    }

    override fun initState() {
        repeatOnStarted {
            launch {
                viewModel.uiEvent.collect {
                    // TODO 이벤트 처리

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
