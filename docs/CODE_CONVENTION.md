# 시니어온 코드 컨벤션

이 프로젝트는 Jetpack Compose 기반으로 시작합니다. XML 스타일은 Android 시스템 레벨 테마를 위해 남겨둘 수 있지만, 화면 UI 규칙은 Compose 디자인 토큰에서 관리합니다.

## 프로젝트 구조

- `ui/theme`: 색상, 글꼴, 모양, 간격 같은 디자인 토큰을 관리합니다.
- `ui/app`: 앱 전체 내비게이션, 모드 선택, 루트 화면 상태를 관리합니다.
- `ui/auth`: 로그인, 회원가입, 가족코드 흐름을 관리합니다.
- `ui/child`: 자녀 모드 화면과 바텀네비를 관리합니다.
- `ui/parent`: 부모님 런처 모드 화면을 관리합니다.
- `domain`: 비즈니스 로직이 커질 때 순수 모델과 유스케이스를 둡니다.
- `data`: 저장소, 네트워크, 로컬 저장, 플랫폼 연동을 둡니다.

## 이름 규칙

- Compose 화면은 `Screen` 접미사를 사용합니다: `ModeSelectionScreen`.
- 재사용 Compose 컴포넌트는 의미가 드러나는 이름을 사용합니다: `SeniorOnButton`, `FamilyCodeInput`.
- 상태 객체는 `State` 접미사를 사용합니다: `LoginUiState`.
- 사용자 액션 파라미터는 `on` 접두사를 사용합니다: `onLoginClick`, `onBackClick`.
- 상수와 디자인 토큰은 의미가 분명한 이름을 사용합니다: `SeniorOnGreen`, `ScreenHorizontalPadding`.

## 대소문자 규칙

- 변수, 함수, 파라미터는 lower camelCase를 사용합니다: `familyCode`, `onLoginClick`, `isParentMode`.
- 클래스, data class, enum class, Composable 화면/컴포넌트는 PascalCase를 사용합니다: `LoginUiState`, `ModeSelectionScreen`, `SeniorOnButton`.
- 패키지명은 소문자만 사용합니다: `ui.child.display`, `data.remote.api`.
- enum 값은 PascalCase를 기본으로 사용합니다: `Child`, `Parent`.
- 파일명은 대표 클래스나 Composable 이름과 맞춥니다: `ModeSelectionScreen.kt`, `SeniorOnButton.kt`.
- 약어는 이름 중간에 있어도 한 단어처럼 다룹니다: `userId`, `imageUrl`, `loginUiState`.

## Compose 규칙

- 화면 Composable은 상태와 콜백을 호출자로부터 전달받습니다.
- 작은 재사용 컴포넌트 안에서는 내비게이션을 직접 호출하지 않습니다.
- Preview는 해당 컴포넌트나 화면 가까이에 둡니다.
- 상태바와 내비게이션바가 콘텐츠를 가리지 않도록 `Scaffold`, `WindowInsets`, 또는 부모 padding을 사용합니다.
- 디자인 토큰이 있다면 기능 화면에서 색상, 글꼴, 간격, 모양을 하드코딩하지 않습니다.
- 화면 회전 후에도 유지되어야 하는 짧은 UI 상태는 `rememberSaveable`을 사용합니다.

## 디자인 시스템 규칙

- 색상은 `Color.kt`에 정의합니다.
- 글꼴 체계는 `Type.kt`에 정의하며, Compose 화면에서는 XML `textAppearance` 역할을 대신합니다.
- 여러 화면이 의존하기 전에 간격과 모양은 별도 토큰 파일로 추가합니다.
- `themes.xml`은 NoActionBar, 스플래시 배경, 상태바, 내비게이션바 같은 Activity/시스템 동작에 사용합니다.
- 사용자의 시스템 테마 색을 제품이 의도적으로 지원하기 전까지 Dynamic Material color는 끕니다.

## Android 기기 대응 규칙

- 중요한 화면은 작은 높이, 큰 높이, 큰 글씨 크기에서 확인합니다.
- 버튼이 많아질 수 있는 런처 모드 화면은 반드시 스크롤 가능해야 합니다.
- 제스처 내비게이션과 3버튼 내비게이션 중 하나만 가정하지 말고, 두 경우의 하단 시스템 영역을 모두 고려합니다.
- 상태바 높이를 고정값으로 가정하지 않습니다.
- 부모님 런처 동작은 출시 전 Manifest와 정책 검토가 반드시 필요합니다.

## Kotlin 스타일

- Kotlin 들여쓰기는 공백 4칸을 사용합니다.
- 가능하면 불변 값인 `val`을 사용합니다.
- 하나의 화면 상태 흐름이 읽히도록 함수는 작게 유지합니다.
- 화면이 커지면 흩어진 원시 타입 파라미터보다 명시적인 UI 상태 모델을 사용합니다.
- `Ui`, `Url`, `Id`처럼 Android에서 흔한 표현이 아니라면 이름에 약어를 피합니다.

## 주석

- 의도나 플랫폼 위험을 설명할 때 주석을 사용합니다.
- 뻔한 대입이나 단순 UI 라벨에는 주석을 달지 않습니다.
- 임시 결정은 이유를 포함한 짧은 `TODO`로 남깁니다.
