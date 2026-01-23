package com.umc.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.umc.presentation.databinding.CustomDialogWarningBinding

class UWarningDialog(
    private val model: UWarningDialogModel,
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
        binding.model = model // 모델 바인딩

        binding.imvIconClose.setOnClickListener { dismiss() }
        binding.btnNegative.setOnClickListener { dismiss() }
        binding.btnPositive.setOnClickListener {
            //확인 버튼을 누를 경우의 로직을 람다로 빼기
            onConfirm()
            dismiss()
        }
    }

}



data class UWarningDialogModel(
    val title: String,                    // 제목
    val content: String? = null,          // (선택) 상세 설명
    val negativeText: String = "취소",     // 왼쪽 버튼 텍스트
    val positiveText: String,             // 오른쪽 버튼 텍스트
)