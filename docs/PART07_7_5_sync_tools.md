# PART 7 — 멀티스레딩과 동시성: 7.5 동기화 도구 3종 (synchronized / volatile / Atomic)

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.5 동기화 도구 3종**을 다룬다.
> (Atomic·CAS는 "9-섹션 깊이 학습" 권장) 7.4의 가시성·원자성 문제를 실제로 해결한다.

---

## 1. 학습 내용 — 세 도구의 진화

진화 서사: **안전하지만 느린 synchronized → 가시성만 싸게 주는 volatile → 락 없이 원자성을 얻는 Atomic.**

### synchronized — 둘 다 해결(가시성+원자성), 대신 느림
- 메서드/블록에 붙인다. 모든 객체는 **모니터 락(intrinsic lock)** 을 하나 갖고, synchronized 구역에
  들어가려면 그 락을 얻어야 한다. 한 스레드가 락을 쥐면 다른 스레드는 **BLOCKED** 상태로 대기한다.
- 한 번에 한 스레드만 임계 구역에 들이므로(상호 배제) **원자성**, 락 경계에서 메모리를 동기화하므로
  **가시성**을 함께 보장한다.
- static synchronized의 락 대상은 `Class` 객체. 블록(`synchronized(obj){}`)으로 범위를 최소화할 수 있다.
- **한계 3가지**: ① 무한 대기(타임아웃 불가) ② 인터럽트 불가 ③ 공정성 보장 X. → 7.6 ReentrantLock에서 보완.

### volatile — 가시성만, 원자성 ❌
- 변수를 각 코어 캐시 대신 **항상 메인 메모리에서 직접 R/W** → 가시성 보장. 하지만 `count++`(읽기-증가-쓰기
  3단계)는 여전히 위험하다(7.4 예시3).
- 적합: **한 스레드만 쓰고 여러 스레드가 읽는 플래그**(`volatile boolean shutdown`).

### Atomic + CAS — 락 없이 원자성, 빠름
- `AtomicInteger.incrementAndGet()`처럼 락 없이 원자적. 내부적으로 **CAS(Compare And Swap)** CPU 명령을 쓴다.
- **CAS 3단계**:
  1. 현재 값 읽기 (A)
  2. 새 값 계산 (B = A + 1)
  3. "메모리 값이 아직 A면 B로 교체"를 원자적으로 시도 → 같으면 성공, 다르면(다른 스레드가 수정) **재시도**.
- 락으로 막는 게 아니라 "바뀌었으면 다시 해"라서 **논블로킹**이고, 보통 더 빠르다.
- 주의: 경쟁이 심하면 CAS **재시도**가 잦아지고, **ABA 문제**(A→B→A로 돌아오면 안 바뀐 걸로 오인) 가능.

### 비교표

| 도구 | 가시성 | 원자성 | 방식 | 성능 |
|---|---|---|---|---|
| synchronized | ✅ | ✅ | 락(블로킹) | 가장 느림 |
| volatile | ✅ | ❌ | 메모리 동기화 | 빠름(단 원자성 X) |
| Atomic | ✅ | ✅ | CAS(논블로킹) | 빠름 |

---

## 2. 실습으로 확인하기

> - **가설 1**: synchronized로 감싸면 count++ 유실이 사라진다(원자성+가시성).
> - **가설 2**: Atomic은 락 없이 CAS로 원자성을 보장한다(직접 CAS 루프도 정확).
> - **가설 3**: volatile은 유실(원자성 X), synchronized/Atomic은 정확. Atomic이 보통 더 빠르다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 도구 | 시나리오 |
|---|---|---|
| `Example1_Synchronized` | synchronized | synchronized 메서드로 count++ |
| `Example2_AtomicCAS` | Atomic/CAS | incrementAndGet + 직접 compareAndSet 루프 |
| `Example3_ThreeToolsComparison` | 셋 비교 | 정확성 + 성능 측정 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s05_sync_tools.Example1_Synchronized
java -cp build/classes/java/main com.study.part07_concurrency.s05_sync_tools.Example2_AtomicCAS
java -cp build/classes/java/main com.study.part07_concurrency.s05_sync_tools.Example3_ThreeToolsComparison
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (synchronized)** — 가설 1.
- 2스레드 × 100만 = 정확히 **2,000,000**(유실 없음). 원자성+가시성 둘 다 해결. ✅

**예시 2 (Atomic/CAS)** — 가설 2.
- `incrementAndGet()` → 2,000,000. 직접 `compareAndSet` 루프(get→+1→CAS, 실패 시 재시도) → 2,000,000.
- 락 없이도 정확. ✅

**예시 3 (세 도구 비교)** — 가설 3. (2스레드 × 100만, 실측)

| 도구 | 결과 | 시간 | 판정 |
|---|---|---|---|
| volatile | 1,135,641 / 200만 | 33 ms | 유실(원자성 X), 빠름 |
| synchronized | 2,000,000 | 42 ms | 정확, 느린 편 |
| Atomic(CAS) | 2,000,000 | 31 ms | 정확, 가장 빠름 |

→ volatile은 빠르지만 틀리고, synchronized/Atomic은 정확하며 Atomic이 보통 더 빠르다. ✅
(수치는 실행/환경마다 다름)

### 세 예시를 관통하는 결론
세 도구는 보장 범위와 방식이 다르다. **volatile은 가시성만**(원자성 필요한 카운터엔 부적합),
**synchronized는 락으로 둘 다**(확실하지만 느리고 한계 있음), **Atomic은 CAS로 락 없이 둘 다**(빠름).
선택 기준: 읽기 전용 플래그 → volatile, 단일 카운터/단일 변수 원자 연산 → Atomic, **여러 변수를 한
묶음으로** 보호하거나 복합 로직의 원자성이 필요 → synchronized(또는 7.6의 ReentrantLock).

---

## 3. 자기 점검

- **Q. synchronized/volatile/Atomic의 해결 범위와 성능을 비교하면?**
  - 내 답: synchronized=가시성+원자성/락/가장 느림, volatile=가시성만/메모리 동기화/빠름, Atomic=
    가시성+원자성/CAS 논블로킹/빠름. (Example3 표)

- **Q. CAS의 3단계와 ABA 문제는?**
  - 내 답: ①현재 값 읽기(A) ②새 값 계산(B) ③메모리가 아직 A면 B로 교체(아니면 재시도). ABA 문제는
    값이 A→B→A로 돌아왔을 때 "안 바뀐 것"으로 오인하는 것(AtomicStampedReference로 버전을 붙여 방지). (Example2)

- **Q. 단순 카운터에 synchronized보다 Atomic이 나은 이유는?**
  - 내 답: Atomic은 락을 안 잡고 CAS로 처리해(논블로킹) 대기가 없고 보통 더 빠르다. 단 단일 변수
    원자 연산에 한정 — 여러 변수를 한꺼번에 보호해야 하면 락(synchronized)이 필요하다. (Example3)
