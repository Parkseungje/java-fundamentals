# PART 7 — 멀티스레딩과 동시성: 7.9 비동기 (CompletableFuture · 가상 스레드)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.9 비동기**를 다룬다. PART 7과 java-fundamentals의 마지막.
> 7.2의 Async-Non-blocking을 CompletableFuture로 구현하고, Java 21 가상 스레드까지 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **CompletableFuture**: Future의 한계(get은 블로킹·작업 연결 불가)를 넘어, '끝나면 콜백이 알아서 이어 실행'되게 하는 비동기 도구.
- **콜백(callback)**: "작업이 끝나면 이걸 실행해줘"라고 미리 등록해두는 함수. 결과를 기다리지 않고 위임한다.
- **supplyAsync / thenApply / thenAccept**: 비동기 작업 시작 / 결과 변환(값→값) / 결과 소비(반환 없음).
- **thenCompose / thenCombine / exceptionally**: 의존 작업 연결 / 독립 작업 둘 합치기 / 예외를 대체값으로 복구.
- **가상 스레드(virtual thread, Java 21)**: JVM이 관리하는 '경량 스레드'. 수십만~수백만 개 생성 가능. I/O 블로킹 시 OS 스레드(캐리어)를 놓아준다.
- **플랫폼 스레드 vs 가상 스레드**: OS 스레드와 1:1(무겁다) vs JVM이 다수를 소수 OS 스레드 위에서 돌림(가볍다).
- **캐리어 스레드(carrier)**: 가상 스레드를 실제로 실행하는 OS 스레드. 가상 스레드가 블로킹되면 캐리어를 양보(언마운트).

한 줄 그림: **CompletableFuture는 콜백 체이닝으로 '안 멈추는' 비동기를 만든다(7.2 Async-Non-blocking). 가상 스레드(Java 21)는 블로킹 코드 그대로 쓰면서 I/O 대량 동시성을 적은 자원으로 처리한다.**

---

## 1. 학습 내용 — 논블로킹 비동기와 경량 스레드

### CompletableFuture — Future의 한계 극복
7.8의 `Future`는 결과를 `get()`으로 **기다려야** 했고(블로킹), 작업을 **이어붙일 수 없었다**.
`CompletableFuture`는 "작업이 끝나면 등록한 콜백이 알아서 실행"되도록 체이닝한다(7.2 Async-Non-blocking).
- `supplyAsync(작업)`: 결과를 내는 비동기 작업 시작.
- `thenApply(f)`: 앞 결과를 받아 **변환**(값 → 값).
- `thenAccept(c)`: 앞 결과를 **소비**(반환 없음).
- `thenCompose(f)`: 앞 결과로 **또 다른 비동기 작업을 연결**(값 → CompletableFuture, 의존 관계). 중첩 평탄화.
- `thenCombine(other, f)`: **독립인 두 작업**을 동시에 돌리고 둘 다 끝나면 결과를 **합친다**.
- `exceptionally(f)`: 체인 중 예외가 나면 잡아 **대체값으로 복구**(try-catch의 비동기 버전).

→ 동기 코드의 '순차 호출 / 병렬 후 합산 / try-catch'를 **논블로킹**으로 표현한 것.

### 가상 스레드 (Virtual Thread, Java 21)
기존 자바 스레드(플랫폼 스레드)는 **OS 스레드와 1:1**이라 스택 ~1MB를 쓰고, 수만 개를 만들면 메모리·
컨텍스트 스위칭이 폭증한다(7.1). **가상 스레드**는 JVM이 관리하는 **경량 스레드**로 수십만~수백만 개를
만들 수 있다.
- 핵심: 가상 스레드가 I/O 등으로 **블로킹되면, 실제 OS 스레드(캐리어)를 놓아주고(언마운트)** 잠든다.
  그 OS 스레드는 그동안 다른 가상 스레드를 실행한다. 그래서 **적은 OS 스레드로 엄청난 수의 블로킹
  작업**을 동시에 처리한다.
- 의미: 6.2의 Non-blocking이 주던 확장성을, **블로킹 코드 스타일 그대로** 얻는다(콜백 지옥 없이).
- `Executors.newVirtualThreadPerTaskExecutor()` 또는 `Thread.ofVirtual().start(...)`.
- 단, **CPU 바운드**는 코어 수가 한계라 가상 스레드여도 더 빨라지지 않는다(7.1). 가상 스레드의 이점은
  **I/O 바운드 대량 동시성**에 있다.

