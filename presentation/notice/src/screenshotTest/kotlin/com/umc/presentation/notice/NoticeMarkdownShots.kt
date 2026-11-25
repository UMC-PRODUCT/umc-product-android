package com.umc.presentation.notice

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.umc.component.theme.UmcTheme

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
fun NoticeMarkdownWriteShot() {
    UmcTheme { NoticeMarkdownWritePreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400, heightDp = 1000)
@Composable
fun NoticeMarkdownDetailShot() {
    UmcTheme { NoticeMarkdownDetailPreview() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun NoticeMarkdownCardShot() {
    UmcTheme { NoticeMarkdownCardPreview() }
}
