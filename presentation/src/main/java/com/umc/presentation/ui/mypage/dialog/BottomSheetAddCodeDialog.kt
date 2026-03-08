package com.umc.presentation.ui.mypage.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.databinding.LayoutBottomSheetAddCodeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomSheetAddCodeDialog() : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetAddCodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BottomSheetAddCodeViewModel by viewModels()

    override fun onStart() {
        super.onStart()


        // 다이얼로그의 내부 뷰(design_bottom_sheet)를 찾아 높이를 설정
        (dialog as? BottomSheetDialog)?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let { bottomSheet ->
            val behavior = BottomSheetBehavior.from(bottomSheet)

            // 1. 레이아웃 파라미터의 높이를 화면 전체의 80%로 설정
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            bottomSheet.layoutParams = layoutParams

            // 2. 초기 상태를 확장 상태(EXPANDED)로 고정
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

        }


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetAddCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = viewModel

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect { event ->
                    handleEvent(event)
                }
            }
        }



    }

    private fun handleEvent(event: BottomSheetAddCodeEvent) {
        when (event) {
            is BottomSheetAddCodeEvent.Confirm -> {
                Toast.makeText(requireContext(), "활동기록이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            is BottomSheetAddCodeEvent.ShowToast -> {
                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    protected fun LifecycleOwner.repeatOnStarted(
        viewLifecycleOwner: LifecycleOwner,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}