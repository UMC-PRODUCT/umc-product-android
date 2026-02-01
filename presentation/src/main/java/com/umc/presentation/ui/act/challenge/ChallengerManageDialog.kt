package com.umc.presentation.ui.act.challenge

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.presentation.R
import com.umc.domain.model.act.challenger.HistoryItem
import com.umc.presentation.component.UButton
import com.umc.domain.model.act.challenger.UChallengerManageDialogModel
import com.umc.presentation.databinding.CustomDialogChallengerManageBinding
import com.umc.presentation.ui.act.adapter.ChallengerHistoryAdapter

class ChallengerManageDialog(
    private val model: UChallengerManageDialogModel,
    private val onAbsenceSubmit: (String) -> Unit = {},
    private val onWarningSubmit: (String) -> Unit = {},
    private val onDeleteHistory: (HistoryItem) -> Unit = {}
) : DialogFragment() {

    private var _binding: CustomDialogChallengerManageBinding? = null
    private val binding get() = _binding!!

    private enum class DialogMode { DEFAULT, ABSENCE, WARNING, EDIT }
    private var currentMode = DialogMode.DEFAULT

    private val historyAdapter by lazy {
        ChallengerHistoryAdapter(onDeleteClick = { item -> onDeleteHistory(item) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomDialogChallengerManageBinding.inflate(inflater, container, false)
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
        }
        historyAdapter.submitList(model.history)
    }

    private fun initEvent() {
        binding.imvIconClose.setOnClickListener { dismiss() }

        // 이미 선택된 모드를 다시 누르면 DEFAULT로 복구
        binding.btnWarningView.setOnClickListener { toggleMode(DialogMode.WARNING) }
        binding.btnAbsenceView.setOnClickListener { toggleMode(DialogMode.ABSENCE) }
        binding.btnRecordEdit.setOnClickListener { toggleMode(DialogMode.EDIT) }

        binding.btnAbsenceConfirm.setOnClickListener {
            if (it.isEnabled) {
                onAbsenceSubmit(binding.etAbsenceReason.getText())
                updateUIForMode(DialogMode.DEFAULT)
            }
        }

        binding.btnWarningConfirm.setOnClickListener {
            if (it.isEnabled) {
                onWarningSubmit(binding.etWarningReason.getText())
                updateUIForMode(DialogMode.DEFAULT)
            }
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
        if (isNotEmpty) {
            button.setUBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neutral000))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral800))
        } else {
            button.setUBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neutral100))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral400))
        }
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

        // 수정 모드일 때만 X 버튼 표시
        historyAdapter.setEditMode(mode == DialogMode.EDIT)

        when (mode) {
            DialogMode.DEFAULT -> { /* 초기 상태: 버튼 스타일 초기화 및 히스토리만 표시 */ }
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
        }
    }

    private fun highlightButton(button: UButton, bgColorRes: Int, colorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), colorRes)
        button.setUBackgroundColor(ContextCompat.getColor(requireContext(), bgColorRes))
        button.strokeColor = color
        button.setTextColor(color)
    }

    private fun resetButtonStyles() {
        val context = requireContext()
        listOf(binding.btnWarningView, binding.btnAbsenceView, binding.btnRecordEdit).forEach { btn ->
            btn.setUBackgroundColor(ContextCompat.getColor(context, R.color.neutral000))
            btn.strokeColor = ContextCompat.getColor(context, R.color.neutral300)
            btn.setTextColor(ContextCompat.getColor(context, R.color.neutral400))
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