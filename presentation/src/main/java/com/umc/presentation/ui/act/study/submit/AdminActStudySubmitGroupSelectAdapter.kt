package com.umc.presentation.ui.act.study.submit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActStudyBottomSheetSelectBinding

class AdminActStudySubmitGroupSelectAdapter(
    private val onClick: (String) -> Unit,
) : RecyclerView.Adapter<AdminActStudySubmitGroupSelectAdapter.VH>() {

    private val items = mutableListOf<String>()

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemActStudyBottomSheetSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size

    inner class VH(
        private val binding: ItemActStudyBottomSheetSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(name: String) {
            binding.tvItem.text = name
            binding.root.setOnClickListener { onClick(name) }
        }
    }
}
