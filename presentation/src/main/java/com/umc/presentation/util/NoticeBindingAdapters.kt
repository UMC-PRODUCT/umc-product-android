package com.umc.presentation.util

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.NoticeTarget
import com.umc.presentation.component.UButton

@BindingAdapter("noticeMarkdownContent")
fun setNoticeMarkdownContent(textView: TextView, content: String?) {
    textView.text = if (content.isNullOrEmpty()) {
        ""
    } else {
        MarkdownParser.parse(content, textView.context)
    }
}

@BindingAdapter("noticeCentralVisibility")
fun setNoticeCentralVisibility(button: UButton, target: NoticeTarget?) {
    if (target == null) {
        button.visibility = View.GONE
        return
    }
    val visible = target.targetGisuId != 0 ||
            (target.targetChapterId == null && target.targetSchoolId == null && target.targetParts.isEmpty())
    button.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("noticeChapterText")
fun setNoticeChapterText(button: UButton, target: NoticeTarget?) {
    val chapterName = target?.targetChapterName
    button.setText(chapterName ?: "지부")
}

@BindingAdapter("noticePartText")
fun setNoticePartText(button: UButton, target: NoticeTarget?) {
    val part = target?.targetParts?.firstOrNull()
    val label = part?.let { UserPart.from(it).label } ?: "파트"
    button.setText(label)
}
