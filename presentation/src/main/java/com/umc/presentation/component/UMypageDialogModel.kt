package com.umc.presentation.component

data class UMypageDialogModel (
    
    val title : String,         //제목
    val content : String,       //내용
    val isTwoButton : Boolean,  //버튼 2개 or 1개
    val positiveText : String = "",  //긍정 버튼 내용
    val negativeText : String = "취소",  //부정 버튼 내용
    val confirmText : String = "확인",   //단일 버튼 내용

)