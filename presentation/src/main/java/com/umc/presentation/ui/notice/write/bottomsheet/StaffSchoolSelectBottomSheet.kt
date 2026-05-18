package com.umc.presentation.ui.notice.write.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.school.SchoolInfo
import com.umc.presentation.databinding.BottomSheetStaffSchoolSelectBinding
import com.umc.presentation.ui.signUp.adapter.SchoolListAdapter

class StaffSchoolSelectBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val STAFF_SCHOOL_SELECT = "staffSchoolSelect"
        const val BUNDLE_KEY_SCHOOL_ID = "staff_school_id"
        const val BUNDLE_KEY_SCHOOL_NAME = "staff_school_name"
        private const val ARG_SCHOOL_LIST = "arg_school_list"
        private const val ARG_SELECTED_SCHOOL_ID = "arg_selected_school_id"

        fun newInstance(
            schoolList: List<SchoolInfo>,
            selectedSchoolId: Long? = null
        ): StaffSchoolSelectBottomSheet {
            return StaffSchoolSelectBottomSheet().apply {
                arguments = bundleOf(
                    ARG_SCHOOL_LIST to ArrayList(schoolList),
                    ARG_SELECTED_SCHOOL_ID to (selectedSchoolId ?: -1L)
                )
            }
        }
    }

    private val schoolList: List<SchoolInfo> by lazy {
        @Suppress("UNCHECKED_CAST")
        (arguments?.getSerializable(ARG_SCHOOL_LIST) as? ArrayList<SchoolInfo>)?.toList() ?: emptyList()
    }

    private val selectedSchoolId: Long? by lazy {
        arguments?.getLong(ARG_SELECTED_SCHOOL_ID, -1L)?.takeIf { it != -1L }
    }

    private var _binding: BottomSheetStaffSchoolSelectBinding? = null
    private val binding get() = _binding!!

    private val schoolListAdapter: SchoolListAdapter by lazy {
        SchoolListAdapter(object : SchoolListAdapter.SchoolListDelegate {
            override fun onClickNotice(item: SchoolInfo) {
                setFragmentResult(
                    STAFF_SCHOOL_SELECT,
                    bundleOf(
                        BUNDLE_KEY_SCHOOL_ID to item.schoolId.toLong(),
                        BUNDLE_KEY_SCHOOL_NAME to item.schoolName
                    )
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
        _binding = BottomSheetStaffSchoolSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textAllOption.setOnClickListener {
            setFragmentResult(
                STAFF_SCHOOL_SELECT,
                bundleOf(BUNDLE_KEY_SCHOOL_ID to -1L)
            )
            dismiss()
        }

        binding.recyclerSchoolList.apply {
            adapter = schoolListAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }

        schoolListAdapter.submitList(schoolList)
        schoolListAdapter.setSelectedSchoolId(selectedSchoolId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
