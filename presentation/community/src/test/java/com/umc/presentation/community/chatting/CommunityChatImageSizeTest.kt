package com.umc.presentation.community.chatting

import com.umc.domain.model.enums.UploadFileCategory
import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommunityChatImageSizeTest {
    @Test fun fourImagesAllowExactlyFortyMiBAndRejectOneExtraByte() {
        val tenMiB = UploadFileCategory.ETC.maxSizeBytes
        assertNull(validateCommunityChatImageSizes(List(4) { tenMiB }))
        assertEquals(CommunityChatImageSizeError.TOTAL_TOO_LARGE,
            validateCommunityChatImageSizes(listOf(tenMiB, tenMiB, tenMiB, tenMiB + 1)))
        assertEquals(CommunityChatImageSizeError.TOTAL_TOO_LARGE,
            validateCommunityChatImageSizes(listOf(Long.MAX_VALUE, 1)))
    }

    @Test fun totalLimitDoesNotReplaceTheIndividualLimitOrAllowUnknownSizes() {
        assertEquals(CommunityChatImageSizeError.IMAGE_TOO_LARGE,
            validateCommunityChatImageSizes(listOf(UploadFileCategory.ETC.maxSizeBytes + 1)))
        assertEquals(CommunityChatImageSizeError.UNREADABLE, validateCommunityChatImageSizes(listOf(1L, null)))
    }

    @Test fun exactlyTenMbIsAllowedAndOneExtraByteIsRejected() {
        val limit = UploadFileCategory.ETC.maxSizeBytes.toInt()
        assertEquals(limit.toLong(), readCommunityChatImageSize(ByteArrayInputStream(ByteArray(limit))))
        assertEquals(limit + 1L, readCommunityChatImageSize(ByteArrayInputStream(ByteArray(limit + 1))))
    }

    @Test fun unknownSizeDoesNotRequireReadingTheEntireLargeImage() {
        val input = ByteArrayInputStream(ByteArray(100_000))
        assertEquals(10_001L, readCommunityChatImageSize(input, limit = 10_000))
        assertEquals(89_999, input.available())
        assertEquals(0L, readCommunityChatImageSize(ByteArrayInputStream(byteArrayOf()), limit = 10_000))
    }
}
