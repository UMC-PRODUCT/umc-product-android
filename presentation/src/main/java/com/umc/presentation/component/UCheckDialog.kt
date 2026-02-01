package com.umc.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.umc.presentation.databinding.CustomDialogCheckBinding
import androidx.core.graphics.drawable.toDrawable

class UCheckDialog(
    private val model: UCheckDialogModel,
    private val onConfirm: (String) -> Unit
) : DialogFragment() {

    private var _binding: CustomDialogCheckBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = CustomDialogCheckBinding.inflate(inflater, container, false)
        dialog?.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner

        // 레이아웃 겹침 방지 동적 처리
        val params = binding.layoutButtons.layoutParams as ConstraintLayout.LayoutParams
        if (model.isWriteMode) {
            params.topToBottom = binding.layoutInputGroup.id
        } else {
            params.topToBottom = binding.tvReasonDisplay.id
        }
        binding.layoutButtons.layoutParams = params

        initEvent()
    }
    private fun initEvent() {
        binding.imvIconClose.setOnClickListener { dismiss() }
        binding.btnNegative.setOnClickListener { dismiss() }

        binding.btnPositive.setOnClickListener {
            if (model.isWriteMode) {
                // 작성 모드일 때는 입력값 전달
                val result = binding.etReasonInput.getText()
                onConfirm(result)
            } else {
                // 확인 모드일 때는 단순 콜백
                onConfirm("")
            }
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        // 다이얼로그 가로 너비 설정 (화면의 90%)
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}