package com.umc.presentation.ui.act.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.R // R 클래스 임포트 필수
import com.umc.presentation.databinding.ItemSectionHeaderBinding

class SectionHeaderAdapter(
    private val title: String
) : RecyclerView.Adapter<SectionHeaderAdapter.ViewHolder>() {

    private var count: Int? = null

    inner class ViewHolder(private val binding: ItemSectionHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvSectionTitle.text = title

            val currentCount = count
            if (currentCount != null) {
                binding.btnSectionCount.visibility = View.VISIBLE
                val formattedText = binding.root.context.getString(
                    R.string.attendance_count_remaining_format,
                    currentCount
                )
                binding.btnSectionCount.setText(formattedText)
            } else {
                binding.btnSectionCount.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind()

    override fun getItemCount(): Int = 1

    fun updateCount(count: Int) {
        this.count = count
        notifyItemChanged(0)
    }
}