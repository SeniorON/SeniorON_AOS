# 시니어 ON Android

부모님과 자녀를 가족공유코드로 연결해 부모님의 스마트폰 사용 환경, 건강 정보, 알림, 가족 공유 기능을 관리하는 Android 앱입니다.

## 프로젝트 소개

시니어 ON은 자녀 모드와 부모님 모드를 제공하는 디지털 케어 서비스입니다.

- 자녀 모드: 일반 앱 형태로 동작하며 화면, 건강, 알림, 가족, 설정 기능을 관리합니다.
- 부모님 모드: 런처 앱 형태로 동작하며 큰 버튼과 단순한 흐름으로 스마트폰 사용을 돕습니다.
- 가족공유코드: 가족 구성원, 건강 정보, 알림 이벤트, 가족 사진 공유를 연결하는 기준입니다.

## 팀원 소개 및 역할 분담

| 이름 | 역할 | 담당 영역 |
| --- | --- | --- |
| 원스톤 | Android Lead | 앱 구조, 디자인 시스템, 인증 구조, 알림 탭, 부모님 런처 구조 |
| 데쿠 | Android | 화면 탭(화면 지원), 가족 탭 |
| 린린 | Android | 건강 탭, 설정 탭 |

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material3 |
| Architecture | MVVM 예정 |
| 주요 라이브러리 | AndroidX Core KTX, Lifecycle Runtime KTX, Activity Compose, Compose BOM, Compose UI, Compose Material3 |
| 도입 예정 | Kotlin Coroutines, Retrofit/OkHttp, Room DB |

## 프로젝트 폴더 구조

```text
SENIOR_ON/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
├── app/
│   └── src/main/
│       ├── java/com/example/senior_on/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   ├── domain/
│       │   ├── ui/
│       │       ├── app/
│       │       ├── auth/
│       │       ├── child/
│       │       ├── parent/
│       │       ├── component/
│       │       └── theme/
│       │   └── util/
│       └── res/
├── docs/
│   ├── CODE_CONVENTION.md
│   ├── GITHUB_WORKFLOW.md
│   ├── SCREEN_FLOW.md
│   └── NOTION_PROJECT_GUIDE.md
└── gradle/
```

`data`, `domain`, `util` 패키지는 API 연동, 비즈니스 모델, 공통 유틸이 추가될 때 사용할 예정 구조입니다.

## 컨벤션 문서

- [CODE_CONVENTION.md](docs/CODE_CONVENTION.md): 브랜치, 커밋, 코드 네이밍, 패키지 구조 규칙
- [GITHUB_WORKFLOW.md](docs/GITHUB_WORKFLOW.md): 이슈, PR, 리뷰, 머지 규칙
- [SCREEN_FLOW.md](docs/SCREEN_FLOW.md): 화면 목록과 상세 플로우
- [NOTION_PROJECT_GUIDE.md](docs/NOTION_PROJECT_GUIDE.md): 노션 공유용 프로젝트 문서

## 실행 기준

기본 확인 환경:

| 항목 | 기준 |
| --- | --- |
| IDE | Android Studio |
| 기준 에뮬레이터 | Pixel 8 |
| 화면 기준 | 360 x 800dp에 가까운 일반 Android 세로 화면 |
| 확인 방향 | 상태바, 내비게이션바, 작은 화면, 큰 글씨 설정을 함께 고려 |

## 화면 목록

