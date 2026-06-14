# PART 7 — 멀티스레딩과 동시성: 7.6 정교한 락 (LockSupport → ReentrantLock → tryLock)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.6 정교한 락**을 다룬다.
> 7.5에서 본 synchronized의 한계(무한 대기·인터럽트 불가·불공정)를 ReentrantLock으로 보완하고, 데드락을 다룬다.

---

## 0. 들어가기 전에 — 핵심 용어
- **락(lock)**: 공유 자원에 한 번에 하나만 들어가게 하는 '자물쇠'. synchronized는 암묵적 락, ReentrantLock은 명시적 락.
- **ReentrantLock**: 직접 `lock()`/`unlock()` 하는 명시적 락. synchronized와 같은 효과 + 추가 기능. (unlock은 반드시 finally에서)
- **Reentrant(재진입 가능)**: 같은 스레드가 이미 쥔 락을 다시 lock()해도 되는 성질.
- **tryLock()**: 락을 '시도'하고 못 얻으면 기다리지 않고 false 반환(또는 시간 제한). 무한 대기 회피·데드락 회피에 사용.
- **데드락(deadlock, 교착 상태)**: 두 스레드가 서로가 쥔 락을 기다리며 영원히 멈추는 상황(락 순서 엇갈림).
- **공정 락(fair lock)**: 먼저 기다린 스레드가 먼저 락을 얻게 보장(`new ReentrantLock(true)`).
- **LockSupport**: park()/unpark()로 스레드를 멈추고 깨우는 저수준 도구(ReentrantLock의 내부 부품).

한 줄 그림: **ReentrantLock은 synchronized의 한계(무한 대기·인터럽트 불가·불공정)를 tryLock·lockInterruptibly·fair로 보완한다. 데드락은 락 순서 엇갈림이 원인이고, tryLock으로 회피한다.**

---

## 1. 학습 내용 — 명시적 락과 데드락

### 1-0. 출발점 — synchronized로 충분한데 왜 또 락이 필요한가
7.5에서 synchronized가 가시성·원자성을 둘 다 막아준다고 했다. 그런데 왜 또 다른 락이 필요할까?
synchronized에는 실무에서 곤란한 **세 가지 한계**가 있기 때문이다.
1. **무한 대기** — 락을 못 얻으면 얻을 때까지 무조건 기다린다. "0.5초만 기다려보고 안 되면 포기"가 불가능.
2. **인터럽트 불가** — 락을 기다리는 스레드를 중간에 취소(깨우기)할 수 없다. 종료 신호를 줘도 못 빠져나온다.
3. **공정성 없음** — 먼저 기다린 스레드가 먼저 얻는다는 보장이 없다(어떤 스레드는 계속 밀릴 수 있음 — 기아 starvation).

이 세 가지를 풀어주는 게 `ReentrantLock`이고, 그 바닥에는 `LockSupport`라는 저수준 부품이 있다.
아래에서 '바닥(LockSupport) → 실무 표준(ReentrantLock) → 데드락과 회피' 순으로 쌓아 올린다.

### 1-1. LockSupport — 락의 바닥에 있는 저수준 부품
`LockSupport.park()`(현재 스레드를 멈춤) / `unpark(thread)`(특정 스레드를 깨움)는 스레드를 재우고 깨우는
**가장 저수준** 도구다. 직접 쓸 일은 거의 없지만, **ReentrantLock 같은 상위 락이 내부적으로 이걸로 만들어진다.**
"락이 '기다린다'는 동작의 바닥에는 결국 park/unpark가 있다" 정도로 알면 된다. (7.3의 LockSupport 부품 언급과 연결)

### 1-2. ReentrantLock — 직접 잠그고 푸는 '명시적 락' (실무 표준)
**무엇**: synchronized와 같은 효과(상호 배제 → 원자성·가시성)를 주되, 위 한계를 보완하는 기능을 추가한
**명시적 락**. synchronized가 '자동 자물쇠'라면 ReentrantLock은 '내가 직접 잠그고 푸는 자물쇠'다.

**기본 사용 — `try { lock } finally { unlock }`**:
```java
private final ReentrantLock lock = new ReentrantLock();

void increment() {
    lock.lock();              // 락 획득 (얻을 때까지 대기)
    try {
        count++;             // 임계 구역(보호 대상)
    } finally {
        lock.unlock();       // ★ 반드시 finally에서 — 예외가 나도 풀리도록
    }
}
```

