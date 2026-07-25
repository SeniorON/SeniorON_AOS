# 시니어 ON Android

부모님과 자녀를 가족 공유 코드로 연결해 부모님의 스마트폰 사용 환경, 건강 일정, 알림과 가족 사진을 함께 관리하는 Android 앱입니다.

26.07.26 - 현재 목데이터를 레포지토리에 연결해둠으로써 다양한 경우의 화면을 테스트 해볼 수 있습니다.

## 팀 구성

| 이름 | 역할 | 담당 영역 |
| --- | --- | --- |
| 원스톤 | Android Lead | 앱 구조, 디자인 시스템, 인증, 알림, 부모님 모드 |
| 데쿠 | Android | 화면 탭, 가족 탭 |
| 린린 | Android | 건강 탭, 설정 탭 |

## 주요 기능

### 공통·온보딩

- 스플래시 및 자녀/부모님 모드 선택
- 모드별 로그인과 계정 역할 검증
- 회원가입 및 이메일 인증
- 아이디·비밀번호 찾기
- 가족 공유 코드 입력·생성
- 부모님 정보 입력

### 자녀 모드

- 화면 탭: 부모님 폰 연결 상태, 글씨 및 런처 버튼 구성
- 건강 탭: 건강/병원 일정 확인 및 진료 일정 관리
- 알림 탭: 알림 현황, 알림 이력과 감지 시간 설정
- 가족 탭: 주담당자·보조담당자 관리 및 가족 사진 공유
- 설정 탭: 계정, 연결 기기, 문의, 로그아웃 및 회원 탈퇴

### 부모님 모드

- 큰 버튼 중심의 부모님 홈 화면
- 오늘 일정 확인
- AI 말벗 UI
- 가족 사진 목록·갤러리·사진 뷰어
- 복약 확인과 복약 알림 UI
- 긴급 알림 카운트다운
- 외부 링크 안전성 검사 UI
- 외부 앱 실행 및 미설치 앱의 Play 스토어 이동 구조

> 부모님 모드는 현재 일반 Android 화면에서 UI와 흐름을 검증하는 단계입니다. 기본 홈 앱 등록을 포함한 실제 런처 동작은 이후 단계에서 적용합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Repository 패턴, 수동 DI |
| Async | Kotlin Coroutines, StateFlow |
| Network | Retrofit 3, OkHttp 5, Gson |
| Image | Coil 3 |
| Android | minSdk 26, targetSdk 36, compileSdk 37 |
| Build | Gradle 9.3.1, Android Gradle Plugin 9.1.1, Java 11 |
| Test | JUnit 4, AndroidX Test, Espresso, Compose UI Test |

## 프로젝트 폴더 구조

```text
SENIOR_ON/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/example/senior_on/
│       │   │   ├── MainActivity.kt
│       │   │   ├── SeniorOnApplication.kt
│       │   │   ├── di/
│       │   │   │   └── AppContainer.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── address/
│       │   │   │   │   ├── auth/
│       │   │   │   │   ├── display/
│       │   │   │   │   ├── family/
│       │   │   │   │   ├── health/
│       │   │   │   │   ├── notification/
│       │   │   │   │   └── parent/
│       │   │   │   └── repository/
│       │   │   │       ├── auth/
│       │   │   │       ├── display/
│       │   │   │       ├── family/
│       │   │   │       ├── health/
│       │   │   │       ├── notification/
│       │   │   │       └── parent/
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   ├── remote/
│       │   │   │   │   ├── api/
│       │   │   │   │   ├── dto/
│       │   │   │   │   └── mapper/
│       │   │   │   └── repository/
│       │   │   │       ├── mock/
│       │   │   │       └── impl/
│       │   │   └── ui/
│       │   │       ├── app/
│       │   │       ├── onboarding/
│       │   │       │   ├── login/
│       │   │       │   ├── signup/
│       │   │       │   ├── findaccount/
│       │   │       │   └── familycode/
│       │   │       ├── child/
│       │   │       │   ├── display/
│       │   │       │   ├── health/
│       │   │       │   ├── notification/
│       │   │       │   ├── family/
│       │   │       │   └── settings/
│       │   │       ├── parent/
│       │   │       │   ├── component/
│       │   │       │   ├── schedule/
│       │   │       │   ├── chat/
│       │   │       │   ├── medication/
│       │   │       │   ├── photo/
│       │   │       │   ├── emergency/
│       │   │       │   └── link/
│       │   │       ├── common/
│       │   │       │   ├── account/
│       │   │       │   └── seniorinfo/
│       │   │       └── theme/
│       │   └── res/
│       ├── test/
│       └── androidTest/
├── docs/
│   ├── CODE_CONVENTION.md
│   ├── GITHUB_WORKFLOW.md
│   ├── MOCK_TEST_DATA.md
│   ├── SCREEN_FLOW.md
│   └── NOTION_PROJECT_GUIDE.md
└── gradle/
```

