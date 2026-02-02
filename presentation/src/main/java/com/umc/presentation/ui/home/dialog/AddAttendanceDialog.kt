package com.umc.presentation.ui.home.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.umc.presentation.databinding.CustomDialogAddAttendanceBinding

//onConfirm은 생성하기 눌렀을 때
class AddAttendanceDialog(
    private val onReject: () -> Unit,
    private val onConfirm: () -> Unit
) : DialogFragment() {

    private var _binding: CustomDialogAddAttendanceBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        // 디자인 가이드에 따라 화면 너비의 90% 설정
        val params = dialog?.window?.attributes
        params?.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomDialogAddAttendanceBinding.inflate(inflater, container, false)
        // 다이얼로그 배경을 투명하게 하여 커스텀 배경(radius16)이 보이게 설정
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 우측 상단 닫기 아이콘
            imvIconClose.setOnClickListener { dismiss() }

            // '아니오' 버튼 (부정)
            btnNegative.setOnClickListener {
                onReject()
                dismiss()
            }

            // '네, 등록할게요' 버튼 (긍정)
            btnPositive.setOnClickListener {
                onConfirm()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}