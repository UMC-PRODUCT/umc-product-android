package com.umc.presentation.ui.mypage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.mypage.MyContentItem
import com.umc.presentation.databinding.ItemMypageContentBinding

interface MyContentItemDelegate {
    fun onItemClicked(item: MyContentItem)
}

class MyContentAdapter(private val delegate: MyContentItemDelegate):
ListAdapter<MyContentItem, MyContentAdapter.MyContentViewHolder>(MyContentDiffCallback){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyContentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMypageContentBinding.inflate(layoutInflater, parent, false)
        return MyContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyContentViewHolder, position: Int) {
        holder.bind(getItem(position), delegate)
    }

    inner class MyContentViewHolder(private val binding: ItemMypageContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyContentItem, delegate: MyContentItemDelegate) {
            // <variable name="item">에 데이터를 주입
            binding.item = item

            // 클릭 리스너 연결
            binding.root.setOnClickListener {
                delegate.onItemClicked(item)
            }

            // 데이터 바인딩 즉시 반영
            binding.executePendingBindings()
        }
    }

    companion object {
        private val MyContentDiffCallback = object : DiffUtil.ItemCallback<MyContentItem>() {
            override fun areItemsTheSame(oldItem: MyContentItem, newItem: MyContentItem): Boolean {
                // 고유 식별자(제목 등)가 같으면 같은 아이템으로 간주
                return oldItem.title == newItem.title && oldItem.username == newItem.username
            }

            override fun areContentsTheSame(oldItem: MyContentItem, newItem: MyContentItem): Boolean {
                // 데이터 내용 전체가 같은지 비교
                return oldItem == newItem
            }
        }
    }

}