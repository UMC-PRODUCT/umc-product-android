#!/usr/bin/env python3
"""커스텀 Lint(UMC) 위반만 담은 모듈별 lint-baseline.xml 을 다시 만든다.

언제 쓰나: :lint-rules 에 규칙을 새로 추가해서, 이미 있던 코드의 위반을 baseline 으로 묶어야 할 때.
새로 작성한 코드의 위반을 숨기려고 돌리지 않는다. 그런 위반은 코드를 고친다.

하는 일
1. 모든 Android 모듈에서 updateLintBaseline 을 돌린다. app 은 checkDependencies 로 의존 모듈까지 포함한다.
2. 결과에서 :lint-rules 에 정의된 규칙 항목만 남긴다. 내장 규칙 경고는 지금처럼 리포트에 그대로 나온다.
3. 남은 항목이 없는 모듈의 baseline 파일은 지운다. 파일이 없으면 빈 baseline 으로 취급된다.

실행: 저장소 루트에서 python3 lint-rules/update_baseline.py
"""
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


def main() -> int:
    ids = umc_issue_ids()
    if not ids:
        print("lint-rules 에서 규칙 ID 를 찾지 못했습니다.", file=sys.stderr)
        return 1
    print("UMC 규칙:", ", ".join(sorted(ids)))

    # 지난 결과가 남아 있으면 updateLintBaseline 이 그 위에 덧쓰지 않도록 먼저 지운다.
    for path in baseline_files():
        path.unlink()

    subprocess.run(
        ["./gradlew", "updateLintBaseline", "-PlintCheckDependencies=true", "--console=plain"],
        cwd=ROOT,
        check=True,
    )

    # app 은 내장 규칙 경고가 항상 있어 baseline 이 반드시 생긴다. 없으면 이전 lint 모델이 재사용된 것이다.
    if not (ROOT / "app" / BASELINE_NAME).exists():
        print("app/lint-baseline.xml 이 생성되지 않았습니다. ./gradlew clean 후 다시 실행하세요.", file=sys.stderr)
        return 1

    for path in baseline_files():
        tree = ET.parse(path)
        root = tree.getroot()
        for issue in list(root.findall("issue")):
            if issue.get("id") not in ids:
                root.remove(issue)
        kept = root.findall("issue")
        relative = path.relative_to(ROOT)
        if not kept:
            path.unlink()
            print(f"{relative}: UMC 위반 없음 → 삭제")
            continue
        ET.indent(tree, space="    ")
        tree.write(path, encoding="UTF-8", xml_declaration=True)
        path.write_text(path.read_text(encoding="utf-8") + "\n", encoding="utf-8")
        print(f"{relative}: {len(kept)}건")
    return 0


if __name__ == "__main__":
    sys.exit(main())