핵심 의존성은 다음 방향으로 유지합니다.

```text
UI / ViewModel
      ↓
domain 모델 + Repository 인터페이스
      ↑
data의 MockRepository 또는 실제 Repository 구현체
```

| 패키지 | 역할 |
| --- | --- |
| `domain/model` | UI와 데이터 구현에 독립적인 비즈니스 모델 |
| `domain/repository` | UI와 ViewModel이 의존하는 Repository 인터페이스 |
| `data/repository/mock` | 화면 시연과 테스트용 Repository 구현체 및 목데이터 |
| `data/repository/impl` | 실제 서버·로컬 데이터 소스를 사용하는 Repository 구현체 |
| `data/remote/api` | Retrofit API 인터페이스 및 네트워크 구성 |
| `data/remote/dto` | 서버 요청·응답 전용 모델 |
| `data/remote/mapper` | DTO와 도메인 모델 사이의 변환 |
| `data/local` | 파일, DataStore, Room 등 기기 내부 데이터 처리 |
| `di` | Repository와 의존 객체 생성 및 주입 |
| `ui/app` | 앱 진입과 최상위 화면 전환 |
| `ui/onboarding` | 로그인, 회원가입, 계정 찾기, 가족 공유 코드 |
| `ui/child` | 자녀 모드의 기능별 화면 |
| `ui/parent` | 부모님 홈과 부모님 전용 상세 화면 |
| `ui/common` | 여러 기능에서 재사용하는 공통 화면과 컴포넌트 |
| `ui/theme` | 색상, 글꼴, 그라데이션, 모서리 등 디자인 시스템 |

## 컨벤션 문서

- [CODE_CONVENTION.md](docs/CODE_CONVENTION.md): 브랜치, 커밋, 코드 네이밍, 패키지 구조 규칙
- [GITHUB_WORKFLOW.md](docs/GITHUB_WORKFLOW.md): 이슈, PR, 리뷰, 머지 규칙
- [MOCK_TEST_DATA.md](docs/MOCK_TEST_DATA.md): 화면 흐름 확인을 위한 목 계정과 입력값
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

주소 검색 기능을 확인하려면 프로젝트 루트의 `local.properties`에 Kakao REST API 키를 추가합니다.

## 화면 목록

