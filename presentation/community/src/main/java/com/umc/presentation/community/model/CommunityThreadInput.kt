package com.umc.presentation.community.model

import android.icu.text.BreakIterator
import java.util.Locale

fun communityCharacterCount(value: String): Int {
    val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
    iterator.setText(value.trim())
    var count = 0
    iterator.first()
    while (iterator.next() != BreakIterator.DONE) count++
    return count
}

fun isCommunityThreadInputValid(title: String, description: String): Boolean =
    title.isNotBlank() && communityCharacterCount(title) <= 80 && communityCharacterCount(description) <= 500
