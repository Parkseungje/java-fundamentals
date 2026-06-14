# PART 7 — 멀티스레딩과 동시성: 7.4 동시성 문제 2가지 — 가시성과 원자성

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.4 동시성 문제(가시성·원자성)**를 다룬다.
> PART 2.3/7.1에서 본 "공유 변수 race"의 정체를 가시성·원자성 두 문제로 정확히 분해한다.

---

## 0. 들어가기 전에 — 핵심 용어
- **동시성 문제**: 여러 스레드가 공유 데이터를 같이 다룰 때 생기는 버그. 크게 가시성·원자성 두 갈래.
- **공유 변수**: 여러 스레드가 함께 접근하는 변수(인스턴스/static — Heap/Method Area). 지역 변수(Stack)는 안전(7.1).
- **가시성(visibility)**: 한 스레드가 바꾼 값이 다른 스레드에 '보이는가'. 안 보이면 옛 값을 읽는 문제.
- **CPU 캐시 / JIT 최적화**: 성능을 위해 각 코어가 값을 캐시하거나, 루프 밖으로 값을 빼두는 것. → 가시성 문제의 원인.
- **원자성(atomicity)**: 어떤 연산이 '쪼개지지 않고 한 번에' 일어나는가. `count++`는 읽기-증가-쓰기 3단계라 원자적이지 않다.
- **경쟁 상태(race condition) / 갱신 유실(lost update)**: 동시 수정으로 결과가 꼬이는 상황 / 한쪽 증가가 다른 쪽에 덮여 사라지는 것.
- **volatile**: 가시성을 보장하는 키워드(항상 메인 메모리에서 R/W). 단 원자성은 보장 못 함.

한 줄 그림: **동시성 버그는 '가시성(변경이 안 보임)'과 '원자성(동시 수정이 덮어씀)' 둘이다. 별개의 문제이고, volatile은 가시성만 해결한다(7.5에서 원자성까지).**

---

## 1. 학습 내용 — 두 가지 별개의 문제

동시성 버그는 크게 두 가지로 나뉜다. **둘은 다른 문제이고 해결 도구도 다르다.**

| 문제 | 정의 | 예시 |
|---|---|---|
| **가시성(Visibility)** | 한 스레드의 변경이 다른 스레드에 안 보임 | `running=false` 했는데 무한 루프 |
| **원자성(Atomicity)** | 동시 수정이 서로 덮어씀 | `count++`가 일부 손실 |

### 가시성(Visibility)
CPU는 성능을 위해 각 코어의 **캐시**에 변수 값을 들고 일하고, JIT 컴파일러는 `while(running){}`처럼
루프 안에서 안 바뀌어 보이는 변수를 **루프 밖으로 빼서 캐시**하는 최적화를 한다. 그 결과 한 스레드가
메인 메모리의 값을 바꿔도, 다른 스레드는 자기 캐시의 **옛 값**만 봐서 변경을 못 본다. 그래서 종료
플래그를 false로 바꿔도 워커가 멈추지 않는 일이 생긴다.

### 원자성(Atomicity)
`count++`는 한 번에 끝나는 연산처럼 보이지만 실제로는 **3단계**다.
1. **읽기**: 현재 count 읽기
2. **증가**: +1
3. **쓰기**: 결과 저장

두 스레드가 이 3단계를 동시에 진행하면, 둘 다 같은 옛 값(예: 100)을 읽고 둘 다 101을 써서, **두 번
증가했는데 한 번만 반영**되는 갱신 유실(lost update)이 생긴다.

### ★ 둘은 별개 — volatile은 가시성만 해결한다
헷갈리기 쉬운 핵심: **`volatile`은 가시성을 해결하지만 원자성은 해결하지 못한다.**
- `volatile boolean flag` → 항상 메인 메모리에서 읽고 쓰므로 가시성 해결(워커가 변경을 즉시 봄).
- `volatile int count++` → 읽기/쓰기 각각은 메인 메모리와 동기화되지만, "읽기-증가-쓰기 3단계를
  하나로 묶지"는 못한다. 그래서 동시 증가는 **여전히 유실**된다.

