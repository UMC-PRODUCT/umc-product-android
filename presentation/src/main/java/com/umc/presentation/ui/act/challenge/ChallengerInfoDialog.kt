package com.umc.presentation.ui.act.challenge

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.presentation.databinding.DialogChallengerInfoBinding
import com.umc.presentation.ui.act.adapter.ChallengerInfoHistoryAdapter
import androidx.core.graphics.drawable.toDrawable
import com.umc.domain.model.act.challenger.ChallengerInfoHistory
import com.umc.presentation.extension.px

class ChallengerInfoDialog(
    private val model: ChallengerInfoDialogModel
) : DialogFragment() {

    private var _binding: DialogChallengerInfoBinding? = null
    private val binding get() = _binding!!

    private val historyAdapter = ChallengerInfoHistoryAdapter()

    override fun onResume() {
        super.onResume()
        // 다이얼로그 너비 설정
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogChallengerInfoBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = model

        setupRecyclerView()

        binding.imvIconClose.setOnClickListener { dismiss() }
    }

    private fun setupRecyclerView() {
        val historyAdapter = ChallengerInfoHistoryAdapter()
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position != 0) outRect.top = 12.px
                }
            })
        }

        historyAdapter.submitList(model.history)

        // 아이템이 3개 이상일 때 높이 고정 로직
        if (model.history.size > 3) {
            binding.rvHistory.post {
                val viewHolder = binding.rvHistory.findViewHolderForAdapterPosition(0)
                val itemHeight = viewHolder?.itemView?.height ?: 0

                if (itemHeight > 0) {
                    val spacing = 12.px
                    val verticalPadding = 32.px

                    val totalHeight = (itemHeight * 3) + (spacing * 2) + verticalPadding

                    binding.rvHistory.layoutParams = binding.rvHistory.layoutParams.apply {
                        height = totalHeight
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}