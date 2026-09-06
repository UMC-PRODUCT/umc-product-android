#!/usr/bin/env python3
"""커밋 목록에서 Play 스토어 업데이트 내용과 GitHub Release 본문을 만든다.

두 산출물의 성격이 다르다.
  - Play "업데이트 내용": 사용자가 읽는 글이다. 빌드/CI 작업은 넣지 않는다. 500자 제한.
  - GitHub Release: 개발자가 읽는 글이다. 전부 분류해서 남긴다.

접두사만으로는 사용자에게 보일 항목인지 알 수 없다.
"[Fix] 워크플로 표현식 수정" 같은 커밋은 Fix 지만 사용자와 무관하다.
그래서 변경된 파일 경로를 함께 보고, 앱 코드를 건드리지 않은 커밋은 내부 작업으로 본다.

사용:
    git log --format=$'\x01%s' --name-only <이전태그>..HEAD | python3 release_notes.py ...
"""

import argparse
import re
import sys
from pathlib import Path

PLAY_LIMIT = 500

# 커밋 제목 접두사 -> (표시 이름, 사용자에게 보여줄지)
# 이 레포는 [Feat] [Fix-266] [Chore/#159] [docs-#270] 등 표기가 제각각이라 느슨하게 받는다.
TYPES = {
    "feat": ("새로운 기능", True),
    "feature": ("새로운 기능", True),
    "fix": ("버그 수정", True),
    "bugfix": ("버그 수정", True),
    "hotfix": ("버그 수정", True),
    "ui": ("화면 개선", True),
    "perf": ("성능 개선", True),
    "refactor": ("코드 구조 개선", False),
    "refator": ("코드 구조 개선", False),  # 레포에 실재하는 오타
    "chore": ("기타 작업", False),
    "docs": ("문서", False),
    "doc": ("문서", False),
    "test": ("테스트", False),
    "style": ("스타일", False),
}

# 이 경로만 바뀐 커밋은 사용자에게 보이지 않는 내부 작업이다.
INTERNAL_PATHS = (
    ".github/", "gradle/", "lint-rules/", "benchmark/", ".idea/",
)
INTERNAL_FILES = re.compile(
    r"(^|/)(.*\.gradle\.kts|gradle\.properties|.*\.pro|.*\.md|"
    r"\.gitignore|\.coderabbit\.ya?ml|libs\.versions\.toml|gradlew(\.bat)?)$"
)


def is_internal(paths):
    """앱 코드를 하나도 건드리지 않았으면 내부 작업."""
    if not paths:
        return True
    for p in paths:
        if p.startswith(INTERNAL_PATHS) or INTERNAL_FILES.search(p):
            continue
        return False
    return True


SKIP = re.compile(r"^(Merge (pull request|branch|remote)|Revert )", re.I)
# [Feat] / [Fix-266] / [Chore/#159] / [docs-#270] 을 모두 받아낸다.
PREFIX = re.compile(r"^\[\s*([A-Za-z]+)\s*[-/]?\s*#?\d*\s*\]\s*[-:]?\s*(.+)$")


def read_records(stream):
    """\x01 로 시작하는 제목과 그 뒤에 이어지는 변경 경로들을 묶어 읽는다."""
    subject, paths, records = None, [], []
    for raw in stream:
        line = raw.rstrip("\n")
        if line.startswith("\x01"):
            if subject is not None:
                records.append((subject, paths))
            subject, paths = line[1:].strip(), []
        elif line.strip() and subject is not None:
            paths.append(line.strip())
    if subject is not None:
        records.append((subject, paths))
    return records


def parse(records):
    """커밋을 (표시이름, 사용자노출여부, 본문) 으로 분류한다."""
    out = []
    for s, paths in records:
        if not s or SKIP.match(s):
            continue
        m = PREFIX.match(s)
        if m:
            key = m.group(1).lower()
            subject = m.group(2).strip()
            label, user_facing = TYPES.get(key, ("기타", False))
        else:
            subject = s
            label, user_facing = ("기타", False)
        # 접두사가 Feat/Fix 라도 앱 코드를 건드리지 않았으면 사용자에게 보이지 않는다.
        if is_internal(paths):
            user_facing = False
        subject = re.sub(r"\s*\(#\d+\)$", "", subject).strip(" -·")
        if subject:
            out.append((label, user_facing, subject))
    return out


