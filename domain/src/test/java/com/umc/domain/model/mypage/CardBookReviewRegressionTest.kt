package com.umc.domain.model.mypage

import org.junit.Assert.*
import org.junit.Test

class CardBookReviewRegressionTest {
    private val session = CardSession(1, 1)
    private val card = UserCard(memberNo = "2", name = "Peer", avatarURL = "https://example.com/photo")

    @Test fun `delete clears personal data and fresh save does not restore note or receipt`() {
        val saved = CardBook().save(card, CardWriteContext(session, 0), 10, "save").updateNote("2", "private")
        val deleted = saved.delete("2", 20, "delete")
        assertEquals(UserCard(memberNo = "2"), deleted.cards.getValue("2").card)
        assertEquals("", deleted.cards.getValue("2").note)
        val restored = deleted.save(card, CardWriteContext(session, deleted.revision), 30, "new-save")
        assertEquals("", restored.cards.getValue("2").note)
        assertEquals(30L, restored.cards.getValue("2").receivedAt)
        assertTrue(runCatching { deleted.save(card, CardWriteContext(session, 0), 40, "stale") }.isFailure)
    }

    @Test fun `repeated local updates retain latest pending operation for each peer`() {
        var book = CardBook()
        repeat(100) { index ->
            book = book.save(card, CardWriteContext(session, book.revision), index.toLong(), "save-$index")
        }
        assertEquals(1, book.journal.size)
        assertEquals("save-99", book.journal.single().operationId)
        val retry = book.save(card, CardWriteContext(session, book.revision - 1), 200, "save-99")
        assertEquals(book, retry)
        val deleted = book.delete("2", 300, "delete")
        assertEquals("DELETE", deleted.journal.single().kind)
    }

    @Test fun `old tombstones are scrubbed without changing conflict version`() {
        val old = StoredCard(card, 10, 20, "private", 4, pendingDelete = true)
        val normalized = CardBook(4, mapOf("2" to old)).withoutDeletedContent()
        assertEquals(4L, normalized.cards.getValue("2").version)
        assertEquals(UserCard(memberNo = "2"), normalized.cards.getValue("2").card)
        assertEquals("", normalized.cards.getValue("2").note)
    }

    @Test fun `bare web links work while credentials and executable schemes are rejected`() {
        assertEquals("https://github.com/user", validCardWebLink(" github.com/user "))
        assertEquals("http://example.com", validCardWebLink("http://example.com"))
        assertNull(validCardWebLink("javascript:alert(1)"))
        assertNull(validCardWebLink("https://user:password@example.com"))
        assertNull(validCardWebLink(" "))
    }
}
