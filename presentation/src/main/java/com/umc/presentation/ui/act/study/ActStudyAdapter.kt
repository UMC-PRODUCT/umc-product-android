package com.umc.presentation.ui.act.study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.databinding.ItemActStudyBinding
import androidx.core.content.ContextCompat
import com.umc.presentation.R


class ActStudyAdapter(
    private val onToggle: (Int) -> Unit,
    private val onLongApprove: (Long) -> Unit,
    private val onSubmitClick: (Long, String) -> Unit,
) : ListAdapter<ActStudyItemUiModel, ActStudyAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ActStudyItemUiModel>() {
            override fun areItemsTheSame(old: ActStudyItemUiModel, new: ActStudyItemUiModel) =
                old.id == new.id

            override fun areContentsTheSame(old: ActStudyItemUiModel, new: ActStudyItemUiModel) =
                old == new
        }
    }


    private val draftLinks = mutableMapOf<Long, String>()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    fun updateDraftLink(itemId: Long, link: String) {
        draftLinks[itemId] = link
    }

    inner class VH(private val binding: ItemActStudyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentItemId: Long = -1L

        private fun updateSubmitButtonUi(link: String) {
            val enabled = link.isNotBlank()

            binding.btnSubmit.isEnabled = enabled

            if (enabled) {
                binding.btnSubmit.setText("링크 제출하기")
                binding.btnSubmit.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.neutral000)
                )
                binding.btnSubmit.setUBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.primary500)
                )
            } else {
                binding.btnSubmit.setText("링크 제출하기")
                binding.btnSubmit.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.neutral500)
                )
                binding.btnSubmit.setUBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.neutral200)
                )
            }
        }

        init {


            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onToggle(pos)
            }
            binding.ivArrow.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onToggle(pos)
            }


            binding.etLink.setOnTextChangedListener { text ->
                if (currentItemId != -1L) {
                    draftLinks[currentItemId] = text
                    updateSubmitButtonUi(text)
                }
            }


            binding.btnSubmit.setOnClickListener {
                if (currentItemId == -1L) return@setOnClickListener
                val link = draftLinks[currentItemId] ?: binding.etLink.getText()
                onSubmitClick(currentItemId, link)
            }

            binding.btnConfirm.setOnClickListener {
                if (currentItemId == -1L) return@setOnClickListener
                val link = draftLinks[currentItemId] ?: binding.etLink.getText()
                onSubmitClick(currentItemId, link)
            }

            binding.btnStatus.setOnLongClickListener {
                if (currentItemId != -1L) onLongApprove(currentItemId)
                true
            }


        }


        fun bind(item: ActStudyItemUiModel) {
            currentItemId = item.id
            binding.item = item


            val currentLink = draftLinks[item.id] ?: item.link


            if (currentLink != binding.etLink.getText()) {
                binding.etLink.setText(currentLink)
            }


            updateSubmitButtonUi(currentLink)

            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemActStudyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
