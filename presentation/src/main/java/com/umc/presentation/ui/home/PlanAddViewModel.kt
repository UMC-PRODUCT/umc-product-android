package com.umc.presentation.ui.home

import android.util.Log
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.model.home.ParticipantItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class PlanAddViewModel @Inject
constructor() : BaseViewModel<PlanAddFragmentUiState, PlanAddFragmentEvent>(
    PlanAddFragmentUiState()){

    private val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    private val timeSdf = SimpleDateFormat("a h:mm", Locale.KOREAN)


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

                    // 이 경우, 덮어쓰기 (기존 꺼 유지 하니 꼬인다)
                    //val addList = (selectedParticipants + event.participants).distinct().toList()
                    copy(selectedParticipants = event.participants.toList())
                }
                Log.d("log_home", "추가 결과: ${uiState.value.selectedParticipants}")
            }

            //recylcerview에서 삭제할 경우
            is PlanAddFragmentEvent.RemoveParticipants -> {
                updateState {
                    // event에서 전달해준 string name을 뺀 data list를 새로 성성
                    val newList = selectedParticipants.filter { it.name != event.user.name }
                    copy(selectedParticipants = newList)
                }
                Log.d("log_home", "삭제 결과: ${uiState.value.selectedParticipants}")
            }

            //인원을 검색할 경우
            is PlanAddFragmentEvent.SearchParticipants -> {
                //더미데이터
                val searchQuery = event.user.name
                val results = listOf("어헛차", "박유수", "어헛차", "김하나")
                    .filter { it.contains(searchQuery) }
                    .map { ParticipantItem(it) }

                updateState {
                    copy(
                        searchResults = results,
                        isSearchOverlayVisible = true
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
                    copy(selectedParticipants = newList.toList())
                }
            }

            //검색 결과 초기화
            is PlanAddFragmentEvent.ClearSearch -> {
                updateState {
                    copy(
                        searchResults = emptyList(),
                        isSearchOverlayVisible = false
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
                    val selectedCategories = categories.map{
                        if(it.name == event.category.name){
                            it.copy(isChecked = !it.isChecked)
                        }
                        else{
                            it
                        }
                    }
                    copy(categories = selectedCategories)
                }
            }

                else -> {}
        }
    }


}


data class PlanAddFragmentUiState(
    val dummyData: String = "",

    //하루 종일 부분에 체크가 되었나
    val isAllDay: Boolean = false,

    //일정 관련
    val planTitle: String = "",    //필수
    val planLocation: String = "",
    val planDetail: String = "",

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
    val isSearchOverlayVisible: Boolean = false, //검색 결과창 보여주기

    //카테고리 관련
    val categories: List<CategoryItem> = listOf(
        CategoryItem("팀 활동"),
        CategoryItem("개인 학습"),
        CategoryItem("멘토링"),
        CategoryItem("행사"),
    ), //카테고리 리스트
    val selectedCategory: List<CategoryItem> = emptyList(), //선택 결과(recyclerview)

    
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
            val isDateTimeValid = startDateText != "시작 날짜" &&
                    startTimeText != "시작 시간" &&
                    endDateText != "종료 날짜" &&
                    endTimeText != "종료 시간"

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