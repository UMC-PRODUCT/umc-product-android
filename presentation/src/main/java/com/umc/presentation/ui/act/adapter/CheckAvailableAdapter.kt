package com.umc.presentation.ui.act.adapter

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemActCheckAvailableBinding
import com.umc.presentation.ui.act.check.CheckAvailableUIModel

class CheckAvailableAdapter(
    private val locationSource: FusedLocationSource,
    private val onItemClick: (Long) -> Unit,
    private val onReasonClick: (Long) -> Unit,
    private val onAttendanceRequestClick: (Long) -> Unit
) : ListAdapter<CheckAvailableUIModel, CheckAvailableAdapter.ViewHolder>(
    AvailableSessionDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActCheckAvailableBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActCheckAvailableBinding) :
        RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

        private var naverMap: NaverMap? = null
        private var currentModel: CheckAvailableUIModel? = null
        private var circleOverlay: CircleOverlay? = null

        init {
            // MapView 초기화 시점 최적화
            binding.mapView.onCreate(null)
            binding.mapView.getMapAsync(this)
        }

        fun bind(uiModel: CheckAvailableUIModel) {
            currentModel = uiModel
            binding.uiModel = uiModel

            // 데이터 바인딩 즉시 반영
            binding.executePendingBindings()

            // 지도가 준비된 상태라면 콘텐츠 업데이트
            naverMap?.let { updateMapContent(it) }

            // 출석 요청 버튼 클릭
            binding.btnAttendanceRequest.setOnClickListener {
                onAttendanceRequestClick(uiModel.session.sheetId)
            }

            binding.layoutFailReasonContainer.setOnClickListener {
                if (!uiModel.isWithinRange) {
                    onReasonClick(uiModel.session.sheetId)
                }
            }

            binding.root.setOnClickListener {
                // 펼치기/접기 애니메이션 설정
                val transition = AutoTransition().apply {
                    duration = 300
                    excludeTarget(binding.btnStatusBadge, true)
                }
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
                onItemClick(uiModel.session.id)
            }
        }

        override fun onMapReady(map: NaverMap) {
            this.naverMap = map

            // 초기 UI 및 제스처 설정
            map.uiSettings.apply {
                isZoomControlEnabled = false
                isLocationButtonEnabled = false
                isCompassEnabled = false
                isScaleBarEnabled = false
                setAllGesturesEnabled(false)

                logoGravity = Gravity.TOP or Gravity.START
                setLogoMargin(20, 20, 0, 0)
            }

            updateMapContent(map)
        }

        private fun updateMapContent(map: NaverMap) {
            val model = currentModel ?: return
            val sessionPos = LatLng(model.session.latitude, model.session.longitude)
            val context = binding.root.context

            // 오버레이 객체 재사용 로직
            if (circleOverlay == null) {
                circleOverlay = CircleOverlay().apply {
                    radius = GEOFENCE_RADIUS
                    color = ContextCompat.getColor(context, R.color.geofence_fill)
                    outlineColor = ContextCompat.getColor(context, R.color.geofence_stroke)
                    outlineWidth = GEOFENCE_STROKE_WIDTH
                }
            }

            circleOverlay?.apply {
                center = sessionPos
                this.map = map
            }

            binding.layoutLocationStatus.root.post {
                if (naverMap == null) return@post

                val bottomUiHeight = binding.layoutLocationStatus.root.height
                val density = context.resources.displayMetrics.density
                val marginPx = (6 * density).toInt()

                map.setContentPadding(0, 0, 0, bottomUiHeight + marginPx)

                val cameraUpdate = CameraUpdate.scrollAndZoomTo(sessionPos, CAMERA_ZOOM_LEVEL)
                    .animate(CameraAnimation.None)
                map.moveCamera(cameraUpdate)
            }

            map.locationTrackingMode = LocationTrackingMode.None
            map.locationOverlay.isVisible = false
        }
    }

    class AvailableSessionDiffCallback : DiffUtil.ItemCallback<CheckAvailableUIModel>() {
        override fun areItemsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem.session.id == newItem.session.id

        override fun areContentsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem == newItem
    }

    companion object {
        private const val GEOFENCE_RADIUS = 50.0
        private const val GEOFENCE_STROKE_WIDTH = 3
        private const val CAMERA_ZOOM_LEVEL = 16.0
    }
}