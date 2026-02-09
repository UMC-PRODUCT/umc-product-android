package com.umc.presentation.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.enums.CategoryType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.home.LocationItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.presentation.R
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class PlanAddViewModel @Inject
constructor(
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase, //최근 장소 기록 불러오기;
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase, //최근 장소 기록 업데이트하기
    private val getSearchLocationUseCase: GetSearchLocationUseCase,
) : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){

    private val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val timeSdf = SimpleDateFormat("a h:mm", Locale.KOREAN)

    // 임시 더미 데이터 (실제로는 서버나 DB에서 가져와야 함)
    private val allChallengers = listOf(
        ParticipantItem("박유수", UserPart.ANDROID, "숭실대학교"),
        ParticipantItem("어헛차", UserPart.ANDROID, "숭실대학교"),
        ParticipantItem("김하나", UserPart.DESIGN, "서울대학교"),
        ParticipantItem("이두리", UserPart.IOS, "고려대학교"),
        ParticipantItem("최삼이", UserPart.NODE_JS, "연세대학교"),
        ParticipantItem("박사성", UserPart.ANDROID, "숭실대학교")
    )

    init {
        loadRecentPlaces()
    }


    //handleEvent
    fun handleEvent(event: PlanAddFragmentEvent){
        when(event){
            // 1. 날짜 및 시간 관련 이벤트
            is PlanAddFragmentEvent.UpdateStartDate,
            is PlanAddFragmentEvent.UpdateStartTime,
            is PlanAddFragmentEvent.UpdateEndDate,
            is PlanAddFragmentEvent.UpdateEndTime -> handleDateTime(event)

            // 2. 인원 및 검색 관련 이벤트
            is PlanAddFragmentEvent.UpdateParticipants,
            is PlanAddFragmentEvent.RemoveParticipants,
            is PlanAddFragmentEvent.SearchParticipants,
            is PlanAddFragmentEvent.ToggleParticipants,
            is PlanAddFragmentEvent.ClearSearch -> handleParticipants(event)

            // 3. 카테고리 관련 이벤트
            is PlanAddFragmentEvent.SelectCategory -> handleCategory(event)

            // 4. 기타 관련
            is PlanAddFragmentEvent.UpdatePlanTitle -> {
                updateState {
                    copy(planTitle = event.title)
                }
            }
            is PlanAddFragmentEvent.UpdatePlanLocation -> {
                updateState {
                    copy(planLocation = event.location)
                }
            }
            is PlanAddFragmentEvent.UpdatePlanDetail -> {
                updateState {
                    copy(planDetail = event.detail)
                }
            }

            else -> {}
        }
    }


    //날짜 조정 (얘는 Fragment에서 날짜 받아온 후 수행하는 이벤트입니다)
    private fun handleDateTime(event: PlanAddFragmentEvent){
        when(event){
            //시작 날짜 바꾸기
            is PlanAddFragmentEvent.UpdateStartDate -> {
                val newCalendar = (uiState.value.startDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(startDate = newCalendar,
                        startDateText = dateSdf.format(newCalendar.time)
                    )
                }
            }

            //시작 시간 바꾸기
            is PlanAddFragmentEvent.UpdateStartTime -> {
                val newCalendar = (uiState.value.startTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(startTime = newCalendar,
                        startTimeText = timeSdf.format(newCalendar.time)
                    )

                }
            }

            //끝나는 날짜 바꾸기
            is PlanAddFragmentEvent.UpdateEndDate -> {
                val newCalendar = (uiState.value.endDate.clone() as Calendar).apply {
                    set(event.year, event.month, event.day)
                }
                updateState {
                    copy(endDate = newCalendar,
                        endDateText = dateSdf.format(newCalendar.time)
                    )

                }
            }

            //끝나는 시간 바꾸기
            is PlanAddFragmentEvent.UpdateEndTime -> {
                val newCalendar = (uiState.value.endTime.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, event.hour)
                    set(Calendar.MINUTE, event.minute)
                }
                updateState {
                    copy(endTime = newCalendar,
                        endTimeText = timeSdf.format(newCalendar.time)
                    )
                }
            }

            else -> {}
        }
    }

    //CSV 및 인원 값 업데이트
    private fun handleParticipants(event: PlanAddFragmentEvent) {
        when(event){
            //CSV에서 업데이트 한 경우
            is PlanAddFragmentEvent.UpdateParticipants -> {
                updateState {

                    val newList = event.participants.toList()

                    // 결과 텍스트 만들기
                    val summaryText = when {
                        newList.isEmpty() -> ""
                        newList.size == 1 -> newList[0].name
                        else -> "${newList[0].name} 외 ${newList.size - 1}명"
                    }

                    // 이 경우, 덮어쓰기 (기존 꺼 유지 하니 꼬인다)
                    //val addList = (selectedParticipants + event.participants).distinct().toList()
                    copy(
                        selectedParticipants = newList,
                        selectedParticipantsString = summaryText
                        )
                }
                Log.d("log_home", "추가 결과: ${uiState.value.selectedParticipants}")
            }

            //recylcerview에서 삭제할 경우
            is PlanAddFragmentEvent.RemoveParticipants -> {
                updateState {
                    // event에서 전달해준 string name을 뺀 data list를 새로 성성
                    val newList = selectedParticipants.filter { it.name != event.user.name }

                    // 결과 텍스트 만들기
                    val summaryText = when {
                        newList.isEmpty() -> ""
                        newList.size == 1 -> newList[0].name
                        else -> "${newList[0].name} 외 ${newList.size - 1}명"
                    }

                    copy(
                        selectedParticipants = newList.toList(),
                        selectedParticipantsString = summaryText
                    )
                }
                Log.d("log_home", "삭제 결과: ${uiState.value.selectedParticipants}")
            }

            //인원을 검색할 경우
            is PlanAddFragmentEvent.SearchParticipants -> {
                
                //텍스트가 비어있으면 = 전부 보여주기
                //아니면 필터링
                val searchQuery = event.user.name
                val results = if (searchQuery.isBlank()) {
                    allChallengers
                } else {
                    allChallengers.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }

                updateState {
                    copy(
                        searchResults = results,
                        searchQuery = searchQuery,
                        isSearching = true
                    )
                }

            }

            //토글을 할 때, 해당 유저가 있는지 판단해서 추가하기 로직
            is PlanAddFragmentEvent.ToggleParticipants -> {
                updateState {
                    //현재 토글한 유저의 정보가 이미 선택창에 있는지 확인
                    /**여기서는 선택창에 있다면 이미 선택되었다고 판단해서 지우고, 없으면 추가하는 로직**/
                    val isExist = selectedParticipants.any { it.name == event.user.name }
                    val newList = if (isExist) {
                        selectedParticipants.filter { it.name != event.user.name } // 있으면 제거
                    }
                    else {
                        selectedParticipants + event.user // 없으면 추가
                    }

                    //결과 스트링 작성
                    val summaryText = when {
                        newList.isEmpty() -> ""
                        newList.size == 1 -> newList[0].name
                        else -> "${newList[0].name} 외 ${newList.size - 1}명"
                    }


                    copy(
                        selectedParticipants = newList.toList(),
                        selectedParticipantsString = summaryText
                        )
                }
                Log.d("log_home", "토글 결과: ${uiState.value.selectedParticipants}")
            }

            //검색 결과 초기화
            is PlanAddFragmentEvent.ClearSearch -> {
                updateState {
                    copy(
                        searchResults = emptyList(),
                        searchQuery = "",
                        isSearching = false
                    )
                }
            }

            else -> {}
        }
    }

    // 카테고리 관련 handleEvent
    private fun handleCategory(event: PlanAddFragmentEvent) {
        when(event){
            is PlanAddFragmentEvent.SelectCategory -> {
                updateState {
                    //카테고리 uistate 업데이트
                    val selectedCategories = categories.map{
                        //만약 터치한 놈이 리스트 중 하나랑 같으면
                        if(it.name == event.category.name){
                            it.copy(isChecked = !it.isChecked)
                        }
                        else{
                            it.copy(isChecked = it.isChecked)
                        }
                    }

                    //선택된 카테코리 필터링
                    val selectedOnes = selectedCategories.filter { it.isChecked }
                    //더미 텍스트 및 선택 여부 반영
                    val isSelected = when {
                        selectedOnes.isEmpty() -> false
                        else -> true
                    }
                    val summaryText = when {
                        selectedOnes.isEmpty() -> ""
                        selectedOnes.size <= 3 -> selectedOnes.joinToString(", ") { it.name }
                        else -> "${selectedOnes.take(3).joinToString(", ") { it.name }} 외 ${selectedOnes.size - 3}개"
                    }

                    copy(categories = selectedCategories,
                        isSeletedCategory = isSelected,
                        selectedCategoriesString = summaryText
                    )

                }
            }

                else -> {}
        }
    }


    //하루종일 관련
    //모집 중 스위치 누를 때마다 상태 변화하고 필터링
    fun setAllday(isAllday: Boolean) {
        updateState { copy(isAllDay = isAllday) }
    }

    //장소 검색 기록 dataStore usecase
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places) }
            }
        }
    }

    //장소 선택 시 기록 추가
    fun saveRecentPlace(place: String) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(place)
        }
    }

    //장소 검색 = KAKAO API
    fun searchLocation(query: String) {
        if (query.isBlank()) return // 빈 값은 검색하지 않음

        viewModelScope.launch {
            resultResponse(
                response = getSearchLocationUseCase(query),
                successCallback = { locationList ->
                    updateState {
                        copy(searchResultList = locationList)
                    }
                },
                errorCallback = { errorCode ->
                   /**검색 실패 로직**/
                }
            )
        }
    }


}


