package com.umc.presentation.community.chatting

data class CommunityDraftMention(
    val memberId: String,
    val label: String,
    val start: Int,
    val end: Int,
)

data class CommunityMentionQuery(val start: Int, val end: Int, val text: String)

internal fun communityMentionQuery(text: String, cursor: Int): CommunityMentionQuery? {
    if (cursor !in 1..text.length) return null
    val start = text.lastIndexOf('@', cursor - 1)
    if (start < 0 || (start > 0 && !text[start - 1].isWhitespace())) return null
    val query = text.substring(start + 1, cursor)
    if (query.any { it.isWhitespace() || it == '@' }) return null
    return CommunityMentionQuery(start, cursor, query)
}

// Preserve identities only for untouched mention spans, including duplicate display names.
internal fun updatedCommunityMentions(
    before: String,
    after: String,
    mentions: List<CommunityDraftMention>,
    changeStart: Int? = null,
    changeEnd: Int? = null,
): List<CommunityDraftMention> {
    if (before == after) return mentions
    val prefix = changeStart ?: before.commonPrefixWith(after).length
    val suffix = if (changeEnd != null) before.length - changeEnd
        else before.substring(prefix).commonSuffixWith(after.substring(prefix)).length
    if (prefix !in 0..minOf(before.length, after.length) || suffix < 0 ||
        prefix + suffix > minOf(before.length, after.length) ||
        before.take(prefix) != after.take(prefix) || before.takeLast(suffix) != after.takeLast(suffix)
    ) return emptyList()
    val oldEnd = before.length - suffix
    val delta = after.length - before.length
    return mentions.mapNotNull { mention ->
        when {
            mention.end <= prefix -> mention
            mention.start >= oldEnd -> mention.copy(start = mention.start + delta, end = mention.end + delta)
            else -> null
        }
    }.filter { mention ->
        after.substring(mention.start, mention.end) == "@${mention.label}" &&
            (mention.start == 0 || after[mention.start - 1].isWhitespace()) &&
            (mention.end == after.length || after[mention.end].isWhitespace())
    }
}

internal fun CommunityChattingState.withMentionDraft(
    value: String,
    changeStart: Int? = null,
    changeEnd: Int? = null,
): CommunityChattingState {
    val retained = updatedCommunityMentions(draft, value, draftMentions, changeStart, changeEnd)
    return copy(
        draft = value,
        draftMentions = retained,
        mentionedMembers = retained.associate { it.memberId to it.label },
    )
}
