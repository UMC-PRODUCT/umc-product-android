package com.umc.presentation.community.component.chatting

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

internal fun communityMentionText(
    text: String,
    ranges: List<IntRange>,
    color: Color,
): AnnotatedString = buildAnnotatedString {
    append(text)
    ranges.forEach { range ->
        if (range.first >= 0 && range.last < text.length && !range.isEmpty()) {
            addStyle(SpanStyle(color = color), range.first, range.last + 1)
        }
    }
}

internal fun communityMentionNameRanges(text: String, names: List<String>): List<IntRange> {
    val alternatives = names.filter(String::isNotBlank).distinct().sortedByDescending(String::length)
        .joinToString("|") { Regex.escape(it) }
    if (alternatives.isEmpty()) return emptyList()
    // Server mentions provide identities/names, but no text offsets.
    return Regex("(?<!\\S)@(?:$alternatives)(?![\\p{L}\\p{N}_])")
        .findAll(text).map { it.range }.toList()
}