data class PlanAddFragmentUiState(

    //운영진 여부 판단
    val isManager: Boolean = true,

    //하루 종일 부분에 체크가 되었나
    val isAllDay: Boolean = false,

    //일정 및 장소 관련
    val planTitle: String = "",    //필수
    val planLocation: String = "",
    val planDetail: String = "",
    val recentSearchList: List<String> = emptyList(), //최근 장소 검색 기록 (DATASTORE)
    val searchResultList: List<LocationItem> = emptyList(), //장소 검색 결과를 보여줄 곳

    //시간 관련
    val startDate: Calendar = Calendar.getInstance(),
    val startTime: Calendar = Calendar.getInstance(),
    val endDate: Calendar = Calendar.getInstance(),
    val endTime: Calendar = Calendar.getInstance(),
    //(필수)
    val startDateText: String = "시작 날짜",
    val startTimeText: String = "시작 시간",
    val endDateText : String = "종료 날짜",
    val endTimeText : String = "종료 시간",


    //인원 검색 관련
    /**TODO 일단은 이름만 받는다고 가정**/
    val selectedParticipants: List<ParticipantItem> = emptyList(), //선택된 참여자 결과(recyclerview에 쓰임)
    val searchResults: List<ParticipantItem> = emptyList(), //검색 결과
    val searchQuery: String = "", //검색하는 내용
    val isSearching: Boolean = false,
    val selectedParticipantsString : String = "", //cdv에 보여줄 string

    //카테고리 리스트
    val categories: List<CategoryItem> = listOf(
        CategoryItem(CategoryType.NETWORKING.label, R.drawable.ic_networking_off, R.drawable.ic_networking_on),
        CategoryItem(CategoryType.PROJECT.label, R.drawable.ic_project_off, R.drawable.ic_project_on),
        CategoryItem(CategoryType.DUES.label, R.drawable.ic_fees_off, R.drawable.ic_fees_on),
        CategoryItem(CategoryType.MEETING.label, R.drawable.ic_meeting_off, R.drawable.ic_meeting_on),

        CategoryItem(CategoryType.ORIENTATION.label, R.drawable.ic_orientation_off, R.drawable.ic_orientation_on),
        CategoryItem(CategoryType.PRESENTATION.label, R.drawable.ic_presentation_off, R.drawable.ic_presentation_on),
        CategoryItem(CategoryType.RETROSPECTIVE.label, R.drawable.ic_retrospective_off, R.drawable.ic_retrospective_on),
        CategoryItem(CategoryType.GENERAL.label, R.drawable.ic_general_off, R.drawable.ic_general_on),

        CategoryItem(CategoryType.LEADERSHIP.label, R.drawable.ic_leadership_off, R.drawable.ic_leadership_on),
        CategoryItem(CategoryType.STUDY.label, R.drawable.ic_study_off, R.drawable.ic_study_on),
        CategoryItem(CategoryType.HACKATHON.label, R.drawable.ic_hackathon_off, R.drawable.ic_hackathon_on),
        CategoryItem(CategoryType.WORKSHOP.label, R.drawable.ic_workshop_off, R.drawable.ic_workshop_on),
    ),
    val isSeletedCategory: Boolean = false,
    val selectedCategoriesString: String = ""
    

    
    ) : UiState {
    //참여자 명단(recyclerview를 보여주는지 체크 여부)
    val isSelectedParticipant: Boolean
        get() = selectedParticipants.isNotEmpty()


    //최종 여부를 판단하는 실시간 계산
    val isRegisterOk: Boolean
        get() {
            //1. 텍스트 3종 세트가 비어있지 않음
            val isTextValid = planTitle.isNotBlank()

            //2. 날짜/시간이 초기값이 아님
            val isDateTimeValid = (isAllDay && (startDateText != "시작 날짜" && endDateText != "종료 날짜"))
                    || (!isAllDay && (startDateText != "시작 날짜" && startTimeText != "시작 시간" &&
                            endDateText != "종료 날짜" && endTimeText != "종료 시간"))

            //3. 참여자가 1명 이상임
            val isParticipantValid = isSelectedParticipant

            return isTextValid && isDateTimeValid && isParticipantValid
        }
}