def dedupe(items):
    seen, out = set(), []
    for it in items:
        if it[2] not in seen:
            seen.add(it[2])
            out.append(it)
    return out


def group(items, only_user_facing):
    """표시 이름별로 묶는다. 순서는 TYPES 등장 순서를 따른다."""
    priority = []
    for name, _ in TYPES.values():
        if name not in priority:
            priority.append(name)
    priority.append("기타")

    buckets = {}
    for label, user_facing, subject in items:
        if only_user_facing and not user_facing:
            continue
        buckets.setdefault(label, []).append(subject)
    return [(label, buckets[label]) for label in priority if label in buckets]


def build_play_notes(items, max_items=8):
    """Play 업데이트 내용. 사용자에게 의미 있는 항목만, 500자 안에서.

    커밋 제목은 개발자용 축약어인 경우가 많다. 전부 나열하면 릴리스 노트가 아니라
    변경 로그 덤프가 되므로 상위 몇 건만 남긴다.
    """
    groups = group(items, only_user_facing=True)
    if not groups:
        # 이번 릴리스가 전부 내부 작업일 수 있다. 그때 CI 얘기를 쓰면 안 된다.
        return "안정성 개선 및 내부 구조 정리"

    # 그룹 순서를 유지하며 전체 항목 수를 제한한다.
    remaining, capped, dropped = max_items, [], 0
    for label, subjects in groups:
        if remaining <= 0:
            dropped += len(subjects)
            continue
        take = subjects[:remaining]
        dropped += len(subjects) - len(take)
        remaining -= len(take)
        capped.append((label, take))
    groups = capped

    lines, truncated = [], dropped > 0
    for label, subjects in groups:
        block = [label]
        for s in subjects:
            block.append(f"· {s}")
        candidate = lines + ([""] if lines else []) + block
        if len("\n".join(candidate)) > PLAY_LIMIT:
            # 그룹 단위로 넘치면 항목을 하나씩 넣어보며 최대한 채운다.
            partial = lines + ([""] if lines else []) + [label]
            for s in subjects:
                trial = partial + [f"· {s}"]
                if len("\n".join(trial)) > PLAY_LIMIT - 20:
                    truncated = True
                    break
                partial = trial
            if len(partial) > (len(lines) + (1 if lines else 0) + 1):
                lines = partial
            truncated = True
            break
        lines = candidate

    if truncated:
        tail = lines + ["· 그 외 개선 사항"]
        if len("\n".join(tail)) <= PLAY_LIMIT:
            lines = tail

    text = "\n".join(lines)
    return text[:PLAY_LIMIT].rstrip()


def build_github_notes(items, version, prev, repo):
    groups = group(items, only_user_facing=False)
    out = [f"## {version}", ""]
    if not groups:
        out.append("커밋 내역이 없습니다.")
    for label, subjects in groups:
        out.append(f"### {label}")
        out.extend(f"- {s}" for s in subjects)
        out.append("")
    if repo and prev:
        out.append(f"**전체 변경 내역**: https://github.com/{repo}/compare/{prev}...v{version}")
    return "\n".join(out).rstrip() + "\n"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True, help="예: 3.2.0")
    ap.add_argument("--prev", default="", help="이전 릴리스 태그. 예: v3.1.0")
    ap.add_argument("--repo", default="", help="owner/name")
    ap.add_argument("--whatsnew-dir", default="whatsnew")
    ap.add_argument("--locale", default="ko-KR")
    ap.add_argument("--github-out", default="release-notes.md")
    ap.add_argument("--max-items", type=int, default=8, help="Play 노트에 담을 최대 항목 수")
    args = ap.parse_args()

    items = dedupe(parse(read_records(sys.stdin)))

    play = build_play_notes(items, args.max_items)
    d = Path(args.whatsnew_dir)
    d.mkdir(parents=True, exist_ok=True)
    (d / f"whatsnew-{args.locale}").write_text(play + "\n", encoding="utf-8")

    Path(args.github_out).write_text(
        build_github_notes(items, args.version, args.prev, args.repo), encoding="utf-8"
    )

    print(f"커밋 {len(items)}건 반영")
    print(f"Play 업데이트 내용 ({len(play)}/{PLAY_LIMIT}자) -> {d}/whatsnew-{args.locale}")
    print("─" * 50)
    print(play)
    print("─" * 50)


if __name__ == "__main__":
    main()
