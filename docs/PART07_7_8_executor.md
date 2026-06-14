# PART 7 — 멀티스레딩과 동시성: 7.8 스레드풀 (Executor / ExecutorService, Callable / Future)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.8 스레드풀**을 다룬다.
> 스레드 직접 생성의 비용을 풀로 해결하고, Callable/Future로 결과를 받는다. (7.7 BlockingQueue가 작업 큐로 등장)

---

## 0. 들어가기 전에 — 핵심 용어
- **스레드풀(thread pool)**: 미리 만들어 둔 소수의 스레드를 '재사용'하며 작업을 처리하는 구조. 매번 스레드를 만드는 비용을 없앤다.
- **ExecutorService**: 스레드풀의 표준 인터페이스. `submit(작업)`으로 작업을 맡기고 `shutdown()`으로 닫는다.
- **Executors(팩토리)**: 풀을 만드는 도구. `newFixedThreadPool(n)`(고정), `newCachedThreadPool()`(가변), `newSingleThreadExecutor()`(1개).
- **작업 큐(work queue)**: 제출된 작업이 대기하는 줄. 노는 스레드가 꺼내 실행한다. 내부적으로 BlockingQueue(7.7).
- **Runnable vs Callable**: Runnable은 결과 없음(void)·예외 못 던짐 / Callable<V>는 V 반환·예외 가능.
- **Future<V>**: '미래에 올 결과를 담는 약속'. `get()`으로 결과를 받는다(준비될 때까지 블로킹).
- **shutdown()**: 풀을 닫는 것. 안 하면 풀 스레드가 살아 있어 JVM이 안 끝난다.

한 줄 그림: **스레드풀(ExecutorService)은 소수 스레드를 재사용해 생성 비용·폭증을 막는다. 결과가 필요하면 Callable+Future를 쓰고, 다 쓰면 shutdown() 필수.**

---

## 1. 학습 내용 — 스레드풀과 결과 받기

### 1-0. 출발점 — 왜 스레드를 '직접 만들면' 안 되나
지금까지(7.3) 스레드를 `new Thread(...).start()`로 만들었다. 작업이 몇 개면 괜찮지만, 웹 서버처럼
**요청마다 스레드를 새로 만들면** 두 가지 문제가 터진다.
1. **생성/소멸 비용**: 스레드 하나는 공짜가 아니다 — 스택 ~1MB 할당, OS에 등록, 끝나면 정리. 이 비용을
   작업마다 치르면 느리다.
2. **무제한 생성 위험**: 요청이 폭주하면 스레드가 1만, 10만 개로 폭증한다. 메모리가 터지고, 컨텍스트
   스위칭(7.1)만 하다 끝난다(오히려 더 느려지거나 죽음).

해법이 **스레드풀(thread pool)**이다. **미리 만들어 둔 소수의 스레드를 '재사용'**하며 작업을 처리한다.
- 작업이 와도 새 스레드를 안 만들고, **노는 스레드가 작업을 받아** 처리하고, 끝나면 다음 작업을 받는다.
- 동시에 도는 스레드 수가 **풀 크기로 제한**되니, 폭주해도 스레드가 안 터진다(나머지는 큐에서 대기).
- 비유: 식당에서 손님마다 새 종업원을 고용(직접 생성)하는 게 아니라, **정해진 수의 종업원이 테이블을
  돌아가며** 응대(풀)하는 것. 손님이 몰리면 대기 줄(작업 큐)에 세운다.

### 1-1. ExecutorService — 스레드풀의 표준 인터페이스
**무엇**: 스레드풀을 다루는 표준 창구. 작업을 '맡기고(submit)' 풀은 알아서 스레드에 배분한다.

**풀 만들기 (`Executors` 팩토리)**:
- `newFixedThreadPool(n)`: 스레드 **n개 고정** 풀. 가장 흔함(동시 실행 수를 n으로 제어).
- `newCachedThreadPool()`: 필요에 따라 **늘었다 줄어드는** 풀. 짧은 작업이 많을 때.
- `newSingleThreadExecutor()`: 스레드 **1개**(작업을 넣은 순서대로 순차 처리 보장).

**작업 맡기고 닫기**:
- `submit(작업)`: 작업을 **작업 큐**에 넣는다. 이 작업 큐가 바로 7.7의 **BlockingQueue**다 — 노는
  스레드가 `take()`로 꺼내 실행하고, 큐가 비면 알아서 기다린다. (7.7이 7.8의 부품으로 쓰이는 셈.)
- `shutdown()`: 새 작업은 더 안 받고, 큐에 남은 것까지 마친 뒤 닫는다. **반드시 호출해야 한다** — 안 하면
  풀 스레드(비데몬)가 계속 살아 있어 **프로그램(JVM)이 안 끝난다.** `awaitTermination(시간)`으로 종료를 기다린다.

```
submit(작업) → [작업 큐(BlockingQueue)] → 노는 스레드가 꺼내 실행 → 끝나면 다음 작업
                     (꽉 차거나 비면 알아서 대기)
```

> **동작 핵심**: 코어 스레드들이 큐에서 작업을 꺼내 처리하고 끝나면 또 꺼낸다. 그래서 작업이 아무리 많아도
> '동시에 도는 스레드 수'는 풀 크기를 안 넘는다 — 이게 직접 생성과의 결정적 차이(자원 제어).

### 1-2. 결과를 받으려면 — Runnable vs Callable, 그리고 Future
작업을 맡겼는데 **결과값**이 필요하면? 작업의 종류가 둘로 갈린다.
- **Runnable**: `run()`이 `void` → **결과를 못 돌려주고**, 검사 예외(checked exception)도 못 던진다. "그냥 실행만".
- **Callable<V>**: `call()`이 **`V`를 반환** + 예외도 던질 수 있다. "결과가 필요한 작업".

