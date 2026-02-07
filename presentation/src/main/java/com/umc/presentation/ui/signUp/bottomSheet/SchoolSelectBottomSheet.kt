package com.umc.presentation.ui.signUp.bottomSheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.umc.domain.model.signUp.School
import com.umc.presentation.databinding.BottomSheetSchoolListBinding
import com.umc.presentation.ui.signUp.adapter.SchoolListAdapter

class SchoolSelectBottomSheet: BottomSheetDialogFragment() {

    companion object {
        const val SCHOOL_SELECT = "schoolSelect"
        const val BUNDLE_KEY_SELECT = "school_select_key"
        private const val ARG_SCHOOL_LIST = "arg_school_list"

        fun newInstance(schoolList: List<School>): SchoolSelectBottomSheet {
            return SchoolSelectBottomSheet().apply {
                arguments = bundleOf(
                    ARG_SCHOOL_LIST to schoolList
                )
            }
        }
    }

    private val schoolList : List<School> by lazy {
        (arguments?.getSerializable(ARG_SCHOOL_LIST) as? List<School>) ?: emptyList()
    }

    private val schoolListAdapter : SchoolListAdapter by lazy {
        SchoolListAdapter(object : SchoolListAdapter.SchoolListDelegate{
            override fun onClickNotice(item: School) {
                setFragmentResult(SCHOOL_SELECT, bundleOf(BUNDLE_KEY_SELECT to item))
                dismiss()
            }
        })
    }

    private val binding: BottomSheetSchoolListBinding by lazy {
        BottomSheetSchoolListBinding.inflate(layoutInflater)
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
            recyclerSchoolList.apply {
                adapter = schoolListAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
            }

            schoolListAdapter.submitList(schoolList)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}