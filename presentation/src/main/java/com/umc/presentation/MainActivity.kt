package com.umc.presentation

import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
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
    private lateinit var navController: NavController

    override fun initView() {
        binding.apply {
            vm = viewModel
            initNavigation()
        }
        

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

    // Navigation 세팅
    private fun initNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController
        binding.mainBnv.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            changeBottomNavigationView(destination.id)
        }
    }

    private fun changeBottomNavigationView(id: Int) {
        // TODO 다른 화면들도 정의해야 함.
        when (id) {
            R.id.homeFragment -> {
                binding.mainBnv.visibility = View.VISIBLE
            }
            else -> binding.mainBnv.visibility = View.GONE
        }
    }
}
