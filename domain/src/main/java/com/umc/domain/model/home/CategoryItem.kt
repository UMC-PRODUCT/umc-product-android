package com.umc.domain.model.home


//일정 추가 화면에서 각 카테고리를 선택했는지 여부를 담은 부분입니다.
//이 부분의 경우, 게시글 카테고리 / 위치 등을 전부 담기 때문에 String으로 설정합니다.
data class CategoryItem(
    val name : String,
    val isChecked : Boolean = false
)

