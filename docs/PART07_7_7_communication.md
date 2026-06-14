# PART 7 — 멀티스레딩과 동시성: 7.7 스레드 통신 (wait/notify · Condition · BlockingQueue)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.7 스레드 통신**을 다룬다.
> 생산자-소비자 문제를 저수준(wait/notify) → Condition → 고수준(BlockingQueue)으로 풀며 진화를 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **스레드 통신**: 한 스레드가 '조건이 될 때까지 기다리고', 다른 스레드가 '깨워서' 협력하게 하는 것.
- **생산자-소비자(producer-consumer)**: 한쪽(생산자)은 데이터를 만들어 넣고, 다른 쪽(소비자)은 꺼내 쓰는 대표 협력 문제.
- **wait() / notify() / notifyAll()**: 객체 모니터 기반 대기/깨움. (synchronized 블록 안에서만 호출, 조건은 while로 재확인)
- **spurious wakeup(가짜 깨어남)**: wait가 이유 없이 깨어날 수 있는 현상. 그래서 깬 뒤 조건을 'while'로 다시 확인해야 한다.
- **Condition**: ReentrantLock과 함께 쓰는 '대기 줄'. notFull/notEmpty처럼 여러 조건을 분리해 필요한 쪽만 깨운다(await/signal).
- **BlockingQueue**: 가득 차면 put이, 비면 take가 '알아서 블록'되는 동시성 안전 큐. wait/notify를 직접 안 써도 된다(실무 표준).

한 줄 그림: **생산자-소비자를 wait/notify(저수준) → Condition(대기 줄 분리) → BlockingQueue(고수준, 알아서 블록)로 푼다. 원리는 wait/notify로 이해하되 실무는 BlockingQueue.**

---

## 1. 학습 내용 — 스레드 간 "기다림과 깨움"

스레드 통신의 대표 문제는 **생산자-소비자(producer-consumer)**다. 용량이 정해진 버퍼에 생산자는 넣고
소비자는 꺼내는데, **버퍼가 가득 차면 생산자가 대기**하고, **비면 소비자가 대기**해야 한다. 이 "기다림/
깨움"을 어떻게 구현하느냐가 세 도구로 진화한다.

### wait / notify (저수준, 객체 모니터)
- `wait()`: 가진 락을 **놓고** 대기 상태로 들어간다(다른 스레드가 락을 쓸 수 있게).
- `notify()`/`notifyAll()`: 그 객체에서 wait 중인 스레드를 깨운다(All은 전부).
- **규칙 3가지**:
  1. 반드시 **synchronized 블록 안**(그 객체의 락을 쥔 상태)에서 호출.
  2. 조건 검사는 `if`가 아니라 **`while`** — 깨어나도 조건이 또 안 맞을 수 있다(spurious wakeup, 또는
     다른 스레드가 먼저 가져감). 그래서 깨면 **다시 확인**해야 한다.
  3. 보통 **`notifyAll`** — 한 모니터를 생산자/소비자가 공유하므로, notify로 아무나 깨우면 엉뚱한 쪽만
     깨워 멈출 수 있다.

### Condition (ReentrantLock과 함께)
wait/notify는 객체 하나의 모니터라 **대기 줄이 하나뿐**이라 notifyAll로 전부 깨워야 한다(낭비).
`ReentrantLock.newCondition()`은 한 락에 **여러 대기 줄**을 만든다.
- `notFull`(생산자 줄) / `notEmpty`(소비자 줄)로 분리 → `signal()`로 **필요한 쪽만** 정확히 깨운다.
- 대응: `wait()`→`await()`, `notify()`→`signal()`, `notifyAll()`→`signalAll()`. unlock은 finally에서(7.6).

### BlockingQueue (고수준, 실무 표준)
위의 "용량 제한 버퍼 + 대기/깨움" 로직은 `java.util.concurrent.BlockingQueue`에 이미 다 들어 있다.
- `put(x)`: 가득 차면 **알아서 블록**(대기). `take()`: 비면 **알아서 블록**.
- 락·wait·notify·while을 **한 줄도 안 써도** 내부적으로 정확히 처리한다. (Executor 스레드풀도 작업 큐로
  BlockingQueue를 쓴다 — 7.8)

---

## 2. 실습으로 확인하기

> - **가설 1**: wait/notify로 생산자-소비자가 동작한다(while 재확인·notifyAll·synchronized 필요).
> - **가설 2**: Condition으로 notFull/notEmpty를 분리하면 필요한 쪽만 signal로 깨운다.
> - **가설 3**: BlockingQueue는 put/take가 알아서 블록해, 동기화 코드 없이 같은 일을 한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 도구 | 시나리오 |
|---|---|---|
| `Example1_WaitNotify` | wait/notify | synchronized 버퍼(용량 3) |
| `Example2_ConditionVariable` | Lock+Condition | notFull/notEmpty 분리 |
| `Example3_BlockingQueue` | BlockingQueue | put/take만으로 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s07_communication.Example1_WaitNotify
java -cp build/classes/java/main com.study.part07_concurrency.s07_communication.Example2_ConditionVariable
java -cp build/classes/java/main com.study.part07_concurrency.s07_communication.Example3_BlockingQueue
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (wait/notify)** — 가설 1.
- 생산/소비가 버퍼 용량 3 안에서 진행. 가득 차면 생산자 대기, 비면 소비자 대기하며 서로 깨운다.
  10개 모두 생산·소비 완료. ✅

**예시 2 (Condition)** — 가설 2.
- 같은 동작이되 notFull/notEmpty를 분리해 `signal()`로 **필요한 쪽만** 깨운다(notifyAll 낭비 없음). ✅

**예시 3 (BlockingQueue)** — 가설 3.
- `put`/`take`만으로 동일 동작. 락·wait·notify가 코드에서 사라짐(내부가 알아서 처리). ✅

### 세 예시를 관통하는 결론
같은 생산자-소비자를 세 수준으로 풀었다. **wait/notify**는 모니터 기반의 바닥 원리지만 규칙(while·
notifyAll·synchronized)을 직접 지켜야 해 실수하기 쉽다(예시1). **Condition**은 대기 줄을 분리해 정확한
스레드만 깨운다(예시2). **BlockingQueue**는 이 모두를 캡슐화해 `put`/`take`만으로 안전하게 끝낸다(예시3).
원리는 wait/notify로 이해하되, **실무에선 검증된 고수준 도구(BlockingQueue)** 를 써서 버그를 피한다.

---

## 3. 자기 점검

- **Q. wait()에서 조건을 if가 아니라 while로 검사하는 이유는?**
  - 내 답: 깨어나도 조건이 또 안 맞을 수 있어서(spurious wakeup, 또는 다른 스레드가 먼저 자원을
    가져감). 그래서 깬 뒤 조건을 다시 확인해야 한다. (Example1)

- **Q. Condition이 wait/notify보다 나은 점은?**
  - 내 답: 한 락에 여러 대기 줄(notFull/notEmpty)을 만들어, signal로 필요한 쪽만 깨운다. wait/notify는
    대기 줄이 하나라 notifyAll로 전부 깨워야 해 비효율적이다. (Example2)

- **Q. 실무에서 생산자-소비자를 직접 wait/notify로 짜기보다 BlockingQueue를 쓰는 이유는?**
  - 내 답: BlockingQueue가 락/대기/깨움을 검증된 형태로 캡슐화해, put/take만으로 안전하다. 직접
    짜면 notify 누락·if 오용 같은 버그가 나기 쉽다. (Example3)
