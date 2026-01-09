package com.umc.presentation.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.home.SchedulePlan
import com.umc.presentation.databinding.ItemHomePlanActiveBinding
import com.umc.presentation.databinding.ItemHomePlanDefaultBinding

//delete 만들기
interface ScheduleItemDelegate{
    fun onItemClicked(item: SchedulePlan)
}

class ScheduleAdapter(private val delegate: ScheduleItemDelegate) :
    ListAdapter<SchedulePlan, RecyclerView.ViewHolder>(ScheduleDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isPast) TYPE_DEFAULT else TYPE_ACTIVE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ACTIVE -> ActiveViewHolder(ItemHomePlanActiveBinding.inflate(inflater, parent, false))
            else -> DefaultViewHolder(ItemHomePlanDefaultBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ActiveViewHolder -> holder.bind(item, delegate)
            is DefaultViewHolder -> holder.bind(item, delegate)
        }
    }



    companion object {
        private const val TYPE_ACTIVE = 0
        private const val TYPE_DEFAULT = 1
        private val ScheduleDiffCallback = object : DiffUtil.ItemCallback<SchedulePlan>() {
            override fun areItemsTheSame(oldItem: SchedulePlan, newItem: SchedulePlan): Boolean {
                // 제목과 날짜와 시간이 모두 같으면 같은 아이템으로 간주
                return oldItem.title == newItem.title &&
                        oldItem.date == newItem.date &&
                        oldItem.time == newItem.time
            }

            override fun areContentsTheSame(oldItem: SchedulePlan, newItem: SchedulePlan): Boolean {
                // 내용 전체가 같은지 비교
                return oldItem == newItem
            }
        }
    }
}

//각 ViewHolder 클래스 정의
public class ActiveViewHolder(private val binding: ItemHomePlanActiveBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SchedulePlan, delegate: ScheduleItemDelegate) {
        binding.apply {
            cardTvDayweek.text = item.dayOfWeek
            cardTvDay.text = item.day
            cardTvTitle.text = item.title
            cardTvTime.text = item.time
            cardCdvLeftdate.setText(item.dDay)
            root.setOnClickListener { delegate.onItemClicked(item) }
        }
    }
}

public class DefaultViewHolder(private val binding: ItemHomePlanDefaultBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SchedulePlan, delegate: ScheduleItemDelegate) {
        binding.apply {
            cardTvDayweek.text = item.dayOfWeek
            cardTvDay.text = item.day
            cardTvTitle.text = item.title
            cardTvTime.text = item.time
            root.setOnClickListener { delegate.onItemClicked(item) }
        }
    }
}