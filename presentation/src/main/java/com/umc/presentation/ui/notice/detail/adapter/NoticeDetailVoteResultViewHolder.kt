package com.umc.presentation.ui.notice.detail.adapter

import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.databinding.ItemNoteDetailVoteResultBinding
import kotlin.math.roundToInt

class NoticeDetailVoteResultViewHolder(
    private val binding: ItemNoteDetailVoteResultBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: NoticeVoteOption, isSelected: Boolean = false) {
        binding.apply {
            textName.text = item.content
            textPercentage.text = String.format("%.1f%%", item.voteRate)
            textVoteCount.text = "${item.voteCount}명"
            progressBar.progress = item.voteRate.roundToInt().coerceIn(0, 100)

            // 내가 선택한 항목일 때 체크마크 표시
            imageCheck.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}
