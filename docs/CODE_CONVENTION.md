# 시니어 ON 코드 컨벤션

이 문서는 시니어 ON Android 프로젝트의 코드 작성 규칙을 정의합니다.

## 기본 원칙

- Kotlin과 Jetpack Compose를 기준으로 작성합니다.
- 화면 UI는 XML layout이 아니라 `ui` 패키지 아래 Compose 파일로 관리합니다.
- 디자인 시스템에 있는 색상, 글꼴, 간격, 모양은 화면에서 직접 하드코딩하지 않습니다.
- 하드코딩된 디자인 값을 추가하기 전에는 기존 토큰을 먼저 검색합니다.
- Android 기기별 시스템 UI, 작은 화면, 큰 글씨 설정을 고려합니다.

## 브랜치 네이밍

| 유형 | 규칙 | 예시 |
| --- | --- | --- |
| 기능 | `feat/<short-name>` | `feat/initial-mode-bottom-nav` |
| 버그 수정 | `fix/<short-name>` | `fix/family-code-validation` |
| 디자인 | `design/<short-name>` | `design/color-token-add` |
| 문서 | `docs/<short-name>` | `docs/update-repository-guide` |
| 설정/잡무 | `chore/<short-name>` | `chore/add-issue-template` |
| 리팩터링 | `refactor/<short-name>` | `refactor/split-auth-flow` |

브랜치 이름은 소문자와 하이픈을 사용합니다.

## 커밋 메시지

간단한 Conventional Commits 형식을 사용합니다.

```text
type: 변경 내용
```

예시:

```text
feat: 모드 선택 화면 추가
design: Primary 색상 토큰 추가
docs: README 화면 플로우 정리
fix: 가족공유코드 입력 버튼 활성화 조건 수정
chore: PR 템플릿 추가
```

주요 타입:

| 타입 | 의미 |
| --- | --- |
| `feat` | 사용자에게 보이는 기능 추가 |
| `fix` | 버그 수정 |
| `design` | 디자인 시스템, 색상, 글꼴, UI 스타일 변경 |
| `docs` | 문서 변경 |
| `chore` | 설정, 빌드, 템플릿, 기타 작업 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `test` | 테스트 추가 또는 수정 |

## 코드 네이밍

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 변수 | lower camelCase | `familyCode`, `selectedTab` |
| 함수 | lower camelCase | `onLoginClick`, `validateFamilyCode` |
| 클래스 | PascalCase | `LoginUiState`, `FamilyMember` |
| Composable 화면 | PascalCase + `Screen` | `ModeSelectionScreen` |
| Composable 컴포넌트 | PascalCase | `SeniorOnButton`, `FamilyCodeInput` |
| enum 값 | PascalCase | `Child`, `Parent` |
| 상수 | 의미가 드러나는 PascalCase 또는 Upper Snake Case | `ScreenHorizontalPadding` |
| 패키지 | 소문자 | `ui.child.display` |

약어는 단어처럼 취급합니다.

- 권장: `userId`, `imageUrl`, `loginUiState`
- 비권장: `userID`, `imageURL`, `loginUIState`

## 파일 네이밍

- 파일명은 대표 클래스 또는 대표 Composable 이름과 맞춥니다.
- 화면 파일은 `Screen` 접미사를 사용합니다.

예시:

```text
ModeSelectionScreen.kt
ChildMainScreen.kt
SeniorOnButton.kt
FamilyCodeInput.kt
```

## 패키지 구조

기본 구조:

```text
com.example.senior_on/
├── data/
│   ├── remote/
│   ├── repository/
│   └── model/
├── domain/
│   ├── model/
│   └── usecase/
└── ui/
    ├── app/
    ├── auth/
    ├── child/
    │   ├── display/
    │   ├── health/
    │   ├── notification/
    │   ├── family/
    │   ├── settings/
    │   └── component/
    ├── parent/
    ├── component/
    └── theme/
```

패키지 역할:

| 패키지 | 역할 |
| --- | --- |
| `ui/app` | 앱 진입, 모드 선택, 루트 흐름 |
| `ui/auth` | 로그인, 회원가입, 가족공유코드 흐름 |
| `ui/child` | 자녀 모드 화면 |
| `ui/parent` | 부모님 런처 모드 화면 |
| `ui/component` | 여러 화면에서 재사용하는 공통 컴포넌트 |
| `ui/theme` | 색상, 글꼴, Material Theme |
| `data` | REST API, DTO, Repository 구현 |
| `domain` | 비즈니스 모델, UseCase |

## Compose 작성 규칙

- 화면 Composable은 상태와 이벤트를 파라미터로 받습니다.
- 작은 공통 컴포넌트 안에서 직접 네비게이션을 호출하지 않습니다.
- 화면 전환 후 유지되어야 하는 UI 상태는 `rememberSaveable` 사용을 우선 검토합니다.
- `Scaffold`, `WindowInsets`, `systemBarsPadding()` 등을 사용해 시스템 UI 영역을 고려합니다.
- 작은 화면에서 내용이 잘릴 수 있는 경우 `LazyColumn` 또는 스크롤 가능한 구조를 사용합니다.
- Preview는 주요 화면과 공통 컴포넌트에 추가합니다.

## 디자인 시스템 규칙

- 색상은 `SeniorOnColors`를 우선 사용합니다.
- 글꼴은 `Type.kt`에 정의된 Typography를 우선 사용합니다.
- Figma 값이 소수점으로 제공되면 4dp 또는 2dp 단위로 보정합니다.
- 같은 색상 값이 이미 존재하면 새 토큰을 만들지 않고 기존 토큰을 재사용합니다.
- 디자이너가 확정하지 않은 값은 임시 토큰으로 두고 화면 코드에 직접 흩뿌리지 않습니다.

## Android 기기 대응 규칙

- 상태바와 내비게이션바 높이를 고정값으로 가정하지 않습니다.
- 터치 가능한 요소는 가능한 한 최소 48dp 터치 영역을 확보합니다.
- 부모님 런처 화면처럼 버튼이 늘어날 수 있는 화면은 스크롤 구조를 기본으로 고려합니다.
- 작은 화면, 큰 화면, 큰 글씨 설정에서 주요 텍스트와 버튼이 겹치지 않는지 확인합니다.
- 런처 앱 동작은 Manifest, 권한, 기본 홈 앱 설정 등 출시 전 별도 검토가 필요합니다.

## 주석

- 코드만으로 의도가 분명하면 주석을 추가하지 않습니다.
- 복잡한 정책, 임시 결정, 위험 지점에는 짧은 주석을 남깁니다.
- 임시 코드는 `TODO`와 이유를 함께 적습니다.
