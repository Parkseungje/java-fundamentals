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

### 1-0. 출발점 — 왜 '통신'이 필요한가 (busy-wait의 낭비)
지금까지(7.5/7.6)는 "여러 스레드가 공유 자원을 안전하게 '동시에' 만지는" 문제였다. 이번엔 다른 종류의
협력이다 — **한 스레드가 "어떤 조건이 될 때까지 기다렸다가" 다른 스레드가 "조건을 만들고 깨워주는"** 것.

대표 문제가 **생산자-소비자(producer-consumer)**다. 용량이 정해진 버퍼(상자)에 생산자는 데이터를 넣고
소비자는 꺼낸다. 그런데:
- 버퍼가 **가득 차면** 생산자는 빈자리가 생길 때까지 **기다려야** 하고,
- 버퍼가 **비면** 소비자는 데이터가 들어올 때까지 **기다려야** 한다.

이 "기다림"을 어떻게 구현할까? 가장 순진한 방법은 **계속 확인하며 도는 것**이다:
```java
while (버퍼가 가득 참) { }   // 빈자리 날 때까지 계속 확인 (busy-wait / 바쁜 대기)
```
하지만 이건 **CPU를 100% 태우며 헛도는** 끔찍한 낭비다(스핀). 차라리 "조건이 안 되면 **잠들었다가**,
조건이 되면 **누가 깨워주는**" 방식이 필요하다. 그게 스레드 통신이고, 아래 세 도구로 진화한다.

### 1-1. wait / notify — 객체 모니터 기반의 바닥 도구
**무엇**: 모든 객체가 가진 모니터(7.5의 락)에 딸린 '대기실'을 이용해, 스레드를 재우고(wait) 깨운다(notify).
- `wait()`: **가진 락을 놓고** 대기 상태로 잠든다. (락을 놓아야 다른 스레드가 그 락으로 들어와 조건을 바꿀 수 있다!)
- `notify()` / `notifyAll()`: 그 객체에서 wait 중인 스레드를 하나만 / 전부 깨운다.

**전형적 패턴 (생산자-소비자 버퍼)**:
```java
synchronized void put(int x) throws InterruptedException {
    while (queue.size() == capacity) wait();   // 가득 차면 -> 락 놓고 잠
    queue.add(x);
    notifyAll();                               // 넣었으니 기다리던 소비자를 깨움
}
synchronized int take() throws InterruptedException {
    while (queue.isEmpty()) wait();            // 비면 -> 락 놓고 잠
    int v = queue.poll();
    notifyAll();                               // 꺼냈으니 기다리던 생산자를 깨움
    return v;
}
```

**꼭 지켜야 할 규칙 3가지 (각각 '왜'가 있다)**:
1. **반드시 synchronized 블록 안에서** 호출. wait/notify는 그 객체의 락을 쥔 상태에서만 쓸 수 있다(안 그러면
   `IllegalMonitorStateException`). "락을 놓고 잔다"는 동작 자체가 락을 쥐고 있어야 성립하기 때문.
2. **조건은 `if`가 아니라 `while`로 검사.** 깨어났다고 조건이 충족됐다는 보장이 없다 — (a) **spurious
   wakeup**(이유 없이 깨는 현상)이 있고, (b) 깨어나 락을 다시 얻는 사이 **다른 스레드가 먼저 자원을 가져갈**
   수 있다. 그래서 깬 뒤 조건을 **다시 확인**해야 한다. (if로 쓰면 조건이 안 맞는데도 진행해 버그.)
3. **보통 `notifyAll`을 쓴다.** 한 객체의 모니터(대기실)를 생산자·소비자가 **함께** 쓰므로, `notify`로 한
   명만 깨우면 하필 '엉뚱한 쪽'(예: 또 다른 생산자)만 깨워 아무도 진행 못 하고 멈출 수 있다. notifyAll로
   전부 깨우면 그중 조건 맞는 스레드가 진행한다(나머지는 다시 while에서 잠).

