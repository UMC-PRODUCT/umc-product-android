# UMC 앱 화면 지도(feature map)

`umc_app.py goto`가 이 표를 읽어 dev 디버그 앱을 이동시키고, `:presentation`의 `FeatureMapTest`가 `MainDestination`과 대조한다.

- **Route**: `MainDestination`에 선언된 화면 클래스 이름.
- **screen_name**: 디버그 빌드가 logcat 태그 `UMC_ANALYTICS`에 `screen_view {screen_name=…}`으로 남기는 이름. Route와 같고, `memberId`가 있는 Mycard만 `MemberCard`다.
- **모듈**: 화면 컴포저블이 있는 Gradle 모듈.
- **진입**: 로그인한 홈 화면에서 출발하는 단계를 ` > `로 잇는다.
  - `tab:글자`(하단 탭), `tap:글자`(text나 content-desc가 정확히 그 글자인 요소). 글자는 presentation 소스에 따옴표째 있어야 한다(테스트가 확인).
  - 상태에 따라 버튼 글자가 다르면 `tap:글자1 / 글자2`처럼 ` / `로 대체 글자를 적는다. 앞 글자부터 찾아 먼저 보이는 것을 누른다.
  - `tap:{인자}`, `deeplink:umc://…{인자}`의 중괄호는 `--arg 인자=값`으로 채운다.
  - `start`(자동 진입), `login`(로그인·가입 흐름), `-`(누를 글자가 없어 단계 이동으로 못 감, 이유는 설명에)는 `goto`가 거절한다.
- **인자**: `goto`에 넘길 값과 얻는 곳. **계정**: 필요한 역할이나 상태(`-`는 로그인한 계정이면 무엇이든). 이메일·비밀번호는 적지 않는다.

