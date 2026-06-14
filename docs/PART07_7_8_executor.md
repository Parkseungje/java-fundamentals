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

### 왜 스레드풀인가 — 직접 생성의 문제
스레드 생성은 공짜가 아니다 — 스택 ~1MB 할당, OS 등록, 컨텍스트 스위칭(7.1). 작업마다 `new Thread`를
만들면 ① 생성/소멸 비용이 크고 ② 무제한 생성 위험(1만 작업 = 1만 스레드)이 있다. **스레드풀**은 미리
만든 소수의 스레드를 **재사용**해 이 문제를 해결한다.

### ExecutorService — 스레드풀의 표준 인터페이스
- `Executors.newFixedThreadPool(n)`: 스레드 n개 고정 풀.
- `Executors.newCachedThreadPool()`: 필요에 따라 늘었다 줄어드는 풀(짧은 작업 多).
- `Executors.newSingleThreadExecutor()`: 스레드 1개(순차 처리 보장).
- `submit(작업)`: 작업을 **작업 큐(내부적으로 BlockingQueue — 7.7)** 에 넣으면, 노는 스레드가 꺼내 실행.
- `shutdown()`: 더 받지 않고 남은 작업을 마친 뒤 닫는다. **반드시 호출**(안 하면 풀 스레드가 남아 JVM이
  안 끝남). `awaitTermination()`으로 종료를 기다린다.

> **스레드풀 동작**: 코어 스레드가 작업 큐에서 작업을 꺼내 처리하고, 끝나면 다음 작업을 꺼낸다. 그래서
> 작업 수가 많아도 스레드 수는 풀 크기로 제한된다(동시 실행 수 제어). 이 작업 큐가 7.7의 BlockingQueue다.

### Runnable vs Callable, 그리고 Future
- **Runnable**: `run()`이 void → 결과 반환 X, 검사 예외 X.
- **Callable<V>**: `call()`이 **V 반환** + 예외 던질 수 있음. 결과가 필요한 작업에 쓴다.
- `submit(Callable)` → **`Future<V>`** 반환. Future = "미래에 올 결과를 담는 약속".
  - `future.get()`: 결과가 준비될 때까지 **블로킹**(7.2의 Async-Blocking) 후 값 수령.
  - 작업 중 예외는 `get()`에서 **`ExecutionException`** 으로 감싸 던진다.

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
