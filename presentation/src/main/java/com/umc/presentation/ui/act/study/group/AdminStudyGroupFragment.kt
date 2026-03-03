package com.umc.presentation.ui.act.study.group

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.WindowManager
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.enums.EditDeleteAction
import com.umc.domain.model.request.organization.EditStudyGroupRequest
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.databinding.DialogAdminStudyGroupDeleteBinding
import com.umc.presentation.databinding.DialogAdminStudyGroupEditBinding
import com.umc.presentation.databinding.FragmentAdminStudyGroupBinding
import com.umc.presentation.ui.act.adapter.DropDownAdapter
import com.umc.presentation.ui.act.study.group.adapter.AdminStudyGroupAdapter
import com.umc.presentation.ui.act.study.group.adapter.StudyGroupSettingMenuAdapter
import com.umc.presentation.ui.act.study.group.model.AdminStudyGroupEvent
import com.umc.presentation.ui.act.study.group.model.AdminStudyGroupItemUiModel
import com.umc.presentation.ui.act.study.group.model.AdminStudyGroupState
import com.umc.presentation.ui.act.study.group.model.AdminStudyGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AdminStudyGroupFragment :
    BaseFragment<
            FragmentAdminStudyGroupBinding,
            AdminStudyGroupState,
            AdminStudyGroupEvent,
            AdminStudyGroupViewModel
            >(FragmentAdminStudyGroupBinding::inflate) {

    override val viewModel: AdminStudyGroupViewModel by viewModels()
    private lateinit var adapter: AdminStudyGroupAdapter

    override fun initView() {
        binding.vm = viewModel

        adapter = AdminStudyGroupAdapter(
            onClickSetting = { anchorView, item ->
                showSettingPopup(anchorView, item)
            },
            onClickAddSchedule = { item ->
                findNavController().navigate(R.id.action_to_schedule_add)
            },
            onClickAddMember = { item ->
                findNavController().navigate(R.id.adminStudyGroupAddFragment)
            }
        )


        binding.btnCreateGroup.setOnClickListener {
            findNavController().navigate(R.id.adminStudyGroupAddFragment)
        }


        binding.rvGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroups.adapter = adapter

        viewModel.loadGroups()


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.groups)
            }
        }
    }

    private fun showSettingPopup(anchor: View, item: AdminStudyGroupItemUiModel) {
        val menuItems = listOf(
            StudyGroupSettingMenuItem(
                action = EditDeleteAction.EDIT,
                title = "정보 수정",
                iconRes = R.drawable.ic_edit,
                titleColorRes = R.color.neutral800,
                iconTintRes = R.color.neutral700,
                arrowTintRes = R.color.neutral400,
            ),
            StudyGroupSettingMenuItem(
                action = EditDeleteAction.DELETE,
                title = "그룹 삭제",
                iconRes = R.drawable.ic_trash_can,
                titleColorRes = R.color.danger500,
                iconTintRes = R.color.danger500,
                arrowTintRes = R.color.danger500,
            ),
        )

        val popup = ListPopupWindow(requireContext())

        val menuAdapter = StudyGroupSettingMenuAdapter(menuItems) { clicked ->
            popup.dismiss()
            when (clicked.action) {
                EditDeleteAction.EDIT -> showEditGroupDialog(item)
                EditDeleteAction.DELETE -> showDeleteGroupDialog(item)
            }
        }

        popup.setAdapter(menuAdapter)
        popup.anchorView = anchor
        popup.isModal = true

        val popupWidth = 220.dp(anchor)
        popup.width = popupWidth

        anchor.post {
            popup.horizontalOffset = anchor.width - popupWidth
            popup.verticalOffset = 8.dp(anchor)

            popup.setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_neutral200_round12)
            )

            popup.show()

            popup.listView?.apply {
                divider = ColorDrawable(ContextCompat.getColor(context, R.color.neutral200))
                dividerHeight = 1
                elevation = 12f
            }
        }
    }

    private fun showEditGroupDialog(item: AdminStudyGroupItemUiModel) {
        val dialog = Dialog(requireContext())
        val b = DialogAdminStudyGroupEditBinding.inflate(layoutInflater)
        dialog.setContentView(b.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        b.etGroupName.setText(item.title)
        b.tvSelectedPart.text = item.partLabel.ifBlank { "Web" }

        var isPartDropdownOpen = false
        val parts = listOf("Web", "Android", "iOS", "Server", "Design", "Plan")

        val dropDownAdapter = DropDownAdapter(object : DropDownAdapter.DropDownDelegate {
            override fun onClickItem(text: String) {
                b.tvSelectedPart.text = text
                isPartDropdownOpen = false
                b.cardPartDropdown.visibility = View.GONE
                b.ivArrow.animate().rotation(0f).setDuration(120).start()
            }
        })

        b.rvPartDropdown.apply {
            adapter = dropDownAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = null
        }
        dropDownAdapter.submitList(parts)

        b.clPartDropdown.setOnClickListener {
            isPartDropdownOpen = !isPartDropdownOpen
            b.cardPartDropdown.visibility = if (isPartDropdownOpen) View.VISIBLE else View.GONE
            b.ivArrow.animate()
                .rotation(if (isPartDropdownOpen) 180f else 0f)
                .setDuration(120)
                .start()

            if (isPartDropdownOpen) {
                b.cardPartDropdown.bringToFront()
                b.cardPartDropdown.requestLayout()
                b.cardPartDropdown.invalidate()
            }
        }

        b.ivClose.setOnClickListener { dialog.dismiss() }
        b.btnCancel.setOnClickListener { dialog.dismiss() }

        b.etGroupName.setText(item.title)

        b.btnConfirm.setOnClickListener {
            val newName = b.etGroupName.getText().trim()
            val uiPart = b.tvSelectedPart.text.toString().trim()

            if (newName.isBlank()) return@setOnClickListener

            val req = EditStudyGroupRequest(
                name = newName,
                part = uiPart.toServerPart()
            )
            viewModel.editGroup(item.groupId, req)
            dialog.dismiss()
        }
    }

    private fun String.toServerPart(): String = when (this.lowercase()) {
        "web" -> "WEB"
        "android" -> "ANDROID"
        "ios", "iOS".lowercase() -> "IOS"
        "server" -> "SERVER"
        "design" -> "DESIGN"
        "plan" -> "PLAN"
        else -> "WEB"
    }

    private fun showDeleteGroupDialog(item: AdminStudyGroupItemUiModel) {
        val dialog = Dialog(requireContext())
        val b = DialogAdminStudyGroupDeleteBinding.inflate(layoutInflater)
        dialog.setContentView(b.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        b.ivClose.setOnClickListener { dialog.dismiss() }
        b.btnCancel.setOnClickListener { dialog.dismiss() }
        b.btnDelete.setOnClickListener {
            viewModel.deleteGroup(item.groupId)
            dialog.dismiss()
        }
    }
}

private fun Int.dp(view: View): Int =
    (this * view.resources.displayMetrics.density).toInt()
