package com.umc.presentation.ui.act.study.common.picker.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemActMemberPickerBinding
import com.umc.presentation.databinding.ItemActMemberPickerHeaderBinding
import com.umc.presentation.ui.act.study.common.model.MemberUiModel

class MemberPickerAdapter(
    private val isMulti: Boolean,
    private val onSinglePick: (MemberUiModel) -> Unit,
    private val onToggle: (MemberUiModel) -> Unit,
    private val isChecked: (MemberUiModel) -> Boolean,
) : ListAdapter<MemberRow, RecyclerView.ViewHolder>(diff) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MemberRow.Header -> VIEW_TYPE_HEADER
            is MemberRow.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemActMemberPickerHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemActMemberPickerBinding.inflate(inflater, parent, false)
                MemberItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is MemberRow.Header -> (holder as HeaderViewHolder).bind(row)
            is MemberRow.Item -> (holder as MemberItemViewHolder).bind(row.member)
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemActMemberPickerHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: MemberRow.Header) {
            binding.title = row.title
            binding.executePendingBindings()
        }
    }

    inner class MemberItemViewHolder(
        private val binding: ItemActMemberPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MemberUiModel) {
            binding.item = item
            binding.isMulti = isMulti


            binding.ivProfile.load(item.profileImageUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_default)
                error(R.drawable.ic_profile_default)
                transformations(CircleCropTransformation())
            }

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
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1

        private val diff = object : DiffUtil.ItemCallback<MemberRow>() {
            override fun areItemsTheSame(old: MemberRow, new: MemberRow): Boolean {
                return when {
                    old is MemberRow.Header && new is MemberRow.Header -> old.title == new.title
                    old is MemberRow.Item && new is MemberRow.Item ->
                        old.member.challengerId == new.member.challengerId
                    else -> false
                }
            }

            override fun areContentsTheSame(old: MemberRow, new: MemberRow): Boolean = old == new
        }
    }
}