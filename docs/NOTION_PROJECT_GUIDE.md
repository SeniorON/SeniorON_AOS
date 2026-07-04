# 시니어 ON 프로젝트 운영 문서

> 가족공유코드로 부모님과 자녀를 연결해 스마트폰 사용 환경, 건강 정보, 알림, 가족 사진을 함께 관리하는 Android 앱

## 프로젝트 요약

| 구분 | 내용 |
| --- | --- |
| 앱 이름 | 시니어 ON |
| 플랫폼 | Android |
| 서비스 형태 | 자녀용 관리 앱 + 부모님 런처 앱 |
| 핵심 연결 단위 | 가족공유코드 |
| 현재 목표 | GitHub 세팅, 앱 구조 설계, 디자인 시스템 정리, 초기 화면 플로우 정리 |

## 팀 역할

| 이름 | 담당 | 주요 책임 |
| --- | --- | --- |
| 원스톤 | Android Lead | 앱 구조, 디자인 시스템, 인증, 알림 탭, 부모님 런처 구조 |
| 데쿠 | Android | 화면 탭(화면 지원), 가족 탭 |
| 린린 | Android | 건강 탭, 설정 탭 |

## 기능 영역

| 영역 | 주요 기능 | 담당 |
| --- | --- | --- |
| 앱 구조 | 모드 선택, 루트 흐름, 자녀 메인 바텀네비 구조 | 원스톤 |
| 인증 | 로그인, 회원가입, 아이디/비밀번호 찾기 | 공통 |
| 가족공유코드 | 코드 확인, 입력, 생성, 부모님 정보 입력 | 공통 |
| 화면 탭 | 부모님 런처화면 미리보기, 버튼 편집, 글씨 편집 | 데쿠 |
| 건강 탭 | 복약 관리, 병원 일정, 진료비 공동 관리 | 린린 |
| 알림 탭 | SOS, 무활동, 위험 링크, 외출/귀가 알림 | 원스톤 |
| 가족 탭 | 구성원 설정, 가족 추가, 가족 사진 공유 | 데쿠 |
| 설정 탭 | 내 계정, 프로필 설정, 연결 기기, 로그아웃 | 린린 |
| 부모님 런처 | 큰 버튼 홈 화면, 긴급알림, 사진/일정 접근 | 원스톤 |

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material3 |
| Architecture | MVVM 예정 |
| Network | REST API 연동 예정 |
| Backend 연동 | Spring Boot REST API 예정 |
| Design | Figma, Design Token, Pretendard |
| Collaboration | GitHub Issues, Pull Requests, Projects |

## 화면 목록

| 화면 | Screen ID | 진입 경로 | 담당 |
| --- | --- | --- | --- |
| 스플래시 | SplashScreen | 앱 실행 | 원스톤 |
| 모드 선택 | ModeSelectionScreen | 스플래시 이후 | 원스톤 |
| 로그인 | LoginScreen | 모드 선택 후 | 원스톤 |
| 회원가입 시작 | SignUpStartScreen | 로그인 > 회원가입 | 공통 |
| 회원가입 정보 입력 | SignUpProfileScreen | 회원가입 시작 후 | 공통 |
| 이메일 인증 | EmailVerificationScreen | 회원가입 정보 입력 후 | 공통 |
| 계정 정보 입력 | SignUpAccountScreen | 이메일 인증 후 | 공통 |
| 약관 동의 | TermsAgreementScreen | 계정 정보 입력 후 | 공통 |
| 가족공유코드 확인 | FamilyShareCodeCheckScreen | 로그인/회원가입 후 | 공통 |
| 가족공유코드 입력 | FamilyShareCodeInputScreen | 코드 있음 선택 후 | 공통 |
| 가족공유코드 생성 | FamilyShareCodeCreateScreen | 코드 없음 선택 후 | 공통 |
| 부모님 정보 입력 | ParentInfoInputScreen | 코드 생성 후 | 공통 |
| 자녀 메인 바텀네비 구조 | ChildMainScreen | 자녀 모드 진입 후 | 원스톤 |
| 화면 탭(화면 지원) | DisplayTabScreen | 자녀 메인 > 화면 | 데쿠 |
| 건강 탭 | HealthTabScreen | 자녀 메인 > 건강 | 린린 |
| 알림 탭 | NotificationTabScreen | 자녀 메인 > 알림 | 원스톤 |
| 가족 탭 | FamilyTabScreen | 자녀 메인 > 가족 | 데쿠 |
| 설정 탭 | SettingsTabScreen | 자녀 메인 > 설정 | 린린 |
| 부모님 런처화면 | ParentLauncherScreen | 부모님 모드 진입 후 | 원스톤 |

## 화면 플로우

앱 진입:

```text
앱 실행
-> 스플래시
-> 모드 선택
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

## GitHub 작업 흐름

| 단계 | 작업 | 완료 기준 |
| --- | --- | --- |
| 1 | 이슈 생성 | 목적, 범위, 담당자 작성 |
| 2 | 브랜치 생성 | 작업 유형에 맞는 브랜치명 사용 |
| 3 | 작업 진행 | 관련 파일 중심으로 수정 |
| 4 | PR 생성 | 변경 내용, 확인 방법, 이슈 번호 작성 |
| 5 | 리뷰 | 승인과 빌드 확인 |
| 6 | 머지 | `Closes #이슈번호`로 이슈 자동 종료 |

## 작업 체크리스트

- [ ] 작업 전 이슈를 생성했다.
- [ ] 이슈에 담당자와 완료 기준을 작성했다.
- [ ] 이슈에 맞는 브랜치를 생성했다.
- [ ] 디자인 값은 기존 토큰을 먼저 검색했다.
- [ ] 화면 작업이면 작은 화면과 시스템 UI를 고려했다.
- [ ] PR 본문에 변경 내용과 확인 방법을 작성했다.
- [ ] PR 본문에 `Closes #이슈번호`를 작성했다.
- [ ] 리뷰 승인 후 머지했다.

## 브랜치 규칙

| 유형 | 형식 | 예시 |
| --- | --- | --- |
| 기능 | `feat/<short-name>` | `feat/initial-mode-bottom-nav` |
| 버그 | `fix/<short-name>` | `fix/family-share-code-validation` |
| 디자인 | `design/<short-name>` | `design/color-token-add` |
| 문서 | `docs/<short-name>` | `docs/update-readme` |
| 설정 | `chore/<short-name>` | `chore/add-issue-template` |
| 리팩터링 | `refactor/<short-name>` | `refactor/split-auth-flow` |

## Android 검토 포인트

| 항목 | 체크 내용 |
| --- | --- |
| 시스템 UI | 상태바, 내비게이션바와 콘텐츠가 겹치지 않는가 |
| 작은 화면 | 세로 공간이 부족할 때 스크롤 가능한가 |
| 큰 글씨 | 접근성 글씨 크기에서 텍스트가 잘리지 않는가 |
| 부모님 런처 | 버튼이 많아져도 사용 흐름이 단순한가 |
| 알림 권한 | 위치, 알림, 위험 링크 감지 등 권한 흐름을 고려했는가 |
| 출시 준비 | 권한, Manifest, Play Console 정책을 검토했는가 |

## 참고 문서

| 문서 | 설명 |
| --- | --- |
| `README.md` | GitHub Repository 첫 화면 |
| `docs/CODE_CONVENTION.md` | 코드 네이밍, 패키지 구조, Compose 규칙 |
| `docs/GITHUB_WORKFLOW.md` | 이슈, 브랜치, PR, 머지 규칙 |
| `docs/SCREEN_FLOW.md` | 화면 목록 및 이동 흐름 |