> ★ 헷갈리는 지점 — "wait()는 sleep()과 뭐가 다른가?" `Thread.sleep`은 **락을 쥔 채** 잠들어(다른 스레드가
> 그 락으로 못 들어옴) 정해진 시간 후 스스로 깬다. `wait()`는 **락을 놓고** 잠들어(다른 스레드가 들어와
> 조건을 바꿀 수 있음) notify로 깨워질 때까지 기다린다. 통신엔 wait이 맞다.

### 1-2. Condition — 대기 줄을 나눠 '필요한 쪽만' 깨우기
wait/notify의 불편: 객체 하나의 모니터라 **대기실(대기 줄)이 하나뿐**이다. 생산자와 소비자가 같은 줄에서
섞여 기다리니, 정확히 한쪽만 콕 깨울 수 없어 `notifyAll`로 전부 깨우는 낭비가 생긴다.

`ReentrantLock.newCondition()`(7.6 락과 함께)은 한 락에 **여러 개의 대기 줄(Condition)**을 만들 수 있다.
```java
ReentrantLock lock = new ReentrantLock();
Condition notFull  = lock.newCondition();   // 생산자 대기 줄
Condition notEmpty = lock.newCondition();   // 소비자 대기 줄

// put: while(가득) notFull.await();  ...  notEmpty.signal();  // 소비자만 콕 깨움
// take: while(빔)  notEmpty.await(); ...  notFull.signal();   // 생산자만 콕 깨움
```
대응 관계: `wait()`→`await()`, `notify()`→`signal()`, `notifyAll()`→`signalAll()`. (unlock은 7.6처럼 finally에서.)
대기 줄이 나뉘어 있으니 **필요한 쪽만 정확히 깨워** notifyAll 낭비가 사라진다.

### 1-3. BlockingQueue — 이 모든 걸 캡슐화한 고수준 도구 (실무 표준)
위의 "용량 제한 버퍼 + 가득/빔 대기 + 깨움" 로직은 `java.util.concurrent.BlockingQueue`에 **이미 다 구현돼**
있다. 우리가 락·wait·notify·while을 한 줄도 안 써도 된다.
```java
BlockingQueue<Integer> q = new ArrayBlockingQueue<>(3);
q.put(x);        // 가득 차면 '알아서' 블록(대기)했다가 자리 나면 넣음
int v = q.take(); // 비면 '알아서' 블록했다가 데이터 들어오면 꺼냄
```
원리는 wait/notify(또는 Condition)로 이해하되, **실무에선 BlockingQueue를 쓴다** — 직접 짜면 notify 누락·
if 오용 같은 버그가 나기 쉽지만, BlockingQueue는 검증돼 있어 안전하다. (7.8의 스레드풀도 내부 작업 큐로 이걸 쓴다.)

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

- **Q. busy-wait(`while(조건){}`로 계속 확인)는 왜 나쁜가?**
  - 내 답: 조건이 될 때까지 CPU를 100% 태우며 헛돈다(낭비). wait/notify는 조건이 안 되면 잠들었다가
    깨워질 때만 동작해 CPU를 안 쓴다. (1-0)

- **Q. wait()와 Thread.sleep()의 결정적 차이는?**
  - 내 답: wait()는 '락을 놓고' 잠들어 다른 스레드가 들어와 조건을 바꿀 수 있고 notify로 깨워진다.
    sleep()은 '락을 쥔 채' 정해진 시간만 잔다. 통신엔 wait()가 맞다. (1-1)

- **Q. notify 대신 notifyAll을 쓰는 이유는?**
  - 내 답: 생산자·소비자가 한 모니터(대기실)를 공유하므로, notify로 한 명만 깨우면 엉뚱한 쪽만 깨워
    멈출 수 있다. notifyAll로 전부 깨우면 조건 맞는 쪽이 진행한다. (Condition은 줄을 나눠 이 문제를 해결.) (1-1, 1-2)