`submit(Callable)`은 즉시 **`Future<V>`**를 돌려준다. Future = **"미래에 도착할 결과를 담아둘 약속(영수증)"**.
```java
Future<Integer> f = pool.submit(() -> 1 + 2);  // 즉시 영수증(Future) 받음, 작업은 워커가 실행
int result = f.get();                           // 결과가 준비될 때까지 '기다렸다가' 받음
```
- `future.get()`: 결과가 준비될 때까지 **블로킹**(호출자가 멈춤 — 7.2의 'Async-Blocking' 조합). 준비됐으면 즉시 반환.
- 작업 도중 예외가 났으면, `get()`에서 **`ExecutionException`**으로 감싸서 던져준다(원래 예외는 그 안에 들어 있음).

> ★ 헷갈리는 지점 — "submit하면 비동기인데 왜 get에서 멈추지?" submit으로 작업을 '던지는' 것까진
> 비동기(즉시 반환)지만, **결과를 get으로 '기다려' 받으면 거기서 블로킹**된다. 그래서 여러 작업을 먼저
> 다 submit해두고 나중에 get하면 그동안 병렬로 돌아 이득이다. 아예 안 멈추고 콜백으로 받으려면 7.9의 CompletableFuture.

---

## 2. 실습으로 확인하기

> - **가설 1**: 풀에 작업을 많이 제출해도 실행 스레드는 풀 크기만큼만 재사용된다.
> - **가설 2**: Callable+Future로 작업 결과를 받을 수 있다(get은 블로킹).
> - **가설 3**: 짧은 작업이 많을 때, 직접 생성은 스레드 폭증으로 느리고 풀은 소수 재사용으로 빠르다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ThreadPoolBasic` | 스레드 재사용? | 풀(3) 에 작업 6개 |
| `Example2_CallableFuture` | 결과 받기? | Callable 제곱 + Future.get |
| `Example3_DirectVsPool` | 직접 vs 풀? | 1만 작업 시간·스레드 수 비교 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s08_executor.Example1_ThreadPoolBasic
java -cp build/classes/java/main com.study.part07_concurrency.s08_executor.Example2_CallableFuture
java -cp build/classes/java/main com.study.part07_concurrency.s08_executor.Example3_DirectVsPool
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (재사용)** — 가설 1.
- 작업 6개의 실행 스레드 이름이 `pool-1-thread-1~3`의 **3종류뿐** → 3개 스레드가 6개 작업을 재사용. ✅

**예시 2 (Callable/Future)** — 가설 2.
- 3개 Callable이 1²·2²·3² 계산 → `future.get()`으로 1,4,9 수집 → 합 **14**. ✅

**예시 3 (직접 vs 풀)** — 가설 3. (1만 개 짧은 작업, 실측)

| 방식 | 시간 | 스레드 수 |
|---|---|---|
| 직접 생성 (작업마다 new Thread) | 794 ms | 10,000개 |
| 스레드풀 (코어 수) | 31 ms | 12개 |

→ 풀이 약 **25배 빠르고** 스레드는 12개만 재사용. 작업이 많을수록 풀이 유리. ✅ (수치는 환경마다 다름)

### 세 예시를 관통하는 결론
스레드풀(ExecutorService)은 소수 스레드를 재사용해 생성 비용·스레드 폭증을 막고 동시 실행 수를
제어한다(예시1, 3). 결과가 필요하면 Callable+Future를 쓰고, get()으로 결과를 받는다(예시2). 내부의 작업
큐는 7.7의 BlockingQueue다. 다만 `Future.get()`은 블로킹이라(7.2 Async-Blocking), 콜백 체이닝으로 논블로킹
처리를 하려면 7.9의 CompletableFuture로 나아간다. shutdown()은 항상 잊지 말 것.

---

## 3. 자기 점검

- **Q. 스레드풀이 직접 생성보다 나은 이유는?**
  - 내 답: 소수 스레드를 재사용해 생성/소멸 비용과 스레드 폭증(1만 작업=1만 스레드)을 막고, 동시 실행
    수를 풀 크기로 제어한다. (Example3의 794ms/1만 vs 31ms/12)

- **Q. Runnable과 Callable의 차이, Future의 역할은?**
  - 내 답: Runnable은 결과 없음(void), Callable<V>는 V 반환+예외 가능. submit(Callable)이 주는 Future로
    결과를 받고(get은 블로킹), 작업 예외는 get()에서 ExecutionException으로 전달된다. (Example2)

- **Q. ExecutorService를 다 쓰면 꼭 해야 할 일은?**
  - 내 답: shutdown(). 안 하면 풀의 (비데몬) 스레드가 살아 있어 JVM이 종료되지 않는다. 필요하면
    awaitTermination으로 완료를 기다린다. (Example1, 3)

- **Q. 작업 큐(BlockingQueue)가 7.8에서 어떤 역할을 하나?**
  - 내 답: submit한 작업이 대기하는 줄. 노는 스레드가 거기서 꺼내 실행하고, 큐가 비면 알아서 기다린다.
    그래서 작업이 많아도 동시 실행 스레드 수는 풀 크기로 제한된다. (7.7이 7.8의 부품) (1-1)

- **Q. submit은 비동기인데 왜 Future.get()에서 멈추나?**
  - 내 답: 작업을 던지는 것까진 비동기(즉시 반환)지만, 결과를 get으로 기다려 받으면 거기서 블로킹된다
    (7.2 Async-Blocking). 여러 작업을 먼저 submit해두고 나중에 get하면 그동안 병렬로 돈다. (1-2)