---

## 2. 실습으로 확인하기

> - **가설 1**: CompletableFuture는 get() 없이 콜백 체이닝으로 단계를 잇는다(main 안 막힘).
> - **가설 2**: thenCompose(연결)/thenCombine(합치기)/exceptionally(복구)로 비동기를 조립한다.
> - **가설 3**: I/O 대량 동시성에서 가상 스레드는 플랫폼 풀보다 훨씬 빠르고, 10만 개도 처리한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_CompletableFutureChaining` | 논블로킹 체이닝? | supplyAsync→thenApply→thenAccept |
| `Example2_ComposeCombineException` | 연결·합치기·복구? | compose/combine/exceptionally |
| `Example3_VirtualThreads` | 가상 스레드 이점? | 플랫폼 풀 vs 가상, 10만 개 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s09_async.Example1_CompletableFutureChaining
java -cp build/classes/java/main com.study.part07_concurrency.s09_async.Example2_ComposeCombineException
java -cp build/classes/java/main com.study.part07_concurrency.s09_async.Example3_VirtualThreads
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (체이닝)** — 가설 1.
- `[main] 안 막히고 다음 일 진행`이 먼저 찍히고, 잠시 뒤 체인 `[1]데이터→[2]변환→[3]HELLO`가 이어짐. ✅

**예시 2 (조립)** — 가설 2.
- thenCompose: userId 42 → `user-42의 상세정보`(연결).
- thenCombine: 1000 + 2500 = **3500**(독립 작업 합산).
- exceptionally: 예외 발생 → **-1**로 복구. ✅

**예시 3 (가상 스레드)** — 가설 3. (각 작업 100ms I/O 대기, 실측)

| 방식 | 작업 수 | 시간 |
|---|---|---|
| 플랫폼 스레드 풀(200) | 2,000 | 1116 ms (200개씩 나눠 처리) |
| 가상 스레드 | 2,000 | 129 ms (모두 동시에 대기) |
| 가상 스레드 | 100,000 | 294 ms (플랫폼이면 수 GB 필요) |

→ I/O 대량 동시성에서 가상 스레드가 약 **9배 빠르고**, 10만 개도 가뿐히 처리. ✅ (수치는 환경마다 다름)

### 세 예시를 관통하는 결론
CompletableFuture는 Future의 블로킹/비조립 한계를 넘어, 논블로킹 콜백 체이닝(예시1)과 연결·합치기·복구
(예시2)로 비동기 흐름을 조립한다 — 7.2의 Async-Non-blocking이 코드로 실현된 것이다. 가상 스레드(Java 21)는
블로킹 코드를 그대로 쓰면서도 I/O 대량 동시성을 적은 자원으로 처리해(예시3), "1만 접속 문제"(6.1/6.2)에
대한 또 하나의 현대적 답을 준다. 이로써 PART 7(동시성)과 java-fundamentals(PART 1~7)가 마무리된다.

---

## 3. 자기 점검

- **Q. CompletableFuture가 Future보다 나은 점은?**
  - 내 답: Future는 get()으로 결과를 기다려야 하고(블로킹) 작업을 이어붙일 수 없다. CompletableFuture는
    thenApply/thenAccept/thenCompose/thenCombine으로 끝나면 자동 실행되는 논블로킹 체인을 만든다. (Example1, 2)

- **Q. thenApply와 thenCompose의 차이는?**
  - 내 답: thenApply는 값→값 변환, thenCompose는 값→CompletableFuture(또 다른 비동기 작업 연결)로
    중첩을 평탄화한다. 의존하는 비동기 작업을 이을 때 thenCompose. (Example2)

- **Q. 가상 스레드가 I/O 바운드에서 강한 이유는? CPU 바운드는?**
  - 내 답: I/O로 블로킹되면 OS 스레드(캐리어)를 놓아줘 적은 OS 자원으로 수만 개를 처리한다. 하지만
    CPU 바운드는 코어 수가 한계라 가상 스레드여도 더 빨라지지 않는다(7.1). (Example3)
