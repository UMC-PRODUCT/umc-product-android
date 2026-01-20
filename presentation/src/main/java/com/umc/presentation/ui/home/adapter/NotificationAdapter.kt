package com.umc.presentation.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.NotificationItem
import com.umc.presentation.databinding.ItemHomeNotificationBinding

//deletegate 정의
interface NotificationDelegate{

}


class NotificationAdapter :
    ListAdapter<NotificationItem, RecyclerView.ViewHolder>(NotificationDiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemHomeNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as NotificationViewHolder).bind(item)
    }


    companion object {
        private val NotificationDiffCallback = object : DiffUtil.ItemCallback<NotificationItem>() {
            override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
                // 제목과 날짜와 시간이 모두 같으면 같은 아이템으로 간주
                return oldItem.title == newItem.title &&
                        oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
                // 내용 전체가 같은지 비교
                return oldItem == newItem
            }
        }
    }

}

public class NotificationViewHolder(private val binding: ItemHomeNotificationBinding)
    : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: NotificationItem) {
        binding.apply {
            itemTvTitle.text = item.title
            itemTvContent.text = item.content
            itemTvTimeline.text = item.date
        }
    }
}