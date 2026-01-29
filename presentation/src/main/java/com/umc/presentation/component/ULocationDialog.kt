package com.umc.presentation.component

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.umc.presentation.databinding.CustomDialogLocationBinding

class ULocationChangeDialog(
    private val initialLat: Double,
    private val initialLng: Double,
    private val onLocationChanged: (String, Double, Double) -> Unit
) : DialogFragment(), OnMapReadyCallback {

    private var _binding: CustomDialogLocationBinding? = null
    private val binding get() = _binding!!
    private var naverMap: NaverMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomDialogLocationBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        initEvent()
    }

    private fun initEvent() {
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnChange.setOnClickListener {
            val targetPos = naverMap?.cameraPosition?.target
            onLocationChanged(
                "",
                targetPos?.latitude ?: initialLat,
                targetPos?.longitude ?: initialLng
            )
            dismiss()
        }
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map
        val initialPos = LatLng(initialLat, initialLng)

        // 카메라를 초기 위치로 이동
        map.moveCamera(CameraUpdate.scrollTo(initialPos))
        map.moveCamera(CameraUpdate.zoomTo(15.0))

        // UI 및 제스처 설정
        map.uiSettings.apply {
            isScrollGesturesEnabled = false   // 드래그(스크롤) 방지
            isZoomGesturesEnabled = false     // 줌(확대/축소) 방지
            isTiltGesturesEnabled = false     // 기울이기 방지
            isRotateGesturesEnabled = false   // 회전 방지
            isZoomControlEnabled = false      // +/- 확대 버튼 숨기기
            isLocationButtonEnabled = false  // 내위치 버튼 숨기기
        }
    }

    override fun onStart() { super.onStart(); binding.mapView.onStart() }
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        // 다이얼로그 너비 설정
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }
    override fun onPause() { super.onPause(); binding.mapView.onPause() }
    override fun onStop() { super.onStop(); binding.mapView.onStop() }
    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
        _binding = null
    }
}