package com.umc.component.util

import com.umc.component.util.UPointFormat.toPointText
import com.umc.component.util.UPointFormat.toSignedPointText
import org.junit.Assert.assertEquals
import org.junit.Test

class UPointFormatTest {
    @Test fun `whole numbers drop the decimal point`() {
        assertEquals("2", 2.0.toPointText())
        assertEquals("-4", (-4.0).toPointText())
        assertEquals("0", (-0.0).toPointText())
    }

    @Test fun `legacy half points keep the fraction`() {
        assertEquals("-0.5", (-0.5).toPointText())
        assertEquals("-4.5", (-4.0 - 0.5).toPointText())
    }

    @Test fun `signed text adds plus only for positive scores`() {
        assertEquals("+2", 2.0.toSignedPointText())
        assertEquals("-0.5", (-0.5).toSignedPointText())
        assertEquals("0", 0.0.toSignedPointText())
    }
}
