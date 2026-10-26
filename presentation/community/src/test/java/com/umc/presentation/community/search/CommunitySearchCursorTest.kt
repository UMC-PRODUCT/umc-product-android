package com.umc.presentation.community.search

import org.junit.Assert.*
import org.junit.Test

class CommunitySearchCursorTest {
    @Test fun onlyAbsentCursorIndicatesEnd() {
        assertNull(validatedSearchOffset(null, 20))
        assertEquals(40, validatedSearchOffset("40", 20))
    }

    @Test fun invalidCursorsFailInsteadOfTruncatingResults() {
        listOf("", "invalid", "2147483648", "-1", "0", "10", "20").forEach { cursor ->
            assertTrue("cursor=$cursor", runCatching { validatedSearchOffset(cursor, 20) }.isFailure)
        }
    }

    @Test fun firstPageCannotRepeatOffsetZero() {
        assertTrue(runCatching { validatedSearchOffset("0", 0) }.isFailure)
        assertEquals(20, validatedSearchOffset("20", 0))
    }
}
