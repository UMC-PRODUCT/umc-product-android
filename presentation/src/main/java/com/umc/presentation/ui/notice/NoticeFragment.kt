package com.umc.presentation.ui.notice

import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.organization.GisuItem
import com.umc.presentation.R
import com.umc.presentation.base.BaseFragment
import com.umc.presentation.component.adapter.DropDownAdapter
import com.umc.presentation.component.adapter.DropDownItem
import com.umc.presentation.databinding.FragmentNoticeBinding
import com.umc.presentation.extension.addInfiniteScrollListener
import com.umc.presentation.ui.notice.adapter.NoticeAdapter
import com.umc.presentation.ui.notice.adapter.NoticeChipAdapter
import com.umc.presentation.ui.notice.bottomsheet.NoticeChipBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeUiState, NoticeEvent, NoticeViewModel>(
    FragmentNoticeBinding::inflate,
) {
    override val viewModel: NoticeViewModel by viewModels()

    private val dropDownAdapter: DropDownAdapter<GisuDropDownItem> by lazy {
        DropDownAdapter(object : DropDownAdapter.DropDownDelegate<GisuDropDownItem> {
            override fun onClickItem(item: GisuDropDownItem) {
                viewModel.onClickShowDropDown()
                viewModel.updateNowTitle(item.gisuItem.displayText, item.gisuItem.gisuId.toLong())
            }
        })
    }

    private val noticeChipAdapter : NoticeChipAdapter by lazy {
        NoticeChipAdapter(object : NoticeChipAdapter.NoticeChipDelegate {
            override fun onClickChip(item: NoticeChipState) {
                viewModel.onClickChip(item)
            }
        })
    }

    private val noticeAdapter : NoticeAdapter by lazy {
        NoticeAdapter(object : NoticeAdapter.NoticeDelegate {
            override fun onClickNotice(item: NoticeSummary) {
                viewModel.markNoticeAsRead(item.id)
                navigateToNoticeDetail(item.id)
            }
        })
    }

    override fun initView() {
        binding.apply {
            vm = viewModel

            textNoticeTitle.text = getString(R.string.notice_title, 12)

            recyclerDropdown.apply {
                adapter = dropDownAdapter
                layoutManager = LinearLayoutManager(context)
            }

            recyclerTag.apply {
                adapter = noticeChipAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                itemAnimator = null
            }

            recyclerNotice.apply {
                adapter = noticeAdapter
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null

                addInfiniteScrollListener {
                    viewModel.loadNextPage()
                }
            }

            uchipAll.setOnClickListener { onClickChipAll() }
            uchipNoticeAdmin.setOnClickListener { onClickChipNoticeAdmin() }
            uchipPart.setOnClickListener { showBottomSheet() }
        }
    }

    override fun initStates() {
        super.initStates()

        repeatOnStarted(viewLifecycleOwner) {
            launch {
                viewModel.uiEvent.collect {
                    handleEvent(it)
                }
            }

            launch {
                viewModel.uiState.collect {
                    dropDownAdapter.submitList(it.dropdownList.map { gisu -> GisuDropDownItem(gisu) })
                    noticeChipAdapter.submitList(it.chipList)
                    noticeAdapter.submitList(it.noticeList)
                    noticeAdapter.setReadNoticeIds(it.readNoticeIds)
                }
            }
        }
    }

    override fun handleEvent(event: NoticeEvent) {
        when (event) {
            NoticeEvent.MoveToWriteEvent -> navigateToNoticeWrite()
            is NoticeEvent.MoveToSearchEvent -> navigateToNoticeSearch(event.gisuId)
        }
    }

    private fun navigateToNoticeSearch(gisuId: Long) {
        val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeSearchFragment(gisuId)
        findNavController().navigate(action)
    }

    private fun navigateToNoticeWrite() {
        val selectedGisuId = viewModel.uiState.value.selectedGisu
        val selectedGisu = viewModel.uiState.value.dropdownList.find { it.gisuId.toLong() == selectedGisuId }
        val gisuName = selectedGisu?.let { "${it.generation}기" } ?: ""
        val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeWriteFragment(
            gisuId = selectedGisuId,
            gisuName = gisuName
        )
        findNavController().navigate(action)
    }

    private fun navigateToNoticeDetail(noticeId: Long) {
        val action = NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(noticeId = noticeId)
        findNavController().navigate(action)
    }

    private fun showBottomSheet() {
        val bottomSheet = NoticeChipBottomSheet(object : NoticeChipBottomSheet.Delegate {
            override fun onClickItem(item: String) {
                onClickChipPart()
                viewModel.updateSubChip(item)
            }
        })
        bottomSheet.show(parentFragmentManager, "")
    }

    @SuppressLint("ResourceAsColor")
    private fun onClickChipAll() {
        binding.apply {
            uchipAll.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral800))
            uchipAll.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral800), 1)
            uchipAll.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral100))

            uchipNoticeAdmin.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipNoticeAdmin.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipNoticeAdmin.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))

            uchipPart.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipPart.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipPart.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun onClickChipNoticeAdmin() {
        binding.apply {
            uchipNoticeAdmin.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral800))
            uchipNoticeAdmin.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral800), 1)
            uchipNoticeAdmin.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral100))

            uchipAll.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipAll.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipAll.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))

            uchipPart.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipPart.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipPart.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))
        }
    }


    @SuppressLint("ResourceAsColor")
    private fun onClickChipPart() {
        binding.apply {
            uchipPart.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral800))
            uchipPart.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral800), 1)
            uchipPart.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral100))

            uchipAll.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipAll.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipAll.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))

            uchipNoticeAdmin.setUChipBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.neutral000))
            uchipNoticeAdmin.setUChipBorder(ContextCompat.getColor(requireActivity(), R.color.neutral200), 1)
            uchipNoticeAdmin.setUChipTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral500))
        }
    }
}

private class GisuDropDownItem(val gisuItem: GisuItem) : DropDownItem {
    override val displayText: String get() = gisuItem.displayText
}
