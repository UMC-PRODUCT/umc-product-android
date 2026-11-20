#!/usr/bin/env python3
"""UMC 앱(dev 디버그 빌드)을 에뮬레이터에서 띄워 화면과 네트워크 호출을 확인한다.

표준 라이브러리만 쓴다. `python3 -I .claude/skills/run-umc-app/scripts/umc_app.py <명령>`

  boot                      연결된 기기를 쓰고, 없으면 에뮬레이터를 띄워 부팅을 기다린다
  install [--build]         dev 디버그 APK 를 설치한다(--build 면 먼저 빌드)
  launch [--fresh]          앱을 띄우고 첫 화면(screen_view)을 알려 준다(--fresh 면 데이터를 지운다)
  login --email E           QA 계정으로 로그인한다. 비밀번호는 환경 변수에서 읽고 출력하지 않는다
  goto ROUTE [--arg k=v]    feature-map.md 의 진입 경로를 따라 이동하고 도착을 확인한다
  verify ROUTE [...]        launch(+login) → goto → 스크린샷 → 네트워크 요약을 한 번에 한다
  net                       마지막 표시 시점 이후의 HTTP 호출을 요약한다(본문·헤더는 출력하지 않는다)
  texts | tap TEXT | shot   화면 글자 목록, 글자로 탭, 스크린샷

환경 변수
  ANDROID_SERIAL   여러 기기가 연결됐을 때 쓸 기기
  UMC_AVD          boot 가 띄울 AVD 이름(없으면 휴대폰 AVD 중 첫 번째)
  UMC_QA_PASSWORD  QA 계정 비밀번호. 없으면 UMC_QA_DOC 문서에서 "공통 비밀번호: [...]" 줄을 읽는다
  UMC_QA_DOC       QA 계정 문서 경로(저장소 밖에 둔다)
"""
from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "com.umc.product"
REPO = Path(__file__).resolve().parents[4]
SKILL_DIR = Path(__file__).resolve().parents[1]
FEATURE_MAP = SKILL_DIR / "feature-map.md"
APK = REPO / "app/build/outputs/apk/dev/debug/app-dev-debug.apk"
WORK = Path(tempfile.gettempdir()) / "umc-app"
DEV_HOST = "api-dev.university.neordinary.com"
PROD_HOST = "api.university.neordinary.com"
NON_PHONE_AVD = re.compile(r"automotive|auto|car|tv|wear|pleos|xr", re.IGNORECASE)


class Failure(Exception):
    """사용자에게 보여 줄 실패. 종료 코드 1."""


# ── adb ──────────────────────────────────────────────────────────────


def adb(*args: str, check: bool = True, secret: bool = False, timeout: int = 60) -> str:
    """adb 를 실행한다. secret 이면 실패해도 인자(비밀번호 등)를 출력하지 않는다."""
    command = ["adb"] + args_for_serial() + list(args)
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        raise Failure("adb 응답이 없습니다" + ("" if secret else f": {' '.join(args)}"))
    if check and result.returncode != 0:
        detail = "" if secret else f": {' '.join(args)}\n{result.stderr.strip()}"
        raise Failure("adb 명령 실패" + detail)
    return result.stdout


def args_for_serial() -> list[str]:
    serial = os.environ.get("ANDROID_SERIAL")
    return ["-s", serial] if serial else []


def connected_devices() -> list[str]:
    out = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout
    return [line.split()[0] for line in out.splitlines()[1:] if line.strip().endswith("device")]


def device_time() -> str:
    """logcat -T 에 넘길 기기 시각. 공백이 들어가면 adb 가 인자를 나누므로 epoch 초 형식을 쓴다."""
    return adb("shell", "date", "+%s").strip() + ".000"


# ── 부팅·설치·실행 ─────────────────────────────────────────────────────


def sdk_root() -> Path:
    root = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    return Path(root) if root else Path.home() / "Library/Android/sdk"


