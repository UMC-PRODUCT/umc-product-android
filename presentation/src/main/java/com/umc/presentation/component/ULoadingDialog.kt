package com.umc.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.umc.presentation.databinding.DialogLoadingBinding

class ULoadingDialog : DialogFragment() {

    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    private var message: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoadingBinding.inflate(inflater, container, false)
        binding.message = message
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 외부 터치로 다이얼로그가 닫히지 않도록 설정
        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ULoadingDialog"

        fun newInstance(message: String? = null): ULoadingDialog {
            return ULoadingDialog().apply {
                this.message = message
            }
        }
    }
}

// 확장 함수로 사용하기 편하게 제공
fun FragmentManager.showLoadingDialog(message: String? = null): ULoadingDialog {
    val dialog = ULoadingDialog.newInstance(message)
    dialog.show(this, ULoadingDialog.TAG)
    return dialog
}

fun ULoadingDialog.dismissLoading() {
    if (isAdded) {
        dismiss()
    }
}
