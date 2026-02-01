package com.umc.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.umc.presentation.databinding.CustomDialogWarningBinding

class UBasicDialog(
    private val model: UBasicDialogModel,
    private val onConfirm: () -> Unit
): DialogFragment() {

    private var _binding: CustomDialogWarningBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        // 화면 너비의 약 90% 정도로 다이얼로그 크기를 설정합니다.
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomDialogWarningBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = model

        // 아이콘 설정
        setupIcon()

        // 버튼 색상 설정
        setupButtons()

        binding.imvIconClose.setOnClickListener { dismiss() }
        binding.btnNegative.setOnClickListener { dismiss() }
        binding.btnPositive.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }

    private fun setupIcon() {
        binding.imvIconWarning.apply {
            // 아이콘 이미지 설정
            setImageResource(model.iconRes)

            // 아이콘 배경색 설정
            setBackgroundResource(model.iconBackgroundRes)

            // 아이콘 틴트 색상 설정
            setColorFilter(ContextCompat.getColor(requireContext(), model.iconTintRes))
        }
    }

    private fun setupButtons() {
        // Positive 버튼 색상 설정
        binding.btnPositive.apply {
            setTextColor(ContextCompat.getColor(requireContext(), model.positiveTextColorRes))
            strokeColor = ContextCompat.getColor(requireContext(), model.positiveBorderColorRes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}