def boot(avd: str | None) -> None:
    if connected_devices():
        print(f"연결된 기기를 사용합니다: {', '.join(connected_devices())}")
        return
    emulator = sdk_root() / "emulator/emulator"
    if not emulator.exists():
        raise Failure(f"에뮬레이터 실행 파일이 없습니다: {emulator}")
    if not avd:
        avds = [a for a in subprocess.run([str(emulator), "-list-avds"], capture_output=True, text=True).stdout.split() if a]
        phones = [a for a in avds if not NON_PHONE_AVD.search(a)]
        if not phones:
            raise Failure("휴대폰 AVD 가 없습니다. Android Studio Device Manager 에서 만들거나 UMC_AVD 를 지정하세요.")
        avd = phones[0]
    WORK.mkdir(parents=True, exist_ok=True)
    with open(WORK / "emulator.log", "w") as log:
        subprocess.Popen([str(emulator), "-avd", avd, "-no-boot-anim"], stdout=log, stderr=subprocess.STDOUT,
                         start_new_session=True)
    print(f"에뮬레이터 {avd} 를 띄우는 중…")
    subprocess.run(["adb", "wait-for-device"], timeout=180)
    for _ in range(90):
        if adb("shell", "getprop", "sys.boot_completed", check=False).strip() == "1":
            print("부팅 완료")
            return
        time.sleep(2)
    raise Failure("180초 안에 부팅이 끝나지 않았습니다")


def install(build: bool) -> None:
    if build:
        print("dev 디버그 빌드 중…")
        if subprocess.run(["./gradlew", ":app:assembleDevDebug", "-q"], cwd=REPO).returncode != 0:
            raise Failure("빌드 실패")
    if not APK.exists():
        raise Failure(f"APK 가 없습니다. --build 로 빌드하세요: {APK}")
    out = subprocess.run(["adb"] + args_for_serial() + ["install", "-r", "-t", str(APK)], capture_output=True, text=True)
    if "INSTALL_FAILED_UPDATE_INCOMPATIBLE" in out.stdout + out.stderr:
        raise Failure("서명이 다른 앱(Play 설치본 등)이 이미 있습니다. 데이터를 지워도 되면 직접 "
                      f"`adb uninstall {PACKAGE}` 후 다시 설치하세요(자동으로 지우지 않습니다).")
    if out.returncode != 0:
        raise Failure("설치 실패: " + (out.stderr or out.stdout).strip())
    grant_notification()
    print("설치 완료(dev 디버그)")


def grant_notification() -> None:
    # 다른 권한은 권한 화면 동작을 바꾸므로 알림 권한만 준다(API 33 미만에서는 실패해도 무시).
    adb("shell", "pm", "grant", PACKAGE, "android.permission.POST_NOTIFICATIONS", check=False)


def launch(fresh: bool) -> str:
    if fresh:
        adb("shell", "pm", "clear", PACKAGE)
        grant_notification()
    adb("shell", "am", "force-stop", PACKAGE)
    since = device_time()
    adb("shell", "monkey", "-p", PACKAGE, "-c", "android.intent.category.LAUNCHER", "1")
    screen = wait_screen(lambda name: name not in {"Splash"}, since, timeout=25)
    print(f"첫 화면: {screen}")
    return screen


# ── 화면 판정 ─────────────────────────────────────────────────────────


def screens_since(since: str) -> list[str]:
    out = adb("logcat", "-d", "-T", since, "-s", "UMC_ANALYTICS", check=False)
    return re.findall(r"screen_name=(\w+)", out)


def current_screen() -> str | None:
    """logcat 에 남은 마지막 screen_view. login 은 logcat 을 비우므로 그 뒤 이동이 없으면 None 이다."""
    names = re.findall(r"screen_name=(\w+)", adb("logcat", "-d", "-s", "UMC_ANALYTICS", check=False))
    return names[-1] if names else None


