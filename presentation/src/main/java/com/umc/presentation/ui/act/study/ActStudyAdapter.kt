package com.umc.presentation.ui.act.study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.umc.domain.model.study.WeeklyCurriculum
import com.umc.presentation.databinding.ItemActStudyBinding

class ActStudyAdapter :
    ListAdapter<WeeklyCurriculum, ActStudyAdapter.ActStudyViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WeeklyCurriculum>() {
            override fun areItemsTheSame(old: WeeklyCurriculum, new: WeeklyCurriculum) =
                old.weeklyCurriculumId == new.weeklyCurriculumId

            override fun areContentsTheSame(old: WeeklyCurriculum, new: WeeklyCurriculum) =
                old == new
        }
    }

    /* 기존 ActStudyAdapter - 나중에 사용 할까봐
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
    */

    class ActStudyViewHolder(private val binding: ItemActStudyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WeeklyCurriculum) {
            binding.tvWeekNo.setText("Week ${item.weekNo}")
            binding.tvTitle.text = item.title
        }

        /* 기존 bind
        private var currentItemId: Long = -1L

        private fun updateSubmitButtonUi(link: String) {
            val enabled = link.isNotBlank()
            val ctx = binding.root.context
            binding.apply {
                btnSubmit.isEnabled = enabled
                btnSubmit.setText("링크 제출하기")
                if (enabled) {
                    btnSubmit.setTextColor(ContextCompat.getColor(ctx, R.color.neutral000))
                    btnSubmit.setUBackgroundColor(ContextCompat.getColor(ctx, R.color.primary500))
                } else {
                    btnSubmit.setTextColor(ContextCompat.getColor(ctx, R.color.neutral000))
                    btnSubmit.setUBackgroundColor(ContextCompat.getColor(ctx, R.color.neutral200))
                }
            }
        }

        fun bind(
            item: ActStudyItemUiModel,
            getItemAt: (Int) -> ActStudyItemUiModel,
            draftLinks: MutableMap<Long, String>,
            onToggle: (Int) -> Unit,
            onSubmitClick: (Long, String) -> Unit,
            onConfirmClick: (Long) -> Unit,
        ) {
            currentItemId = item.id
            binding.item = item

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val curItem = getItemAt(pos)
                if (curItem.isLocked) return@setOnClickListener
                onToggle(pos)
            }

            binding.ivArrow.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val curItem = getItemAt(pos)
                if (curItem.isLocked) return@setOnClickListener
                onToggle(pos)
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
                onConfirmClick(currentItemId)
            }

            val currentLink = draftLinks[item.id] ?: item.link
            if (currentLink != binding.etLink.getText()) {
                binding.etLink.setText(currentLink)
            }

            updateSubmitButtonUi(currentLink)

            val locked = item.isLocked
            binding.etLink.isEnabled = !locked

            binding.executePendingBindings()

            binding.root.doOnLayout {
                val tags = binding.cvItem.findViewById<android.view.View>(R.id.layout_tags) ?: return@doOnLayout
                val offsetInCard = tags.bottom
                val lp = binding.tagsBottomAnchor.layoutParams as ConstraintLayout.LayoutParams
                if (lp.topMargin != offsetInCard) {
                    lp.topMargin = offsetInCard
                    binding.tagsBottomAnchor.layoutParams = lp
                }
            }
        }
        */
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActStudyViewHolder {
        val binding = ItemActStudyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ActStudyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActStudyViewHolder, position: Int) {
        holder.bind(getItem(position))

        /* 기존 onBindViewHolder
        holder.bind(
            item = getItem(position),
            getItemAt = { pos -> getItem(pos) },
            draftLinks = draftLinks,
            onToggle = onToggle,
            onSubmitClick = onSubmitClick,
            onConfirmClick = onConfirmClick,
        )
        */
    }
}