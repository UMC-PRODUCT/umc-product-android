package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemChallengerHeaderBinding

class ChallengerHeaderAdapter(
    private val title: String
) : RecyclerView.Adapter<ChallengerHeaderAdapter.ViewHolder>() {

    private var count: Int = 0

    inner class ViewHolder(private val binding: ItemChallengerHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, count: Int) {
            binding.tvHeaderTitle.text = title
            binding.tvHeaderCount.text = "($count)"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChallengerHeaderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(title, count)
    }

    override fun getItemCount(): Int = 1

    fun updateCount(newCount: Int) {
        this.count = newCount
        notifyItemChanged(0)
    }
}