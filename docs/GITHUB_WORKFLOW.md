# 시니어온 GitHub 작업 흐름

## 브랜치

- `main`: 안정 브랜치입니다.
- `feature/<short-name>`: 사용자에게 보이는 새 기능 작업입니다.
- `fix/<short-name>`: 버그 수정 작업입니다.
- `chore/<short-name>`: 설정, 문서, 빌드, 의존성 변경 작업입니다.
- `design/<short-name>`: 디자인 시스템과 시각 토큰 변경 작업입니다.

## 커밋 메시지

간단한 Conventional Commits 형식을 사용합니다:

- `feat: add mode selection flow`
- `fix: keep family code button disabled for invalid input`
- `chore: add issue templates`
- `docs: define code convention`
- `design: add Senior On color tokens`
- `refactor: split auth screens`

## Pull Request

모든 PR에는 다음 내용을 포함합니다:

- 무엇이 변경되었는지
- 어떤 화면이나 흐름이 영향을 받는지
- UI 변경 시 스크린샷
- 기기와 시스템 UI 고려사항
- 테스트 또는 수동 확인 내용

## Issue

상황에 맞는 템플릿을 사용합니다:

- 버그 제보: 깨진 동작이나 회귀를 기록합니다.
- 기능 제안: 제품 동작이나 화면 흐름을 제안합니다.
- 디자인 시스템: 색상, 글꼴, 간격, 모양, 컴포넌트 규칙을 관리합니다.
- 작업: 설정, 문서, 리팩터링, 내부 작업을 관리합니다.

## 리뷰 중점

이 앱에서는 리뷰 시 다음을 특히 확인합니다:

- 다양한 Android 기기에서 시스템 UI와 화면이 겹치지 않는지
- 큰 글씨와 접근성 설정에서 화면이 무너지지 않는지
- 부모님 런처 모드의 위험 지점이 고려되었는지
- 디자인 토큰이 일관되게 재사용되었는지
- 사용자 모드 흐름이 이해하기 쉽게 유지되는지
