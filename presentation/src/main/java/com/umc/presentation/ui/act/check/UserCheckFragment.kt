package com.umc.presentation.ui.act.check

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.naver.maps.map.util.FusedLocationSource
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.FragmentUserCheckBinding
import com.umc.presentation.ui.act.adapter.CheckAvailableAdapter
import com.umc.presentation.ui.act.adapter.CheckHistoryAdapter
import com.umc.presentation.ui.act.adapter.SectionHeaderAdapter
import com.umc.presentation.ui.act.adapter.EmptyStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserCheckFragment : BaseFragment<FragmentUserCheckBinding, UserCheckUiState, UserCheckEvent, UserCheckViewModel>(
    FragmentUserCheckBinding::inflate
) {
    override val viewModel: UserCheckViewModel by viewModels()

    // 네이버 지도 UI
    private lateinit var locationSource: FusedLocationSource

    // 실시간 위치 계산
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 어댑터 설정
    private val availableHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_available)) }

    private val availableAdapter by lazy {
        CheckAvailableAdapter(
            locationSource = locationSource,
            onItemClick = { sessionId ->
                viewModel.toggleSessionExpansion(sessionId)
            }
        )
    }
    private val availableEmptyAdapter by lazy { EmptyStateAdapter() }
    private val historyHeaderAdapter by lazy { SectionHeaderAdapter(getString(R.string.attendance_header_history)) }
    private val historyAdapter by lazy { CheckHistoryAdapter() }
    private val historyEmptyAdapter by lazy { EmptyStateAdapter() }

    override fun initView() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationCallback()

        checkLocationPermissions()
        setupMainRecyclerView()
    }

    // 위치 업데이트 콜백 설정
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    viewModel.updateLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    // 실시간 위치 업데이트 시작
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 5초마다
            .setMinUpdateIntervalMillis(3000)
            .build()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    @Deprecated("Deprecated in Java")
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (locationSource.isActivated) {
                startLocationUpdates() // 권한 허용 시 위치 업데이트 시작
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiState.collect { state ->
                    availableAdapter.submitList(state.availableSessions)
                    availableHeaderAdapter.updateCount(state.availableCount)

                    if (state.availableSessions.isEmpty()) {
                        availableEmptyAdapter.submitList(listOf(EmptyStateUIModel(R.drawable.ic_people, getString(R.string.attendance_empty_available))))
                    } else {
                        availableEmptyAdapter.submitList(emptyList())
                    }

                    historyAdapter.submitList(state.attendanceHistories)
                    if (state.attendanceHistories.isEmpty()) {
                        historyEmptyAdapter.submitList(listOf(EmptyStateUIModel(R.drawable.ic_document, getString(R.string.attendance_empty_history))))
                    } else {
                        historyEmptyAdapter.submitList(emptyList())
                    }
                }
            }
            launch {
                viewModel.uiEvent.collect { event -> handleEvent(event) }
            }
        }

        // 초기 권한이 이미 있다면 업데이트 시작
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    private fun setupMainRecyclerView() {
        val concatAdapter = ConcatAdapter(
            availableHeaderAdapter, availableAdapter, availableEmptyAdapter,
            historyHeaderAdapter, historyAdapter, historyEmptyAdapter
        )
        binding.rvUserCheckMain.apply {
            adapter = concatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun checkLocationPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val isAllGranted = permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
        if (!isAllGranted) requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun handleEvent(event: UserCheckEvent) {
        when (event) {
            is UserCheckEvent.ShowToast -> { /* 토스트 구현 */ }
            is UserCheckEvent.NavigateToFailureReason -> { /* 이동 구현 */ }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        binding.rvUserCheckMain.adapter = null
        super.onDestroyView()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}