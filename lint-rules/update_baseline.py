#!/usr/bin/env python3
"""지정한 커스텀 Lint(UMC) 규칙의 기존 위반을 모듈별 lint-baseline.xml 에 기록한다.

언제 쓰나: :lint-rules 에 규칙을 새로 추가해서, 이미 있던 코드의 위반을 baseline 으로 묶어야 할 때.
새로 작성한 코드의 위반을 숨기려고 돌리지 않는다. 그런 위반은 코드를 고친다.

    python3 lint-rules/update_baseline.py --issue-id NewRuleId [--issue-id OtherRuleId ...]

하는 일
1. 모든 Android 모듈에서 updateLintBaseline 을 돌린다. app 은 checkDependencies 로 의존 모듈까지 포함한다.
   결과는 저장소 파일이 아니라 실행마다 새로 만드는 시스템 임시 디렉터리에 쓴다(-PumcLintBaselineDir).
   중단된 Gradle 태스크가 데몬에서 끝까지 돌아도 저장소의 baseline 이나 다음 실행의 결과는 건드리지 못한다.
2. --issue-id 로 지정한 규칙의 항목만 새 결과로 바꾸고, 다른 규칙의 항목은 기존 baseline 그대로 둔다.
   그래서 기존 규칙에 새로 생긴 위반이 이 기회에 baseline 으로 섞여 들어가지 않는다.
3. 내장 규칙 항목은 남기지 않는다. 남은 항목이 없는 모듈의 baseline 파일은 지운다(없으면 빈 baseline 으로 취급된다).
4. 모든 결과를 메모리에서 만든 뒤에야 저장소 파일을 원자적으로 바꾼다. 바꾸기 전에 원본을 저장소 밖에 백업하고,
   도중에 실패하거나 중단되면 원래 바이트 그대로 되돌린다.
5. 원본을 읽기 전부터 반영이 끝날 때까지 잠가서, 동시에 두 번 실행해도 서로의 결과를 덮어쓰지 않는다.
"""
from __future__ import annotations

import argparse
import hashlib
import io
import os
import re
import shutil
import stat
import subprocess
import sys
import tempfile
import uuid
import xml.etree.ElementTree as ET
from contextlib import contextmanager
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RULE_SOURCES = ROOT / "lint-rules" / "src" / "main"
BASELINE_NAME = "lint-baseline.xml"


def umc_issue_ids() -> set[str]:
    ids: set[str] = set()
    for source in RULE_SOURCES.rglob("*.kt"):
        ids.update(re.findall(r'\bid\s*=\s*"([A-Za-z0-9_]+)"', source.read_text(encoding="utf-8")))
    return ids


def module_dirs() -> list[Path]:
    """settings.gradle.kts 의 include 로 모듈 디렉터리를 정한다. 저장소 아래의 다른 체크아웃(worktree 등)은 섞이지 않는다."""
    settings = (ROOT / "settings.gradle.kts").read_text(encoding="utf-8")
    paths = re.findall(r'include\(\s*"(:[^"]+)"\s*\)', settings)
    return sorted(ROOT / path.lstrip(":").replace(":", "/") for path in paths)


def baseline_files() -> list[Path]:
    return [module / BASELINE_NAME for module in module_dirs() if (module / BASELINE_NAME).exists()]


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


def _issue_key(issue: ET.Element) -> tuple:
    location = issue.find("location")
    file = location.get("file") if location is not None else None
    return issue.get("id"), issue.get("message"), file, issue.get("errorLine1")


def merge(original: bytes | None, generated: Path | None, targets: set[str], known_ids: set[str]) -> str | None:
    """
    대상 규칙의 항목은 generated 에서, 다른 UMC 규칙의 항목은 original 에서 가져온 XML 문자열. 항목이 없으면 None.
    기존 항목의 순서를 그대로 두고 대상 규칙 항목만 제자리에서 바꾼다. 새로 생긴 항목은 끝에 붙인다.
    그래서 같은 결과로 다시 실행하면 파일이 바뀌지 않는다.
    """
    if original is None and generated is None:
        return None
    generated_root = ET.parse(generated).getroot() if generated is not None else None
    old_root = ET.fromstring(original) if original is not None else None
    remaining = [i for i in generated_root.findall("issue") if i.get("id") in targets] if generated_root is not None else []

    ordered: list[ET.Element] = []
    for issue in old_root.findall("issue") if old_root is not None else []:
        issue_id = issue.get("id")
        if issue_id not in known_ids:
            continue
        if issue_id not in targets:
            ordered.append(issue)
            continue
        match = next((fresh for fresh in remaining if _issue_key(fresh) == _issue_key(issue)), None)
        if match is not None:  # 없으면 고쳐진 위반이라 뺀다.
            remaining.remove(match)
            ordered.append(match)
    ordered += remaining
    if not ordered:
        return None

    root = generated_root if generated_root is not None else old_root
    for issue in list(root.findall("issue")):
        root.remove(issue)
    root.extend(ordered)
    tree = ET.ElementTree(root)
    ET.indent(tree, space="    ")
    buffer = io.BytesIO()
    tree.write(buffer, encoding="UTF-8", xml_declaration=True)
    return buffer.getvalue().decode("utf-8") + "\n"


