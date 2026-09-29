#!/usr/bin/env python3
"""출시가 끝나면 공유 채널에 릴리스 소식을 올린다.

배포 채널(android-github-ci)로 가는 알림은 개발자용이라 커밋과 결과가 잔뜩 붙는다.
이건 그것과 다르다. 읽는 사람은 총괄단이고, 버전 하나에 스레드 하나만 남기면 된다.

Discord 웹훅은 **포럼·미디어 채널에서만** 새 스레드를 만들 수 있다(thread_name).
일반 텍스트 채널이면 400 으로 거절되는데, 그때 메시지만 흘려보내지 않고 그대로 실패시킨다.
버전마다 스레드 하나가 남는 게 이 알림의 전부라, 스레드 없이 올라간 메시지는 의미가 없다.

사용:
    python3 discord_release.py --version 3.4.1 --release-url https://github.com/.../v3.4.1
"""

import argparse
import json
import os
import sys
import urllib.error
import urllib.request
from pathlib import Path


def build_body(intro: str, role_id: str, release_url: str) -> str:
    lines = ["이 버전에서 업그레이드된 사항"]
    if intro:
        lines.append(intro)
    if role_id:
        # 역할 멘션은 <@&ID> 형식이다. 실제로 알림이 가려면 allowed_mentions 에도 넣어야 한다.
        lines += ["", f"<@&{role_id}>"]
    lines += ["", release_url]
    return "\n".join(lines)


def post(webhook: str, payload: dict, timeout: int) -> None:
    request = urllib.request.Request(
        # wait=true 로 보내야 Discord 가 실패를 알려준다. 기본값은 202 로 삼켜 버린다.
        webhook + ("&" if "?" in webhook else "?") + "wait=true",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=timeout) as response:
        response.read()


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", required=True, help="예: 3.4.1")
    ap.add_argument("--release-url", required=True)
    ap.add_argument("--whatsnew", default="whatsnew/whatsnew-ko-KR")
    ap.add_argument("--platform", default="Android")
    ap.add_argument("--timeout", type=int, default=30)
    args = ap.parse_args()

    webhook = os.environ.get("DISCORD_RELEASE_WEBHOOK_URL", "").strip()
    if not webhook:
        print("DISCORD_RELEASE_WEBHOOK_URL 이 없어 공유 채널 알림을 건너뜁니다.")
        return 0

    # Play 노트 첫 줄이 이번 버전을 한 문장으로 요약한 인트로다.
    intro = ""
    whatsnew = Path(args.whatsnew)
    if whatsnew.exists():
        for line in whatsnew.read_text(encoding="utf-8").splitlines():
            if line.strip():
                intro = line.strip()
                break

    role_id = os.environ.get("DISCORD_RELEASE_ROLE_ID", "").strip()
    payload = {
        "content": build_body(intro, role_id, args.release_url),
        # 본문에 적힌 역할만 실제로 알림이 가게 한다. @everyone 은 어떤 경우에도 막는다.
        "allowed_mentions": {"parse": [], "roles": [role_id] if role_id else []},
    }

    thread_name = f"[{args.platform} {args.version}]"
    try:
        post(webhook, dict(payload, thread_name=thread_name), args.timeout)
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")[:300]
        print(f"스레드 생성 실패 {exc.code}: {detail}")
        if exc.code == 400:
            print("웹훅이 걸린 채널이 포럼·미디어 채널인지 확인하세요 — 텍스트 채널은 스레드를 못 만듭니다.")
        return 1
    except Exception as exc:
        print(f"스레드 생성 실패: {exc}")
        return 1

    print(f"스레드 '{thread_name}' 생성 완료")
    return 0


if __name__ == "__main__":
    sys.exit(main())
