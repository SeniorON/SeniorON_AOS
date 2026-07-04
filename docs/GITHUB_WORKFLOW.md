# 시니어 ON GitHub 작업 흐름

이 문서는 시니어 ON Android 팀의 GitHub 협업 방식을 정의합니다.

## 기본 흐름

```text
이슈 생성 -> 브랜치 생성 -> 작업 -> 커밋 -> Pull Request 생성 -> 리뷰 -> 머지 -> 이슈 종료
```

작업은 가능한 한 이슈 단위로 나누어 진행합니다.

## Issue

작업 전에는 먼저 이슈를 생성합니다.

이슈 유형:

| 유형 | 용도 |
| --- | --- |
| 버그 제보 | 사용자에게 보이는 오류, 회귀, 크래시 |
| 기능 제안 | 신규 기능 또는 화면 흐름 제안 |
| 디자인 시스템 | 색상, 글꼴, 간격, 공통 컴포넌트 |
| 작업 | 설정, 문서, 리팩터링, 기타 작업 |

이슈에는 다음 내용을 포함합니다.

- 작업 목적
- 변경 범위
- 완료 기준
- 참고 화면 또는 Figma 링크
- 담당자

## Branch

브랜치는 이슈가 생성된 뒤 만듭니다.

예시:

```bash
git switch main
git pull
git switch -c feat/initial-mode-bottom-nav
```

브랜치 이름은 [CODE_CONVENTION.md](CODE_CONVENTION.md)의 브랜치 네이밍 규칙을 따릅니다.

## Commit

커밋은 하나의 의미 있는 변경 단위로 작성합니다.

예시:

```bash
git add .
git commit -m "docs: README 화면 플로우 정리"
```

커밋 메시지는 다음 형식을 사용합니다.

```text
type: 변경 내용
```

## Pull Request

작업이 끝나면 Pull Request를 생성합니다.

PR에는 다음 내용을 포함합니다.

- 변경 내용 요약
- 관련 이슈 번호
- 화면 변경 시 스크린샷 또는 화면 녹화
- 테스트 또는 수동 확인 내용
- Android 시스템 UI, 작은 화면, 큰 글씨 설정 고려 사항

이슈를 PR 머지와 함께 자동 종료하려면 PR 본문에 다음과 같이 작성합니다.

```text
Closes #이슈번호
```

예시:

```text
Closes #12
```

## Review

리뷰어는 다음 항목을 확인합니다.

- 요구사항을 충족하는지
- 화면 흐름이 사용자가 이해하기 쉬운지
- 디자인 시스템 토큰을 재사용했는지
- 시스템 UI 영역과 다양한 Android 기기를 고려했는지
- 큰 글씨 설정에서 화면이 깨지지 않는지
- 테스트 또는 수동 확인이 충분한지

## Merge

머지는 다음 조건을 만족한 뒤 진행합니다.

- 리뷰어 승인
- 빌드 성공
- 충돌 없음
- 관련 이슈 연결
- PR 템플릿 작성 완료

머지 후 브랜치는 삭제합니다.

## GitHub Projects 운영

이슈는 GitHub Projects에서 상태를 관리합니다.

권장 상태:

| 상태 | 의미 |
| --- | --- |
| Todo | 아직 시작하지 않은 작업 |
| In Progress | 진행 중인 작업 |
| Review | PR 리뷰 중인 작업 |
| Done | 머지 완료 또는 이슈 종료 |

## 작업 예시

```bash
git switch main
git pull
git switch -c docs/update-repository-guide

# 문서 수정 후
git status
git add README.md docs/CODE_CONVENTION.md docs/GITHUB_WORKFLOW.md
git commit -m "docs: 레포지토리 가이드 정리"
git push origin docs/update-repository-guide
```

PR 본문:

```text
Closes #3
```
