package com.umc.domain.model.mypage

data class CardSession(val ownerId: Long, val epoch: Long)
data class CardWriteContext(val session: CardSession, val revision: Long)
data class StoredCard(
    val card: UserCard,
    val receivedAt: Long,
    val updatedAt: Long,
    val note: String = "",
    val version: Long = 0,
    val pendingDelete: Boolean = false,
)
data class CardJournalEntry(
    val operationId: String,
    val peerId: String,
    val kind: String,
    val version: Long,
)
data class CardBook(
    val revision: Long = 0,
    val cards: Map<String, StoredCard> = emptyMap(),
    val journal: List<CardJournalEntry> = emptyList(),
) {
    val visibleCards: List<StoredCard>
        get() = cards.values.filterNot { it.pendingDelete }.sortedByDescending { it.receivedAt }

    // A server contract for mutual approvals and acknowledgement is not available yet.
    val statistics: CardStatistics
        get() = CardStatistics(visibleCards.size, null, journal.map { it.peerId }.distinct().size)

    fun save(card: UserCard, context: CardWriteContext, now: Long, operationId: String): CardBook {
        val id = requireNotNull(card.memberNo?.toLongOrNull()?.takeIf { it > 0 }) { "회원 ID를 확인할 수 없어요." }.toString()
        require(id != context.session.ownerId.toString()) { "내 명함은 보관할 수 없어요." }
        if (journal.any { it.operationId == operationId }) return this
        val previous = cards[id]
        // An older lookup/create result cannot undo a later delete or update.
        check(previous == null || previous.version <= context.revision) { "명함이 변경됐어요. 다시 확인해주세요." }
        val version = revision + 1
        val stored = StoredCard(card.copy(memberNo = id), previous?.receivedAt ?: now, now,
            previous?.note.orEmpty(), version)
        return copy(revision = version, cards = cards + (id to stored),
            journal = journal + CardJournalEntry(operationId, id, "SAVE", version))
    }

    fun delete(id: String, now: Long, operationId: String): CardBook {
        val previous = cards[id] ?: return this
        if (previous.pendingDelete) return this
        val version = revision + 1
        return copy(revision = version,
            cards = cards + (id to previous.copy(pendingDelete = true, updatedAt = now, version = version)),
            journal = journal + CardJournalEntry(operationId, id, "DELETE", version))
    }

    fun updateNote(id: String, note: String): CardBook {
        val previous = cards[id]?.takeUnless { it.pendingDelete } ?: error("삭제된 명함이에요.")
        return copy(cards = cards + (id to previous.copy(note = note)))
    }
}

data class CardStatistics(
    val localStoredCount: Int,
    val mutualExchangeCount: Int?,
    val pendingPeerCount: Int,
)

data class CardBookState(
    val session: CardSession? = null,
    val book: CardBook = CardBook(),
    val hasLegacyRecords: Boolean = false,
    val storageUnavailable: Boolean = false,
)

/** Only an explicit public projection may populate a received card's contact fields. */
fun UserCard.withoutUnverifiedContacts(): UserCard = copy(
    email = null, github = null, blog = null, linkedIn = null, qrPayload = null,
)

fun validCardWebLink(value: String?): String? = value?.trim()?.takeIf { raw ->
    runCatching {
        val uri = java.net.URI(raw)
        uri.scheme?.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank() && uri.userInfo == null
    }.getOrDefault(false)
}
