#!/usr/bin/env python3
"""규칙으로 만든 Play "업데이트 내용" 초안을 사람이 읽는 글로 다듬는다.

release_notes.py 는 커밋 제목을 그대로 옮겨 적는다. 그래서 초안은
"[Feat] 공지 파트 필터를 보고 있는 기수에 맞춰 분기" 처럼 개발자 말투로 남는다.
이 스크립트는 그 초안과 커밋 목록을 Gemini 에게 주고 사용자 말투로 다시 쓰게 한다.

Gemini 무료 등급을 쓴다(flash 계열은 무료). 배포 한 번에 한 번만 부르므로
분당·일일 한도에 걸릴 일이 없다. 의존성을 만들지 않으려고 표준 라이브러리로만 호출한다.

**이 단계는 실패해도 배포를 막지 않는다.** 키가 없거나 호출이 실패하거나
결과가 검사를 통과하지 못하면 초안을 그대로 둔다. 릴리스 노트 문장 때문에
스토어 업로드가 멈추는 게 더 나쁘다.

사용:
    git log --format='%s' <이전태그>..HEAD | python3 polish_release_notes.py --version 3.4.0
"""

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

# Play 스토어 "업데이트 내용" 글자 수 상한. release_notes.py 와 같은 값이다.
PLAY_LIMIT = 500

ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/interactions"

PROMPT = """너는 대학생 IT 동아리 UMC 의 안드로이드 앱 릴리스 노트를 쓴다.
아래 커밋 기록을 보고 **두 가지 글**을 만든다.

[1] Play 스토어 "업데이트 내용" — 읽는 사람은 앱을 쓰는 동아리원이지 개발자가 아니다.
- 첫 줄은 이번 버전의 핵심을 한 문장으로 요약한다. 무엇이 달라졌는지만 담백하게 쓴다.
  비유·감성 표현·농담은 쓰지 마라. 항목을 그대로 이어 붙이지 말고 전체를 아우르는 한 문장으로 쓴다.
- 둘째 줄은 빈 줄로 둔다.
- 그 다음부터 "· " 로 시작하는 줄을 3~5개 쓴다. 사용자가 앱에서 체감하는 변화만 담는다.
- 커밋 제목을 그대로 옮기지 마라. 무엇이 좋아졌는지로 바꿔 써라.
- "내가"·"저희" 같은 1인칭은 쓰지 마라. 바뀐 기능이 주어가 되게 써라.
- 전체 길이는 공백 포함 {limit}자를 넘기면 안 된다.

[2] GitHub 릴리스 본문 — 읽는 사람은 팀의 개발자다.
- 첫 줄은 "🚀 {version}".
- 빈 줄 뒤에 이번 버전을 두세 문장으로 요약한다. 여기도 비유나 감성 표현 없이 사실만 쓴다.
- 그 아래에 "✨ 새로운 기능" 과 "🐛 개선 및 수정" 섹션을 둔다. 해당 항목이 없는 섹션은 통째로 뺀다.
- 각 항목은 "* " 로 시작한다. 새로운 기능은 "이름 — 설명" 꼴로 쓴다.
- 내부 구조 개선이나 빌드 작업도 개발자에게 의미가 있으면 "개선 및 수정" 에 넣되,
  커밋 제목을 복사하지 말고 무엇이 어떻게 달라졌는지 쓴다.

두 글 모두 한국어 존댓말이고 어미는 "~했습니다" 로 맺는다. 이모지는 지정한 자리에만 쓴다.

다음은 {version} 버전에 들어간 커밋 기록이다.

{commits}

그리고 이건 규칙으로 기계 변환한 Play 노트 초안이다. 사실 확인용으로만 참고해라.

{draft}

아래 형식 그대로, 설명 없이 출력해라.

===PLAY===
(Play 업데이트 내용)
===GITHUB===
(GitHub 릴리스 본문)"""


# 모델이 붐비면 503 이 온다. 실제로 겪은 적이 있어 몇 번 더 두드린다.
RETRY_CODES = {429, 500, 502, 503, 504}
RETRIES = 3
RETRY_WAIT = 5


def call_gemini(api_key: str, model: str, prompt: str, timeout: int) -> str:
    """Gemini 에 물어보고 본문 텍스트를 돌려준다. 일시적 실패는 몇 번 다시 시도한다."""
    body = json.dumps({"model": model, "input": prompt}).encode("utf-8")
    request = urllib.request.Request(
        ENDPOINT,
        data=body,
        headers={
            "Content-Type": "application/json",
            "x-goog-api-key": api_key,
        },
        method="POST",
    )

    for attempt in range(1, RETRIES + 1):
        try:
            with urllib.request.urlopen(request, timeout=timeout) as response:
                payload = json.load(response)
            break
        except urllib.error.HTTPError as exc:
            if exc.code not in RETRY_CODES or attempt == RETRIES:
                raise
            print(f"{exc.code} 응답 — {RETRY_WAIT}초 뒤 다시 시도합니다 ({attempt}/{RETRIES - 1})")
            time.sleep(RETRY_WAIT)

    # 응답은 steps 안에 모델 출력이 담겨 온다. 형태가 바뀌어도 터지지 않게 느슨하게 훑는다.
    chunks = []
    for step in payload.get("steps", []):
        for part in step.get("content", []):
            if part.get("type") == "text" and part.get("text"):
                chunks.append(part["text"])
    return "".join(chunks).strip()


