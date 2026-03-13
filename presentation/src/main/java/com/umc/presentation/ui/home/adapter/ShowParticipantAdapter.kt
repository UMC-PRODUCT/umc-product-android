package com.umc.presentation.ui.home.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.databinding.ItemHomeParticipantBinding

// * TODO: 해당 어댑터는 일정 추가 탭의 BottomSheetdDialog 사용으로 인해 사용이 중지되었습니다.
class ShowParticipantAdapter(
    private val onDeleteClick: (ParticipantItem) -> Unit,
) : ListAdapter<ParticipantItem, ShowParticipantAdapter.ViewHolder>(ParticipantDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeParticipantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ParticipantItem) {
            binding.itemChip.apply {
                setText(item.name)

                setOnCloseClickListener { onDeleteClick(item) }
            }
        }
    }


    companion object ParticipantDiffCallback : DiffUtil.ItemCallback<ParticipantItem>() {
        override fun areItemsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem): Boolean {
            return oldItem.id == newItem.id // 문자열 자체가 고유값이므로 동일성 비교
        }

        override fun areContentsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem): Boolean {
            return oldItem == newItem // 내용 비교
        }
    }
}