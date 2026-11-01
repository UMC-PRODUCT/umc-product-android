package com.umc.presentation.community.chatting

import com.umc.presentation.community.component.chatting.communityTextLinks
import org.junit.Assert.*
import org.junit.Test

class CommunityMessageLinksTest {
    @Test fun detectsMultipleLinksWithoutIncludingSurroundingText() {
        val text = "링크 https://example.com/a?q=1&b=2#part 그리고 http://other.test/x"
        val links = communityTextLinks(text)
        assertEquals(listOf("https://example.com/a?q=1&b=2#part", "http://other.test/x"), links.map { it.url })
        links.forEach { assertEquals(it.url, text.substring(it.start, it.end)) }
    }

    @Test fun supportsWwwAndThreadLinks() {
        assertEquals(listOf("https://www.example.com", "umc://thread?threadId=42", "umc://thread/43"),
            communityTextLinks("www.example.com umc://thread?threadId=42 umc://thread/43").map { it.url })
    }

    @Test fun trimsSentencePunctuationButPreservesBalancedUrlParentheses() {
        val text = "(https://example.com/wiki/Some_(topic)). https://other.test/path!"
        val links = communityTextLinks(text)
        assertEquals(listOf("https://example.com/wiki/Some_(topic)", "https://other.test/path"), links.map { it.url })
        links.forEach { assertEquals(it.url, text.substring(it.start, it.end)) }
    }

    @Test fun ignoresUnsupportedSchemesAndMalformedLinks() {
        assertTrue(communityTextLinks("javascript:alert(1) intent://example file:///etc/passwd https://").isEmpty())
        assertTrue(communityTextLinks("@www.example.com 일반 메시지").isEmpty())
    }
}