def split_sections(text: str) -> tuple[str, str]:
    """모델 출력에서 Play 노트와 GitHub 본문을 떼어낸다."""
    if "===PLAY===" not in text or "===GITHUB===" not in text:
        return "", ""
    play, github = text.split("===PLAY===", 1)[1].split("===GITHUB===", 1)
    return play.strip(), github.strip()


def play_looks_sane(text: str) -> bool:
    """Play 노트를 그대로 스토어에 올려도 되는지 본다."""
    if not text or len(text) > PLAY_LIMIT:
        return False
    lines = [ln for ln in text.splitlines() if ln.strip()]
    # 인트로 한 줄 + 항목들. 인트로가 "· " 로 시작하면 형식을 어긴 것이다.
    if len(lines) < 2 or lines[0].startswith("· "):
        return False
    return all(ln.startswith("· ") for ln in lines[1:])


def github_looks_sane(text: str, version: str) -> bool:
    """GitHub 본문이 형식을 지켰는지 본다."""
    lines = [ln for ln in text.splitlines() if ln.strip()]
    if len(lines) < 2:
        return False
    return lines[0].startswith("🚀") and version in lines[0]


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True)
    ap.add_argument("--whatsnew-dir", default="whatsnew")
    ap.add_argument("--locale", default="ko-KR")
    ap.add_argument("--github-out", default="release-notes.md")
    # flash 계열은 무료 등급이다. 바꾸려면 여기만 고치면 된다.
    ap.add_argument("--model", default="gemini-3.8-flash")
    # 주 모델이 붐벼 503 이 이어질 때 쓴다. 더 가볍고 덜 밀린다.
    ap.add_argument("--fallback-model", default="gemini-3.5-flash-lite")
    ap.add_argument("--timeout", type=int, default=60)
    args = ap.parse_args()

    target = Path(args.whatsnew_dir) / f"whatsnew-{args.locale}"
    if not target.exists():
        print("초안이 없어 건너뜁니다 — release_notes.py 가 먼저 돌아야 합니다.")
        return 0

    draft = target.read_text(encoding="utf-8").strip()
    commits = sys.stdin.read().strip()

    api_key = os.environ.get("GEMINI_API_KEY", "").strip()
    if not api_key:
        print("GEMINI_API_KEY 가 없어 초안을 그대로 씁니다.")
        return 0

    if not commits:
        print("커밋 목록이 비어 초안을 그대로 씁니다.")
        return 0

    prompt = PROMPT.format(
        limit=PLAY_LIMIT,
        version=args.version,
        commits=commits,
        draft=draft,
    )

    polished = ""
    for model in (args.model, args.fallback_model):
        if not model:
            continue
        try:
            polished = call_gemini(api_key, model, prompt, args.timeout)
            break
        except urllib.error.HTTPError as exc:
            # 본문에 원인이 적혀 있다(모델명 오타, 한도 초과 등). 키는 헤더라 찍히지 않는다.
            detail = exc.read().decode("utf-8", "replace")[:300]
            print(f"{model} 실패 {exc.code}: {detail}")
        except Exception as exc:  # 네트워크·응답 형식 등 무엇이든 다음 모델로 넘어간다
            print(f"{model} 실패: {exc}")

    if not polished:
        print("다듬기에 실패해 초안을 그대로 씁니다.")
        return 0

    play, github = split_sections(polished)

    if play_looks_sane(play):
        target.write_text(play + "\n", encoding="utf-8")
        print(f"Play 업데이트 내용 ({len(play)}/{PLAY_LIMIT}자, {args.model})")
        print("─" * 50)
        print(play)
        print("─" * 50)
    else:
        print(f"Play 노트가 형식 검사를 통과하지 못해 초안을 그대로 씁니다 ({len(play)}자)")

    # GitHub 본문은 따로 본다. 한쪽이 어긋나도 다른 쪽은 살린다.
    github_out = Path(args.github_out)
    if github_looks_sane(github, args.version):
        github_out.write_text(github + "\n", encoding="utf-8")
        print(f"GitHub 릴리스 본문 ({len(github)}자) -> {github_out}")
    else:
        print("GitHub 본문이 형식 검사를 통과하지 못해 초안을 그대로 씁니다")

    return 0


if __name__ == "__main__":
    sys.exit(main())
