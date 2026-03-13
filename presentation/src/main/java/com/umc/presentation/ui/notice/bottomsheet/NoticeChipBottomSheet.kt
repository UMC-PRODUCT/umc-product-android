package com.umc.presentation.ui.notice.bottomsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.presentation.databinding.BottomSheetNoticePartBinding

class NoticeChipBottomSheet(
    private val listener: Delegate,
): BottomSheetDialogFragment() {

    interface Delegate {
        fun onClickItem(item: String)
    }

    private val binding: BottomSheetNoticePartBinding by lazy {
        BottomSheetNoticePartBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            textPm.setOnClickListener { onClickAndDismiss("PM") }
            textDesign.setOnClickListener { onClickAndDismiss("Design") }
            textWeb.setOnClickListener { onClickAndDismiss("Web") }
            textAndroid.setOnClickListener { onClickAndDismiss("Android") }
            textIos.setOnClickListener { onClickAndDismiss("IOS") }
            textNodejs.setOnClickListener { onClickAndDismiss("Node.js") }
            textSpring.setOnClickListener { onClickAndDismiss("SpringBoot") }
        }
    }

    private fun onClickAndDismiss(item: String) {
        listener.onClickItem(item)
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}