# PART 2 — JVM 메모리 모델과 실행 원리: 2.3 Stack Area의 동작

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.3 Stack Area의 동작**을 다룬다.
> 2.2에서 본 여러 영역 중 Stack에 집중해, 스택 프레임의 생성·제거(LIFO), 한계(StackOverflowError),
> 그리고 "스레드별"이라는 성질이 동시성과 어떻게 이어지는지(PART 7 예고)를 확인한다.

---

## 1. 학습 내용 — Stack은 어떻게 동작하는가

### 스택 프레임과 LIFO
메서드를 호출하면 JVM은 그 호출을 위한 **스택 프레임(stack frame)** 을 Stack 맨 위에 쌓는다(push).
스택 프레임 안에는 그 메서드의 **지역 변수와 매개변수**가 들어간다. 메서드가 끝나면 맨 위의
프레임이 제거된다(pop). Stack은 **LIFO(Last In First Out)** 구조이므로, 가장 나중에 호출된
메서드가 가장 먼저 끝난다.

- **PC(Program Counter)** 가 현재 실행 중인 명령의 위치를 가리킨다.
- 메서드 호출이 중첩되면 프레임이 차곡차곡 쌓이고, 안쪽(나중 호출)부터 역순으로 빠져나간다.
  이것이 "호출했던 곳으로 정확히 되돌아오는" 메커니즘이다.

### StackOverflowError
Stack 공간은 **유한**하다. 종료 조건 없는 재귀처럼 프레임을 계속 쌓기만 하면(pop 없이 push만
반복), Stack 한계를 넘는 순간 **`StackOverflowError`** 가 발생한다. 정상적인 재귀는 base case에서
더 이상 호출하지 않으므로 프레임이 pop되며 줄어든다. 스택 크기는 JVM 옵션 `-Xss`로 조절할 수
있다(예: `-Xss256k`로 줄이면 더 얕은 깊이에서 터진다).

### Stack은 스레드별로 따로
**각 스레드는 자기만의 Stack을 가진다.** 그래서 여러 스레드가 같은 메서드를 동시에 실행해도,
그 메서드의 지역 변수는 스레드마다 별도 Stack에 존재해 **서로 섞이지 않는다(스레드 안전)**.

반대로 Heap의 인스턴스 변수나 Method Area의 static 변수는 **모든 스레드가 공유**하므로,
동기화 없이 여러 스레드가 함께 수정하면 값이 유실될 수 있다. 그래서 "지역 변수는 안전하고
공유 변수는 위험하다"는 동시성의 기본 원칙이 성립한다(PART 7에서 본격적으로 다룬다).

---

## 2. 실습으로 확인하기

> - **가설 1**: 메서드 호출이 중첩되면 프레임이 LIFO로 쌓이고, 나중 호출이 먼저 끝난다.
> - **가설 2**: 종료 조건 없는 재귀는 프레임을 계속 쌓아 유한한 Stack을 넘겨 StackOverflowError를 낸다.
> - **가설 3**: Stack은 스레드별이라 지역 변수는 안전하고, 공유 static 변수는 동기화 없이는 유실된다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_StackFrameLIFO` | 중첩 호출의 시작/종료 순서? | A→B→C 3단 중첩, 진입/종료 로그 |
| `Example2_StackOverflow` | Stack에 한계가 있나? | 종료 조건 없는 재귀로 깊이 측정 |
| `Example3_ThreadLocalStack` | 지역변수는 스레드 안전? | 4스레드 동시 실행: 지역변수 vs static |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s03_stack_area.Example1_StackFrameLIFO
java -cp build/classes/java/main com.study.part02_jvm.s03_stack_area.Example2_StackOverflow
java -cp build/classes/java/main com.study.part02_jvm.s03_stack_area.Example3_ThreadLocalStack
```

스택 크기를 줄여 StackOverflow 깊이가 달라지는지 확인 (선택) — 아래 한 줄만 실행:

```bash
java -Xss256k -cp build/classes/java/main com.study.part02_jvm.s03_stack_area.Example2_StackOverflow
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (LIFO)** — 가설 1. 출력 순서:
```
A 진입 → B 진입 → C 진입 → C 종료 → B 종료 → A 종료
```
→ 진입은 A,B,C 순서지만 종료는 C,B,A 역순. push/pop의 LIFO를 그대로 보여준다. ✅

**예시 2 (StackOverflowError)** — 가설 2.

| 결과 | 값(예시 실행) |
|---|---|
| 발생한 에러 | `StackOverflowError` |
| 터지기 직전 도달 깊이 | 23382 (JVM·스택 크기·프레임 크기에 따라 다름) |

→ 프레임이 pop 없이 쌓이기만 하면 유한한 Stack을 넘긴다. `-Xss`로 스택을 줄이면 깊이가 더 작아진다. ✅

**예시 3 (스레드별 Stack)** — 가설 3.

| 방식 | 결과 | 의미 |
|---|---|---|
| (A) 지역변수 | 4스레드 모두 정확히 100000 | Stack이 스레드별 → 독립·안전 |
| (B) 공유 static | 기대 400000인데 **114809** (유실) | Method Area 공유 → 경쟁 상태 |

→ 지역변수는 스레드별 Stack에 있어 동기화 없이도 정확하고, 공유 static은 갱신이 유실됐다.
(B)의 실제 값은 **실행할 때마다 달라진다** — 경쟁 상태(race condition)의 특징이다. ✅

### 세 예시를 관통하는 결론
Stack은 "메서드 호출의 일생"을 담는 영역이다. 호출마다 프레임이 LIFO로 쌓였다 빠지고(예시1),
그 공간은 유한해서 무한정 쌓으면 터지며(예시2), 무엇보다 **스레드마다 따로** 존재한다(예시3).
이 "스레드별"이라는 성질이 지역 변수를 스레드 안전하게 만드는 근본 이유이며,
"무엇이 공유되는가(Heap·Method Area) vs 무엇이 독립인가(Stack)"라는 구분이 PART 7 동시성의 토대가 된다.

---

## 3. 자기 점검

- **Q. 지역 변수가 스레드 안전한 이유는?**
  - 내 답: Stack이 스레드마다 따로 존재하고, 지역 변수는 그 스레드의 Stack 프레임 안에만 있어
    다른 스레드가 접근할 수 없기 때문. (Example3 A의 결과가 근거)

- **Q. 재귀를 깊게 쓸 때 StackOverflow를 피하려면?**
  - 내 답: 종료 조건을 명확히 두거나, 재귀를 반복문으로 바꾸거나(꼬리 재귀 제거), 필요하면
    `-Xss`로 스택을 키운다. Example2를 base case가 있는 정상 재귀로 고쳐 깊이가 제한되는지 확인해본다.

- **Q. (PART 7 예고) Example3 (B)의 sharedCounter가 매번 다른 값이 나오는 이유는?**
  - `sharedCounter++`는 "읽기 → +1 → 쓰기" 3단계라 원자적이지 않다. 여러 스레드가 끼어들어
    같은 값을 읽고 덮어쓰면 갱신이 유실된다. 이 "원자성" 문제를 PART 7에서 synchronized/Atomic로
    어떻게 푸는지 미리 떠올려본다.
