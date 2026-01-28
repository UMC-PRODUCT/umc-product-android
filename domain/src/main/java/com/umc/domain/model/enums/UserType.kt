package com.umc.domain.model.enums

//유저 정보(현재 기수 or OB)
enum class UserType { ACTIVE, OB }

//유저의 파트
enum class UserPart(val label: String){
    ANDROID("Android"),
    IOS("IOS"),
    WEB("Web"),
    PLAN("Plan"),
    DESIGN("Design"),
    SPRING("Spring"),
    NODEJS("NodeJS")

}