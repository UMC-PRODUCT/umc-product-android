package com.umc.presentation.ui.notice.write.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.databinding.ItemSchoolBinding

class StaffPartListAdapter(
    private val listener: Delegate
) : ListAdapter<UserPart, StaffPartListAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPartName: String? = null

    interface Delegate {
        fun onClickPart(part: UserPart)
    }

    fun setSelectedPartName(name: String?) {
        val previousName = selectedPartName
        selectedPartName = name
        val previousIndex = currentList.indexOfFirst { it.name == previousName }
        val newIndex = currentList.indexOfFirst { it.name == name }
        if (previousIndex != -1) notifyItemChanged(previousIndex)
        if (newIndex != -1 && newIndex != previousIndex) notifyItemChanged(newIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSchoolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], currentList[position].name == selectedPartName)
    }

    inner class ViewHolder(private val binding: ItemSchoolBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(part: UserPart, isSelected: Boolean) {
            binding.textSchool.text = part.label
            binding.imageCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
            binding.root.setOnClickListener { listener.onClickPart(part) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserPart>() {
        override fun areItemsTheSame(oldItem: UserPart, newItem: UserPart) = oldItem == newItem
        override fun areContentsTheSame(oldItem: UserPart, newItem: UserPart) = oldItem == newItem
    }
}