def generate(work: Path) -> dict[Path, Path | None]:
    """
    work 에 updateLintBaseline 결과를 만들고, 저장소 baseline 경로 → 생성 파일(결과 0건이면 None)을 돌려준다.

    - AGP 는 지정한 baseline 파일이 없으면 모듈 기본 위치(저장소)에 쓰므로, 빈 파일을 먼저 만들어 둔다.
    - lint 리포트 모델은 baseline 파일 내용이 같으면 다시 만들지 않아 이전 실행의 경로를 가리킨다.
      빈 파일에 실행마다 다른 값을 넣어 모델을 다시 만들게 한다.
    - updateLintBaseline 은 미리 만든 파일을 지우고, 결과가 0건이면 다시 만들지 않는다(파일 없음 = 0건).
      빈 파일 내용이 그대로 남았으면 그 모듈은 lint 가 돌지 않은 것이다.
    """
    empty = f'<?xml version="1.0" encoding="UTF-8"?>\n<issues format="6" by="update_baseline.py {uuid.uuid4().hex}">\n</issues>\n'
    # 결과를 모으기 전에 작업 디렉터리가 그대로인지 확인하는 표식. 지워졌으면 0건과 구분할 수 없으므로 중단한다.
    marker = work / ".update_baseline_run"
    marker.write_text(empty, encoding="utf-8")
    for module in module_dirs():
        target = work / module.relative_to(ROOT) / BASELINE_NAME
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(empty, encoding="utf-8")
    subprocess.run(
        [
            "./gradlew",
            "updateLintBaseline",
            "-PlintCheckDependencies=true",
            f"-PumcLintBaselineDir={work}",
            "--console=plain",
        ],
        cwd=ROOT,
        check=True,
    )
    if not marker.exists():
        raise RuntimeError(f"Gradle 실행 중 작업 디렉터리({work})가 지워졌습니다. 결과를 믿을 수 없어 반영하지 않습니다.")
    results: dict[Path, Path | None] = {}
    not_run: list[Path] = []
    for module in module_dirs():
        produced = work / module.relative_to(ROOT) / BASELINE_NAME
        repo_path = module / BASELINE_NAME
        if not produced.exists():
            results[repo_path] = None
        elif produced.read_text(encoding="utf-8") == empty:
            not_run.append(repo_path)
        else:
            results[repo_path] = produced
    app = ROOT / "app" / BASELINE_NAME
    problems = [p for p in not_run if p == app or p.exists()]
    if problems:
        names = ", ".join(str(p.relative_to(ROOT).parent) for p in problems)
        raise RuntimeError(f"lint 가 돌지 않아 baseline 을 만들 수 없는 모듈이 있습니다({names}). ./gradlew clean 후 다시 실행하세요.")
    return results


def _replace_atomically(path: Path, data: bytes) -> None:
    """같은 디렉터리의 임시 파일에 쓴 뒤 교체한다. 읽는 쪽이 빈 파일이나 반쯤 쓴 파일을 보지 않는다."""
    staging = path.with_name(f"{path.name}.{uuid.uuid4().hex}.tmp")
    try:
        staging.write_bytes(data)
        os.replace(staging, path)
    finally:
        staging.unlink(missing_ok=True)


def apply(outputs: dict[Path, str | None], originals: dict[Path, bytes]) -> None:
    """
    outputs 대로 저장소 파일을 바꾼다. 바꾸기 전에 원본을 저장소 밖에 백업해 두고, 도중에 실패하거나 중단되면
    이번 실행이 바꾼 파일을 원래 바이트 그대로 되돌린다. 되돌리지 못하면 백업 위치를 알려 준다.
    """
    backup_dir = Path(tempfile.mkdtemp(prefix="umc-lint-baseline-backup-"))
    for path, data in originals.items():
        target = backup_dir / path.relative_to(ROOT)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)

    touched: list[Path] = []
    try:
        for path, text in outputs.items():
            if text is None:
                if path.exists():
                    touched.append(path)
                    path.unlink()
                continue
            touched.append(path)
            _replace_atomically(path, text.encode("utf-8"))
    except BaseException:
        failed: list[Path] = []
        for path in touched:
            try:
                if path in originals:
                    _replace_atomically(path, originals[path])
                else:
                    path.unlink(missing_ok=True)
            except OSError:
                failed.append(path)
        if failed:
            print("되돌리지 못한 baseline 이 있습니다: " + ", ".join(str(p.relative_to(ROOT)) for p in failed), file=sys.stderr)
            print(f"원본 백업은 {backup_dir} 에 그대로 남겨 두었습니다. 같은 경로로 복사해 되돌리세요.", file=sys.stderr)
        else:
            print("반영 도중 실패해서 바꾼 baseline 을 실행 전 상태로 되돌렸습니다.", file=sys.stderr)
            shutil.rmtree(backup_dir, ignore_errors=True)
        raise
    shutil.rmtree(backup_dir, ignore_errors=True)


