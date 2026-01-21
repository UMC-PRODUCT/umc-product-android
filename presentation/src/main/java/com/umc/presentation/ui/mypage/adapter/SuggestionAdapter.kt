package com.umc.presentation.ui.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.SuggestionStatus
import com.umc.domain.model.mypage.SuggestionItem
import com.umc.presentation.databinding.ItemSuggestNoreplyBinding
import com.umc.presentation.databinding.ItemSuggestReplyBinding

class SuggestionAdapter : ListAdapter<SuggestionItem, RecyclerView.ViewHolder>(SuggestionDiffCallback()){

    override fun getItemViewType(position: Int): Int {
        // 답변 내용이 있으면 COMPLETED 타입, 없으면 WAITING 타입으로 반환
        return if (getItem(position).status == SuggestionStatus.COMPLETED) TYPE_COMPLETED else TYPE_WAITING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_COMPLETED -> ReplyViewHolder(
                ItemSuggestReplyBinding.inflate(inflater, parent, false)
            )
            else -> NoreplyViewHolder(
                ItemSuggestNoreplyBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ReplyViewHolder -> holder.bind(item)
            is NoreplyViewHolder -> holder.bind(item)
        }
    }

    // ViewHolder 정의
    inner class NoreplyViewHolder(private val binding: ItemSuggestNoreplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SuggestionItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    inner class ReplyViewHolder(private val binding: ItemSuggestReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SuggestionItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    companion object {
        private const val TYPE_WAITING = 0
        private const val TYPE_COMPLETED = 1

        class SuggestionDiffCallback : DiffUtil.ItemCallback<SuggestionItem>() {
            override fun areItemsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem) : Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: SuggestionItem, newItem: SuggestionItem) : Boolean {
                return oldItem == newItem
            }
        }

    }

}