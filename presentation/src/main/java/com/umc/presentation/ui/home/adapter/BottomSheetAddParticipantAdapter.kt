package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.databinding.ItemBottomSheetParticipantAddBinding
import androidx.recyclerview.widget.DiffUtil


// 인원 추가/삭제 관련 통합 델리게이트
interface AddParticipantDelegate {
    fun onParticipantRemoved(item: ParticipantItem) // 추가 목록 삭제용
}

class BottomSheetAddParticipantAdapter(
    private val delegate: AddParticipantDelegate
) : ListAdapter<ParticipantItem, BottomSheetAddParticipantAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemBottomSheetParticipantAddBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ParticipantItem) {
            binding.item = item
            binding.itemTvName.text = item.name
            binding.itemTvSchool.text = item.school

            // 삭제 버튼 클릭 시 이벤트 전달
            binding.itemBtnDelete.setOnClickListener {
                delegate.onParticipantRemoved(item)
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomSheetParticipantAddBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ParticipantItem>() {
            override fun areItemsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem) = oldItem == newItem
        }
    }
}