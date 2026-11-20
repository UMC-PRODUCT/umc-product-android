---
name: run-umc-app
description: UMC 앱(dev 디버그 빌드)을 에뮬레이터에서 띄워 QA 계정으로 로그인하고, 원하는 화면까지 이동해 화면과 네트워크 호출을 확인한다. UI·DI·네트워크를 바꾼 뒤 "앱을 띄워 확인"할 때, 특정 화면을 보여 달라고 할 때 쓴다.
---

# UMC 앱 실행·확인

화면과 네트워크 변경은 컴파일·단위 테스트로 확인되지 않는다(CLAUDE.md "검증"). 이 스킬은 실제 앱을 띄워
바뀐 화면에 도착했는지(`screen_view` 로그), 어떤 API를 어느 서버로 불렀는지(HTTP 로그 요약)를 확인한다.

## 준비

- Android SDK(`ANDROID_HOME`, 없으면 `~/Library/Android/sdk`)와 휴대폰 AVD 하나.
- QA 계정 비밀번호는 **저장소에 넣지 않는다.** 실행할 때 둘 중 하나로 넘긴다.
  - `UMC_QA_PASSWORD`: 비밀번호
  - `UMC_QA_DOC`: QA 계정 문서(저장소 밖) 경로. 문서의 `공통 비밀번호: [...]` 줄을 읽는다.
- 계정 이메일은 QA 문서에서 화면에 필요한 역할(챌린저, 운영진 등)에 맞는 것을 고른다. feature map의 `계정` 열을 본다.

## 명령

```bash
S=.claude/skills/run-umc-app/scripts/umc_app.py
python3 -I $S boot                      # 연결된 기기가 없으면 에뮬레이터를 띄운다(UMC_AVD 로 지정 가능)
python3 -I $S install --build           # dev 디버그 빌드 후 설치. 알림 권한만 준다
python3 -I $S launch --fresh            # 데이터를 지우고 실행. 첫 화면 이름을 출력
python3 -I $S login --email <QA 이메일>  # 로그인 후 홈 도착 확인
python3 -I $S goto Notice               # feature map 경로대로 이동하고 도착 확인
python3 -I $S goto CommunityChatting --arg threadId=<id>
python3 -I $S verify Notice --email <QA 이메일>   # 실행(+로그인) → 이동 → 스크린샷 → 네트워크 요약
python3 -I $S net                       # HTTP 요약: prod 호스트 호출·401은 실패, 그 밖의 4xx·5xx는 경고
python3 -I $S texts                     # 지금 화면에 보이는 글자 목록
python3 -I $S tap "<글자>"              # 보이는 글자를 찾아 누른다(없으면 실패)
python3 -I $S shot                      # 스크린샷 저장 후 경로 출력(경로를 인자로 줄 수 있다)
```

`verify`와 `net`은 실패가 있으면 종료 코드 1을 돌려준다. 스크린샷 경로를 출력하므로 이미지를 열어 화면을 확인한다.

## 지켜야 할 것

- **비밀번호를 출력하거나 명령줄 인자로 넘기지 않는다.** 스크립트는 실패 메시지에도 비밀번호를 넣지 않는다.
- 디버그 빌드의 HTTP 로그는 요청·응답 줄과 헤더뿐이다(본문은 #374부터 남기지 않고, 인증 헤더 값은 가린다).
  `net`은 `RETROFIT1` 태그의 요청·응답 줄만 본다. 본문 로그를 다시 켜는 변경이 있으면 이 스킬의 logcat 처리부터 고친다.
- 서명이 다른 앱이 설치돼 있으면 설치가 실패한다. 스크립트는 지우지 않는다. 데이터를 지워도 되는지 사용자에게 묻는다.
- 키보드가 한글 입력 상태면 `login`이 입력 언어를 바꿔 다시 입력한다. 키보드가 열려 있을 때만 BACK으로 닫는다.
- 사용자가 끈 에뮬레이터를 말없이 다시 켜지 않는다. 띄워 둔 화면은 보고에 적는다.

## feature map

`feature-map.md`에 모든 화면(`MainDestination`)의 Route, 로그에 찍히는 screen_name, 모듈, 진입 경로, 필요한 인자, 필요한 계정을 적는다.

- 진입 경로 표기: `tab:<하단 탭 글자>`, `tap:<보이는 글자>`, `tap:{인자}`, `deeplink:umc://…`를 ` > `로 잇는다.
  상태에 따라 글자가 다른 버튼은 `tap:<글자1> / <글자2>`로 대체 글자를 적는다.
  `start`(자동 진입), `login`(로그인 흐름), `-`(단계 이동으로 못 감)은 `goto`가 거절한다.
- 화면을 추가·삭제하면 표도 고친다. `:presentation`의 `FeatureMapTest`가 `MainDestination`과 표의 Route가 어긋나면 실패한다.
- 버튼 글자가 바뀌어 경로가 낡으면 `goto`가 "찾지 못했습니다"로 실패한다. 그때 표를 고친다.