def wait_screen(accept, since: str, timeout: int) -> str:
    """since 이후 찍힌 screen_view 중 accept 를 만족하는 마지막 화면을 기다린다."""
    deadline = time.time() + timeout
    while time.time() < deadline:
        names = screens_since(since)
        if names and accept(names[-1]):
            return names[-1]
        time.sleep(1)
    names = screens_since(since)
    raise Failure(f"{timeout}초 안에 기대한 화면에 도착하지 못했습니다(마지막 화면: {names[-1] if names else '없음'})")


def dump() -> ET.Element:
    adb("shell", "uiautomator", "dump", "/sdcard/umc-ui.xml")
    WORK.mkdir(parents=True, exist_ok=True)
    local = WORK / "ui.xml"
    adb("pull", "/sdcard/umc-ui.xml", str(local))
    return ET.parse(local).getroot()


def visible_texts(root: ET.Element | None = None) -> list[str]:
    root = root if root is not None else dump()
    out = []
    for node in root.iter("node"):
        label = node.get("text") or node.get("content-desc")
        if label:
            out.append(label)
    return out


def center(node: ET.Element) -> tuple[int, int]:
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
    return (x1 + x2) // 2, (y1 + y2) // 2


def find_node(root: ET.Element, text: str) -> ET.Element | None:
    for node in root.iter("node"):
        if text in (node.get("text"), node.get("content-desc")):
            return node
    return None


def tap(text: str, wait: float = 1.5) -> None:
    node = find_node(dump(), text)
    if node is None:
        raise Failure(f"화면에서 '{text}'를 찾지 못했습니다")
    x, y = center(node)
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(wait)


def keyboard_shown() -> bool:
    return "mInputShown=true" in adb("shell", "dumpsys", "input_method", check=False)


def hide_keyboard() -> None:
    # 키보드가 떠 있을 때만 BACK 을 보낸다. 닫혀 있을 때 BACK 은 화면을 닫는다.
    if keyboard_shown():
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(0.8)


def shot(path: str | None = None) -> Path:
    WORK.mkdir(parents=True, exist_ok=True)
    target = Path(path) if path else WORK / f"shot-{time.strftime('%Y%m%d-%H%M%S')}.png"
    with open(target, "wb") as f:
        f.write(subprocess.run(["adb"] + args_for_serial() + ["exec-out", "screencap", "-p"], capture_output=True).stdout)
    return target


# ── 로그인 ────────────────────────────────────────────────────────────


def qa_password() -> str:
    password = os.environ.get("UMC_QA_PASSWORD")
    if password:
        return password
    doc = os.environ.get("UMC_QA_DOC")
    if not doc or not Path(doc).exists():
        raise Failure("UMC_QA_PASSWORD 또는 UMC_QA_DOC(QA 계정 문서 경로)를 설정하세요")
    match = re.search(r"공통 비밀번호:\s*\[([^\]]+)\]", Path(doc).read_text(encoding="utf-8"))
    if not match:
        raise Failure("QA 문서에서 '공통 비밀번호: [...]' 줄을 찾지 못했습니다")
    return match.group(1)


def edit_fields() -> list[ET.Element]:
    return [n for n in dump().iter("node") if n.get("class", "").endswith("EditText")]


def type_into(node: ET.Element, value: str, secret: bool) -> None:
    x, y = center(node)
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(0.5)
    adb("shell", "input", "keyevent", "KEYCODE_MOVE_END")
    adb("shell", "input", "keyevent", *(["KEYCODE_DEL"] * 60))
    escaped = value.replace("\\", "\\\\").replace("'", "'\\''").replace("%", "\\%").replace(" ", "%s")
    adb("shell", f"input text '{escaped}'", secret=secret)
    time.sleep(0.4)


