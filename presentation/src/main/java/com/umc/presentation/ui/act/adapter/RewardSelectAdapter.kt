package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.RewardType
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemRewardSelectBinding


interface RewardSelectDelegate {
    fun onClickReward(item: RewardType)
}



class RewardSelectAdapter(
    private val listener: RewardSelectDelegate
) : ListAdapter<RewardType, RecyclerView.ViewHolder>(
    RewardSelectDiffCallBack()
) {

    private var selectedReward: RewardType? = null

    fun updateSelection(newSelection: RewardType?) {
        if (selectedReward == newSelection) return

        val oldIndex = currentList.indexOf(selectedReward)
        val newIndex = currentList.indexOf(newSelection)

        selectedReward = newSelection

        // 변경된 두 포지션만 부분 업데이트하여 성능 최적화
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardSelectViewHolder {
        val binding = ItemRewardSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RewardSelectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RewardSelectViewHolder).bind(getItem(position))
    }

    inner class RewardSelectViewHolder(private val binding: ItemRewardSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RewardType) {
            binding.item = item

            // 현재 아이템이 선택된 아이템인지 여부 확인
            val isSelected = (item == selectedReward)
            binding.isSelected = isSelected

            //점수 색상 및 텍스트 설정
            val scoreText = if (item.score > 0) "+${item.score}" else "${item.score}"
            binding.cdvScore.setText(scoreText)

            val scoreColor = if (item.score > 0) R.color.success100 else R.color.danger100
            val scoreTextColor = if (item.score > 0) R.color.success500 else R.color.danger500
            binding.cdvScore.setUBackgroundColor(ContextCompat.getColor(binding.root.context, scoreColor))
            binding.cdvScore.setTextColor(ContextCompat.getColor(binding.root.context, scoreTextColor))


            //라디오 버튼 상태 업데이트
            val radioIcon = if (isSelected) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
            binding.imvSelect.setImageResource(radioIcon)

            //클릭 리스너
            binding.root.setOnClickListener {
                listener.onClickReward(item)
            }

            binding.executePendingBindings()
        }
    }

}


class RewardSelectDiffCallBack : DiffUtil.ItemCallback<RewardType>() {
    override fun areItemsTheSame(oldItem: RewardType, newItem: RewardType): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: RewardType, newItem: RewardType): Boolean = oldItem == newItem
}