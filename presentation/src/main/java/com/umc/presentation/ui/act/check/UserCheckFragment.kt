package com.umc.presentation.ui.act.check

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.naver.maps.map.util.FusedLocationSource
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.UCheckDialog
import com.umc.presentation.component.UCheckDialogModel
import com.umc.presentation.databinding.FragmentUserCheckBinding
import com.umc.presentation.ui.act.adapter.CheckAvailableAdapter
import com.umc.presentation.ui.act.adapter.CheckHistoryAdapter
import com.umc.presentation.ui.act.adapter.SectionHeaderAdapter
import com.umc.presentation.ui.act.adapter.EmptyStateAdapter
import com.umc.presentation.util.UToast
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
            },
            onReasonClick = { sessionId ->
                showAttendanceReasonDialog(sessionId)
            },
            onAttendanceRequestClick = { sheetId ->
                viewModel.requestAttendance(sheetId)
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

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // 실시간 위치 정보를 뷰모델에 전달하여 거리 계산 수행
                locationResult.lastLocation?.let { location ->
                    viewModel.updateLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    // 실시간 위치 업데이트 시작
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
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
                startLocationUpdates()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun initStates() {
        repeatOnStarted(viewLifecycleOwner) {
            // 리스트 및 카운트 업데이트
            launch {
                viewModel.uiState.collect { state ->
                    // 가용한 세션 업데이트
                    availableAdapter.submitList(state.availableSessions)

                    // 빈 화면 처리 (EmptyStateAdapter)
                    val availableEmptyList = if (state.availableSessions.isEmpty()) {
                        listOf(EmptyStateUIModel(R.drawable.ic_people, getString(R.string.attendance_empty_available)))
                    } else emptyList()
                    availableEmptyAdapter.submitList(availableEmptyList)

                    // 출석 히스토리 업데이트
                    historyAdapter.submitList(state.attendanceHistories)
                    val historyEmptyList = if (state.attendanceHistories.isEmpty()) {
                        listOf(EmptyStateUIModel(R.drawable.ic_checkbook, getString(R.string.attendance_empty_history)))
                    } else emptyList()
                    historyEmptyAdapter.submitList(historyEmptyList)
                }
            }

            // 일회성 액션(토스트, 다이얼로그) 처리
            launch {
                viewModel.uiEvent.collect { event -> handleEvent(event) }
            }
        }

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

            (itemAnimator as? DefaultItemAnimator)?.apply {
                supportsChangeAnimations = false
            }
        }
    }

    private fun checkLocationPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val isAllGranted = permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }
        if (!isAllGranted) requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun handleEvent(event: UserCheckEvent) {
        when (event) {
            is UserCheckEvent.ShowReasonDialog -> showAttendanceReasonDialog(event.sessionId)
            is UserCheckEvent.ShowToast -> {
                val state = if (event.isError) UToast.State.ERROR else UToast.State.CHECK
                UToast.createToast(
                    context = requireContext(),
                    message = event.message,
                    state = state
                ).show()
            }
            is UserCheckEvent.NavigateToFailureReason -> {

            }
        }
    }

    private fun showAttendanceReasonDialog(sessionId: Long) {
        val model = UCheckDialogModel(
            title = getString(R.string.attendance_reason_dialog_title),
            subtitle = getString(R.string.attendance_reason_guide),
            positiveText = getString(R.string.common_submit),
            isWriteMode = true
        )

        UCheckDialog(model) { reason ->
            if (reason.isNotBlank()) {
                viewModel.submitAttendanceReason(sessionId, reason)
            }
        }.show(childFragmentManager, "UCheckDialog")
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroyView() {
        binding.rvUserCheckMain.itemAnimator = null
        binding.rvUserCheckMain.adapter = null
        super.onDestroyView()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}