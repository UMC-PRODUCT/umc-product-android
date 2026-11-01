package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onLongClick as semanticLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import java.net.URI

internal data class CommunityTextLink(val start: Int, val end: Int, val url: String)

private val messageUrlPattern = Regex(
    "(?<![\\p{L}\\p{N}_@])(?:https?://|umc://|www\\.)[^\\s<>\\\"'‘’“”]+",
    RegexOption.IGNORE_CASE,
)

internal fun communityTextLinks(text: String): List<CommunityTextLink> =
    messageUrlPattern.findAll(text).mapNotNull { match ->
        var value = match.value.trimEnd('.', ',', '!', '?', ';', ':', '。', '，', '！', '？')
        // 문장 끝 괄호는 제외하되 URL 안의 짝이 맞는 괄호는 보존한다.
        while (value.isNotEmpty()) {
            val closing = value.last()
            val opening = when (closing) { ')' -> '('; ']' -> '['; '}' -> '{'; else -> null }
            if (opening == null || value.count { it == closing } <= value.count { it == opening }) break
            value = value.dropLast(1).trimEnd('.', ',', '!', '?', ';', ':')
        }
        val url = if (value.startsWith("www.", ignoreCase = true)) "https://$value" else value
        val uri = runCatching { URI(url) }.getOrNull() ?: return@mapNotNull null
        if (uri.scheme.lowercase() !in setOf("http", "https", "umc") || uri.rawAuthority.isNullOrBlank()) return@mapNotNull null
        CommunityTextLink(match.range.first, match.range.first + value.length, url)
    }.toList()

/** 링크 탭과 답장 원문 이동을 구분하고 길게 누르기는 기존 메시지 메뉴로 전달한다. */
@Composable
internal fun CommunityMessageText(
    text: AnnotatedString,
    color: Color,
    linkColor: Color,
    style: TextStyle,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val links = remember(text.text) { communityTextLinks(text.text) }
    val annotated = remember(text, links, linkColor) {
        AnnotatedString.Builder(text).apply {
            links.forEach { addStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline), it.start, it.end) }
        }.toAnnotatedString()
    }
    val uriHandler = LocalUriHandler.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val currentClick by rememberUpdatedState(onClick)
    val currentLongClick by rememberUpdatedState(onLongClick)
    val openLink by rememberUpdatedState<(String) -> Boolean>({ url ->
        runCatching { uriHandler.openUri(url) }.isSuccess
    })
    Text(
        text = annotated,
        color = color,
        style = style,
        onTextLayout = { layout = it },
        modifier = Modifier
            .semantics {
                customActions = links.map { link -> CustomAccessibilityAction(link.url) { openLink(link.url) } }
                if (onLongClick != null) semanticLongClick { currentLongClick?.invoke(); true }
            }
            .pointerInput(annotated) {
                detectTapGestures(
                    onLongPress = if (onLongClick != null) { _ -> currentLongClick?.invoke() } else null,
                    onTap = { position ->
                        val result = layout
                        val offset = result?.getOffsetForPosition(position)
                        val link = links.firstOrNull { offset != null && offset >= it.start && offset < it.end }
                        val hitsText = offset != null && offset < annotated.length && result.getBoundingBox(offset).contains(position)
                        if (link != null && hitsText) openLink(link.url) else currentClick()
                    },
                )
            },
    )
}
