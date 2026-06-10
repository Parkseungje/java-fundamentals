# PART 7 — 멀티스레딩과 동시성: 7.6 정교한 락 (LockSupport → ReentrantLock → tryLock)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.6 정교한 락**을 다룬다.
> 7.5에서 본 synchronized의 한계(무한 대기·인터럽트 불가·불공정)를 ReentrantLock으로 보완하고, 데드락을 다룬다.

---

## 1. 학습 내용 — 명시적 락과 데드락

### LockSupport (저수준 도구)
`LockSupport.park()`/`parkNanos()`/`unpark(thread)`는 스레드를 멈추고 깨우는 **가장 저수준** 도구다.
너무 저수준이라 직접 쓸 일은 거의 없고, **ReentrantLock 등이 내부적으로 쓰는 부품**이다. "락의 바닥에는
이런 park/unpark가 있다" 정도로 이해하면 된다.

### ReentrantLock (실무 표준)
synchronized와 같은 효과(상호 배제 → 원자성·가시성)를 주면서, synchronized가 못 하는 기능을 더한
**명시적 락**이다. `lock()`으로 얻고 `unlock()`으로 푼다.
- ★ **unlock은 반드시 finally에서**: synchronized는 블록을 벗어나면 자동으로 락이 풀리지만,
  ReentrantLock은 직접 unlock해야 한다. 임계 구역에서 예외가 나도 풀리도록 `try { ... } finally { unlock() }`
  패턴이 필수다. 안 그러면 락이 안 풀려 다른 스레드가 영원히 못 들어가는 데드락이 된다.
- **Reentrant(재진입 가능)**: 같은 스레드가 이미 쥔 락을 다시 lock()해도 된다(획득 횟수를 세어 관리).

**synchronized 한계 3가지의 보완**:
1. 무한 대기 → **`tryLock(시간, 단위)`**: 정해진 시간만 시도하고 못 얻으면 `false`(포기 가능).
2. 인터럽트 불가 → **`lockInterruptibly()`**: 락 대기 중 인터럽트로 빠져나올 수 있음.
3. 공정성 보장 X → **`new ReentrantLock(true)`**: 먼저 기다린 스레드가 먼저 획득(FIFO 공정 락).

### 데드락(교착 상태)과 tryLock 회피
**데드락의 전형은 '락을 잡는 순서가 엇갈릴 때'**다.
- 스레드1: lockA 잡고 → lockB 기다림
- 스레드2: lockB 잡고 → lockA 기다림
- 서로 상대가 쥔 락을 기다리며 영원히 멈춘다. `lock()`/synchronized는 무한 대기라 못 빠져나온다.

**회피**: `tryLock(시간)`은 못 얻으면 false를 주므로, **이미 쥔 락을 풀고 잠시 뒤 재시도**해 교착에서
빠져나올 수 있다. (근본 해결책은 **모든 스레드가 락을 같은 순서로** 잡게 하는 '락 순서 통일'.)

---

## 2. 실습으로 확인하기

> - **가설 1**: ReentrantLock으로 보호하면 count++ 유실이 없다(unlock은 finally).
> - **가설 2**: 락 순서가 엇갈리면 데드락. tryLock(시간) 재시도로 회피된다.
> - **가설 3**: tryLock(시간)은 점유 중인 락을 정해진 시간만 기다리고 못 얻으면 포기(false).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ReentrantLockBasic` | 기본 사용/주의? | lock/unlock + try-finally로 count++ |
| `Example2_DeadlockAndTryLock` | 데드락/회피? | 엇갈린 lock() vs tryLock 재시도 |
| `Example3_ReentrantLockFeatures` | synchronized 한계 보완? | tryLock(timeout) 포기 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s06_lock.Example1_ReentrantLockBasic
java -cp build/classes/java/main com.study.part07_concurrency.s06_lock.Example2_DeadlockAndTryLock
java -cp build/classes/java/main com.study.part07_concurrency.s06_lock.Example3_ReentrantLockFeatures
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (기본)** — 가설 1.
- 2스레드 × 100만 = 정확히 **2,000,000**(유실 없음). lock/unlock + try-finally. ✅

**예시 2 (데드락/회피)** — 가설 2.

| 방식 | 결과 |
|---|---|
| lock() 엇갈림 | 1.5초 뒤에도 두 스레드 살아있음 → **데드락 발생** |
| tryLock(시간) 재시도 | 두 작업 모두 **완료**(교착 회피) |

→ 락 순서 엇갈림 = 데드락. tryLock은 못 얻으면 풀고 재시도해 회피. ✅ (데드락 스레드는 데몬이라 정리됨)

**예시 3 (한계 보완)** — 가설 3.
- holder가 락을 2초 점유하는 동안 trier가 `tryLock(300ms)` → **300ms 후 false**("포기").
  synchronized였다면 풀릴 때까지 무한 대기였을 것. ✅

### 세 예시를 관통하는 결론
ReentrantLock은 synchronized와 같은 보호를 하되 **직접 lock/unlock**(unlock은 반드시 finally)하며,
synchronized의 한계(무한 대기·인터럽트 불가·불공정)를 **tryLock(시간)·lockInterruptibly()·fair 락**으로
보완한다(예시1, 3). 그 덕에 **데드락 회피**(못 얻으면 포기·재시도)와 응답성 있는 락 획득이 가능하다(예시2).
다만 근본적 데드락 예방은 '락 순서 통일'이라는 설계 원칙임을 기억한다.

---

## 3. 자기 점검

- **Q. ReentrantLock에서 unlock을 finally에 두는 이유는?**
  - 내 답: synchronized와 달리 자동 해제가 없어, 임계 구역에서 예외가 나면 unlock이 호출되지 않아
    락이 영영 안 풀린다(데드락). finally로 정상/예외 모두에서 풀리게 한다. (Example1)

- **Q. 데드락은 왜 생기고 tryLock으로 어떻게 피하나?**
  - 내 답: 두 스레드가 두 락을 엇갈린 순서로 잡고 서로를 기다리면 데드락. tryLock(시간)은 못 얻으면
    false를 주므로 쥔 락을 풀고 재시도해 교착을 피한다. 근본은 락 순서 통일. (Example2)

- **Q. synchronized 대비 ReentrantLock의 장점 3가지는?**
  - 내 답: tryLock(시간)으로 무한 대기 회피, lockInterruptibly()로 인터럽트 가능, fair 락으로 공정성.
    대신 unlock을 직접 호출해야 하는 책임이 따른다. (Example3)
