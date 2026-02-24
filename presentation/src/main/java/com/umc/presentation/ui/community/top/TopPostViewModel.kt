package com.umc.presentation.ui.community.top

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.TrophyBody
import com.umc.domain.model.community.TrophyItem
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.CategoryItem
import com.umc.domain.usecase.community.GetCommunityTrophyUseCase

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TopPostViewModel @Inject constructor(
    private val getTrophyUseCase: GetCommunityTrophyUseCase
) :
BaseViewModel<TopPostFragmentUiState, TopPostFragmentEvent>(
    TopPostFragmentUiState()
) {


    //명예의 전당게시글 가져오기
    fun fetchTrophies(week: Int?, school: String?, part: String?){
        viewModelScope.launch {
            resultResponse(
                response = getTrophyUseCase(week, school, part),
                successCallback = { trophies ->
                    val uiItems = processTrophyList(trophies)
                    updateState {
                        copy(
                            trophyList = trophies,
                            uiTrophyList = uiItems
                        )
                    }

                },
                errorCallback = {
                    updateState { copy(trophyList = emptyList()) }
                }
            )

        }
    }

    //서버에서 가져온 값을 파싱해서 어댑터에 전송 가능하게 만들기
    fun processTrophyList(trophyList: List<TrophyBody>): List<TrophyItem>{
        //최종 아이템이 들어올 곳
        val uiItems = mutableListOf<TrophyItem>()

        //학교 이름별 그룹 (Map<String, List<TrophyBody>>)
        val groupedBySchool = trophyList.groupBy { it.school }

        groupedBySchool.forEach { (schoolName, items) ->
            //학교 이름 헤더 추가
            uiItems.add(TrophyItem.Header(schoolName))

            //해당 학교에 속한 사람들 추가
            items.forEach { body ->
                uiItems.add(TrophyItem.Content(body))
            }
        }

        return uiItems
    }

    //선택한 주차 선택 및 호출
    fun handleSelectWeek(item: CategoryItem){
        //선택한 것 외에 다 false 로 바꾸는 로직 (UI 바꾸기)
        val updatedWeekList = uiState.value.weekList.map{
            it.copy(isChecked = it.name == item.name)

        }
        updateState {
            copy(weekList = updatedWeekList)
        }

        //정수만 뽑아내는 코드)
        val selectedWeek = item.name.replace("""\D""".toRegex(), "").toIntOrNull() ?: 1
        //다시 서버에서 값을 가져오기
        fetchTrophies(selectedWeek, uiState.value.selectedSchool, uiState.value.selectedPart)

    }

    //선택한 학교 선택 및 호출
    fun handleSelectSchool(schoolName: String){
        updateState {
            copy(selectedSchool = schoolName)
        }
        fetchTrophies(uiState.value.selectedWeek, schoolName, uiState.value.selectedPart)
    }

    //선택한 파트 선택 및 호출
    fun handleSelectPart(partName: String) {
        //저장은 서버에 보내주는 양식을 저장(name)
        val partEnum = UserPart.entries.find { it.label == partName } ?: UserPart.UNKNOWN
        updateState {
            copy(selectedPart = partName)
        }
        fetchTrophies(uiState.value.selectedWeek, uiState.value.selectedSchool, partEnum.name)
    }




    //주차 리스트 최신화하여 recyclerview에 줄 인자 정하기
    fun initWeekList(){
        val calendar = Calendar.getInstance()

        //가장 마지막 날로 이동
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

        //이번 달의 총 주차 수 가져오기
        val maxWeeks = calendar.get(Calendar.WEEK_OF_MONTH)

        val weekList = (1..maxWeeks).map { week ->
            CategoryItem(
                name = "${week}주차",
                isChecked = week == 1 //기본값으로 1주차 선택
            )
        }
        updateState {
            copy(weekList = weekList,
            selectedWeek = 1)
        }
    }
    
    //학교 메뉴 선택
    fun onClickSchoolMenu(){
        emitEvent(TopPostFragmentEvent.OnClickSchoolMenu)
    }
    //파트 메뉴 선택
    fun onClickPartMenu(){
        emitEvent(TopPostFragmentEvent.OnClickPartMenu)
    }
}



data class TopPostFragmentUiState(

    //서버에서 받아온 리스트
    val trophyList : List<TrophyBody> = listOf(),

    //recylcerview에 보여줄 리스트
    val uiTrophyList : List<TrophyItem> = listOf(),


    //이번 달 몇주까지 있는지 리스트
    val weekList : List<CategoryItem> = emptyList(),

    //필터링 요소
    val selectedWeek: Int? = 1,
    val selectedSchool: String? = null,
    val selectedPart: String? = null,


    ) : UiState {
    //참여자 명단(recyclerview를 보여주는지 체크 여부)
    val isShow: Boolean
        get() = trophyList.isNotEmpty()
}

sealed interface TopPostFragmentEvent : UiEvent {

    object OnClickSchoolMenu: TopPostFragmentEvent
    object OnClickPartMenu: TopPostFragmentEvent



}