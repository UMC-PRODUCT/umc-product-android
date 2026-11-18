package com.umc.lint

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 테스트 스텁은 소스에서 컴파일돼 이름이 바뀌지 않으므로, 라이브러리 jar에서 읽은 이름 처리는 여기서 따로 확인한다.
 * material3 Text는 Color·TextUnit을 받아 jar 안의 이름이 "Text--4IGK_g"다.
 */
class SourceNameTest {

    @Test
    fun `value class 때문에 바뀐 JVM 이름을 소스 이름으로 되돌린다`() {
        assertEquals("Text", sourceName("Text--4IGK_g"))
        assertEquals("TextField", sourceName("TextField"))
    }
}
