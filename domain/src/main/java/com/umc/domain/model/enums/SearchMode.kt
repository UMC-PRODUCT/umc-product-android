package com.umc.domain.model.enums

//커뮤니티에서 search 시 모드
enum class SearchMode {
    EMPTY, //검색창이 비었을 때
    TYPING, //타이핑 중일 때
    RESULT // 검색 결과를 눌렀을 때
}