sealed interface PlanAddFragmentEvent : UiEvent {
    //일정 제목이랑 장소, 상세안내를 입력받는 이벤트
    data class UpdatePlanTitle(val title: String) : PlanAddFragmentEvent
    data class UpdatePlanLocation(val location: String) : PlanAddFragmentEvent
    data class UpdatePlanDetail(val detail: String) : PlanAddFragmentEvent


    //TIME 및 DATE Picker로 값을 가져오는 이벤트
    data class UpdateStartDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateStartTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent
    data class UpdateEndDate(val year: Int, val month: Int, val day: Int) : PlanAddFragmentEvent
    data class UpdateEndTime(val hour: Int, val minute: Int) : PlanAddFragmentEvent

    //CSV 파일 파싱 결과를 가져오느 이벤트
    data class UpdateParticipants(val participants: List<ParticipantItem>) : PlanAddFragmentEvent

    //참여자를 제거하는 이벤트
    data class RemoveParticipants(val user: ParticipantItem) : PlanAddFragmentEvent

    //검색 결과를 보여주는 이벤트
    /**TODO 인자 바뀐다 지금은 목업 데이터**/
    data class SearchParticipants(val user: ParticipantItem) : PlanAddFragmentEvent

    //검색 결과로 나온 거를 토클(체크박스)할 때 이벤트
    data class ToggleParticipants(val user: ParticipantItem) : PlanAddFragmentEvent

    //검색한 거 초기화하는 이벤트
    object ClearSearch : PlanAddFragmentEvent


    //카테코리를 선택하는 이벤트
    data class SelectCategory(val category: CategoryItem): PlanAddFragmentEvent

    //뒤로가기
    object MoveBackPressedEvent : PlanAddFragmentEvent

}