def login(email: str) -> None:
    password = qa_password()
    texts = visible_texts()
    if "UMC 계정 로그인" in texts:
        tap("UMC 계정 로그인", wait=2)
    for attempt in range(3):
        fields = edit_fields()
        if len(fields) < 2:
            raise Failure("이메일·비밀번호 입력란을 찾지 못했습니다(로그인 화면인지 확인하세요)")
        type_into(fields[0], email, secret=False)
        # 키보드가 한글 입력 상태면 영문이 한글로 바뀐다. 이메일이 그대로 들어갔는지 확인하고 아니면 입력 언어를 바꾼다.
        if email in visible_texts():
            break
        adb("shell", "input", "keyevent", "KEYCODE_LANGUAGE_SWITCH")
        time.sleep(0.8)
    else:
        raise Failure("이메일을 입력하지 못했습니다(키보드 입력 언어를 영문으로 바꿔 보세요)")
    type_into(edit_fields()[1], password, secret=True)
    hide_keyboard()
    since = device_time()
    tap("로그인", wait=1)
    try:
        wait_screen(lambda name: name == "Home", since, timeout=30)
    except Failure:
        visible = [t for t in visible_texts() if t not in {email}]
        raise Failure("로그인 후 홈으로 가지 못했습니다. 화면 글자: " + " / ".join(visible[:12]))
    finally:
        # 디버그 빌드는 로그인 요청 본문(비밀번호)과 토큰 응답을 logcat 에 남기므로, 성공·실패와 관계없이 바로 지운다.
        adb("logcat", "-c", check=False)
    print(f"{email} 로그인 완료(비밀번호는 출력하지 않음)")


# ── feature map 과 이동 ──────────────────────────────────────────────


def feature_map() -> dict[str, dict[str, str]]:
    rows: dict[str, dict[str, str]] = {}
    header: list[str] | None = None
    for line in FEATURE_MAP.read_text(encoding="utf-8").splitlines():
        if not line.startswith("|"):
            header = None
            continue
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if cells and cells[0] == "Route":
            header = cells
            continue
        if header is None or set("".join(cells)) <= set("-: "):
            continue
        row = dict(zip(header, cells))
        rows[row["Route"].strip("`")] = row
    return rows


def goto(route: str, args: dict[str, str]) -> None:
    rows = feature_map()
    if route not in rows:
        raise Failure(f"feature-map.md 에 '{route}'가 없습니다. 있는 Route: {', '.join(sorted(rows))}")
    row = rows[route]
    entry = row.get("진입", "-")
    expected = row.get("screen_name", route).strip("`") or route
    if entry in {"-", "start", "login"}:
        raise Failure(f"{route} 는 단계 이동으로 갈 수 없습니다(진입: {entry}). {row.get('설명', '')}")
    # 이미 그 화면이면 screen_view 가 다시 찍히지 않으므로 바로 끝낸다.
    if current_screen() == expected:
        print(f"이미 {route} 입니다")
        return
    steps = [s.strip() for s in entry.split(">")]
    # 탭·버튼으로 가는 경로는 홈에서 출발한다고 적었다. 하단 탭이 안 보이는 화면이면 뒤로 가서 탭을 되찾고,
    # 버튼으로 시작하는 경로라면 홈 탭으로 돌아간다.
    if not steps[0].startswith("deeplink:"):
        back_to_tabs()
        if steps[0].startswith("tap:") and current_screen() not in {None, "Home"}:
            tap("홈")
    since = device_time()
    for step in steps:
        kind, _, value = step.partition(":")
        value = re.sub(r"\{(\w+)\}", lambda m: args.get(m.group(1)) or missing_arg(m.group(1), route), value.strip())
        if kind in {"tab", "tap"}:
            tap(value)
        elif kind == "deeplink":
            adb("shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", value, PACKAGE)
            time.sleep(2)
        else:
            raise Failure(f"알 수 없는 진입 단계: {step}")
    wait_screen(lambda name: name == expected, since, timeout=15)
    print(f"{route} 도착(screen_view={expected})")


TAB_ROOTS = {"Home", "Notice", "Act", "Community", "Mycard"}


