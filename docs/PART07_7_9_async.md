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

### 1-0. 출발점 — 7.8 Future의 두 아쉬움
7.8의 `Future`로 "결과를 나중에 받는" 비동기를 맛봤다. 그런데 두 가지가 아쉬웠다.
1. **결과를 받으려면 결국 `get()`에서 멈춘다(블로킹).** 던질 땐 비동기였지만 받을 땐 막힌다(7.2의 Async-Blocking).
2. **작업을 이어붙일 수 없다.** "A가 끝나면 그 결과로 B, B가 끝나면 C"처럼 단계를 연결하려면 결국
   get으로 기다려 받아서 다음을 호출해야 한다 — 결국 멈춘다.

이 둘을 푸는 게 **CompletableFuture**(안 멈추고 콜백으로 잇기)와, 더 나아가 **가상 스레드**(아예 블로킹
코드를 그대로 쓰되 싸게 만들기)다. 7.9는 PART 7의 마지막으로 이 두 현대 도구를 본다.

### 1-1. CompletableFuture — "끝나면 콜백이 알아서 잇는" 논블로킹 비동기
**핵심 아이디어**: 결과를 `get()`으로 기다리는 대신, **"작업이 끝나면 이걸 해줘"라는 콜백을 미리 등록**하고
호출자는 그냥 다음 일을 한다. 작업이 끝나면 등록된 콜백이 (워커 스레드에서) 알아서 실행된다(7.2의 Async-Non-blocking).

```java
CompletableFuture
    .supplyAsync(() -> fetchData())      // ① 비동기로 데이터 가져오기 (즉시 반환)
    .thenApply(data -> data.toUpperCase()) // ② 끝나면 결과를 변환 (콜백)
    .thenAccept(result -> log(result));    // ③ 변환 결과를 소비 (콜백) — get() 없음
// 호출자(main)는 여기서 안 멈추고 바로 다음 줄로 진행
```

**주요 메서드 (동기 코드의 무엇에 대응하는지로 외우면 쉽다)**:
- `supplyAsync(작업)`: 결과를 내는 비동기 작업을 시작(즉시 CompletableFuture 반환).
- `thenApply(f)`: 앞 결과를 받아 **값을 변환**(값 → 값). 동기의 `b = f(a)`에 대응.
- `thenAccept(c)`: 앞 결과를 **소비만**(반환 없음). 체인의 끝(출력·저장 등).
- `thenCompose(f)`: 앞 결과로 **또 다른 비동기 작업을 연결**(값 → CompletableFuture). 의존 관계의
  '순차 호출'에 대응. (thenApply가 '값→값'이면 thenCompose는 '값→비동기작업'이라 중첩을 평탄화해준다.)
- `thenCombine(other, f)`: **독립인 두 작업**을 동시에 돌리고 **둘 다 끝나면 합친다**('병렬 후 합산').
- `exceptionally(f)`: 체인 중간에 예외가 나면 잡아 **대체값으로 복구**(비동기판 try-catch).

> 한 줄 요약: 동기 코드의 '순차 호출(thenCompose) / 병렬 후 합산(thenCombine) / try-catch(exceptionally)'를
> **어디서도 안 멈추고(논블로킹)** 표현한 것이 CompletableFuture다.

> ★ 헷갈리는 지점 — "thenApply vs thenCompose?" 콜백이 **그냥 값**을 돌려주면 `thenApply`, 콜백이 **또
> 다른 CompletableFuture**(비동기 작업)를 돌려주면 `thenCompose`다. compose를 안 쓰면 결과가
> `CompletableFuture<CompletableFuture<T>>`처럼 이중으로 감싸진다 — compose가 그걸 한 겹으로 펴준다.

### 1-2. 가상 스레드 (Virtual Thread, Java 21) — 블로킹 코드를 싸게
**문제 상황**: 콜백 방식(CompletableFuture)은 효율적이지만 코드가 복잡해진다("콜백 지옥"). 반대로
"그냥 막히게(블로킹) 짜는" 코드는 읽기 쉽지만, 7.8에서 봤듯 스레드가 비싸 대량으로 못 만든다.
가상 스레드는 **"읽기 쉬운 블로킹 코드 + 대량 동시성"을 동시에** 주는 해법이다.

**플랫폼 스레드 vs 가상 스레드**:
- **플랫폼 스레드**(기존): OS 스레드와 **1:1**. 스택 ~1MB라 수만 개 만들면 메모리·컨텍스트 스위칭 폭증(7.1).
- **가상 스레드**: JVM이 관리하는 **경량 스레드**. 수십만~수백만 개도 가능. 소수의 OS 스레드(캐리어) 위에서 돌린다.

**핵심 원리 — 블로킹 시 캐리어를 '양보'한다**:
```
가상 스레드가 I/O로 블로킹되면
   → 자기를 실행하던 OS 스레드(캐리어)에서 '내려온다(언마운트)'
   → 그 캐리어는 그동안 '다른 가상 스레드'를 실행
   → I/O가 끝나면 가상 스레드는 다시 아무 캐리어에 '올라타(마운트)' 이어 실행
```
그래서 가상 스레드 10만 개가 모두 I/O 대기 중이어도, 실제 OS 스레드는 몇 개만으로 충분하다.
6.2의 Non-blocking(Selector)이 주던 확장성을, **콜백 없이 평범한 블로킹 코드 스타일 그대로** 얻는 것.

```java
// 작업마다 가상 스레드 (수만 개도 가뿐)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> { someBlockingIo(); });  // 블로킹 코드 그대로!
    }
}
```

> ★ 주의 — 가상 스레드는 **I/O 바운드**(대기가 많은 작업)에서 빛난다. **CPU 바운드**(계속 계산)는
> 코어 수가 한계라 가상 스레드를 아무리 늘려도 더 빨라지지 않는다(7.1). "스레드가 싸다 ≠ 계산이 빨라진다".

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

- **Q. 콜백(CompletableFuture)과 가상 스레드는 둘 다 비동기/동시성 도구인데 무엇이 다른가?**
  - 내 답: CompletableFuture는 '안 멈추는 콜백 체인'으로 효율적이나 코드가 복잡(콜백 지옥). 가상
    스레드는 '평범한 블로킹 코드를 그대로' 쓰되 스레드를 싸게 만들어 대량 동시성을 얻는다(읽기 쉬움). (1-1, 1-2)

- **Q. "스레드가 싸다"고 CPU 계산도 빨라지나?**
  - 내 답: 아니다. 가상 스레드는 I/O 대기 동시성을 싸게 해줄 뿐, 실제 계산은 코어 수가 한계다.
    스레드 수와 계산 처리량은 별개. (1-2 주의)
