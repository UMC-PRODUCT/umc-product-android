package com.umc.presentation.ui.notice.write.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.databinding.BottomSheetStaffPartSelectBinding
import com.umc.presentation.ui.notice.write.adapter.StaffPartListAdapter

class StaffPartSelectBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val STAFF_PART_SELECT = "staffPartSelect"
        const val BUNDLE_KEY_PART_NAME = "staff_part_name"
        private const val ARG_SELECTED_PART = "arg_selected_part"

        fun newInstance(selectedPartName: String? = null): StaffPartSelectBottomSheet {
            return StaffPartSelectBottomSheet().apply {
                arguments = bundleOf(ARG_SELECTED_PART to selectedPartName)
            }
        }
    }

    private val selectedPartName: String? by lazy {
        arguments?.getString(ARG_SELECTED_PART)
    }

    private var _binding: BottomSheetStaffPartSelectBinding? = null
    private val binding get() = _binding!!

    private val parts: List<UserPart> = UserPart.entries.filter { it != UserPart.UNKNOWN }

    private val partListAdapter: StaffPartListAdapter by lazy {
        StaffPartListAdapter(object : StaffPartListAdapter.Delegate {
            override fun onClickPart(part: UserPart) {
                setFragmentResult(
                    STAFF_PART_SELECT,
                    bundleOf(BUNDLE_KEY_PART_NAME to part.name)
                )
                dismiss()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetStaffPartSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textAllOption.setOnClickListener {
            setFragmentResult(
                STAFF_PART_SELECT,
                bundleOf(BUNDLE_KEY_PART_NAME to null as String?)
            )
            dismiss()
        }

        binding.recyclerPartList.apply {
            adapter = partListAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }

        partListAdapter.submitList(parts)
        partListAdapter.setSelectedPartName(selectedPartName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