def _private_state_dir() -> Path:
    """현재 사용자만 쓸 수 있는 상태 디렉터리. 다른 사용자가 만들었거나 권한이 열려 있으면 쓰지 않는다."""
    # HOME 환경 변수가 아니라 계정 정보의 홈 디렉터리를 써서, 같은 사용자의 실행은 항상 같은 잠금을 연다.
    import pwd

    state_dir = Path(pwd.getpwuid(os.getuid()).pw_dir) / ".local" / "state" / "umc-lint-baseline"
    state_dir.mkdir(parents=True, exist_ok=True, mode=0o700)
    info = state_dir.lstat()
    if not stat.S_ISDIR(info.st_mode) or info.st_uid != os.getuid() or info.st_mode & 0o077:
        raise RuntimeError(f"잠금 디렉터리 {state_dir} 의 소유자나 권한이 올바르지 않습니다(현재 사용자 전용, 0700 이어야 함).")
    return state_dir


@contextmanager
def exclusive_run():
    """
    두 실행이 같은 원본을 읽고 서로의 결과를 덮어쓰지 않도록, 원본을 읽기 전부터 반영이 끝날 때까지 잠근다.
    잠금 파일은 빌드 정리(./gradlew clean, git clean)가 지우지 않고 TMPDIR·HOME 같은 환경 변수에 따라 바뀌지 않는
    계정 홈의 사용자 전용 디렉터리(~/.local/state/umc-lint-baseline)에 두고, 체크아웃 경로마다 다른 이름을 써서
    다른 체크아웃끼리는 막지 않는다.
    """
    try:
        import fcntl
    except ImportError:  # Windows 는 잠금 없이 실행한다.
        yield
        return
    checkout = hashlib.sha256(os.fsencode(ROOT)).hexdigest()[:16]
    lock_path = _private_state_dir() / f"umc-lint-baseline-{checkout}.lock"
    # 링크를 따라가지 않고 열어, 미리 만들어 둔 심볼릭 링크로 다른 파일을 자르지 않게 한다.
    descriptor = os.open(lock_path, os.O_RDWR | os.O_CREAT | getattr(os, "O_NOFOLLOW", 0), 0o600)
    with os.fdopen(descriptor, "w") as handle:
        try:
            fcntl.flock(handle, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError:
            raise SystemExit("다른 update_baseline.py 가 실행 중입니다. 끝난 뒤 다시 실행하세요.")
        try:
            yield
        finally:
            fcntl.flock(handle, fcntl.LOCK_UN)


def summary(text: str | None) -> str:
    if text is None:
        return "UMC 항목 없음 → 삭제"
    counts: dict[str, int] = {}
    for issue in ET.fromstring(text).findall("issue"):
        counts[issue.get("id")] = counts.get(issue.get("id"), 0) + 1
    return ", ".join(f"{k} {v}건" for k, v in sorted(counts.items()))


def main() -> int:
    known_ids = umc_issue_ids()
    if not known_ids:
        print("lint-rules 에서 규칙 ID 를 찾지 못했습니다.", file=sys.stderr)
        return 1
    targets = parse_args(known_ids)
    print("기록할 규칙:", ", ".join(sorted(targets)))

    with exclusive_run():
        # 줄바꿈까지 그대로 되돌릴 수 있도록 바이트로 보관한다.
        originals = {path: path.read_bytes() for path in baseline_files()}
        # Gradle 은 저장소가 아니라 실행마다 새로 만드는 시스템 임시 디렉터리에 쓴다. 중단된 이전 실행의 데몬 태스크가
        # 늦게 써도, 빌드 정리가 build 를 지워도 저장소 파일이나 이번 실행의 결과와 섞이지 않는다.
        work = Path(tempfile.mkdtemp(prefix="umc-lint-baseline-"))
        try:
            generated = generate(work)
            outputs = {
                path: merge(originals.get(path), generated.get(path), targets, known_ids)
                for path in sorted(set(generated) | set(originals))
            }
        finally:
            shutil.rmtree(work, ignore_errors=True)
        apply(outputs, originals)
    for path, text in outputs.items():
        if text is not None or path in originals:
            print(f"{path.relative_to(ROOT)}: {summary(text)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
