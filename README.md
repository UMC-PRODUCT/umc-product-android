# 프로젝트 이름
<img width="2048" height="1365" alt="배너 이미지 및 로고" src="" />

   

> 애플리케이션에 대한 한 줄 소개   

   
 
## 📝 프로젝트 설명
<img width="1920" height="1080" alt="온보딩 이미지" src="" />




## 👥 팀원 소개
| **김도연(도리)** | **박유수(어헛차)** | **양지애(나루)** | **조경석(조나단)** |
| :------: |  :------: | :------: | :------: |
| <img width="150" height="150" alt="image" src="https://avatars.githubusercontent.com/u/147322566?v=4" /> | <img width="150" height="150" alt="image" src="https://avatars.githubusercontent.com/u/57581969?v=4" /> | <img width="150" height="150" alt="image" src="https://avatars.githubusercontent.com/u/140368421?v=4" /> | <img width="150" height="150" alt="image" src="https://avatars.githubusercontent.com/u/83599356?v=4" /> |
| [@kimdoyeon1234](https://github.com/kimdoyeon1234) | [@Park-yu-su](https://github.com/Park-yu-su) | [@yangjiae12](https://github.com/yangjiae12) | [@rudtjr1106](https://github.com/rudtjr1106) |
  
## 🛠️ 기술 스택

### 🛠 Environment
<img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white"> <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white"> <img src="https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white">

### 💻 Development
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white) <img src="https://img.shields.io/badge/Retrofit2-orange?style=for-the-badge&logoColor=white"> <img src="https://img.shields.io/badge/Hilt-0078D4?style=for-the-badge&logo=google&logoColor=white"> <img src="https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=android&logoColor=white"> ![Firebase](https://img.shields.io/badge/firebase-a08021?style=for-the-badge&logo=firebase&logoColor=ffcd34)

### 🤝 Communication
<img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white"> <img src="https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white"> <img src="https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white"> <img src="https://img.shields.io/badge/Trello-0052CC?style=for-the-badge&logo=trello&logoColor=white">



## 🌿 브랜치 컨벤션

**기본 브랜치**: `master`(배포), `develop`(개발)  
**전략**: 모든 작업은 `master` / `develop` 브랜치에서 파생된 기능별 하위 브랜치에서 진행하며, 브랜치명은 `태그/#이슈번호` 형식을 따른다.
  - 예: `feat/#2`, `ui/#15`

   
**💡 태그 종류**
| 태그 | 설명 |
| :--- | :--- |
| **master** | 애플리케이션 배포 브랜치 |
| **develop** | 개발을 위한 통합 브랜치 |
| **feat** | 새로운 기능 개발(로직 포함) |
| **ui** | 레이아웃 및 UI 작업 |
| **fix** | 버그 수정 및 에러 해결|
| **refactor** | 코드 리팩토링 |
| **chore** | 빌드 설정 및 라이브러리 관리 |

## 📌 이슈 컨벤션
  
이슈는 템플릿 형태로 주어지며, 아래 내용을 작성한다.
- `기능 개요`: 구현하고자 하는 기능을 요약해 작성
- `상세 내용`: 기능과 관련된 추가적인 정보를 작성
- `작업 체크리스트`: 작업 간 수행 여부 작성 및 체크
- `참고 사항`: 외부 문서나 추후 작업 시 참고 사항 작성
   
<br>

**제목 형식**: `[태그] 작업 제목`

- 예: `[UI] 온보딩 화면 UI 구현`
   

**💡 태그 종류 (커밋/PR 컨벤션과 동일)**
| 태그 | 설명 |
| :--- | :--- |
| **[Feat]** | 새로운 기능 추가 및 로직 구현 |
| **[UI]** | XML, Drawable, UI 컴포넌트 등 디자인 작업 |
| **[Fix]** | 버그 수정 및 예외 처리 |
| **[Data]** | API 연결, DTO 구성, 로컬 DB 등 데이터 관련 작업 |
| **[Refactor]** | 리팩토링 및 파일/변수 이름 변경 |
| **[Merge]** | 브랜치 머지 및 충돌 해결 |
| **[Chore]** | 빌드 설정, 라이브러리 관리 등 기타 

## ✅ PR 컨벤션
PR 작성 시 탬플릿 형태가 주어지며, 아래 내용을 작성한다.
 - `작업 내용`: 이번 PR에서 작업한 내용을 간단히 요약
 - `리뷰 요구사항`: 리뷰어가 중점적으로 봐주었으면 하는 부분 작성
 - `체크리스트`: 코드에 이상 없는지 확인 및 체크
 - `스크린샷(선택)`: 작업한 화면 스크린샷


<br>

**제목 형식**: `[태그/#이슈번호] 작업 제목`

- 예: `[Feat/#2] 온보딩 화면 구현`


## 🏷️ 커밋 컨벤션

커밋 메시지는 `[태그/#이슈번호] 작업 내용` 형식을 권장한다.
   
- 예: `[Feat/#2] 온보딩 화면 UI 구현`

| 태그 | 설명 |
| :--- | :--- |
| **[Feat]** | 새로운 기능 추가 및 로직 구현 |
| **[UI]** | XML, Drawable, UI 컴포넌트 등 디자인 작업 |
| **[Fix]** | 버그 수정 및 예외 처리 |
| **[Data]** | API 연결, DTO 구성, 로컬 DB 등 데이터 관련 작업 |
| **[Refactor]** | 리팩토링 및 파일/변수 이름 변경 |
| **[Merge]** | 브랜치 머지 및 충돌 해결 |
| **[Chore]** | 빌드 설정, 라이브러리 관리 등 기타 환경 설정 |


## 💻 코드 컨벤션
### 1. UI 네이밍 규칙 (Snake Case)
* **XML ID**: `화면명_위젯명_기능` 순으로 작성한다.

    (예: `main_btn_back`)
* **Layout**: `type_name.xml` 

    (예: `activity_main.xml`)
* **Drawable**: `ic_(icon명)`, `bg_(background명)`

    (예: `ic_back_24dp.xml`)

### 2. Kotlin 네이밍 규칙
**Ktlint**을 통해 안드로이드 공식 스타일 가이드를 준수하며, 자동 점검한다.
* **Class / Interface**: `PascalCase` 

    (예: `UserViewModel`)
* **함수 / 변수**: `camelCase` 

    (예: `getUserId()`)
* **상수**: `UPPER_SNAKE_CASE` 
    
    (예: `BASE_URL`)


## 📂 폴더 컨벤션

```text
com.project.name
├── di              # Hilt Module 주입
├── extension       # 확장 함수 정의 (ex. String.toJson())
├── data            # 데이터 로직 처리 (Data Layer)
│   ├── api         # API Interface
│   ├── base        # Data 모듈 베이스 코드
│   ├── dto         # Request / Response 데이터 객체
│   ├── dataSource  # Local(Room, DataStore), Remote 인터페이스
│   │   ├── local   # dataSource의 Local implementation (ex. RoomDB)
│   │   └── remote  # dataSource의 Network implementation
│   └── repository  # Domain Repository Implementation
├── domain          # 비즈니스 로직 (Domain Layer)
│   ├── repository  # Repository Interface
│   ├── model       # UI 전달용 실제 Data Class
│   └── usecase     # Repository 호출을 위한 UseCase
└── presentation    # UI 레이어 (Presentation Layer)
    ├── base        # 공통 내용 정의 (BaseFragment, ViewModel)
    └── ui          # UI 구현 (Activity, Fragment, Adapter)
