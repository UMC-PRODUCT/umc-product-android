package com.umc.data.response.challenger

import com.google.gson.Gson
import com.umc.data.response.challenger.ChallengerResponse.Companion.toManageModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class ChallengerResponseTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    @Test fun `utc record time after 15h shows the next local day`() {
        assertEquals("2026-11-03", "2026-11-02T15:42:10.123456Z".toDateOnly(seoul))
        assertEquals("2026-11-02", "2026-11-02T14:59:59Z".toDateOnly(seoul))
    }

    @Test fun `offset time and values without zone keep working`() {
        assertEquals("2026-11-03", "2026-11-03T00:42:10+09:00".toDateOnly(seoul))
        assertEquals("2026-11-02", "2026-11-02T15:42:10".toDateOnly(seoul))
        assertEquals("", null.toDateOnly(seoul))
    }

    @Test fun `server numbers sent as strings map to ids and decimal scores`() {
        // 서버는 WRITE_NUMBERS_AS_STRINGS 라 숫자를 문자열로 보낸다
        val json = """
            {"challengerId":"356","gisuId":"6","gisu":"11","schoolId":"29","totalPoints":"-2.5",
             "points":[{"id":"1","pointType":"BEST_WORKBOOK","point":"-0.5","description":"구형","createdAt":"2024-05-01T03:00:00Z"},
                       {"id":"2","pointType":"NO_WORKBOOK_MISSION","point":"-4.0","description":"과제","createdAt":"2026-11-02T15:42:10Z"},
                       {"id":"3","pointType":"BEST_WORKBOOK_V2","point":"2.0","description":"","createdAt":"2026-11-02T00:00:00Z"}]}
        """.trimIndent()
        val model = Gson().fromJson(json, ChallengerResponse::class.java).toManageModel()

        assertEquals(6L, model.gisuId)
        assertEquals(29L, model.schoolId)
        assertEquals(-2.5, model.totalScore, 0.0)
        assertEquals(2.0, model.rewardScore, 0.0)
        assertEquals(4.5, model.penaltyScore, 0.0)
        assertEquals(-0.5, model.history.first().value, 0.0)
    }
}
