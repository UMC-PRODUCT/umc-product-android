package com.umc.presentation.ui.signUp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.databinding.ItemSchoolBinding

class SchoolListAdapter(
    private val listener: SchoolListDelegate
) : ListAdapter<SchoolInfo, RecyclerView.ViewHolder> (
    SchoolListDiffCallBack()
) {

    private var selectedSchoolId: Long? = null

    interface SchoolListDelegate {
        fun onClickNotice(item: SchoolInfo)
    }

    fun setSelectedSchoolId(id: Long?) {
        val previousId = selectedSchoolId
        selectedSchoolId = id
        
        // 이전 선택 항목과 새 선택 항목의 position을 찾아서 갱신
        val previousPosition = currentList.indexOfFirst { it.schoolId.toLong() == previousId }
        val newPosition = currentList.indexOfFirst { it.schoolId.toLong() == id }
        
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition)
        }
        if (newPosition != -1 && newPosition != previousPosition) {
            notifyItemChanged(newPosition)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is SchoolListViewHolder -> {
                val item = currentList[position]
                val isSelected = item.schoolId.toLong() == selectedSchoolId
                holder.bind(item, isSelected)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemSchoolBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SchoolListViewHolder(binding, listener)
    }
}

class SchoolListDiffCallBack : DiffUtil.ItemCallback<SchoolInfo>() {
    override fun areContentsTheSame(
        oldItem: SchoolInfo,
        newItem: SchoolInfo
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: SchoolInfo,
        newItem: SchoolInfo
    ): Boolean {
        return oldItem.schoolId == newItem.schoolId
    }
}