| 화면 이름 | 스크린 ID | 진입 경로 | 담당자 |
| --- | --- | --- | --- |
| 스플래시 화면 | SplashScreen | 앱 실행 | 원스톤 |
| 모드 선택 화면 | ModeSelectionScreen | 스플래시 이후 | 원스톤 |
| 로그인 화면 | LoginScreen | 모드 선택 후 | 원스톤 |
| 회원가입 시작 화면 | SignUpStartScreen | 로그인 > 회원가입 | 공통 |
| 회원가입 정보 입력 화면 | SignUpProfileScreen | 회원가입 시작 후 | 공통 |
| 이메일 인증 화면 | EmailVerificationScreen | 회원가입 정보 입력 후 | 공통 |
| 계정 정보 입력 화면 | SignUpAccountScreen | 이메일 인증 후 | 공통 |
| 약관 동의 화면 | TermsAgreementScreen | 계정 정보 입력 후 | 공통 |
| 가족공유코드 확인 화면 | FamilyShareCodeCheckScreen | 로그인/회원가입 후 | 공통 |
| 가족공유코드 입력 화면 | FamilyShareCodeInputScreen | 가족공유코드 있음 선택 후 | 공통 |
| 가족공유코드 생성 화면 | FamilyShareCodeCreateScreen | 가족공유코드 없음 선택 후 | 공통 |
| 부모님 정보 입력 화면 | ParentInfoInputScreen | 가족공유코드 생성 후 | 공통 |
| 아이디 찾기 화면 | FindIdScreen | 로그인 > 아이디 찾기 | 공통 |
| 비밀번호 찾기 화면 | FindPasswordScreen | 로그인 > 비밀번호 찾기 | 공통 |
| 비밀번호 재설정 화면 | ResetPasswordScreen | 비밀번호 인증 완료 후 | 공통 |
| 자녀 메인 바텀네비 구조 | ChildMainScreen | 자녀 모드 진입 후 | 원스톤 |
| 화면 탭(화면 지원) | DisplayTabScreen | 자녀 메인 > 화면 | 데쿠 |
| 버튼 편집 화면 | ParentButtonEditScreen | 화면 탭 > 버튼 편집 | 데쿠 |
| 글씨 편집 화면 | ParentFontEditScreen | 화면 탭 > 글씨 편집 | 데쿠 |
| 건강 탭 | HealthTabScreen | 자녀 메인 > 건강 | 린린 |
| 복약 관리 화면 | MedicationManageScreen | 건강 탭 > 복약 관리 | 린린 |
| 약 추가 화면 | MedicationAddScreen | 복약 관리 > 약 추가 | 린린 |
| 병원 일정 화면 | HospitalScheduleScreen | 건강 탭 > 병원 | 린린 |
| 진료 추가 화면 | HospitalScheduleAddScreen | 병원 일정 > 진료 등록 | 린린 |
| 알림 탭 | NotificationTabScreen | 자녀 메인 > 알림 | 원스톤 |
| 알림 상세 화면 | AlertDetailScreen | 알림 탭 > 알림 카드 | 원스톤 |
| 무활동 감지 시간 설정 화면 | InactivityTimeSettingScreen | 알림 탭 > 무활동 감지 | 원스톤 |
| 가족 탭 | FamilyTabScreen | 자녀 메인 > 가족 | 데쿠 |
| 구성원 설정 화면 | FamilyMemberSettingScreen | 가족 탭 > 구성원 설정 | 데쿠 |
| 가족 사진 공유 화면 | FamilyPhotoShareScreen | 가족 탭 > 사진 더보기 | 데쿠 |
| 설정 탭 | SettingsTabScreen | 자녀 메인 > 설정 | 린린 |
| 자녀 프로필 설정 화면 | ChildProfileEditScreen | 설정 탭 > 내 계정 | 린린 |
| 비밀번호 변경 화면 | PasswordChangeScreen | 프로필 설정 > 비밀번호 | 린린 |
| 부모님 런처화면 | ParentLauncherScreen | 부모님 모드 진입 후 | 원스톤 |

## 화면 플로우

앱 진입:

```text
앱 실행
-> 스플래시
-> 모드 선택
-> 자녀 모드 또는 부모님 모드 선택
-> 로그인 / 회원가입
-> 가족공유코드 확인
```

회원가입:

```text
회원가입 시작
-> 이름/생년월일 입력
-> 이메일 인증
-> 아이디 중복 확인
-> 비밀번호 입력
-> 약관 동의
-> 가족공유코드 확인
```

가족공유코드:

```text
가족공유코드 확인
-> 있는 경우: 가족공유코드 입력 -> 메인 화면
-> 없는 경우: 가족공유코드 생성 -> 부모님 정보 입력 -> 메인 화면
```

자녀 메인:

```text
자녀 메인
-> 화면 탭(화면 지원)
-> 건강 탭
-> 알림 탭
-> 가족 탭
-> 설정 탭
```

부모님 모드:

```text
부모님 모드
-> 가족공유코드 연결
-> 부모님 런처화면
```

상세 화면 플로우는 [SCREEN_FLOW.md](docs/SCREEN_FLOW.md)를 참고합니다.

## GitHub 협업 방식

1. 작업 전 이슈를 생성합니다.
2. 이슈에 맞는 브랜치를 생성합니다.
3. 작업 후 Pull Request를 생성합니다.
4. PR 본문에 `Closes #이슈번호`를 작성해 머지 시 이슈가 자동 종료되도록 합니다.
5. 리뷰 승인과 빌드 확인 후 머지합니다.
