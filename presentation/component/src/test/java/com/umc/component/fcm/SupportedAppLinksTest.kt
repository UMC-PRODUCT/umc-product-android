package com.umc.component.fcm

import org.junit.Assert.*
import org.junit.Test

class SupportedAppLinksTest {
    @Test fun unsupportedSharedWebPathsOpenInBrowser() {
        listOf(
            "https://api.university.neordinary.com/community/threads/abc",
            "https://api.university.neordinary.com/community/threads/42/43",
            "https://api.university.neordinary.com/community/threads/0",
            "https://api.university.neordinary.com/community/threads/",
            "https://api.university.neordinary.com/community/threads/card?memberId=-1",
        ).forEach { assertTrue(it, shouldOpenAppLinkInBrowser(it)) }
    }

    @Test fun validAppLinksAndOtherSchemesDoNotUseBrowserFallback() {
        listOf(
            "https://api.university.neordinary.com/community/threads/42",
            "https://api.university.neordinary.com/community/threads?threadId=42",
            "https://api.university.neordinary.com/community/threads/card?memberId=42",
            "umc://thread/42", "umc://thread/abc", "file:///community/threads/abc",
            "https://evil.test/community/threads/abc", "not a uri",
        ).forEach { assertFalse(it, shouldOpenAppLinkInBrowser(it)) }
    }

    @Test fun acceptsOnlyRegisteredThreadAndCardTargets() {
        listOf("umc://thread/42", "umc://thread?threadId=42", "umc://card?memberId=42",
            "https://api.university.neordinary.com/community/threads?threadId=42",
            "https://api.university.neordinary.com/community/threads/42",
            "https://api.university.neordinary.com/community/threads/card?memberId=42")
            .forEach { assertTrue(it, isSupportedAppLink(it)) }
    }

    @Test fun rejectsUnsupportedHostPathAndMissingOrInvalidIds() {
        listOf("https://play.google.com/store/apps/details?id=com.umc.product", "umc://other/42",
            "umc://thread", "umc://thread/42/43", "umc://thread?threadId=0", "umc://card?memberId=-1",
            "https://evil.test/community/threads?threadId=42", "umc://thread:123/42",
            "https://api.university.neordinary.com.evil.test/community/threads?threadId=42",
            "https://api.university.neordinary.com/community/threads?threadId=%20", "not a uri",
            "https://api.university.neordinary.com/community/threads/0",
            "https://api.university.neordinary.com/community/threads/-1",
            "https://api.university.neordinary.com/community/threads/42/43",
            "https://api.university.neordinary.com/community/threads/42/",
            "https://api.university.neordinary.com/community/threads/",
            "https://api.university.neordinary.com/community/threads/abc",
            "https://evil.test/community/threads/42")
            .forEach { assertFalse(it, isSupportedAppLink(it)) }
    }
}
