package com.umc.presentation.ui.act.study.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.R
import com.umc.presentation.databinding.ItemWeekOptionBinding
import com.umc.presentation.databinding.LayoutBottomSheetWeekPickerBinding

class BottomSheetWeekPickerDialog(
    private val selectedWeek: Int?,
    private val onSelect: (Int) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetWeekPickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = LayoutBottomSheetWeekPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WeekAdapter(selectedWeek) { week ->
            onSelect(week)
            dismiss()
        }

        binding.rvWeeks.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvWeeks.adapter = adapter
        adapter.submitList((1..10).toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class WeekAdapter(
        private val selectedWeek: Int?,
        private val onItemClick: (Int) -> Unit,
    ) : ListAdapter<Int, WeekAdapter.ViewHolder>(DIFF) {

        inner class ViewHolder(val binding: ItemWeekOptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(week: Int) {
                val ctx = itemView.context
                binding.tvWeekLabel.text = "${week}주차"
                val isSelected = week == selectedWeek

                val bgColorRes = if (isSelected) R.color.primary100 else R.color.neutral100
                val textColorRes = if (isSelected) R.color.primary500 else R.color.neutral600
                val strokeColorRes = if (isSelected) R.color.primary500 else R.color.neutral200
                val strokePx = (ctx.resources.displayMetrics.density * 1).toInt()

                binding.cardWeekOption.setCardBackgroundColor(ContextCompat.getColor(ctx, bgColorRes))
                binding.cardWeekOption.strokeColor = ContextCompat.getColor(ctx, strokeColorRes)
                binding.cardWeekOption.strokeWidth = strokePx
                binding.tvWeekLabel.setTextColor(ContextCompat.getColor(ctx, textColorRes))

                binding.root.setOnClickListener { onItemClick(week) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemWeekOptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        companion object {
            val DIFF = object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(a: Int, b: Int) = a == b
                override fun areContentsTheSame(a: Int, b: Int) = a == b
            }
        }
    }
}
