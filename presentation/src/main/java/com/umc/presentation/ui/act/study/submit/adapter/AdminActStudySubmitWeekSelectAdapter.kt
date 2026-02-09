package com.umc.presentation.ui.act.study.submit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActStudyBottomSheetSelectBinding

class AdminActStudySubmitWeekSelectAdapter(
    private val onClick: (Int) -> Unit,
) : RecyclerView.Adapter<AdminActStudySubmitWeekSelectAdapter.ViewHolder>() {

    private val items = mutableListOf<Int>()

    fun submitList(list: List<Int>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActStudyBottomSheetSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemActStudyBottomSheetSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(week: Int) {
            binding.tvItem.text = "${week}주차"
            binding.root.setOnClickListener { onClick(week) }
        }
    }
}
