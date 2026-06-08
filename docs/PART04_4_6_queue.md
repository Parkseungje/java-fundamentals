# PART 4 — 문자열과 컬렉션: 4.6 Queue

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.6 Queue**를 다룬다.
> FIFO 동작, 안전/강제 메서드 두 벌, 그리고 ArrayDeque로 큐·스택을 모두 표현하는 법을 본다.

---

## 1. 학습 내용 — Queue와 Deque

### Queue = FIFO
Queue는 **FIFO(First In First Out, 선입선출)** 자료구조다. 줄 서기처럼 먼저 들어온 것이 먼저
처리된다. 사용처: **BFS(너비 우선 탐색), 버퍼, 메시지 큐(MQ)** 등 "들어온 순서대로 처리"가 필요한 곳.

또한 **null을 원소로 넣을 수 없다**. poll/peek가 "비었음"을 null로 표현하기 때문에, null 원소를
허용하면 "값이 null인지, 큐가 빈 건지" 구분할 수 없어진다.

### 안전 메서드 vs 강제 메서드 (두 벌)
Queue 연산은 실패할 수 있다(빈 큐에서 꺼내기, 가득 찬 큐에 넣기). 자바는 실패 시 동작이 다른
두 벌의 메서드를 제공한다.

| 동작 | 안전 (실패 시 false/null) | 강제 (실패 시 예외) |
|---|---|---|
| 삽입 | `offer()` | `add()` |
| 조회 | `peek()` | `element()` |
| 제거 | `poll()` | `remove()` |

- **안전 버전**: 실패해도 예외 없이 false(삽입 실패)나 null(조회/제거 실패)을 반환 → 흐름 제어가 쉽다.
- **강제 버전**: 실패하면 예외(`NoSuchElementException` 등)를 던진다 → "비면 안 되는" 상황에서 빠르게 발견.

실무에서는 흐름 제어가 쉬운 안전 버전(offer/peek/poll)을 더 자주 쓴다.

### Deque와 구현체 선택
- **Deque(Double Ended Queue)**: 양쪽 끝에서 넣고 뺄 수 있는 큐. `ArrayDeque`가 대표 구현.
- ArrayDeque는 양끝 조작이 되므로, 메서드만 바꾸면 **Queue(FIFO)로도 Stack(LIFO)으로도** 쓸 수 있다.
  - Queue처럼: `offerLast`(뒤로 넣기) + `pollFirst`(앞에서 빼기) → FIFO
  - Stack처럼: `push`(앞으로 넣기) + `pop`(앞에서 빼기) → LIFO
- **권장**: 큐가 필요하면 LinkedList보다 ArrayDeque(배열 기반, 빠르고 캐시 효율 ↑). 스택이 필요해도
  구식 `java.util.Stack`(Vector 기반, 동기화로 느림, 설계 낡음) 대신 ArrayDeque를 쓰는 게 표준 권장이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: offer로 넣고 poll로 빼면 FIFO 순서. peek는 제거 없이 조회한다.
> - **가설 2**: 빈 큐에서 안전 메서드는 null, 강제 메서드는 예외. null 원소는 못 넣는다.
> - **가설 3**: ArrayDeque는 메서드만 바꿔 Queue(FIFO)·Stack(LIFO) 둘 다 표현한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_Fifo` | FIFO와 peek/poll 차이? | offer/peek/poll |
| `Example2_SafeVsThrow` | 두 벌 메서드 차이? | 빈 큐 + null 삽입 |
| `Example3_DequeAsQueueAndStack` | ArrayDeque 활용? | Queue vs Stack 방식 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s06_queue.Example1_Fifo
java -cp build/classes/java/main com.study.part04_collections.s06_queue.Example2_SafeVsThrow
java -cp build/classes/java/main com.study.part04_collections.s06_queue.Example3_DequeAsQueueAndStack
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (FIFO)** — 가설 1.
- offer A,B,C 후 `peek()`=A (두 번 해도 size 그대로 3) → peek는 제거 안 함.
- `poll()` 반복 → A, B, C 순서(FIFO). ✅

**예시 2 (안전 vs 강제)** — 가설 2.

| 호출(빈 큐) | 결과 |
|---|---|
| `poll()` / `peek()` | null (예외 없음) |
| `remove()` / `element()` | NoSuchElementException |
| `offer(null)` | NullPointerException (null 원소 금지) |

→ 안전 버전은 null, 강제 버전은 예외. Queue는 null을 못 담는다. ✅

**예시 3 (Deque)** — 가설 3. 같은 ArrayDeque, 메서드만 다르게:

| 방식 | 넣기 A,B,C → 빼기 | 순서 |
|---|---|---|
| Queue(FIFO) | offerLast → pollFirst | **A, B, C** |
| Stack(LIFO) | push → pop | **C, B, A** |

→ 하나의 ArrayDeque로 FIFO/LIFO를 모두 표현. ✅

### 세 예시를 관통하는 결론
Queue는 "먼저 온 것 먼저 처리(FIFO)"가 본질이고(예시1), 실패 상황을 false/null로 받을지 예외로
받을지 두 벌의 메서드로 선택할 수 있다(예시2). 그리고 ArrayDeque는 양끝 조작이 가능한 Deque라
메서드 선택만으로 큐와 스택을 모두 구현한다(예시3). 큐든 스택이든 실무 기본 선택은 ArrayDeque다
(LinkedList·구식 Stack보다 빠르고 깔끔). 우선순위 기반이 필요하면 PriorityQueue(힙)를 쓴다.

---

## 3. 자기 점검

- **Q. poll()과 remove()의 차이는?**
  - 내 답: 빈 큐에서 poll()은 null을 반환(안전), remove()는 NoSuchElementException을 던진다(강제).
    흐름 제어엔 poll, 비면 안 되는 상황엔 remove. (Example2)

- **Q. Queue가 null 원소를 금지하는 이유는?**
  - 내 답: poll/peek가 "비었음"을 null로 표현하는데, null 원소를 허용하면 "값이 null인지 큐가 빈
    건지" 구분할 수 없기 때문. (Example2의 offer(null) NPE)

- **Q. (적용) 스택이 필요할 때 `java.util.Stack` 대신 ArrayDeque를 쓰는 이유는?**
  - `java.util.Stack`은 Vector 기반이라 모든 메서드가 synchronized(불필요한 동기화로 느림)이고 설계가
    낡았다. ArrayDeque의 push/pop이 더 빠르고 권장된다. 직접 둘로 같은 작업을 해보고 차이를 정리해본다.
