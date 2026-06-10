# PART 7 — 멀티스레딩과 동시성: 7.2 Sync/Async × Blocking/Non-Blocking 4분면

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.2 Sync/Async × Blocking/Non-Blocking**을 다룬다.
> (커리큘럼에서 "★ 면접 단골"로 표시된 핵심 구간) 두 축을 코드 동작으로 구분한다.

---

## 1. 학습 내용 — 두 개의 독립된 축

이 둘은 자주 헷갈리지만 **서로 다른 축**이다. 같이 보면 안 되고, 따로 본 뒤 조합해야 한다.

### 축 1 — Blocking / Non-Blocking ("제어권")
**호출한 함수가 일을 끝낼 때까지 제어권을 붙잡는가**의 문제.
- **Blocking**: 일이 끝날 때까지 제어권을 안 돌려준다 → 호출자가 그 줄에서 **멈춘다**.
- **Non-Blocking**: 일이 안 끝났어도 제어권을 **즉시 돌려준다** → 호출자는 다른 일을 하거나 나중에 폴링.

### 축 2 — Sync / Async ("결과 처리")
**결과(완료)를 누가 챙기는가**의 문제.
- **Sync(동기)**: 호출한 쪽이 결과를 **직접 받아** 처리한다(반환값을 받아 다음 줄에서 처리).
- **Async(비동기)**: 호출된 쪽이 일을 끝낸 뒤 **콜백으로 알려준다** → 호출자는 결과를 직접 챙기지 않는다.

### 두 축을 합친 4분면

| | Blocking | Non-Blocking |
|---|---|---|
| **Sync** | 전통 IO (가장 단순/비효율) | NIO Polling |
| **Async** | `Future.get()` | `CompletableFuture`/콜백 (가장 효율적) |

- **Sync-Blocking**: 호출자가 멈춰 결과를 직접 받음. 가장 단순하지만 비효율(전통 IO read).
- **Sync-Non-Blocking**: 제어권은 즉시 받지만 결과는 호출자가 반복 확인(polling). (NIO non-blocking + while)
- **Async-Blocking**: 작업은 비동기로 던졌지만 결과는 `Future.get()`에서 **기다린다** → 결국 막힘.
- **Async-Non-Blocking**: 던지고 콜백만 등록 → 안 막히고 끝나면 콜백이 처리. **가장 효율적**(CompletableFuture).

핵심: **"비동기로 던졌다"고 다 좋은 게 아니다.** `Future.get()`은 Async지만 받을 때 Blocking이라 결국
호출자가 멈춘다. 콜백 기반 CompletableFuture라야 안 막히고 효율적이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: Blocking은 호출자를 멈추고, Non-blocking은 제어권을 즉시 돌려줘 호출자가 다른 일을 한다.
> - **가설 2**: Sync는 호출자가 결과를 직접 받고, Async는 콜백이 완료 시 결과를 처리한다.
> - **가설 3**: Future.get()은 Async지만 get()에서 막히고(Async-Blocking), CompletableFuture 콜백은 안 막힌다(Async-Non-blocking).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 축 | 시나리오 |
|---|---|---|
| `Example1_BlockingVsNonBlocking` | 제어권 | 500ms 작업을 blocking/non-blocking 호출 |
| `Example2_SyncVsAsync` | 결과 처리 | 결과를 직접 받기 vs 콜백 |
| `Example3_FourQuadrants` | 종합 | Future.get vs CompletableFuture |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s02_sync_async.Example1_BlockingVsNonBlocking
java -cp build/classes/java/main com.study.part07_concurrency.s02_sync_async.Example2_SyncVsAsync
java -cp build/classes/java/main com.study.part07_concurrency.s02_sync_async.Example3_FourQuadrants
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (제어권)** — 가설 1.
- Blocking: `blockingFetch()` 줄에서 **약 513ms 멈춤**(그동안 출력 없음) → 결과.
- Non-blocking: 호출 즉시 제어권 반환 → main이 "다른 일 하는 중..."을 5번 출력하다 완료 확인. ✅

**예시 2 (결과 처리)** — 가설 2.
- Sync: 호출자가 결과를 직접 받아 "동기 결과 처리: 데이터".
- Async: main이 "콜백 등록 후 바로 다음 일"을 먼저 찍고, 잠시 뒤 **콜백**이 결과 처리. ✅

**예시 3 (4분면)** — 가설 3.
- `Future.get()`: submit은 즉시 반환(Async)이지만 **get()에서 멈춤**(Blocking) → 결국 호출자 멈춤.
- `CompletableFuture.thenAccept()`: 호출자 안 멈추고 다음 일, 끝나면 콜백 처리(Async-Non-blocking). ✅

### 세 예시를 관통하는 결론
Blocking/Non-blocking은 "제어권을 바로 돌려주는가"(예시1), Sync/Async는 "결과를 누가 챙기는가"(예시2)로
**서로 독립된 축**이다. 둘을 합치면 4분면이 되고, 가장 효율적인 조합은 **Async-Non-blocking**(콜백 기반
CompletableFuture)이다(예시3). 흔한 오해는 "비동기면 빠르다"인데, `Future.get()`처럼 결과를 기다리면
결국 Blocking이 된다 — 진짜 비논블로킹은 콜백으로 "끝나면 알려줘"여야 한다. (Future/CompletableFuture는
7.8/7.9에서 본격적으로 다룬다.)

---

## 3. 자기 점검

- **Q. Blocking/Non-blocking과 Sync/Async를 가르는 두 기준은?**
  - 내 답: Blocking/Non-blocking = "제어권을 바로 돌려주는가". Sync/Async = "결과를 호출자가 직접
    챙기는가, 콜백이 알려주는가". 둘은 독립된 축. (Example1 vs Example2)

- **Q. Future.get()은 4분면 중 어디인가? 왜?**
  - 내 답: Async-Blocking. 작업은 비동기로 던졌지만(submit) 결과는 get()에서 기다리며 호출자가 멈추기
    때문. (Example3 A)

- **Q. 가장 효율적인 조합과 그 이유는?**
  - 내 답: Async-Non-blocking(CompletableFuture 콜백). 호출자가 안 멈추고(Non-blocking) 결과는 끝났을
    때 콜백이 처리(Async)하므로 자원을 가장 잘 쓴다. (Example3 B)