| 화면 이름 | 스크린 ID | 진입 경로 | 담당자       |
| --- | --- | --- |-----------|
| 스플래시 화면 | SplashScreen | 앱 실행 | 원스톤       |
| 모드 선택 화면 | ModeSelectionScreen | 스플래시 이후 | 원스톤       |
| 로그인 화면 | LoginScreen | 모드 선택 후 | 원스톤       |
| 회원가입 시작 화면 | SignupScreen | 로그인 > 회원가입 | 공통        |
| 회원가입 모드 안내 화면 | SignupModeGuideScreen | 회원가입 시작 후 | 공통        |
| 이름·생년월일 입력 화면 | SignupNameBirthScreen | 회원가입 모드 안내 후 | 공통        |
| 이메일 인증 화면 | SignupEmailVerificationScreen | 이름·생년월일 입력 후 | 공통        |
| 계정 정보 입력 화면 | SignupAccountInfoScreen | 이메일 인증 후 | 공통        |
| 약관 동의 화면 | SignupTermsAgreementScreen | 계정 정보 입력 후 | 공통        |
| 가족 공유 코드 확인 화면 | FamilyShareCodeScreen | 가족 연결 흐름 | 공통        |
| 가족 공유 코드 입력 화면 | FamilyShareCodeInputScreen | 가족 공유 코드 있음 선택 후 | 공통        |
| 가족 공유 코드 생성 화면 | FamilyShareCodeCreatedScreen | 가족 공유 코드 없음 선택 후 | 공통        |
| 부모님 정보 입력 화면 | ParentInfoInputScreen | 가족 공유 코드 생성 후 | 공통        |
| 아이디·비밀번호 찾기 화면 | FindAccountScreen | 로그인 > 계정 찾기 | 공통        |
| 아이디 찾기 결과 화면 | FindIdResultScreen | 아이디 찾기 완료 후 | 공통        |
| 비밀번호 인증 화면 | FindPasswordVerifyScreen | 비밀번호 찾기 계정 확인 후 | 공통        |
| 비밀번호 재설정 화면 | FindPasswordResetScreen | 인증번호 확인 후 | 공통        |
| 자녀 메인 바텀 내비게이션 | ChildMainScreen | 자녀 모드 로그인 성공 후 | 원스톤       |
| 화면 탭 | DisplayTabScreen | 자녀 메인 > 화면 | 데쿠        |
| 기기 연결 화면 | DeviceConnectionScreen | 화면 탭 > 기기 연결 | 데쿠        |
| 버튼 추가 화면 | DisplayButtonAddScreen | 화면 탭 > 버튼 편집 | 데쿠        |
| 버튼 편집 화면 | DisplayButtonEditScreen | 화면 탭 > 버튼 편집 | 데쿠        |
| 버튼 순서 편집 화면 | DisplayButtonOrderScreen | 화면 탭 > 버튼 순서 편집 | 데쿠        |
| 글씨 편집 화면 | DisplayFontEditScreen | 화면 탭 > 글씨 편집 | 데쿠        |
| 건강·병원 탭 | HealthMainScreen | 자녀 메인 > 건강 | 린린        |
| 병원 일정 화면 | HospitalScreen | 건강 탭 > 병원 | 린린 -> 원스톤 |
| 진료 일정 추가·수정 화면 | HospitalAppointmentScreen | 병원 일정 > 진료 일정 | 린린 -> 원스톤 |
| 알림 탭 | NotificationScreen | 자녀 메인 > 알림 | 원스톤       |
| 알림 상세 화면 | NotificationDetailScreen | 알림 탭 > 알림 카드 | 원스톤       |
| 알림 이력 화면 | NotificationHistoryScreen | 알림 탭 > 지난 알림 | 원스톤       |
| 감지 시간 설정 화면 | NotificationDetectionTimeSettingScreen | 알림 탭 > 감지 시간 설정 | 원스톤       |
| 가족 탭 | FamilyTabScreen | 자녀 메인 > 가족 | 데쿠        |
| 가족 구성원 설정 화면 | FamilyMemberSettingsScreen | 가족 탭 > 구성원 설정 | 데쿠        |
| 가족 초대 화면 | FamilyInvitationScreen | 가족 탭 > 가족 추가 | 데쿠        |
| 가족 사진 목록 화면 | FamilyPhotoGalleryScreen | 가족 탭 > 사진 더보기 | 데쿠        |
| 가족 사진 상세 화면 | FamilyPhotoDetailScreen | 가족 사진 선택 | 데쿠        |
| 가족 사진 공유 화면 | FamilyPhotoShareScreen | 가족 탭 > 사진 올리기 | 데쿠        |
| 설정 탭 | SettingsScreen | 자녀 메인 > 설정 | 린린        |
| 내 계정 화면 | MyAccountScreen | 설정 > 내 계정 | 린린        |
| 연결 기기 화면 | ConnectedDevicesScreen | 설정 > 연결 기기 | 린린        |
| 도움말·문의 화면 | HelpInquiryScreen | 설정 > 도움말 및 문의 | 린린        |
| 일대일 문의 화면 | OneOnOneInquiryScreen | 도움말 및 문의 > 일대일 문의 | 린린        |
| 부모님 홈 화면 | ParentLauncherScreen | 부모님 모드 로그인 성공 후 | 원스톤       |
| 부모님 일정 화면 | ParentScheduleScreen | 부모님 홈 > 일정 | 원스톤       |
| 부모님 말벗 화면 | ChatBuddyScreen | 부모님 홈 > 말벗 | 원스톤       |
| 부모님 복약 화면 | ParentMedicationScreen | 부모님 홈 > 복약 | 원스톤       |
| 가족 사진 구성원 화면 | ParentFamilyMembersPhotoScreen | 부모님 홈 > 사진 > 가족이 보낸 사진 | 원스톤       |
| 구성원별 사진 목록 화면 | ParentMemberPhotoGridScreen | 가족 사진 구성원 선택 | 원스톤       |
| 부모님 사진 뷰어 | ParentPhotoViewerScreen | 사진 선택 | 원스톤       |
| 부모님 긴급 알림 화면 | ParentEmergencyAlertScreen | 부모님 홈 > 긴급 알림 | 원스톤       |
| 링크 안전성 검사 화면 | ParentLinkDetectionScreen | 링크 검사 테스트 버튼 | 원스톤       |

