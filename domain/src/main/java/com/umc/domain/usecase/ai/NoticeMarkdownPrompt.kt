package com.umc.domain.usecase.ai

/**
 * 공지 본문 생성 프롬프트가 공유하는 형식 규칙.
 * MarkdownVisualTransformation이 렌더링하는 문법과 일치해야 한다
 */
internal const val NOTICE_MARKDOWN_FORMAT_RULES = """
형식 규칙 (아래 마크다운 문법만 사용):
- 큰 제목: "# 제목", 소제목: "## 소제목"
- 강조: **굵게**, 밑줄: <u>텍스트</u>, 취소선: ~~텍스트~~
- 목록: "- 항목", 인용: "> 문장"
- 마커와 내용 사이에 공백을 넣지 말 것 (** 텍스트 ** 금지, **텍스트** 형태만 허용)
- 위에 없는 다른 마크다운 문법은 사용 금지
"""
