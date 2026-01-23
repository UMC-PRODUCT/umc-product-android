package com.umc.presentation.ui.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.mypage.CommentItem
import com.umc.domain.model.mypage.ContentItem
import com.umc.presentation.databinding.ItemCommunityCommentBinding
import com.umc.presentation.databinding.ItemCommunityCommentsCountBinding
import com.umc.presentation.databinding.ItemCommunityContentBinding
import com.umc.presentation.databinding.ItemCommunityNoCommentBinding
import com.umc.presentation.ui.community.detail.PostDetailItem

interface PostItemDelegate {
    // 좋아요 버튼 클릭
    fun onLikeClicked(item: ContentItem)

    // 스크랩 버튼 클릭
    fun onScrapClicked(item: ContentItem)

    // 댓글 메뉴 버튼 클릭
    fun onCommentMenuClicked(item: CommentItem)
}


class PostDetailAdapter(
    private val delegate: PostItemDelegate
) : ListAdapter<PostDetailItem, RecyclerView.ViewHolder>(PostDetailDiffCallback) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_COUNT = 1
        private const val TYPE_COMMENT = 2
        private const val TYPE_EMPTY = 3
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PostDetailItem.Header -> TYPE_HEADER
        is PostDetailItem.CommentHeader -> TYPE_COUNT
        is PostDetailItem.Comment -> TYPE_COMMENT
        is PostDetailItem.EmptyComment -> TYPE_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemCommunityContentBinding.inflate(inflater, parent, false),
                delegate)
            TYPE_COUNT -> CountViewHolder(
                ItemCommunityCommentsCountBinding.inflate(inflater, parent, false))
            TYPE_COMMENT -> CommentViewHolder(
                ItemCommunityCommentBinding.inflate(inflater, parent, false),
                delegate)
            else -> EmptyViewHolder(
                ItemCommunityNoCommentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind((item as PostDetailItem.Header).post)
            is CountViewHolder -> holder.bind((item as PostDetailItem.CommentHeader).count)
            is CommentViewHolder -> holder.bind((item as PostDetailItem.Comment).comment)
        }
    }

    // 각 뷰홀더 클래스 정의
    // 헤더 = 게시글 내용 부분
    class HeaderViewHolder(val binding: ItemCommunityContentBinding,
        private val delegate: PostItemDelegate
        ) : RecyclerView.ViewHolder(binding.root) {

            //여기서 좋아요 / 싫어요 클릭 시 delegate로 ㄱㄱ
            fun bind(item: ContentItem) {
                binding.item = item
                binding.itemBtnLike.setOnClickListener { delegate.onLikeClicked(item) }
                binding.itemBtnScrap.setOnClickListener { delegate.onScrapClicked(item) }
                binding.executePendingBindings()
        }
    }

    // 카운트 = 댓글 개수 및 분리 부분
    class CountViewHolder(val binding: ItemCommunityCommentsCountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Int) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    // 코멘트 = 댓글 쭈욱
    class CommentViewHolder(val binding: ItemCommunityCommentBinding,
                            private val delegate: PostItemDelegate) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: CommentItem) {
            binding.item = item
            binding.itemBtnMenu.setOnClickListener { delegate.onCommentMenuClicked(item) }
            binding.executePendingBindings()
        }

    }

    // 노코멘트 = 댓글이 없을 때
    class EmptyViewHolder(val binding: ItemCommunityNoCommentBinding) : RecyclerView.ViewHolder(binding.root)

}

object PostDetailDiffCallback : DiffUtil.ItemCallback<PostDetailItem>() {
    override fun areItemsTheSame(oldItem: PostDetailItem, newItem: PostDetailItem): Boolean {
        return when {
            // 1. 게시글 본문: 제목이 같으면 같은 아이템으로 간주
            oldItem is PostDetailItem.Header && newItem is PostDetailItem.Header ->
                oldItem.post.title == newItem.post.title

            // 2. 댓글 헤더: 둘 다 CommentHeader 타입이면 '같은 영역'으로 간주
            oldItem is PostDetailItem.CommentHeader && newItem is PostDetailItem.CommentHeader -> true

            // 3. 댓글 아이템: 작성자와 시간이 같으면 같은 아이템으로 간주
            oldItem is PostDetailItem.Comment && newItem is PostDetailItem.Comment ->
                oldItem.comment.username == newItem.comment.username &&
                        oldItem.comment.writeTime == newItem.comment.writeTime

            // 4. 빈 댓글 안내: 타입이 같으면 같은 아이템
            else -> oldItem is PostDetailItem.EmptyComment && newItem is PostDetailItem.EmptyComment
        }
    }

    override fun areContentsTheSame(oldItem: PostDetailItem, newItem: PostDetailItem): Boolean {
        // Data class의 equals를 활용해 모든 필드값을 비교합니다.
        return oldItem == newItem
    }
}