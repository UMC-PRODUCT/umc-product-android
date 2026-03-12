package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.databinding.ItemBottomSheetParticipantAddBinding
import androidx.recyclerview.widget.DiffUtil
import coil.load
import coil.transform.CircleCropTransformation
import com.umc.presentation.R


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
            val nameType = "${item.nickname}/${item.name}(${item.gisu}기)"
            binding.item = item
            binding.itemTvName.text = nameType
            binding.itemTvSchool.text = item.school

            binding.itemImvProfile.load(item.profileImage.ifEmpty { null }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_default) // 로딩 중 이미지
                error(R.drawable.ic_profile_default)       // 실패/비어있을 때 이미지
                transformations(CircleCropTransformation()) // 원형 변환
            }

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
            override fun areItemsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem) = oldItem.name == newItem.name
            override fun areContentsTheSame(oldItem: ParticipantItem, newItem: ParticipantItem) = oldItem == newItem
        }
    }
}