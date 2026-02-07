package com.umc.presentation.ui.signUp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.signUp.School
import com.umc.presentation.databinding.ItemSchoolBinding

class SchoolListAdapter(
    private val listener: SchoolListDelegate
) : ListAdapter<School, RecyclerView.ViewHolder> (
    SchoolListDiffCallBack()
) {

    interface SchoolListDelegate {
        fun onClickNotice(item: School)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is SchoolListViewHolder -> holder.bind(currentList[position])
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

class SchoolListDiffCallBack : DiffUtil.ItemCallback<School>() {
    override fun areContentsTheSame(
        oldItem: School,
        newItem: School
    ): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(
        oldItem: School,
        newItem: School
    ): Boolean {
        return oldItem.schoolId == newItem.schoolId
    }
}