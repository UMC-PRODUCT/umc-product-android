package com.umc.presentation.ui.act.study.common.picker.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemActMemberPickerBinding
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

class MemberPickerAdapter(
    private val isMulti: Boolean,
    private val onSinglePick: (MemberUiModel) -> Unit,
    private val onToggle: (MemberUiModel) -> Unit,
    private val isChecked: (MemberUiModel) -> Boolean,
) : ListAdapter<MemberUiModel, MemberPickerAdapter.ViewHolder>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActMemberPickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemActMemberPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MemberUiModel) {
            binding.item = item
            binding.isMulti = isMulti

            val checked = isChecked(item)
            binding.isChecked = checked

            if (isMulti) applyCheckTint(checked) else binding.ivCheck.imageTintList = null

            binding.btnSelect.setOnClickListener {
                if (!isMulti) onSinglePick(item)
            }

            binding.root.setOnClickListener {
                if (isMulti) {
                    onToggle(item)
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                } else {
                    onSinglePick(item)
                }
            }

            binding.ivCheck.setOnClickListener {
                if (isMulti) {
                    onToggle(item)

                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                }
            }

            binding.executePendingBindings()
        }

        private fun applyCheckTint(checked: Boolean) {
            binding.ivCheck.imageTintList =
                if (checked) ColorStateList.valueOf(
                    ContextCompat.getColor(binding.ivCheck.context, R.color.primary500)
                ) else null
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<MemberUiModel>() {
            override fun areItemsTheSame(old: MemberUiModel, new: MemberUiModel) =
                old.challengerId == new.challengerId

            override fun areContentsTheSame(old: MemberUiModel, new: MemberUiModel) =
                old == new
        }
    }
}