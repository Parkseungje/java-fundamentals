# java-fundamentals

자바 → 스프링 통합 마스터 커리큘럼 중 **PART 1~7 (자바 언어 / JVM / GC / 컬렉션 / 제네릭·함수형 / I/O / 동시성)** 학습용 프로젝트.
순수 자바(Spring 의존성 없음)로, IntelliJ + 터미널을 함께 사용해 "코드 → 실제 동작(바이트코드, 메모리, 스레드)"까지 직접 확인하는 것이 목표.

## 환경
- **Java 21** (Gradle toolchain: `JavaLanguageVersion.of(21)`, `build.gradle` 참고) — 정상 실행 확인됨
  - 커리큘럼 예제 중 일부(예: PART 1.5 `instanceof` 패턴 매칭, PART 7 가상 스레드 등)는
    **자바 버전에 따라 문법/동작이 달라질 수 있으므로**, 다른 버전으로 실행 시 toolchain 값을 맞추거나
    버전 차이를 docs에 별도로 기록할 것

## 구조

```
src/main/java/com/study/partNN_topic/
    traditional/   # 로우레벨(고통) 버전 코드
    improved/      # 하이레벨(해결) 버전 코드
docs/
    PART01_OOP.md  # 학습 노트 + 실행 결과(바이트코드 덤프, 힙 분석 등) 기록
```

PART 번호와 패키지명은 커리큘럼 문서의 PART 구분과 1:1 대응한다.

> 📌 전체 커리큘럼 원본: [`../master_curriculum.md`](../master_curriculum.md) (`C:\develop\study\master_curriculum.md`)
> — `spring-fundamentals`와 공유하는 참고 문서이므로 이 프로젝트 내부로 복사하지 않는다.

## 실행 / 도구

```bash
./gradlew test                      # 전체 테스트
./gradlew compileJava                # 컴파일 (build/classes 생성)

# 바이트코드 / 상수 풀 확인 (PART 2)
javap -c -v build/classes/java/main/com/study/part02_jvm/Sample.class

# GC 로그 확인 (PART 3)
java -Xlog:gc*:file=gc.log -cp build/classes/java/main com.study.part03_gc.Main

# 스레드 덤프 (PART 7)
jps                  # PID 확인
jstack <PID>
```

## 진행 상황

- [x] PART 1 객체지향(OOP) 기초 (1.1~1.8 완료)
- [ ] PART 2 JVM 메모리 모델과 실행 원리
- [ ] PART 3 GC
- [ ] PART 4 문자열과 컬렉션
- [ ] PART 5 제네릭·비교·함수형
- [ ] PART 6 I/O와 직렬화
- [ ] PART 7 멀티스레딩과 동시성
