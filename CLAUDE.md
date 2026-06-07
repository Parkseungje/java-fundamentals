# CLAUDE.md — java-fundamentals

이 프로젝트에서 작업할 때 따를 컨벤션.

## 목적
"자바 → 스프링 통합 마스터 커리큘럼"의 PART 1~7을 코드로 직접 검증한다.
이론을 읽는 데서 그치지 않고, **터미널 도구(javap, jstack, jcmd, GC 로그 등)로 실제 동작을 눈으로 확인**하는 것이 핵심.

## 패키지 컨벤션
- `com.study.partNN_topic` — PART 번호(2자리) + 주제. 커리큘럼의 PART 번호와 정확히 일치시킨다.
- 커리큘럼이 "로우레벨의 고통 → 하이레벨 해결" 구조로 서술되는 단원은,
  가능하면 `traditional/`(고통 버전)과 `improved/`(해결 버전) 하위 패키지로 나눠 대비를 코드로 보여준다.
  예: `part08_ioc.traditional.UserDao` (Connection 직접 생성) vs `part08_ioc.improved.UserDao` (DI)

## 코드 작성 원칙
- 각 예제는 **실행 가능한 `main()` 또는 테스트**로 작성 — 눈으로 결과를 봐야 학습이 됨
- 자기 점검 질문(커리큘럼의 "자기 점검" 섹션)에 답하는 코드/실험을 우선순위로 작성
- 주석은 "왜 이렇게 동작하는지"에 집중 (코드가 무엇을 하는지는 이름으로 표현)

## docs/ 작성 규칙
- 파일명: `PARTNN_주제.md` (예: `PART02_JVM.md`)
- 각 문서에는 다음을 포함:
  1. 커리큘럼 요약이 아니라 **직접 실행해서 확인한 결과** (javap 출력, GC 로그, 스레드 덤프 등)
  2. 자기 점검 질문에 대한 본인의 답
  3. 예상과 다르게 동작한 부분이 있다면 기록

## 빌드/테스트
- Gradle (Groovy DSL), JUnit 5 + AssertJ
- `./gradlew test`로 실행, 새 PART 추가 시 build.gradle 의존성은 최소한으로 유지 (순수 자바 학습이 목적)
