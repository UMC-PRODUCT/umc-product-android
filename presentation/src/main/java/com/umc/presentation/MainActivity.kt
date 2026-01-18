package com.umc.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한 허용
            Log.d("log_fcm", "알림 권한이 허용되었습니다.")
        } else {
            // 권한 거부
            Log.d("log_fcm", "알림 권한이 거부되었습니다.")
        }
    }


    override fun initView() {
        //enableEdgeToEdge()

        binding.apply {
            vm = viewModel
            initNavigation()
            checkFCMToken() //FCM 토큰 체크
            checkPermissionNotification() //알람 권한 체크

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

    // 알람 권한을 요청
    private fun checkPermissionNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // 이미 권한이 허용된 경우
                Log.d("log_fcm", "이미 알림 권한이 있습니다.")
            } else {
                // 권한이 없다면 유저에게 요청
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
            R.id.homeFragment,
            R.id.mypageFragment,
            R.id.activityManagementFragment -> {
                binding.mainBnv.visibility = View.VISIBLE
            }
            else -> binding.mainBnv.visibility = View.GONE
        }
    }
}