## 화면 플로우

앱 진입:

```text
앱 실행
-> 스플래시
-> 모드 선택
-> 자녀 모드 또는 부모님 모드 선택
-> 로그인 / 회원가입
```

로그인:

```text
자녀 모드 + 자녀 계정
-> 자녀 메인 화면

부모님 모드 + 부모님 계정
-> 부모님 홈 화면

선택 모드와 다른 계정
-> 계정 역할 불일치 안내
```

회원가입:

```text
회원가입 시작
-> 회원가입 모드 안내
-> 이름/생년월일 입력
-> 이메일 인증
-> 아이디 중복 확인 및 비밀번호 입력
-> 약관 동의
-> 로그인 화면
```

가족 공유 코드:

```text
가족 공유 코드 확인
-> 있는 경우: 가족 공유 코드 입력
-> 없는 경우: 가족 공유 코드 생성 -> 부모님 정보 입력
```

현재 회원가입 완료 후에는 로그인 화면으로 이동합니다. 가족 공유 코드 화면은 구현되어 있으며 메인 인증 흐름과의 최종 연결은 추후 확정합니다.

자녀 메인:

```text
자녀 메인
-> 화면 탭
-> 건강 탭
-> 알림 탭
-> 가족 탭
-> 설정 탭
```

부모님 모드:

```text
부모님 홈
-> 일정
-> 말벗
-> 복약
-> 가족 사진
-> 긴급 알림
-> 링크 안전성 검사
-> 외부 앱 실행
```

상세 화면 플로우는 [SCREEN_FLOW.md](docs/SCREEN_FLOW.md)를 참고합니다.

## 목데이터 테스트

로그인:

| 선택 모드 | 아이디 | 비밀번호 | 주요 시나리오 |
| --- | --- | --- | --- |
| 자녀 | `child` | `child1234` | 주담당자, 최근 알림이 많은 상태 |
| 자녀 | `child01` | `senioron1` | 보조담당자, 알림이 없는 상태 |
| 부모님 | `senior` | `senior1234` | 부모님 화면 진입 |

인증 및 가족 코드:

| 항목 | 성공 입력값 |
| --- | --- |
| 회원가입 이메일 인증번호 | `111111` |
| 비밀번호 찾기 인증번호 | `123456` |
| 가족 공유 코드 | `43TS6GTE` 또는 `43TS-6GTE` |

