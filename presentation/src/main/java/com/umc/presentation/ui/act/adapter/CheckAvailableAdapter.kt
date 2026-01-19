package com.umc.presentation.ui.act.adapter

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.umc.domain.model.enums.CheckAvailableStatus
import com.umc.presentation.databinding.ItemActCheckAvailableBinding
import com.umc.presentation.databinding.LayoutCustomMarkerBinding
import com.umc.presentation.ui.act.check.CheckAvailableUIModel

class CheckAvailableAdapter(
    private val locationSource: FusedLocationSource,
    private val onItemClick: (Int) -> Unit
) : ListAdapter<CheckAvailableUIModel, CheckAvailableAdapter.ViewHolder>(
    AvailableSessionDiffCallback()
) {

    private data class ButtonSize(val width: Int, val height: Int)
    private val buttonSizeCache = mutableMapOf<String, ButtonSize>()

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

        private var mapView: MapView? = null
        private var currentModel: CheckAvailableUIModel? = null

        fun bind(uiModel: CheckAvailableUIModel) {
            currentModel = uiModel
            binding.uiModel = uiModel
            cleanupMapView()

            if (uiModel.isExpanded) {
                initMapView()
            }

            binding.executePendingBindings()

            binding.btnStatusBadge.post {
                val cacheKey = "${uiModel.session.id}_${uiModel.session.status}"
                val cachedSize = buttonSizeCache[cacheKey]
                val params = binding.btnStatusBadge.layoutParams
                if (cachedSize != null) {
                    params.width = cachedSize.width
                    params.height = cachedSize.height
                } else {
                    val measuredWidth = binding.btnStatusBadge.width
                    val measuredHeight = binding.btnStatusBadge.height
                    buttonSizeCache[cacheKey] = ButtonSize(measuredWidth, measuredHeight)
                    params.width = measuredWidth
                    params.height = measuredHeight
                }
                binding.btnStatusBadge.layoutParams = params
            }

            binding.root.setOnClickListener {
                val transition = AutoTransition().apply {
                    duration = 300
                    excludeTarget(binding.btnStatusBadge, true)
                }
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)
                onItemClick(uiModel.session.id)
            }
        }

        private fun initMapView() {
            mapView = MapView(binding.root.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCreate(null)
                getMapAsync(this@ViewHolder)
            }
            binding.mapContainer.addView(mapView)
        }

        fun cleanupMapView() {
            mapView?.let {
                it.onPause()
                it.onStop()
                it.onDestroy()
            }
            binding.mapContainer.removeAllViews()
            mapView = null
        }

        override fun onMapReady(map: NaverMap) {
            val model = currentModel ?: return
            val sessionPos = LatLng(model.session.latitude, model.session.longitude)

            // 마커 이미지 생성 및 설정
            val markerBinding = LayoutCustomMarkerBinding.inflate(LayoutInflater.from(binding.root.context))
            val customIcon = OverlayImage.fromView(markerBinding.root)

            Marker().apply {
                position = sessionPos
                icon = customIcon
                anchor = android.graphics.PointF(0.5f, 0.5f)
                this.map = map
            }

            binding.layoutLocationStatus.root.post {
                val bottomUiHeight = binding.layoutLocationStatus.root.height
                val density = binding.root.context.resources.displayMetrics.density
                val marginPx = (6 * density).toInt()

                map.setContentPadding(0, 0, 0, bottomUiHeight + marginPx)
                map.moveCamera(CameraUpdate.scrollTo(sessionPos))
            }
            // UI 및 제스처 설정
            map.uiSettings.apply {
                isZoomControlEnabled = false
                isLocationButtonEnabled = false
                isCompassEnabled = false
                isScaleBarEnabled = false

                setAllGesturesEnabled(false)

                logoGravity = Gravity.TOP or Gravity.START
                setLogoMargin(20, 20, 0, 0)
            }

            if (model.session.status == CheckAvailableStatus.BEFORE) {
                map.locationSource = locationSource
                map.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.cleanupMapView()
        super.onViewRecycled(holder)
    }

    class AvailableSessionDiffCallback : DiffUtil.ItemCallback<CheckAvailableUIModel>() {
        override fun areItemsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem.session.id == newItem.session.id
        override fun areContentsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem == newItem
    }
}