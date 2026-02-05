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
import com.umc.domain.model.act.challenger.ChallengerPoint
import com.umc.presentation.component.UButton
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.presentation.databinding.DialogChallengerManageBinding
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


    fun updateData(newModel: ChallengerManageDialogModel) {
        this.model = newModel
        binding.model = newModel
        historyAdapter.submitList(newModel.history)

        binding.etAbsenceReason.apply {
            clearText()
            clearFocus()
        }
        binding.etWarningReason.apply {
            clearText()
            clearFocus()
        }

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
        }
        historyAdapter.submitList(model.history)
    }

    private fun initEvent() {
        binding.imvIconClose.setOnClickListener { dismiss() }

        binding.btnWarningView.setOnClickListener { toggleMode(DialogMode.WARNING) }
        binding.btnAbsenceView.setOnClickListener { toggleMode(DialogMode.ABSENCE) }
        binding.btnRecordEdit.setOnClickListener { toggleMode(DialogMode.EDIT) }

        binding.btnAbsenceConfirm.setOnClickListener {
            if (it.isEnabled) {
                onAbsenceSubmit(binding.etAbsenceReason.getText())
            }
        }

        binding.btnWarningConfirm.setOnClickListener {
            if (it.isEnabled) {
                onWarningSubmit(binding.etWarningReason.getText())
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
    }

    private fun resetButtonStyles() {
        listOf(binding.btnWarningView, binding.btnAbsenceView, binding.btnRecordEdit).forEach { btn ->
            btn.setUBackgroundColor(ContextCompat.getColor(requireContext(), R.color.neutral000))
            btn.strokeColor = ContextCompat.getColor(requireContext(), R.color.neutral300)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.neutral400))
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