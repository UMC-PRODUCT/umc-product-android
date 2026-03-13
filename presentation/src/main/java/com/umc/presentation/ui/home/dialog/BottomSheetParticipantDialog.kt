package com.umc.presentation.ui.home.dialog

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.opencsv.CSVReader
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.model.home.SearchResultItem
import com.umc.presentation.databinding.LayoutBottomSheetParticipantAddBinding
import com.umc.presentation.ui.home.PlanAddFragmentEvent
import com.umc.presentation.ui.home.PlanAddViewModel
import com.umc.presentation.ui.home.adapter.AddParticipantDelegate
import com.umc.presentation.ui.home.adapter.BottomSheetAddParticipantAdapter
import com.umc.presentation.ui.home.adapter.BottomSheetSearchParticipantAdapter
import com.umc.presentation.ui.home.adapter.SearchParticipantDelegate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@AndroidEntryPoint
class BottomSheetParticipantDialog(
    private val onConfirm: (List<ParticipantItem>, String) -> Unit
) : BottomSheetDialogFragment(), AddParticipantDelegate, SearchParticipantDelegate {

    private var _binding: LayoutBottomSheetParticipantAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BottomSheetParticipantViewModel by viewModels({ requireParentFragment() })
    private lateinit var addAdapter: BottomSheetAddParticipantAdapter
    private lateinit var searchAdapter: BottomSheetSearchParticipantAdapter

    // csv 파싱 로직 이관
    private val csvPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            //돌아오고 난 뒤, 아래의 처리로직을 수행
            parseCsvFile(it)
        }
    }

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomSheetParticipantAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        //얻배터 초기화
        initAdapters()
        //찾기 초기화
        initSearch()
        //상태 관리
        observeState()
        //무한 스크롤 관리
        setupInfiniteScroll()

        //csv 파일 파싱
        binding.btnUploadCsv.setOnClickListener {
            val mimeTypes = arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "text/plain",
                "application/vnd.ms-excel",
            )
            csvPickerLauncher.launch(mimeTypes)
        }

        //확인 시, viewModel에서 선택한 것들을 전송
        binding.btnConfirm.setOnClickListener { 
            onConfirm(viewModel.uiState.value.selectedParticipants, viewModel.uiState.value.selectedParticipantsString)
            dismiss()
        }
    }

    private fun initAdapters() {
        // 1. 추가된 인원 어댑터 (ParticipantDelegate 구현체인 this 전달)
        addAdapter = BottomSheetAddParticipantAdapter(this)
        binding.rcvAdded.adapter = addAdapter

        // 2. 검색 결과 어댑터 (SearchParticipantDelegate 구현체인 this 전달)
        searchAdapter = BottomSheetSearchParticipantAdapter(this)
        binding.rcvSearchResult.adapter = searchAdapter
    }

    private fun initSearch() {
        // USearchBar 내부의 텍스트 변경 감지
        binding.searchbarParticipant.apply {
            // 텍스트 입력 시 실시간 검색
            setOnTextChangedListener { query ->
                viewModel.searchParticipants(query)

            }


            // 포커스 변경 리스너 등록
            setOnFocusChangedListener { hasFocus ->
                if (getText().isEmpty()) {
                    viewModel.clearSearch()
                }
            }

        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // 추가된 인원 리스트 업데이트
                addAdapter.submitList(state.selectedParticipants)

                // 검색 결과 리스트 가공 (파트별 헤더 추가 로직)
                val processedResults = processSearchResults(state.searchResults)
                searchAdapter.submitList(processedResults)

                // 검색 결과 어댑터 내부의 선택 상태(체크박스) 동기화
                searchAdapter.updateSelectedList(state.selectedParticipants)
            }
        }
    }

    /** List<ParticipantItem>을 헤더가 포함된 List<SearchResultItem>으로 변환 **/
    private fun processSearchResults(results: List<ParticipantItem>): List<SearchResultItem> {
        if (results.isEmpty()) return emptyList()

        // 파트별로 그룹화 (UserPart 필드 기준)
        return results.groupBy { it.userPart }
            .flatMap { (part, members) ->
                listOf(SearchResultItem.Header(part.label)) +
                        members.map { SearchResultItem.Participant(it) }
            }
    }

    private fun setupInfiniteScroll() {
        binding.rcvSearchResult.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // 아래로 스크롤 중이며, 더 이상 아래로 갈 수 없을 때 (바닥 도달)
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    // 다음 페이지가 있으면 추가 로딩
                    viewModel.loadMore()
                }
            }
        })
    }

    /**CSV 파싱 로직은 depricated**/

    //CSV 파일 불러오고 난 뒤 파싱 로직
    private fun parseCsvFile(uri: Uri){
        //이름 정보를 임시로 담을 곳
        val names = mutableListOf<String>()

        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = InputStreamReader(inputStream, "UTF-8")
                val csvReader = CSVReader(reader)

                /**TODO CSV 파일 형식은 추후 물어봐서 수정해야 할수도**/
                // 첫 줄은 건너뛰기 (목차 부분)
                val header = csvReader.readNext()

                //1. 그냥 특정 번째 열을 뒤진다.
                /*
                var nextLine: Array<String>?
                while (csvReader.readNext().also { nextLine = it } != null) {
                    // 첫 번째 열에 이름이 있다고 가정하고 추출한다. (후에 따라 다르게 변경)
                    nextLine?.getOrNull(0)?.let { name ->
                        if (name.isNotBlank()) {
                            names.add(name.trim())
                        }
                    }
                    }
                }
                */

                //2. header에서 뜯어봐서 '이름'이 있는지 확인한다.
                Log.d("log_home", header.contentToString())
                val nameColumnIndex = header?.indexOfFirst { it.trim().contains("이름") } ?: -1
                if(nameColumnIndex == -1){
                    Toast.makeText(requireContext(), "이름 정보가 없는 파일입니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                //그 후, 파싱한다.
                var nextLine: Array<String>?
                while (csvReader.readNext().also { nextLine = it } != null) {
                    nextLine?.getOrNull(nameColumnIndex)?.let { name ->
                        if (name.isNotBlank()) {
                            names.add(name.trim())
                        }
                    }
                }


            }
            // 추출된 이름 리스트를 뷰모델로 전송
            /**TODO CSV 파싱 로직은 현재 엑셀 형식에서 id를 필수로 주어야 하는 문제가 deprecated 된 상태임**/
            val newUsers = mutableListOf<ParticipantItem>()
            for(name in names){
                newUsers.add(ParticipantItem(-1L, name))
            }

            viewModel.updateParticipant(newUsers)

            // 임시 토스트
            Toast.makeText(requireContext(), "${names.size}명의 명단을 불러왔습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()

            // 임시 토스트
            Toast.makeText(requireContext(), "CSV 파일을 읽는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        //다이얼로그 닫히면 검색 초기화(선택 화면 보이게 하기)
        viewModel.clearSearch()
    }


    // --- Delegate 구현부 ---

    override fun onParticipantToggled(item: ParticipantItem) {
        // 검색 결과 리스트에서 항목 클릭 시 (추가 혹은 삭제)
        viewModel.toggleParticipant(item)
    }

    override fun onParticipantRemoved(item: ParticipantItem) {
        // 추가된 인원 리스트에서 '삭제' 버튼 클릭 시 (차피 로직은 같아)
        viewModel.toggleParticipant(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}