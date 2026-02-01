package com.umc.presentation.ui.act.study.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.umc.presentation.databinding.ItemStudyGroupSettingMenuBinding

class StudyGroupSettingMenuAdapter(
    private val items: List<StudyGroupSettingMenuItem>,
    private val onClick: (StudyGroupSettingMenuItem) -> Unit,
) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemStudyGroupSettingMenuBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        } else {
            ItemStudyGroupSettingMenuBinding.bind(convertView)
        }

        val item = items[position]
        val ctx = parent.context

        binding.tvTitle.text = item.title
        binding.tvTitle.setTextColor(ContextCompat.getColor(ctx, item.titleColorRes))

        binding.ivIcon.setImageResource(item.iconRes)
        binding.ivIcon.imageTintList = ContextCompat.getColorStateList(ctx, item.iconTintRes)

        binding.ivArrow.imageTintList = ContextCompat.getColorStateList(ctx, item.arrowTintRes)

        binding.root.setOnClickListener { onClick(item) }

        return binding.root
    }
}
