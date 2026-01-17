package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.databinding.ItemHomeSearchParticipantBinding

class SearchParticipantAdapter(
    private val onToggleClick: (ParticipantItem) -> Unit
) : ListAdapter<ParticipantItem, SearchParticipantAdapter.ViewHolder>(DiffCallback) {

    // 현재 이미 선택된 이름들을 저장하는 곳(CSV에서 저장하는 경우가 있을 수 있기에)
    private var selectedNames = setOf<String>()

    // ViewModel에서 Uistate에서 selectedParticipant가 수정될 때마다 여기에도 바로 반영할 수 있도록 처리
    fun updateSelectedList(list: List<ParticipantItem>) {
        selectedNames = list.map { it.name }.toSet()
        notifyDataSetChanged() 
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeSearchParticipantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHomeSearchParticipantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ParticipantItem) {
            binding.tvSearchName.text = item.name
            
            // 라사이크러뷰 자체 리스너는 null로 해 꼬임 방지
            binding.cbSearchSelect.setOnCheckedChangeListener(null)
            // 명단에 있으면 체크된 상태로 표시 / 없으면 체크 X로 표시
            binding.cbSearchSelect.isChecked = selectedNames.contains(item.name)

            // 체크박스 클릭 시 토글
            /** 실질적으로 어댑터에서 터치 로직을 짜는게 아니라
             * -> ViewModel에서 selectedParticipant가 수정되면, updatedSelectList를 호출해서
             * 반영하도록 하게 하기
            **/ 
            val toggleAction = { onToggleClick(item) }
            binding.cbSearchSelect.setOnClickListener { toggleAction() }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ParticipantItem>() {
            override fun areItemsTheSame(old: ParticipantItem, new: ParticipantItem) : Boolean{
                return old.name == new.name
            }
            override fun areContentsTheSame(old: ParticipantItem, new: ParticipantItem) : Boolean{
                return old == new
            }
        }
    }
}