> ★ 가장 중요한 주의 — **unlock은 반드시 finally에서.** synchronized는 블록을 벗어나면(예외 포함) 락이
> 자동으로 풀린다. 하지만 ReentrantLock은 내가 직접 `unlock()`을 호출해야 한다. 만약 임계 구역에서
> 예외가 났는데 unlock이 try 안에 있었다면 그 줄에 도달 못 해 **락이 영영 안 풀리고**, 다른 스레드는
> 영원히 못 들어간다(사실상 데드락). 그래서 unlock은 항상 finally에 둔다(7.3 try-with-resources의 자원 정리와 같은 사고).

**"Reentrant(재진입 가능)"의 뜻**: 같은 스레드가 **이미 쥔 락을 다시 `lock()` 해도** 된다는 것. JVM이
획득 횟수를 세어, 그만큼 unlock해야 완전히 풀린다. (synchronized도 재진입 가능하다.) 덕분에 락을 쥔
메서드가 같은 락을 쓰는 다른 메서드를 호출해도 자기 자신에게 막히지 않는다.

**synchronized 한계 3가지의 보완**:
1. 무한 대기 → **`tryLock()` / `tryLock(시간, 단위)`**: 락을 '시도'만 하고, 못 얻으면(또는 정해진 시간
   안에 못 얻으면) `false`를 반환해 **포기**할 수 있다. → 데드락 회피·응답성에 핵심.
2. 인터럽트 불가 → **`lockInterruptibly()`**: 락을 기다리는 동안 인터럽트가 오면 대기를 멈추고 빠져나온다.
3. 공정성 없음 → **`new ReentrantLock(true)`**: '공정(fair) 락'. 먼저 기다린 스레드가 먼저 얻도록(FIFO)
   보장한다. (대신 약간 느릴 수 있어, 기본값은 비공정.)

### 1-3. 데드락(교착 상태) — 왜 생기고 어떻게 푸나
**데드락(deadlock)** = 둘 이상의 스레드가 **서로가 쥔 락을 기다리며 영원히 멈추는** 상황. 전형은
'락을 잡는 순서가 엇갈릴 때'다.
```
시간 →
스레드1: lockA.lock() (성공) ──── lockB.lock() 대기... (스레드2가 쥠)  ┐
스레드2: lockB.lock() (성공) ──── lockA.lock() 대기... (스레드1이 쥠)  ┘  서로 무한 대기
```
스레드1은 B를, 스레드2는 A를 기다리는데, 둘 다 자기가 쥔 걸 안 놓으니 영원히 안 풀린다. `lock()`/
synchronized는 무한 대기라 스스로 빠져나올 수 없다.

> 참고 — 데드락이 성립하는 4가지 조건(코프만 조건): ① 상호 배제(한 번에 하나만), ② 점유하며 대기
> (가진 채 또 기다림), ③ 비선점(강제로 못 뺏음), ④ 순환 대기(서로 물고 물림). 이 중 **하나만 깨도**
> 데드락이 안 생긴다 — 아래 두 해법이 각각 조건 하나씩 깨는 것이다.

**해법 두 가지**:
- **(근본) 락 순서 통일** — 모든 스레드가 락을 **항상 같은 순서로**(예: 무조건 A 먼저, 그다음 B) 잡게
  하면 '순환 대기'(조건 ④)가 사라져 데드락이 원천 차단된다. 가장 권장되는 설계 원칙.
- **(회피) tryLock(시간)** — 둘째 락을 `tryLock`으로 시도해 못 얻으면, **이미 쥔 첫 락을 풀고** 잠시 뒤
  재시도한다. '점유하며 대기'(조건 ②)를 깨는 것. 순서를 못 통일하는 상황에서 교착을 피하는 안전장치.

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

- **Q. "Reentrant(재진입)"가 무슨 뜻인가?**
  - 내 답: 같은 스레드가 이미 쥔 락을 다시 lock()해도 막히지 않는 것(획득 횟수를 세어 그만큼 unlock해야
    완전히 풀림). 락을 쥔 메서드가 같은 락을 쓰는 다른 메서드를 호출해도 자기 자신에게 안 막힌다. (1-2)

- **Q. 데드락의 근본 해법과 임시 회피책의 차이는?**
  - 내 답: 근본 해법 = 모든 스레드가 락을 같은 순서로 잡기(순환 대기 제거). 회피책 = tryLock(시간)으로
    못 얻으면 쥔 락 풀고 재시도(점유하며 대기 제거). 가능하면 순서 통일이 우선. (1-3)

- **Q. synchronized도 충분한데 ReentrantLock을 쓰는 경우는?**
  - 내 답: "못 얻으면 포기/타임아웃"이 필요하거나(tryLock), 대기 중 취소가 필요하거나(lockInterruptibly),
    공정성이 필요할 때. 단순한 보호면 synchronized가 더 간결하고 안전(자동 해제)하다. (1-0, 1-2)