| Route | screen_name | 모듈 | 설명 | 진입 | 인자 | 계정 |
|---|---|---|---|---|---|---|
| Splash | Splash | :presentation:splash | 시작 화면. 저장된 토큰으로 자동 로그인을 판정해 Home·Login·SignUpFailCode 중 하나로 보낸다 | start | - | 모든 상태 |
| Login | Login | :presentation:login | 로그인 방식 선택(카카오·구글·UMC 계정). 토큰이 없거나 로그아웃·세션 만료 때 들어온다 | login | - | 로그아웃 상태 |
| EmailLogin | EmailLogin | :presentation:login | 이메일 로그인. Login에서 UMC 계정 로그인을 누르면 들어온다 | login | - | 로그아웃 상태 |
| FindPassword | FindPassword | :presentation:login | 비밀번호 찾기(이메일 인증 후 새 비밀번호). EmailLogin의 비밀번호 찾기 | login | - | 로그아웃 상태 |
| SignUp | SignUp | :presentation:signUp | 가입 개인정보 입력. 소셜·이메일 인증 뒤 들어오고 signUpType에 따라 가입 API가 갈린다 | login | 가입 흐름에서 자동 전달 | 미가입 계정 |
| SocialSignUp | SocialSignUp | :presentation:signUp | 소셜 가입의 이메일 인증. 카카오·구글 로그인 결과가 미가입이면 들어온다 | login | 소셜 로그인 결과(자동) | 미가입 소셜 계정 |
| EmailSignUp | EmailSignUp | :presentation:signUp | 이메일 가입(이메일 인증·비밀번호 설정). EmailLogin의 회원가입 | login | - | 미가입 이메일 |
| Permission | Permission | :presentation:permission | 권한 안내와 가입 마무리. SignUp 다음 단계 | login | - | 미가입 계정 |
| SignUpFail | SignUpFail | :presentation:failCode | 가입 실패 안내(챌린저 기록 없음) | login | - | 챌린저 기록이 없는 계정 |
| SignUpFailCode | SignUpFailCode | :presentation:failCode | 활동 코드 입력. 챌린저 기록이 없는 계정이면 스플래시·로그인에서 자동으로 들어온다 | login | - | 챌린저 기록이 없는 계정 |
| Act | Act | :presentation:act | 활동 관리(출석·스터디/활동·챌린저 탭). 스터디 탭 내용은 :presentation:study가 그린다 | tab:활동 | - | - |
| AdminChallengerDetail | AdminChallengerDetail | :presentation:act | 운영진용 챌린저 상세. 활동 탭 Admin 스위치 → 챌린저 관리 → 항목인데, 스위치에 글자·설명이 없어 단계 이동 불가 | - | challengerId: 챌린저 관리 목록에서 자동 | 운영진 |
| AdminStudyGroupCreate | AdminStudyGroupCreate | :presentation:study | 스터디 그룹 생성. Admin 스위치 → 스터디 관리 → 스터디 그룹 생성하기(스위치 때문에 단계 이동 불가) | - | - | 운영진 |
| AdminStudyGroupSchedule | AdminStudyGroupSchedule | :presentation:study | 스터디 일정 등록. Admin 스위치 → 스터디 관리 → 그룹 카드의 스터디 일정 등록하기(스위치 때문에 단계 이동 불가) | - | 그룹 카드에서 자동 | 운영진, 관리 중인 스터디 그룹 |
| Notice | Notice | :presentation:notice | 공지 목록(기수 드롭다운·필터 칩) | tab:공지 | - | - |
| NoticeSearch | NoticeSearch | :presentation:notice | 공지 검색. 돋보기 아이콘에 글자·설명이 없어 단계 이동 불가 | - | 목록의 현재 기수·필터에서 자동 | - |
| AdminNotice | AdminNotice | :presentation:notice | 운영진 공지(권한별 탭). 방패 아이콘에 글자·설명이 없어 단계 이동 불가 | - | gisuId: 공지 탭의 기수(자동) | 운영진 |
| NoticeWrite | NoticeWrite | :presentation:notice | 공지 작성(noticeId가 있으면 수정) | tab:공지 > tap:공지 작성 | - | 운영진(MEMBER 외 역할) |
| NoticeDetail | NoticeDetail | :presentation:notice | 공지 상세(투표 포함) | tab:공지 > tap:{noticeTitle} | noticeTitle: 공지 목록에 보이는 제목 | - |
| Home | Home | :presentation:home | 홈(활동 요약, 다가오는 일정). 로그인하면 자동으로 들어온다 | start | - | - |
| Notification | Notification | :presentation:home | 알림 보관함. 홈 오른쪽 위 종 아이콘(content-desc) | tap:알림 | - | - |
| ScheduleAdd | ScheduleAdd | :presentation:home | 일정 생성. 다가오는 일정 옆 + 아이콘(content-desc). 기수 기록이 5개 이상이면 같은 설명의 아이콘이 위에 하나 더 있다(확인 필요) | tap:일정 추가 | - | - |
| ScheduleEdit | ScheduleEdit | :presentation:home | 일정 수정(ScheduleAdd와 같은 화면). 일정 상세 케밥에 글자·설명이 없어 단계 이동 불가 | - | scheduleId: 일정 상세에서 자동 | 일정 수정 권한(서버 판정) |
| ScheduleDetail | ScheduleDetail | :presentation:home | 일정 상세. 홈 일정 목록 항목(화면 밖이면 스크롤 필요, 확인 필요) | tap:{scheduleTitle} | scheduleTitle: 홈 다가오는 일정의 제목 | 일정이 있는 계정 |
| Mycard | Mycard | :presentation:mypage | MY 탭의 내 명함. umc://card?memberId=숫자로 열면 타인 명함(screen_name MemberCard) | tab:MY | - | - |
| MemberProfile | MemberProfile | :presentation:mypage | 타인 프로필(차단·신고). 스레드 참여자 관리 → 첫 참여자 메뉴 → 프로필 보기(팝업 메뉴가 덤프에 잡히는지 확인 필요) | deeplink:umc://thread?threadId={threadId} > tap:더보기 > tap:참여자 관리 > tap:참여자 관리 메뉴 > tap:프로필 보기 | threadId: 참여 중인 스레드 ID | 해당 스레드 참여자 |
| Mypage | Mypage | :presentation:mypage | 설정(외부 링크·문의·알림·소셜 연동·약관·로그아웃·탈퇴). MY 탭 톱니 아이콘(content-desc) | tab:MY > tap:Setting | - | - |
| MyContent | MyContent | :presentation:mypage | 내 활동(글·댓글·스크랩). 설정 화면이 이동 콜백을 부르지 않아 들어갈 곳이 없다 | - | - | - |
| MyProfile | MyProfile | :presentation:mypage | 프로필 정보 편집 | tab:MY > tap:프로필 정보 편집 | - | - |
| ActivityHistory | ActivityHistory | :presentation:mypage | 활동 이력(작은 화면에서는 스크롤 필요할 수 있음, 확인 필요) | tab:MY > tap:활동 이력 | - | - |
| Qrcode | Qrcode | :presentation:mypage | 내 QR 코드. 명함 교환을 임시로 막아 QR 버튼은 안내 다이얼로그만 띄운다(#380) | - | - | - |
| ReceivedCard | ReceivedCard | :presentation:mypage | 받은 명함. MY 탭 메뉴가 숨겨져 들어갈 곳이 없다 | - | - | - |
| Community | Community | :presentation:community | 커뮤니티 스레드 목록 | tab:커뮤니티 | - | - |
| CommunitySearch | CommunitySearch | :presentation:community | 스레드 검색. 돋보기 아이콘(content-desc) | tab:커뮤니티 > tap:검색 | - | - |
| CommunityCreate | CommunityCreate | :presentation:community | 스레드 만들기. 목록(또는 고른 카테고리)이 비어 있으면 떠 있는 새 스레드 버튼 대신 스레드 만들기 버튼이 보인다 | tab:커뮤니티 > tap:새 스레드 / 스레드 만들기 | - | - |
| CommunityEdit | CommunityEdit | :presentation:community | 스레드 편집. 채팅방 더보기 메뉴(OWNER·ADMIN만 보임) | deeplink:umc://thread?threadId={threadId} > tap:더보기 > tap:스레드 편집 | threadId: 내가 OWNER·ADMIN인 스레드 ID | 해당 스레드 OWNER·ADMIN |
| CommunityChatting | CommunityChatting | :presentation:community | 스레드 채팅방. 딥링크는 홈 도착 뒤 처리된다. 참여하지 않은 스레드는 보기 전용 | deeplink:umc://thread?threadId={threadId} | threadId: 커뮤니티에서 스레드를 연 뒤 net 요약의 community/threads/숫자 경로 | - |

## 메모

- 이미 그 화면에 있으면 `screen_view`가 다시 찍히지 않는다. `goto`는 목표 화면에 이미 있으면 바로 끝내고, 탭으로 시작하는 경로는 먼저 홈 탭으로 돌아간 뒤 출발한다.
- 딥링크 ID는 숫자만 받는다(`SupportedAppLinks.kt`). 외부 링크는 Home이 뜬 뒤 처리된다.
- 목록 항목(공지 제목, 일정 제목, MY 탭 아래 메뉴)은 화면에 보여야 덤프에 잡힌다. 안 보이면 스크롤한 뒤 `tap`으로 누른다.
- 글자·설명이 없는 아이콘(공지 방패·돋보기, 일정 상세 케밥, 활동 탭 Admin 스위치 등)에 contentDescription을 붙이면 `-`를 `tap:`으로 바꿀 수 있다.
