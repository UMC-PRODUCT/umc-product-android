package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.SearchResultItem
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemBottomSheetParticipantSearchBinding
import com.umc.presentation.databinding.ItemBottomSheetParticipantSearchHeaderBinding

// 검색 결과 리스트 전용 델리게이트
interface SearchParticipantDelegate {
    fun onParticipantToggled(item: ParticipantItem)
}

/**SearchResultItem은 ParticipantItem에 존재합니다.**/

class BottomSheetSearchParticipantAdapter(
    private val delegate: SearchParticipantDelegate
) : ListAdapter<SearchResultItem, RecyclerView.ViewHolder>(DiffCallback) {

    private var selectedList: List<ParticipantItem> = emptyList()

    fun updateSelectedList(newList: List<ParticipantItem>) {
        selectedList = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is SearchResultItem.Header -> TYPE_HEADER
        is SearchResultItem.Participant -> TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemBottomSheetParticipantSearchHeaderBinding.inflate(inflater, parent, false))
        } else {
            ItemViewHolder(ItemBottomSheetParticipantSearchBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is HeaderViewHolder && item is SearchResultItem.Header) {
            holder.bind(item.partName)
        } else if (holder is ItemViewHolder && item is SearchResultItem.Participant) {
            holder.bind(item.user)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemBottomSheetParticipantSearchHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.title = title
            binding.executePendingBindings()
        }
    }

    inner class ItemViewHolder(private val binding: ItemBottomSheetParticipantSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: ParticipantItem) {
            val nameType = "${user.name}/${user.nickname}"
            binding.item = user
            binding.itemTvName.text = nameType
            binding.itemTvSchool.text = user.school

            binding.itemImvProfile.load(user.profileImage.ifEmpty { null }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_default) // 로딩 중 이미지
                error(R.drawable.ic_profile_default)       // 실패/비어있을 때 이미지
                transformations(CircleCropTransformation()) // 원형 변환
            }
            
            // 토글 되어있는 여부는 selectedList에 있는지 여부로 체크
            val isSelected = selectedList.any { it.id == user.id }
            binding.isSelected = isSelected

            binding.root.setOnClickListener {
                delegate.onParticipantToggled(user)
            }
            binding.itemCbSelect.setOnClickListener {
                delegate.onParticipantToggled(user)
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<SearchResultItem>() {
            override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean {
                return if (oldItem is SearchResultItem.Participant && newItem is SearchResultItem.Participant) {
                    oldItem.user.id == newItem.user.id
                } else oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem) = oldItem == newItem
        }
    }
}