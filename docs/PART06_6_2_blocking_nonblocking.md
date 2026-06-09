# PART 6 — I/O와 직렬화: 6.2 Stream vs Channel, Blocking vs Non-blocking

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.2 Blocking vs Non-blocking**을 다룬다.
> 6.1에서 예고한 "1만 접속 문제"와 NIO Selector의 해법을 본다.

---

## 1. 학습 내용 — Blocking의 한계와 Non-blocking + Selector

### 로우레벨의 불편함 — Blocking Stream
전통 Stream(java.io)은 **1바이트씩·단방향·Blocking**이다. 특히 Blocking이 핵심 문제다 —
`read()`가 읽을 데이터가 아직 없으면 **데이터가 올 때까지 그 스레드를 멈춰 세운다**(반환하지 않음,
`close()`로만 빠져나올 수 있다). 그래서 **연결 1개 = 스레드 1개**가 되어, 동시 접속 1만 명이면
스레드도 1만 개가 필요하다. 스레드 1개 ≈ 1MB + 컨텍스트 스위칭 비용이라 비현실적이다.

### NIO — Channel + Buffer + Non-blocking
- **Channel ↔ Buffer**: 데이터는 항상 Buffer를 거쳐 양방향 Channel로 오간다.
- **Non-blocking**: `configureBlocking(false)`로 두면 `read()`가 데이터가 없어도 **즉시 0을 반환**한다
  (멈추지 않음). 스레드는 다른 일을 하거나 나중에 다시 확인(polling)할 수 있다.
- **Selector(멀티플렉서)**: 여러 Non-blocking 채널을 한 곳에 등록하고, `select()`로 "지금 읽을
  데이터가 준비된 채널"만 골라낸다. 그래서 **한 스레드가 수천~수만 채널을 감시**할 수 있다.

### 1만 접속 문제의 해법
- **Blocking IO**: 연결 1만 개 → 스레드 1만 개 (각 스레드가 read()에 묶여 멈춤). 비현실적.
- **NIO + Selector**: 소수 스레드(심지어 1개)가 1만 채널을 감시하다, 데이터가 준비된 채널만 처리.
- **주의**: Non-blocking이 항상 좋은 건 아니다. **I/O 대기가 많을 때** 이득이고, **CPU 바운드(계산
  위주) 작업에는 오히려 손해**다(어차피 CPU가 바쁘므로 멀티플렉싱 이점이 없고 복잡성만 늘어난다).

---

## 2. 실습으로 확인하기

> - **가설 1**: Blocking read는 데이터가 올 때까지 스레드를 멈춰 세운다.
> - **가설 2**: Non-blocking read는 데이터가 없어도 멈추지 않고 즉시 0을 반환한다.
> - **가설 3**: Selector로 한 스레드가 여러 채널을 감시·처리할 수 있다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_BlockingRead` | Blocking은 어떻게? | PipedStream + 지연 write로 대기 측정 |
| `Example2_NonBlockingRead` | Non-blocking은? | Pipe 채널 non-blocking read |
| `Example3_SelectorMultiplexing` | 다수 연결 처리? | Selector + Pipe 2개를 1스레드로 |

(네트워크는 비결정적이라, JVM 내부 `Pipe` 채널로 결정적으로 재현한다.)

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s02_blocking_nonblocking.Example1_BlockingRead
java -cp build/classes/java/main com.study.part06_io.s02_blocking_nonblocking.Example2_NonBlockingRead
java -cp build/classes/java/main com.study.part06_io.s02_blocking_nonblocking.Example3_SelectorMultiplexing
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Blocking)** — 가설 1.
- reader 스레드가 `read()`에서 약 1초간 멈춰 있다가, main이 `write(42)`한 뒤에야 값을 반환. ✅
- → Blocking IO는 데이터가 올 때까지 스레드를 점유한 채 멈춘다(연결 1개 = 스레드 1개의 원인).

**예시 2 (Non-blocking)** — 가설 2.

| 시점 | `source.read(buffer)` |
|---|---|
| 데이터 쓰기 전 | **0** (즉시 반환, 대기 안 함) |
| 데이터 쓴 후 | **3** (읽은 바이트 수) |

→ Non-blocking은 데이터가 없어도 멈추지 않고 즉시 0을 준다(Blocking과 정반대). ✅

**예시 3 (Selector)** — 가설 3.
- 단 하나의 `selector-thread`가 `PIPE-1`, `PIPE-2` 두 채널의 데이터를 모두 처리.
- main이 시차를 두고 두 파이프에 쓰면 `select()`가 준비된 채널을 깨워 같은 스레드가 순서대로 처리. ✅
- → 한 스레드가 다수 채널을 감시(멀티플렉싱) = 1만 접속을 소수 스레드로 처리하는 원리.

### 세 예시를 관통하는 결론
Blocking IO는 직관적이지만 read()가 스레드를 멈춰(예시1) "연결당 스레드 1개"를 강요해 대규모 동시
접속에 불리하다. NIO는 Non-blocking으로 스레드를 멈추지 않고(예시2), Selector로 한 스레드가 여러
채널을 감시(예시3)해 소수 스레드로 다수 연결을 처리한다. 단 이 이득은 **I/O 대기가 많은 경우**에
한정되며, CPU 바운드 작업에는 오히려 손해다 — "Non-blocking = 무조건 빠름"이 아니라 상황에 맞는 도구다.

---

## 3. 자기 점검

- **Q. Blocking IO가 동시 접속 1만 명에 불리한 이유는?**
  - 내 답: read()가 데이터 올 때까지 스레드를 멈추므로 연결마다 스레드가 필요하다. 1만 연결 = 1만
    스레드 ≈ 막대한 메모리 + 컨텍스트 스위칭 비용. (Example1의 스레드 정지)

- **Q. Selector(멀티플렉서)가 그 문제를 어떻게 푸나?**
  - 내 답: 여러 Non-blocking 채널을 한 Selector에 등록하고 select()로 준비된 채널만 골라, 한 스레드가
    다수 채널을 처리한다. (Example3의 1스레드 2채널)

- **Q. Non-blocking이 항상 좋은가?**
  - 내 답: 아니다. I/O 대기가 많을 때 이득이고, CPU 바운드(계산 위주) 작업에는 멀티플렉싱 이점이
    없고 복잡성만 늘어 오히려 손해다.
