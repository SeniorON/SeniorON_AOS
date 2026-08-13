# 시니어ON Android

시니어ON은 자녀(보호자)와 시니어를 가족 공유 코드로 연결해 홈 화면, 건강 일정, 안전 알림과 가족 사진을 함께 관리하는 Android 애플리케이션입니다.

- **자녀(보호자) 모드**: 시니어 기기·홈 화면·복약·병원 일정·안전 알림·가족 정보를 관리합니다.
- **시니어 모드**: 큰 글씨와 간편한 버튼을 제공하는 런처형 홈 화면으로 동작합니다.

현재 앱은 실제 서버 API를 기본으로 사용합니다. Compose Preview와 일부 화면 시연 데이터는 Mock fixture를 사용하며, 말벗 기능은 프론트 API 연동이 완료되지 않아 데모 데이터로 동작합니다.

## 팀 구성 및 구현 담당

| 이름 | 역할 | 주요 구현 내용 |
| --- | --- | --- |
| 원스톤 | Android Lead | 앱 아키텍처와 패키지 구조<br>공통 디자인 시스템과 Compose 컴포넌트<br>스플래시·모드 선택·로그인과 인증 세션<br>자녀 메인 내비게이션<br>알림 탭·알림 내역·알림 상세·감지 시간 설정<br>건강 탭 구조 리팩터링과 병원 일정 연동 보완<br>시니어 런처 홈·일정·복약·가족 사진·SOS·위험 링크<br>FCM, 기기 상태, Geofence, 외출·귀가와 무활동 감지 |
| 데쿠 | Android | 자녀 화면 탭<br>시니어 기기 연결 상태와 홈 화면 미리보기<br>홈 버튼 추가·수정·삭제·순서 변경<br>글씨 크기 편집<br>가족 탭과 가족 구성원 관리<br>가족 초대·공유 코드<br>가족 사진 목록·상세·업로드 |
| 린린 | Android | 자녀 건강 탭 초기 화면과 복약 UI<br>자녀 설정 탭<br>내 계정과 프로필 관리<br>연결 기기 관리<br>도움말·FAQ·1:1 문의<br>문의 내역과 계정 설정 |

공통 온보딩의 회원가입, 이메일 인증, 계정 찾기, 가족 공유 코드와 시니어 정보 입력 화면은 팀 공통 작업으로 구현하고 이후 API 연동과 흐름을 통합했습니다.

## 주요 기능

### 공통·온보딩

- 자녀/시니어 모드 선택과 계정 역할 검증
- 일반 회원가입, 이메일 인증, 카카오·구글 소셜 로그인/가입
- 아이디 찾기, 비밀번호 재설정
- 가족 공유 코드 생성·참여
- 로그인 후 온보딩 상태에 따른 화면 분기
- 주 담당자 시니어 정보 등록과 보조 담당자 관계 등록
- 액세스·리프레시 토큰 기반 로그인 세션 복구

### 자녀(보호자) 모드

- **화면 탭**: 시니어 기기 상태, 홈 화면 미리보기, 글씨 크기와 홈 버튼 편집
- **건강 탭**: 일별·월별 복약 현황, 약 등록·수정·삭제, 병원 일정 관리
- **알림 탭**: SOS, 무활동, 위험 링크, 외출·귀가 알림과 상세 위치 확인
- **가족 탭**: 가족 구성원·담당자 관리, 가족 코드 공유, 사진 업로드와 앨범
- **설정 탭**: 프로필, 계정, 연결 기기, FAQ, 1:1 문의, 로그아웃·회원 탈퇴
- FCM 시스템 알림 선택 시 해당 탭 또는 알림 상세 화면으로 이동

### 시니어 모드

- 기본 홈 앱으로 사용할 수 있는 런처형 홈 화면
- 시간·날씨·오늘 병원 일정과 서버에서 구성한 홈 버튼 표시
- 전화, 메시지, 카메라, 사진과 설치된 외부 앱 실행
- 홈 화면 당겨서 새로고침
- 오늘 복약 목록과 복약 완료 처리
- 복약 시간 FCM 알림과 안내 모달
- 가족 구성원별 사진 앨범과 사진 확인 처리
- 5초 카운트다운 SOS와 위치·배터리 전송
- 외부 링크 안전성 검사 후 안전 링크만 브라우저로 전달
- Geofence 기반 외출·귀가 감지와 외출 중 위치 갱신
- 화면 잠금 해제·런처 조작 기반 무활동 감지
- 재부팅 후 런처 관련 상태·Geofence·백그라운드 작업 복구

## 아키텍처

MVVM과 Repository 패턴을 기반으로 UI, 도메인, 데이터 계층을 분리합니다. 의존성은 `AppContainer`에서 생성해 수동으로 주입합니다.

