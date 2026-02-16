package com.umc.presentation.ui.act.challenge

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.umc.presentation.R
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.presentation.component.UButton
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.presentation.databinding.DialogChallengerManageBinding
import com.umc.presentation.extension.px
import com.umc.presentation.ui.act.adapter.ChallengerHistoryAdapter

class ChallengerManageDialog(
    private var model: ChallengerManageDialogModel,
    private val onAbsenceSubmit: (String) -> Unit = {},
    private val onWarningSubmit: (String) -> Unit = {},
    private val onDeleteHistory: (ChallengerPoint) -> Unit = {}
) : DialogFragment() {

    private var _binding: DialogChallengerManageBinding? = null
    private val binding get() = _binding!!

    private enum class DialogMode { DEFAULT, ABSENCE, WARNING, EDIT }
    private var currentMode = DialogMode.DEFAULT

    private val historyAdapter by lazy {
        ChallengerHistoryAdapter(onDeleteClick = { item -> onDeleteHistory(item) })
    }

    /**
     * ItemDecoration for adding extra spacing to first and last items
     */
    private inner class EdgeSpacingDecoration(private val extraSpacing: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val itemCount = state.itemCount

            when (position) {
                0 -> outRect.top = extraSpacing  // 첫 번째 아이템 상단 여백
                itemCount - 1 -> outRect.bottom = extraSpacing  // 마지막 아이템 하단 여백
            }
        }
    }

    fun updateData(newModel: ChallengerManageDialogModel) {
        this.model = newModel
        binding.model = newModel
        historyAdapter.submitList(newModel.history) {
            adjustRecyclerViewHeight()
        }

        binding.etAbsenceReason.apply {
            clearText()
            clearFocus()
        }
        binding.etWarningReason.apply {
            clearText()
            clearFocus()
        }

        // 입력창 초기화 로직
        binding.etAbsenceReason.clearText()
        binding.etWarningReason.clearText()
        updateConfirmButton(binding.btnAbsenceConfirm, false)
        updateConfirmButton(binding.btnWarningConfirm, false)

        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

        binding.executePendingBindings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogChallengerManageBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        initEvent()
        setupTextWatchers()
        updateUIForMode(DialogMode.DEFAULT)
    }

    private fun setupRecyclerView() {
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            // 중복 방지 후 데코레이션 추가
            while (itemDecorationCount > 0) { removeItemDecorationAt(0) }
            addItemDecoration(HistoryItemDecoration())
        }
        historyAdapter.submitList(model.history) { adjustRecyclerViewHeight() }
    }

    class HistoryItemDecoration : RecyclerView.ItemDecoration() {
        private val edgePadding = 16.px // 끝 여백 16dp

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val itemCount = state.itemCount

            // 첫 번째 아이템 위에만 16dp
            if (position == 0) {
                outRect.top = edgePadding
            }

            // 마지막 아이템 아래에만 16dp 추가
            if (position == itemCount - 1) {
                outRect.bottom = edgePadding
            }
        }
    }

    /**
     * 아이템이 2개보다 많을 경우, 동적으로 2개 높이만큼만 RecyclerView 높이를 제한
     */
    private fun adjustRecyclerViewHeight() {
        binding.rvHistory.post {
            if (historyAdapter.itemCount > 2) {
                val firstChild = binding.rvHistory.getChildAt(0)
                if (firstChild != null) {
                    val itemHeight = firstChild.height
                    val params = binding.rvHistory.layoutParams

                    params.height = (itemHeight * 2) + 16.px + 12.px + 16.px
                    binding.rvHistory.layoutParams = params
                }
            } else {
                binding.rvHistory.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            binding.rvHistory.requestLayout()
        }
    }

    private fun initEvent() {
        binding.imvIconClose.setOnClickListener { dismiss() }
        binding.btnWarningView.setOnClickListener { toggleMode(DialogMode.WARNING) }
        binding.btnAbsenceView.setOnClickListener { toggleMode(DialogMode.ABSENCE) }
        binding.btnRecordEdit.setOnClickListener { toggleMode(DialogMode.EDIT) }

        binding.btnAbsenceConfirm.setOnClickListener {
            if (it.isEnabled) onAbsenceSubmit(binding.etAbsenceReason.getText())
        }
        binding.btnWarningConfirm.setOnClickListener {
            if (it.isEnabled) onWarningSubmit(binding.etWarningReason.getText())
        }
    }

    private fun setupTextWatchers() {
        binding.etAbsenceReason.setOnTextChangedListener { text ->
            updateConfirmButton(binding.btnAbsenceConfirm, text.isNotBlank())
        }
        binding.etWarningReason.setOnTextChangedListener { text ->
            updateConfirmButton(binding.btnWarningConfirm, text.isNotBlank())
        }
    }

    private fun updateConfirmButton(button: UButton, isNotEmpty: Boolean) {
        button.isEnabled = isNotEmpty
        val bgColor = if (isNotEmpty) R.color.neutral000 else R.color.neutral100
        val textColor = if (isNotEmpty) R.color.neutral800 else R.color.neutral400
        button.setUBackgroundColor(ContextCompat.getColor(requireContext(), bgColor))
        button.setTextColor(ContextCompat.getColor(requireContext(), textColor))
    }

    private fun toggleMode(targetMode: DialogMode) {
        currentMode = if (currentMode == targetMode) DialogMode.DEFAULT else targetMode
        updateUIForMode(currentMode)
    }

    private fun updateUIForMode(mode: DialogMode) {
        resetButtonStyles()
        binding.layoutAbsenceInput.visibility = View.GONE
        binding.layoutWarningInput.visibility = View.GONE
        binding.tvNewBadge.visibility = View.GONE

        historyAdapter.setEditMode(mode == DialogMode.EDIT)

        when (mode) {
            DialogMode.ABSENCE -> {
                binding.layoutAbsenceInput.visibility = View.VISIBLE
                highlightButton(binding.btnAbsenceView, R.color.danger100, R.color.danger500)
            }
            DialogMode.WARNING -> {
                binding.layoutWarningInput.visibility = View.VISIBLE
                highlightButton(binding.btnWarningView, R.color.warning100, R.color.warning500)
            }
            DialogMode.EDIT -> {
                binding.tvNewBadge.visibility = View.VISIBLE
                highlightButton(binding.btnRecordEdit, R.color.primary100, R.color.primary500)
            }
            else -> {}
        }
    }

    private fun highlightButton(button: UButton, bgColorRes: Int, colorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), colorRes)
        button.setUBackgroundColor(ContextCompat.getColor(requireContext(), bgColorRes))
        button.strokeColor = color
        button.setTextColor(color)
        button.setTopIconTint(color)
    }

    private fun resetButtonStyles() {
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.neutral400)
        listOf(binding.btnWarningView, binding.btnAbsenceView, binding.btnRecordEdit).forEach { btn ->
            btn.setUBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neutral000))
            btn.strokeColor = ContextCompat.getColor(requireContext(), R.color.neutral300)
            btn.setTextColor(defaultColor)
            btn.setTopIconTint(defaultColor)
        }
    }

    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}