def back_to_tabs() -> None:
    """하단 탭이 보일 때까지 뒤로 간다. 탭 화면(TAB_ROOTS)에서는 BACK 이 앱을 닫으므로 누르지 않는다."""
    for _ in range(4):
        if "홈" in visible_texts() or current_screen() in TAB_ROOTS:
            return
        adb("shell", "input", "keyevent", "KEYCODE_BACK")
        time.sleep(1.2)
    raise Failure("뒤로 가도 하단 탭이 보이지 않습니다")


def missing_arg(name: str, route: str) -> str:
    raise Failure(f"{route} 로 가려면 --arg {name}=값 이 필요합니다")


# ── 네트워크 요약 ─────────────────────────────────────────────────────


def net(since: str | None) -> int:
    """HTTP 요청·응답 줄만 요약한다. prod 호스트 호출과 로그인 후 401 은 실패로 본다."""
    args = ["logcat", "-d", "-s", "RETROFIT1"]
    if since:
        args[2:2] = ["-T", since]
    out = adb(*args, check=False)
    requests = re.findall(r"--> (GET|POST|PUT|PATCH|DELETE) (https?://\S+)", out)
    responses = re.findall(r"<-- (\d{3}) [^\n]*?(https?://\S+)", out)
    problems = 0
    for method, url in requests:
        host = re.sub(r"https?://([^/]+).*", r"\1", url)
        mark = "실패(prod 호스트)" if host == PROD_HOST else ""
        problems += bool(mark)
        print(f"  {method:6} {url.split('?')[0]} {mark}")
    for status, url in responses:
        code = int(status)
        if code == 401:
            print(f"  실패 401 {url.split('?')[0]}")
            problems += 1
        elif code >= 400:
            print(f"  경고 {code} {url.split('?')[0]}")
    print(f"요청 {len(requests)}건, 응답 {len(responses)}건, 실패 {problems}건")
    return problems


# ── 진입점 ────────────────────────────────────────────────────────────


def parse_args(pairs: list[str]) -> dict[str, str]:
    out = {}
    for pair in pairs or []:
        key, sep, value = pair.partition("=")
        if not sep:
            raise Failure(f"--arg 는 이름=값 형식입니다: {pair}")
        out[key] = value
    return out


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("boot"); p.add_argument("--avd", default=os.environ.get("UMC_AVD"))
    p = sub.add_parser("install"); p.add_argument("--build", action="store_true")
    p = sub.add_parser("launch"); p.add_argument("--fresh", action="store_true")
    p = sub.add_parser("login"); p.add_argument("--email", required=True)
    p = sub.add_parser("goto"); p.add_argument("route"); p.add_argument("--arg", action="append")
    p = sub.add_parser("verify"); p.add_argument("route"); p.add_argument("--arg", action="append")
    p.add_argument("--email"); p.add_argument("--fresh", action="store_true")
    sub.add_parser("net")
    sub.add_parser("texts")
    p = sub.add_parser("tap"); p.add_argument("text")
    p = sub.add_parser("shot"); p.add_argument("path", nargs="?")
    a = parser.parse_args()

    try:
        if a.command == "boot":
            boot(a.avd)
        elif a.command == "install":
            install(a.build)
        elif a.command == "launch":
            launch(a.fresh)
        elif a.command == "login":
            login(a.email)
        elif a.command == "goto":
            goto(a.route, parse_args(a.arg))
        elif a.command == "verify":
            boot(os.environ.get("UMC_AVD"))
            first = launch(a.fresh)
            if first in {"Login", "EmailLogin"}:
                if not a.email:
                    raise Failure("로그아웃 상태입니다. --email 로 QA 계정을 지정하세요")
                login(a.email)
            since = device_time()
            if a.route != "Home":
                goto(a.route, parse_args(a.arg))
            time.sleep(2)
            print(f"스크린샷: {shot()}")
            return 1 if net(since) else 0
        elif a.command == "net":
            return 1 if net(None) else 0
        elif a.command == "texts":
            print("\n".join(visible_texts()))
        elif a.command == "tap":
            tap(a.text)
        elif a.command == "shot":
            print(shot(a.path))
    except Failure as error:
        print(f"실패: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