```text
Compose Screen
      │ event / UiState
      ▼
Route ── ViewModel
             │
             ▼
      Repository interface     domain/
             │
             ▼
      Repository implementation
             │
      ┌──────┴────────┐
      ▼               ▼
Remote DataSource   Local/Android DataSource
      │               │
Retrofit API      Keystore, Firebase, Location,
                  WorkManager, PackageManager
```

### 데이터 처리 원칙

- Retrofit 응답 DTO를 Repository에서 도메인/UI 모델로 변환합니다.
- ViewModel은 `StateFlow` 기반 `UiState`와 일회성 UI 이벤트를 제공합니다.
- 화면은 Repository나 Retrofit API를 직접 호출하지 않습니다.
- 로딩·성공·실패 상태를 UI에 반영하고 저장·수정·삭제 중 중복 요청을 차단합니다.
- 화면 최초 진입 갱신은 기존 콘텐츠를 유지하고, 사용자의 당겨서 새로고침은 별도 진행 상태를 표시합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3, Compose BOM 2025.08.00 |
| Architecture | MVVM, Repository pattern, manual DI |
| Async | Kotlin Coroutines 1.10.2, Flow, StateFlow |
| Network | Retrofit 3.0.0, OkHttp 5.3.0, Gson |
| Authentication | Firebase Authentication, Credential Manager, Kakao SDK |
| Push | Firebase Cloud Messaging |
| Image | Coil 3.3.0 |
| Map·Location | Kakao Maps SDK, Kakao Local API, Google Play Services Location |
| Background | WorkManager, Foreground Service, Geofencing API |
| Android | minSdk 26, targetSdk 36, compileSdk 37 |
| Build | Gradle 9.3.1, Android Gradle Plugin 9.1.1, Java 11 |
| Test | JUnit 4, AndroidX Test, Espresso, Compose UI Test |

## 프로젝트 구조

```text
app/src/main/java/com/example/senior_on/
├── SeniorOnApplication.kt       # 앱 초기화와 런타임 의존성 구성
├── MainActivity.kt              # 공통·자녀 모드 진입점
├── data/
│   ├── local/                   # 토큰 암호화 저장, 파일 전처리
│   ├── remote/
│   │   ├── api/                 # Retrofit API와 네트워크 클라이언트
│   │   ├── dto/                 # 서버 요청·응답 모델
│   │   ├── interceptor/         # 인증, 토큰 갱신, 로그 마스킹
│   │   └── mapper/              # DTO 변환
│   ├── repository/impl/         # Repository 구현체
│   └── source/                  # Remote·Android·Mock DataSource
├── domain/
│   ├── model/                   # 도메인 모델
│   └── repository/              # Repository 인터페이스
├── di/AppContainer.kt           # 수동 의존성 주입
├── notification/                # FCM 수신, 시스템 알림, 토큰 동기화
├── device/                      # 기기 상태·무활동 감지 Worker
├── location/tracking/           # Geofence와 외출 중 위치 추적
└── ui/
    ├── onboarding/              # 인증과 로그인 후 온보딩
    ├── child/                   # 화면·건강·알림·가족·설정 탭
    ├── parent/                  # 시니어 런처와 상세 기능
    ├── common/                  # 공통 컴포넌트와 외부 동작
    └── theme/                   # 색상·타이포그래피·디자인 토큰
```

UI 기능은 일반적으로 다음 단위로 나눕니다.

```text
FeatureScreen.kt          순수 Compose UI
FeatureComponents.kt      기능 전용 재사용 컴포넌트
route/FeatureRoute.kt     ViewModel 연결과 화면 전환
viewmodel/FeatureViewModel.kt
```

## 네트워크와 인증

- 서버 기본 주소: `https://senioron.site/`
- 일반 API 요청에는 저장된 액세스 토큰을 자동으로 첨부합니다.
- 인증 오류 발생 시 OkHttp `Authenticator`가 리프레시 토큰으로 토큰 갱신을 한 번 수행하고 원래 요청을 재시도합니다.
- 리프레시 토큰까지 유효하지 않으면 세션을 정리하고 모드에 맞는 화면으로 안내합니다.
- 토큰은 Android Keystore 기반 암호화를 적용하며 백업·데이터 추출 대상에서 제외합니다.
- HTTP 로그의 비밀번호, 인증번호, 토큰, FCM 토큰과 기기 식별자는 마스킹합니다.

## 알림과 백그라운드 동작

