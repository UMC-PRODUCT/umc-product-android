package com.umc.presentation.ui.act.study.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemAdminStudyGroupBinding
import com.umc.presentation.ui.act.study.group.model.AdminStudyGroupItemUiModel
import com.umc.presentation.databinding.ItemAdminStudyGroupMemberBinding
import coil.load
import coil.transform.CircleCropTransformation
import com.umc.presentation.R

class AdminStudyGroupAdapter(
    private val onClickSetting: ((View, AdminStudyGroupItemUiModel) -> Unit)? = null,
    private val onClickAddSchedule: ((AdminStudyGroupItemUiModel) -> Unit)? = null,
    private val onClickAddMember: ((AdminStudyGroupItemUiModel) -> Unit)? = null,
) : ListAdapter<AdminStudyGroupItemUiModel, AdminStudyGroupAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminStudyGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onClickSetting, onClickAddSchedule, onClickAddMember)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
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


            val container = binding.layoutMemberChips
            container.removeAllViews()

            val inflater = LayoutInflater.from(container.context)



            binding.ivLeaderProfile.load(item.leaderProfileImageUrl?.takeIf { it.isNotBlank() }) {
                crossfade(true)
                placeholder(R.drawable.ic_profile_default)
                error(R.drawable.ic_profile_default)
                transformations(CircleCropTransformation())
            }

            item.members.forEach { member ->
                val chipBinding = ItemAdminStudyGroupMemberBinding.inflate(inflater, container, false)
                chipBinding.tvName.text = member.name

                chipBinding.ivProfile.load(member.profileImageUrl?.takeIf { it.isNotBlank() }) {
                    crossfade(true)
                    placeholder(R.drawable.ic_profile_default)
                    error(R.drawable.ic_profile_default)
                    transformations(CircleCropTransformation())
                }

                container.addView(chipBinding.root)
            }


            binding.btnAddMember.setOnClickListener {
                onClickAddMember?.invoke(item)
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