→ 원자성까지 필요하면 `synchronized`나 `Atomic`(CAS)을 써야 한다(7.5).

---

## 2. 실습으로 확인하기

> - **가설 1**: 가시성 — 일반 boolean은 변경이 다른 스레드에 안 보여 무한 루프가 될 수 있다.
> - **가설 2**: 원자성 — count++는 3단계라 동시 증가 시 유실된다(기대값보다 작음).
> - **가설 3**: volatile은 가시성은 해결하지만(워커 종료) 원자성은 미해결(count++ 여전히 유실).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 문제 | 시나리오 |
|---|---|---|
| `Example1_Visibility` | 가시성 | 일반 boolean flag로 종료 시도 |
| `Example2_Atomicity` | 원자성 | 2스레드 count++ |
| `Example3_TwoProblemsAreDifferent` | 둘은 별개 | volatile flag vs volatile count++ |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s04_concurrency_problems.Example1_Visibility
java -cp build/classes/java/main com.study.part07_concurrency.s04_concurrency_problems.Example2_Atomicity
java -cp build/classes/java/main com.study.part07_concurrency.s04_concurrency_problems.Example3_TwoProblemsAreDifferent
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (가시성)** — 가설 1.
- main이 `running=false`로 바꿔도 worker가 못 보고 **2초 뒤에도 살아있음(무한 루프)** → 가시성 문제 재현. ✅
- (worker는 데몬이라 main 종료 시 JVM이 정리. 이 문제는 JVM/CPU 의존이지만 JDK 21에서 안정 재현.)

**예시 2 (원자성)** — 가설 2.
- 2스레드 × 1,000,000 = 기대 2,000,000인데 실제 **1,061,403** (약 93만 유실). count++가 원자적이지 않음. ✅

**예시 3 (둘은 별개)** — 가설 3.

| | 결과 |
|---|---|
| (A) `volatile boolean flag` | worker가 변경을 보고 **즉시 종료** (가시성 해결) |
| (B) `volatile int count++` | 기대 2,000,000인데 **932,322** (원자성 미해결, 여전히 유실) |

→ volatile은 가시성만 해결, 원자성은 못 해결. 두 문제는 별개. ✅

### 세 예시를 관통하는 결론
동시성 버그는 **가시성**(변경이 안 보임 — 예시1)과 **원자성**(동시 수정이 덮어씀 — 예시2)의 두 갈래다.
둘은 별개이고, `volatile`은 가시성만 해결한다(예시3). 그래서 도구를 문제에 맞게 골라야 한다 —
가시성만 필요하면 volatile, 원자성(또는 둘 다)이 필요하면 synchronized/Atomic. 이 분해가 7.5
(synchronized/volatile/Atomic 비교)의 출발점이다.

---

## 3. 자기 점검

- **Q. 가시성과 원자성 문제의 차이는?**
  - 내 답: 가시성=한 스레드의 변경이 다른 스레드에 안 보임(캐시/최적화 탓). 원자성=count++ 같은
    다단계 연산이 동시에 진행돼 갱신이 유실됨. 둘은 별개 문제. (Example1 vs Example2)

- **Q. count++가 원자적이지 않은 이유는?**
  - 내 답: 읽기 → +1 → 쓰기 3단계라, 두 스레드가 같은 옛 값을 읽고 각자 +1해 쓰면 한 번만 반영된다. (Example2)

- **Q. volatile로 count++를 안전하게 만들 수 있나?**
  - 내 답: 없다. volatile은 가시성만 해결하고, 읽기-증가-쓰기 3단계를 묶지 못해 원자성은 미해결.
    synchronized나 AtomicInteger(CAS)가 필요하다(7.5). (Example3 B)
