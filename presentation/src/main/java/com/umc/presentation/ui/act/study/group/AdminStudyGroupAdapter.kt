package com.umc.presentation.ui.act.study.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemAdminStudyGroupBinding

class AdminStudyGroupAdapter(
    private val onClickSetting: ((View, AdminStudyGroupItemUiModel) -> Unit)? = null,
    private val onClickAddSchedule: ((AdminStudyGroupItemUiModel) -> Unit)? = null,
    private val onClickAddMember: ((AdminStudyGroupItemUiModel) -> Unit)? = null,
) : ListAdapter<AdminStudyGroupItemUiModel, AdminStudyGroupAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAdminStudyGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClickSetting, onClickAddSchedule, onClickAddMember)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemAdminStudyGroupBinding,
        private val onClickSetting: ((View, AdminStudyGroupItemUiModel) -> Unit)?,
        private val onClickAddSchedule: ((AdminStudyGroupItemUiModel) -> Unit)?,
        private val onClickAddMember: ((AdminStudyGroupItemUiModel) -> Unit)?,


    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminStudyGroupItemUiModel) {
            binding.item = item
            binding.executePendingBindings()

            binding.ivSetting.setOnClickListener { v -> onClickSetting?.invoke(v, item) }
            binding.ubAddSchedule.setOnClickListener { onClickAddSchedule?.invoke(item) }
            binding.ubAddMember.setOnClickListener { onClickAddMember?.invoke(item) }
            binding.ubAddSchedule.setOnClickListener {
                onClickAddSchedule?.invoke(item)
            }


        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AdminStudyGroupItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: AdminStudyGroupItemUiModel,
                newItem: AdminStudyGroupItemUiModel
            ): Boolean = oldItem.groupId == newItem.groupId

            override fun areContentsTheSame(
                oldItem: AdminStudyGroupItemUiModel,
                newItem: AdminStudyGroupItemUiModel
            ): Boolean = oldItem == newItem
        }
    }
}
