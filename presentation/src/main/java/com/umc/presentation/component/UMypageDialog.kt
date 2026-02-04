package com.umc.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.umc.presentation.databinding.CustomDialogMypageBinding

class UMypageDialog(
    private val model: UMypageDialogModel,
    private val onPositive: () -> Unit
) : DialogFragment() {

    private var _binding: CustomDialogMypageBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        // 화면 너비의 약 90% 정도로 다이얼로그 크기를 설정
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogMypageBinding.inflate(inflater, container, false)
        // 배경을 투명하게 설정 (커스텀 배경 radius 적용을 위함)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 데이터 바인딩 객체에 모델 전달
        binding.model = model
        binding.executePendingBindings()

        setupListeners()
    }

    private fun setupListeners() {
        // 단일 버튼 모드일 때의 확인 버튼
        binding.btnComfirm.setOnClickListener {
            dismiss()
        }

        // 2버튼 모드일 때의 취소 버튼
        binding.btnNegative.setOnClickListener {
            dismiss()
        }

        // 2버튼 모드일 때의 긍정(로직 처리) 버튼
        binding.btnPositive.setOnClickListener {
            onPositive() // 전달받은 람다 실행
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}