- `FirebaseMessagingService`가 FCM 메시지와 새 FCM 토큰을 수신합니다.
- 새 FCM 토큰은 로그인 세션이 있을 때 서버로 동기화하고, 실패하면 WorkManager로 재시도합니다.
- 시니어 모드가 화면에 표시되는 동안 기기 상태를 5분 간격으로 갱신합니다.
- 백그라운드에서는 WorkManager의 최소 주기인 15분 간격으로 기기 상태와 무활동 조건을 확인합니다.
- 외출 경계는 집 기준 1km, 귀가 경계는 800m로 구분해 경계 부근의 반복 알림을 줄입니다.
- 외출 상태에서는 위치를 5분 간격으로 서버에 갱신합니다.

## 실행 준비

### 요구 환경

- Android Studio
- JDK 11
- Android SDK 37
- Google Play 서비스가 포함된 에뮬레이터 또는 Android 실기기

### 1. 로컬 키 설정

프로젝트 루트의 `local.properties`에 Android SDK 경로와 Kakao 키를 추가합니다.

```properties
sdk.dir=C\:\\Users\\<USER>\\AppData\\Local\\Android\\Sdk
KAKAO_REST_API_KEY=<KAKAO_REST_API_KEY>
KAKAO_NATIVE_APP_KEY=<KAKAO_NATIVE_APP_KEY>
```

- `KAKAO_REST_API_KEY`: 시니어 주소 검색과 좌표 변환
- `KAKAO_NATIVE_APP_KEY`: 카카오 로그인·공유·지도 SDK 초기화
- Kakao Developers에 패키지명 `com.example.senior_on`과 개발·배포 서명 키 해시를 등록해야 합니다.

`local.properties`와 API 키는 Git에 커밋하지 않습니다.

### 2. Firebase 설정

Firebase 프로젝트에서 Android 앱 `com.example.senior_on`을 등록하고 다음 파일을 배치합니다.

```text
app/google-services.json
```

Firebase Authentication에서 Google 로그인을 활성화하고 개발·배포 SHA 인증서 지문을 등록합니다. `google-services.json`은 저장소에 커밋하지 않습니다.

### 3. 빌드와 테스트

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
```

macOS/Linux:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

## 주요 권한과 시스템 설정

| 항목 | 사용 목적 |
| --- | --- |
| 알림 | 복약, 병원 일정, SOS와 안전 이벤트 수신 |
| 위치·백그라운드 위치 | SOS 위치, 지도, 외출·귀가 감지와 위치 갱신 |
| 포그라운드 서비스 | 외출 중 위치 추적 |
| 부팅 완료 수신 | 재부팅 후 Geofence와 백그라운드 작업 복구 |
| 기본 홈 앱 | 시니어ON을 시니어용 런처로 사용 |
| 웹 링크 처리 | 시니어ON을 거치는 외부 링크의 안전성 검사 |

화면은 세로 방향으로 고정되어 있습니다. Android 버전에 따라 백그라운드 위치 권한과 기본 홈·브라우저 역할은 시스템 설정 화면에서 사용자가 직접 승인해야 합니다.

## 구현 상태와 제한 사항

- 주요 온보딩, 자녀 모드, 시니어 모드 기능은 실제 서버 API에 연결되어 있습니다.
- 말벗 기능은 프론트 API 연동 미완료로 `MockChatBuddyDataSource`를 사용합니다.
- 진료 과목은 서버 데이터가 아닌 앱의 정적 카탈로그입니다.
- Compose Preview와 UI 테스트용 Mock fixture는 실제 런타임 데이터와 분리되어 있습니다.
- 위험 링크 검사는 시니어ON이 처리하도록 연결된 외부 URL에만 적용됩니다. 사용자가 다른 브라우저를 직접 열어 검색하거나 주소를 입력하는 동작은 감지하지 않습니다.
- 실기기에서는 제조사 배터리 최적화와 위치 정책에 따라 Geofence·백그라운드 작업 전달 시간이 달라질 수 있습니다.

## 문서

- [사용자 시나리오 보완본](docs/시니어ON_사용자_시나리오_보완본.md)
- [화면 흐름](docs/SCREEN_FLOW.md)
- [코드 컨벤션](docs/CODE_CONVENTION.md)
- [GitHub 작업 방식](docs/GITHUB_WORKFLOW.md)
- [Mock 테스트 데이터](docs/MOCK_TEST_DATA.md)
- [프로젝트 문서 가이드](docs/NOTION_PROJECT_GUIDE.md)

## GitHub 작업 흐름

```text
이슈 생성
→ develop 최신화
→ 작업 브랜치 생성
→ 구현 및 로컬 검증
→ 기능 단위 커밋
→ develop 대상 Pull Request
→ 리뷰·빌드 확인 후 Merge
```

브랜치와 커밋 규칙의 상세 내용은 [GITHUB_WORKFLOW.md](docs/GITHUB_WORKFLOW.md)와 [CODE_CONVENTION.md](docs/CODE_CONVENTION.md)를 참고합니다.