실패 사례와 회원가입·아이디 찾기용 전체 입력값은 [MOCK_TEST_DATA.md](docs/MOCK_TEST_DATA.md)를 참고합니다.

## GitHub 협업 방식

기본 흐름:

```text
이슈 생성
-> develop 최신화
-> 작업 브랜치 생성
-> 작업 및 로컬 검증
-> 커밋과 Push
-> develop 대상 Pull Request
-> 리뷰와 빌드 확인
-> Merge
-> 로컬 develop 최신화
```

1. 작업 전 이슈를 생성합니다.
2. 로컬 `develop` 브랜치를 최신 상태로 갱신합니다.
3. 이슈에 맞는 작업 브랜치를 생성합니다.
4. 작업 후 빌드와 테스트를 확인하고 커밋합니다.
5. 원격 작업 브랜치로 Push한 뒤 `develop` 대상 Pull Request를 생성합니다.
6. PR 본문에 `Closes #이슈번호`를 작성해 머지 시 이슈가 자동 종료되도록 합니다.
7. 리뷰 승인과 빌드 확인 후 머지합니다.
8. 머지 후 로컬 `develop`을 다시 최신화하고 다음 작업 브랜치를 생성합니다.

브랜치 역할:

| 브랜치 | 역할 |
| --- | --- |
| `main` | 배포 가능한 안정 버전 |
| `develop` | 기능 통합과 다음 배포 준비 |
| 작업 브랜치 | 기능, 버그, 디자인, 문서, 리팩터링 단위 작업 |

브랜치 이름:

| 유형 | 규칙 | 예시 |
| --- | --- | --- |
| 기능 | `feat/<short-name>` | `feat/family-photo-screen` |
| 버그 수정 | `fix/<short-name>` | `fix/health-header-inset` |
| 디자인 | `design/<short-name>` | `design/color-token-add` |
| 리팩터링 | `refactor/<short-name>` | `refactor/project-package-structure` |
| 문서 | `docs/<short-name>` | `docs/update-readme` |
| 테스트 | `test/<short-name>` | `test/family-repository` |
| 설정·잡무 | `chore/<short-name>` | `chore/update-gradle` |

커밋 메시지:

```text
type: 변경 내용
```

| 타입 | 의미 |
| --- | --- |
| `feat` | 사용자에게 보이는 기능 추가 |
| `fix` | 버그 수정 |
| `design` | 디자인 시스템 또는 UI 스타일 변경 |
| `refactor` | 기능 변화 없는 구조 개선 |
| `test` | 테스트 추가 또는 수정 |
| `docs` | 문서 변경 |
| `chore` | 빌드, 설정, 템플릿 등 |

작업 브랜치 생성:

```bash
git switch develop
git pull origin develop
git switch -c feat/short-description
```

작업 완료:

```bash
git status
git add .
git commit -m "feat: 변경 내용"
git push -u origin feat/short-description
```

PR 머지 후:

```bash
git switch develop
git pull origin develop
git branch -d feat/short-description
```

작업 중 원격 `develop`의 변경사항을 반영해야 할 경우 먼저 현재 작업을 커밋하거나 stash한 뒤 병합합니다.

```bash
git fetch origin
git merge origin/develop
```

충돌을 해결한 뒤 양쪽 변경사항이 모두 반영됐는지 확인하고 다시 빌드합니다. 공유 중인 브랜치에는 팀 협의 없이 강제 Push하지 않습니다.

Pull Request 제목은 커밋 제목과 동일하게 작성합니다.

Pull Request에는 다음 내용을 포함합니다.

- 변경 내용 요약
- 관련 이슈 번호
- 화면 변경 시 변경 전·후 스크린샷 또는 화면 녹화
- 실행한 테스트 또는 수동 확인 내용
- 시스템 UI, 작은 화면, 큰 글씨 설정 고려 사항
- 리뷰어가 알아야 할 미완료 사항이나 위험 요소
