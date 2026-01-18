package com.umc.presentation

import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.umc.presentation.base.BaseActivity
import com.umc.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.graphics.drawable.toDrawable
import com.google.firebase.messaging.FirebaseMessaging

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding, MainActivityUiState, MainActivityEvent, MainActivityViewModel>(
    ActivityMainBinding::inflate,
) {
    override val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navController: NavController

    override fun initView() {
        //enableEdgeToEdge()

        binding.apply {
            vm = viewModel
            initNavigation()
            checkFCMToken()

            window.setBackgroundDrawable(getColor(R.color.neutral000).toDrawable())
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

    // 토큰 세팅 체크
    private fun checkFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("log_fcm", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }

            // 현재 기기의 토큰 확인
            val token = task.result
            Log.d("log_fcm", "현재 기기 토큰: $token")
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
            R.id.homeFragment,
            R.id.mypageFragment,
            R.id.activityManagementFragment -> {
                binding.mainBnv.visibility = View.VISIBLE
            }
            else -> binding.mainBnv.visibility = View.GONE
        }
    }
}
