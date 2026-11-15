package com.umc.data.response.member

import com.google.gson.Gson
import com.umc.data.response.member.MemberResponse.Companion.toDomain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberResponseUmcProductTest {
    private fun parse(extra: String) = Gson().fromJson(
        """{"id":"1","name":"a","nickname":"b","schoolId":"2","schoolName":"s","challengerHistory":[]$extra}""",
        MemberResponse::class.java,
    ).toDomain()

    @Test fun `isUmcProduct true from member me v2 maps to user info`() {
        val userInfo = parse(""","isUmcProduct":true""")

        assertTrue(userInfo.isUmcProduct)
        assertTrue(userInfo.canEnterMain())
    }

    @Test fun `isUmcProduct is kept on the v1 mapping path too`() {
        // challengerHistory 없이 roles 가 오면 v1 매핑을 탄다
        val userInfo = Gson().fromJson(
            """{"id":"1","name":"a","nickname":"b","schoolId":"2","schoolName":"s","roles":[],"isUmcProduct":true}""",
            MemberResponse::class.java,
        ).toDomain()

        assertTrue(userInfo.isUmcProduct)
        assertTrue(userInfo.canEnterMain())
    }

    @Test fun `missing isUmcProduct is false`() {
        val userInfo = parse("")

        assertFalse(userInfo.isUmcProduct)
        assertFalse(userInfo.canEnterMain())
    }
}
