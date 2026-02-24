package com.umc.presentation.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.umc.domain.model.community.TrophyBody
import com.umc.domain.model.community.TrophyItem
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemCommunityTrophyBinding
import com.umc.presentation.databinding.ItemCommunityTrophySchoolBinding

class TrophyDetailAdapter (
    private val onContentClick: (TrophyBody) -> Unit
) : ListAdapter<TrophyItem, RecyclerView.ViewHolder>(TrophyDiffCallback) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TrophyItem.Header -> TYPE_HEADER
            is TrophyItem.Content -> TYPE_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemCommunityTrophySchoolBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemCommunityTrophyBinding.inflate(inflater, parent, false)
                ContentViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind((item as TrophyItem.Header).schoolName)
            is ContentViewHolder -> holder.bind((item as TrophyItem.Content).data)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemCommunityTrophySchoolBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(schoolName: String) {
            binding.schoolName = schoolName
            binding.executePendingBindings()
        }
    }

    inner class ContentViewHolder(private val binding: ItemCommunityTrophyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(body: TrophyBody) {
            binding.item = body
            binding.itemCircleimvProfile.load(body.challengerProfileImage) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_default)
                error(R.drawable.ic_profile_default)
            }

            //버튼 클릭 시
            binding.itemBtnGo.setOnClickListener { onContentClick(body) }
            binding.executePendingBindings()
        }
    }

    object TrophyDiffCallback : DiffUtil.ItemCallback<TrophyItem>() {
        override fun areItemsTheSame(oldItem: TrophyItem, newItem: TrophyItem): Boolean {
            return when {
                oldItem is TrophyItem.Header && newItem is TrophyItem.Header ->
                    oldItem.schoolName == newItem.schoolName
                oldItem is TrophyItem.Content && newItem is TrophyItem.Content ->
                    oldItem.data.trophyId == newItem.data.trophyId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TrophyItem, newItem: TrophyItem): Boolean {
            return oldItem == newItem
        }
    }
}