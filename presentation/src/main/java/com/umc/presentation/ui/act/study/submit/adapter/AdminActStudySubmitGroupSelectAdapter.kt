package com.umc.presentation.ui.act.study.submit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActStudyBottomSheetSelectBinding

class AdminActStudySubmitGroupSelectAdapter(
    private val onClick: (String) -> Unit,
) : RecyclerView.Adapter<AdminActStudySubmitGroupSelectAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    fun submitList(list: List<String>) {
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

        fun bind(name: String) {
            binding.tvItem.text = name
            binding.root.setOnClickListener { onClick(name) }
        }
    }
}
