package com.umc.presentation.ui.act.adapter

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActCheckAvailableBinding
import com.umc.presentation.ui.act.check.CheckAvailableUIModel

class CheckAvailableAdapter(
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
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uiModel: CheckAvailableUIModel) {
            binding.uiModel = uiModel
            binding.executePendingBindings()

            // 버튼 너비와 높이를 캐시에서 가져오거나 측정 후 고정
            binding.btnStatusBadge.post {
                // 세션 ID + 상태를 조합한 키로 캐시 관리
                val cacheKey = "${uiModel.session.id}_${uiModel.session.status}"
                val cachedSize = buttonSizeCache[cacheKey]

                if (cachedSize != null) {
                    // 캐시된 크기 사용
                    val params = binding.btnStatusBadge.layoutParams
                    params.width = cachedSize.width
                    params.height = cachedSize.height
                    binding.btnStatusBadge.layoutParams = params
                } else {
                    // 처음 측정 후 캐시에 저장하고 고정
                    val measuredWidth = binding.btnStatusBadge.width
                    val measuredHeight = binding.btnStatusBadge.height
                    buttonSizeCache[cacheKey] = ButtonSize(measuredWidth, measuredHeight)
                    val params = binding.btnStatusBadge.layoutParams
                    params.width = measuredWidth
                    params.height = measuredHeight
                    binding.btnStatusBadge.layoutParams = params
                }
            }

            binding.root.setOnClickListener {
                // 확장/축소 시 애니메이션 효과 - 현재 아이템만 적용
                val transition = AutoTransition().apply {
                    duration = 300
                    // 상태 버튼, 제목, 드롭다운 아이콘은 애니메이션에서 제외
                    excludeTarget(binding.btnStatusBadge, true)
                    excludeTarget(binding.tvSessionTitle, true)
                    excludeTarget(binding.ivDropdownArrow, true)
                }
                TransitionManager.beginDelayedTransition(binding.root as ViewGroup, transition)

                onItemClick(uiModel.session.id)
            }
        }
    }

    class AvailableSessionDiffCallback : DiffUtil.ItemCallback<CheckAvailableUIModel>() {
        override fun areItemsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem.session.id == newItem.session.id

        override fun areContentsTheSame(oldItem: CheckAvailableUIModel, newItem: CheckAvailableUIModel) =
            oldItem == newItem
    }
}