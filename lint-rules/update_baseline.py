#!/usr/bin/env python3
"""지정한 커스텀 Lint(UMC) 규칙의 기존 위반을 모듈별 lint-baseline.xml 에 기록한다.

언제 쓰나: :lint-rules 에 규칙을 새로 추가해서, 이미 있던 코드의 위반을 baseline 으로 묶어야 할 때.
새로 작성한 코드의 위반을 숨기려고 돌리지 않는다. 그런 위반은 코드를 고친다.

    python3 lint-rules/update_baseline.py --issue-id NewRuleId [--issue-id OtherRuleId ...]

하는 일
1. 모든 Android 모듈에서 updateLintBaseline 을 돌린다. app 은 checkDependencies 로 의존 모듈까지 포함한다.
2. --issue-id 로 지정한 규칙의 항목만 새 결과로 바꾸고, 다른 규칙의 항목은 기존 baseline 그대로 둔다.
   그래서 기존 규칙에 새로 생긴 위반이 이 기회에 baseline 으로 섞여 들어가지 않는다.
3. 내장 규칙 항목은 남기지 않는다. 남은 항목이 없는 모듈의 baseline 파일은 지운다(없으면 빈 baseline 으로 취급된다).

Gradle 이 실패하면 baseline 파일을 실행 전 상태로 되돌린다. Ctrl-C 로 중단한 경우에도 되돌리지만,
Gradle 데몬에서 이미 돌던 태스크가 그 뒤에 파일을 다시 쓸 수 있으므로 빌드가 멈춘 뒤 git status 로 확인한다.
"""
from __future__ import annotations

import argparse
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RULE_SOURCES = ROOT / "lint-rules" / "src" / "main"
BASELINE_NAME = "lint-baseline.xml"


def umc_issue_ids() -> set[str]:
    ids: set[str] = set()
    for source in RULE_SOURCES.rglob("*.kt"):
        ids.update(re.findall(r'\bid\s*=\s*"([A-Za-z0-9_]+)"', source.read_text(encoding="utf-8")))
    return ids


def baseline_files() -> list[Path]:
    return sorted(p for p in ROOT.glob(f"**/{BASELINE_NAME}") if "build" not in p.parts)


def parse_args(known_ids: set[str]) -> set[str]:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument(
        "--issue-id",
        action="append",
        required=True,
        metavar="ID",
        help="baseline 에 기존 위반을 기록할 규칙 ID. 여러 번 줄 수 있다. 사용 가능: " + ", ".join(sorted(known_ids)),
    )
    targets = set(parser.parse_args().issue_id)
    unknown = targets - known_ids
    if unknown:
        parser.error(f":lint-rules 에 없는 규칙 ID: {', '.join(sorted(unknown))}")
    return targets


def merge(original: str | None, generated: Path, targets: set[str], known_ids: set[str]) -> ET.ElementTree | None:
    """다른 UMC 규칙의 항목은 original 에서, 대상 규칙의 항목은 generated 에서 가져온다."""
    tree = ET.parse(generated)
    root = tree.getroot()
    fresh = [issue for issue in root.findall("issue") if issue.get("id") in targets]
    kept = []
    if original is not None:
        kept = [
            issue
            for issue in ET.fromstring(original).findall("issue")
            if issue.get("id") in known_ids and issue.get("id") not in targets
        ]
    for issue in list(root.findall("issue")):
        root.remove(issue)
    root.extend(kept + fresh)
    if not kept and not fresh:
        return None
    ET.indent(tree, space="    ")
    return tree


def main() -> int:
    known_ids = umc_issue_ids()
    if not known_ids:
        print("lint-rules 에서 규칙 ID 를 찾지 못했습니다.", file=sys.stderr)
        return 1
    targets = parse_args(known_ids)
    print("기록할 규칙:", ", ".join(sorted(targets)))

    # 실패하면 되돌릴 수 있도록 실행 전 상태를 메모리에 둔다. updateLintBaseline 이 이 파일들을 덮어쓴다.
    originals = {path: path.read_text(encoding="utf-8") for path in baseline_files()}
    try:
        subprocess.run(
            ["./gradlew", "updateLintBaseline", "-PlintCheckDependencies=true", "--console=plain"],
            cwd=ROOT,
            check=True,
        )
        # app 은 내장 규칙 경고가 항상 있어 baseline 이 반드시 생긴다. 없으면 이전 lint 모델이 재사용된 것이다.
        if not (ROOT / "app" / BASELINE_NAME).exists():
            raise RuntimeError("app/lint-baseline.xml 이 생성되지 않았습니다. ./gradlew clean 후 다시 실행하세요.")

        results = {path: merge(originals.get(path), path, targets, known_ids) for path in baseline_files()}
    except BaseException as error:
        for path in baseline_files():
            if path not in originals:
                path.unlink()
        for path, text in originals.items():
            path.write_text(text, encoding="utf-8")
        print("실패해서 baseline 을 실행 전 상태로 되돌렸습니다.", file=sys.stderr)
        if isinstance(error, KeyboardInterrupt):
            print(
                "중단된 Gradle 태스크가 데몬에서 끝까지 돌며 baseline 을 다시 쓸 수 있습니다. "
                "빌드가 멈춘 뒤 git status 로 lint-baseline.xml 을 확인하세요.",
                file=sys.stderr,
            )
        raise

    for path, tree in results.items():
        relative = path.relative_to(ROOT)
        if tree is None:
            path.unlink()
            print(f"{relative}: UMC 항목 없음 → 삭제")
            continue
        tree.write(path, encoding="UTF-8", xml_declaration=True)
        path.write_text(path.read_text(encoding="utf-8") + "\n", encoding="utf-8")
        counts = {}
        for issue in tree.getroot().findall("issue"):
            counts[issue.get("id")] = counts.get(issue.get("id"), 0) + 1
        print(f"{relative}: " + ", ".join(f"{k} {v}건" for k, v in sorted(counts.items())))
    return 0


if __name__ == "__main__":
    sys.exit(main())
