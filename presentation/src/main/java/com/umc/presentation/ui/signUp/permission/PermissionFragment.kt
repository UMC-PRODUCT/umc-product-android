package com.umc.presentation.ui.signUp.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentPermissionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PermissionFragment :
    BaseFragment<FragmentPermissionBinding, PermissionUiState, PermissionEvent, PermissionViewModel>(
        FragmentPermissionBinding::inflate,
    ) {
    override val viewModel: PermissionViewModel by viewModels()

    private lateinit var requestPermissionsLauncher : ActivityResultLauncher<Array<String>>

    override fun initView() {
        binding.apply {
            vm = viewModel
        }
        requestPermissionsLauncher =
            requireActivity().registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                viewModel.signUp()
            }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect {
                    handleEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                    // TODO 상태 관리
                }
            }
        }
    }

    override fun handleEvent(event: PermissionEvent) {
        when (event) {
            PermissionEvent.MoveToBack -> findNavController().popBackStack()
            PermissionEvent.ShowPermissionDialog -> requestPermissions()
            PermissionEvent.MoveToFailEvent -> moveToFail()
            PermissionEvent.MoveToMainEvent -> moveToMain()
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // 알림 권한
        if (viewModel.uiState.value.isAlarmCheck) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissions += Manifest.permission.POST_NOTIFICATIONS
                }
            }
        }

        // 위치 권한
        if (viewModel.uiState.value.isLocationCheck) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions += Manifest.permission.ACCESS_FINE_LOCATION
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        } else {
            // 이미 모두 허용된 상태
        }
    }

    private fun moveToMain() {
        val action = PermissionFragmentDirections.actionPermissionToHome()
        findNavController().navigate(action)
    }

    private fun moveToFail() {
        val action = PermissionFragmentDirections.actionPermissionToFail()
        findNavController().navigate(action)
    }
}
