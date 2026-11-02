package com.umc.data.api

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.umc.data.request.member.ReportMemberRequest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.POST

class ReportMemberRequestTest {
    @Test fun sendsMultipleReasonsAndDetailInOneRequestWithStableFieldNames() {
        val detail = "반복된 불쾌한 메시지\n추가 설명"
        val json = JsonParser.parseString(Gson().toJson(ReportMemberRequest(listOf("HARASSMENT", "SPAM"), detail))).asJsonObject
        assertEquals(listOf("HARASSMENT", "SPAM"), json["reasons"].asJsonArray.map { it.asString })
        assertEquals(detail, json["detail"].asString)
        assertFalse(json.has("reason"))
        for (field in listOf("reasons", "detail")) assertEquals(field,
            ReportMemberRequest::class.java.getDeclaredField(field).getAnnotation(SerializedName::class.java)?.value)
        val method = MemberApi::class.java.declaredMethods.single { it.name == "reportMember" }
        assertEquals("api/v1/reports/members/{memberId}", method.getAnnotation(POST::class.java)?.value)
    }
}
