#!/usr/bin/env python3
"""변경된 파일에서 검사해야 할 Gradle 모듈과 태스크를 계산한다.

settings.gradle.kts 의 include 목록과 각 모듈 build.gradle.kts 의 project(":...")
의존을 읽어 그래프를 만들고, 그것을 뒤집어 역의존 폐포를 구한다.
모듈이 추가·삭제돼도 이 스크립트를 고칠 필요가 없도록 하드코딩하지 않는다.

사용:
    git diff --name-only base...head | python3 .github/scripts/affected_modules.py

출력(stdout):
    ALL                     전역 파일이 바뀌어 전 모듈을 검사해야 함
    <태스크> <태스크> ...   그 외에는 실행할 Gradle 태스크 목록
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# 이 경로들이 바뀌면 영향 범위를 좁힐 수 없다 — 전 모듈 검사.
GLOBAL_EXACT = {"settings.gradle.kts", "build.gradle.kts", "gradle.properties", "gradlew"}
GLOBAL_PREFIX = ("gradle/", ".github/", "lint-rules/")


def gradle_to_path(name: str) -> str:
    """":presentation:notice" -> "presentation/notice" """
    return name.lstrip(":").replace(":", "/")


def read_modules() -> list[str]:
    text = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
    return re.findall(r'include\("(:[^"]+)"\)', text)


def module_kind(name: str) -> str:
    """빌드 파일의 플러그인으로 모듈 종류를 판별한다."""
    f = ROOT / gradle_to_path(name) / "build.gradle.kts"
    if not f.exists():
        return "unknown"
    text = f.read_text(encoding="utf-8")
    if "android.application" in text:
        return "android"
    if "android.test" in text:
        return "android-test"  # macrobenchmark — 단위 테스트 대상 아님
    if "android.library" in text:
        return "android"
    if "kotlin.jvm" in text:
        return "jvm"
    return "unknown"


def forward_deps() -> dict[str, set[str]]:
    graph: dict[str, set[str]] = {}
    for m in read_modules():
        f = ROOT / gradle_to_path(m) / "build.gradle.kts"
        deps: set[str] = set()
        if f.exists():
            for d in re.findall(r'project\("(:[^"]+)"\)', f.read_text(encoding="utf-8")):
                # lintChecks 는 코드 의존이 아니다. 규칙 자체가 바뀌면 GLOBAL_PREFIX 가 잡는다.
                if d != ":lint-rules":
                    deps.add(d)
        graph[m] = deps
    return graph


def reverse_closure(seeds: set[str], fwd: dict[str, set[str]]) -> set[str]:
    rev: dict[str, set[str]] = {m: set() for m in fwd}
    for m, deps in fwd.items():
        for d in deps:
            rev.setdefault(d, set()).add(m)

    seen, stack = set(seeds), list(seeds)
    while stack:
        cur = stack.pop()
        for parent in rev.get(cur, ()):
            if parent not in seen:
                seen.add(parent)
                stack.append(parent)
    return seen


def module_of(path: str, modules: list[str]) -> str | None:
    """파일 경로를 가장 깊게 일치하는 모듈로 매핑한다."""
    best = None
    for m in modules:
        prefix = gradle_to_path(m) + "/"
        if path.startswith(prefix):
            if best is None or len(prefix) > len(gradle_to_path(best)) + 1:
                best = m
    return best


def tasks_for(modules: set[str]) -> list[str]:
    out: list[str] = []
    for m in sorted(modules):
        kind = module_kind(m)
        if kind == "android":
            out.append(f"{m}:testDebugUnitTest")
            out.append(f"{m}:lintDebug")
        elif kind == "jvm":
            out.append(f"{m}:test")
        # android-test / unknown 은 검사 대상 없음
    return out


def main() -> int:
    changed = [line.strip() for line in sys.stdin if line.strip()]
    if not changed:
        print("ALL")  # 판단 근거가 없으면 안전한 쪽으로
        return 0

    for path in changed:
        if path in GLOBAL_EXACT or path.startswith(GLOBAL_PREFIX):
            print("ALL")
            return 0

    modules = read_modules()
    seeds = {m for m in (module_of(p, modules) for p in changed) if m}
    if not seeds:
        print("ALL")  # 어느 모듈에도 속하지 않는 변경 — 좁히지 않는다
        return 0

    affected = reverse_closure(seeds, forward_deps())
    tasks = tasks_for(affected)
    print(" ".join(tasks) if tasks else "ALL")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
