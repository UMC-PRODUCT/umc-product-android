package com.umc.data.response

import com.google.gson.annotations.SerializedName

/**
 * https://developers.kakao.com/docs/latest/ko/local/dev-guide
 * 참고
 * ㅎ
 * **/

data class KakaoSearchResponse(
    val documents: List<PlaceDocument>
)

data class PlaceDocument(
    @SerializedName("place_name") val placeName: String, // 장소명
    @SerializedName("address_name") val addressName: String, // 지번 주소
    @SerializedName("road_address_name") val roadAddressName: String, // 도로명 주소
    val x: String, // 경도 (Longitude)
    val y: String  // 위도 (Latitude)
)