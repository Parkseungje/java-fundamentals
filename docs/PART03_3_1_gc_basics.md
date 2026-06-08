# PART 3 — GC(가비지 컬렉션): 3.1 GC 기본 + 약한 세대 가설 + 참조 카운팅의 한계

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.1 GC 기본**을 다룬다.
> PART 2(메모리 영역)에서 본 Heap을, 이번엔 "어떻게 자동으로 비우는가"의 관점으로 본다.

---

## 1. 학습 내용 — GC는 무엇을, 어떻게 회수하는가

### Garbage와 GC
**Garbage(쓰레기)** = 더 이상 참조되지 않는 객체. **GC(Garbage Collection)** = 그런 객체를
Heap에서 자동으로 찾아 회수하는 것. C/C++처럼 개발자가 직접 `free`하지 않아도 JVM이 알아서 한다.

### 무엇이 garbage인가 — 도달 가능성(Reachability)
GC가 "이 객체가 쓰레기인가"를 판단하는 기준은 **루트(GC Root)에서 도달 가능한가**다.
GC Root는 스택의 지역 변수, static 필드 등 "확실히 살아있는 시작점"이다. 여기서 참조를 타고
**도달할 수 있으면 살아있는 객체, 도달할 수 없으면 garbage**다. 이를 Reachability Analysis라 한다.

### 약한 세대 가설 (Weak Generational Hypothesis)
GC 설계의 근거가 되는 경험칙 두 가지:
- **대부분의 객체는 금방 죽는다** (생성되고 얼마 안 돼 참조가 끊긴다).
- **오래된 객체가 젊은 객체를 참조하는 경우는 드물다.**

이 가설 덕분에 Heap을 "젊은 객체 구역(Young)"과 "오래된 객체 구역(Old)"으로 나눠, 자주 죽는
Young만 자주 청소하면 효율적이라는 **세대별(generational) 설계**가 나온다(3.2에서 심화).

### STW (Stop The World)
GC가 도는 동안에는 **모든 애플리케이션 스레드가 잠시 멈춘다.** 이 정지 구간이 STW다.
GC 로그의 "Pause ... Xms"가 바로 그 시간이며, **GC 튜닝은 보통 이 STW 시간을 줄이는 것**이다.

### 참조 카운팅의 치명적 한계 (왜 JVM은 안 쓰나)
객체 생존을 판단하는 다른 방법으로 **참조 카운팅(reference counting)** 이 있다. 객체마다 "나를
가리키는 참조 수" 카운터를 두고, 0이 되면 즉시 회수하는 방식이다(장점: 즉시 회수).

하지만 **순환 참조(circular reference)** 에서 치명적으로 무너진다.
```
Root --X--> A ⇄ B   (Root에서 A로 가는 길이 끊겼지만 A와 B가 서로를 가리킴)
```
A는 B가 가리켜서 카운트 1, B는 A가 가리켜서 카운트 1. 외부에서 끊어도 서로 때문에 카운트가
**0이 되지 않아 영원히 회수되지 않는다 = 메모리 누수.** 게다가 참조가 바뀔 때마다 카운터를
갱신하는 비용도 있다. 그래서 **JVM은 참조 카운팅을 쓰지 않고 도달 가능성 분석을 쓴다.**
도달 가능성은 "루트에서 닿는가"만 보므로, 서로만 가리키는 순환 묶음도 루트에서 끊기면 통째로 회수한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: GC는 도달 가능성으로 판단한다 — 루트에서 닿으면 생존, 끊기면 회수.
> - **가설 2**: 순환 참조도 JVM은 회수한다 (참조 카운팅이라면 누수됐을 것).
> - **가설 3**: GC는 실제로 반복 발생하며, 각 GC에는 STW(Pause) 시간이 있다.

### 모델/예시 코드 (`com.study.part03_gc.s01_gc_basics`)
- `Node` — 순환 참조 구성용. `Example1`(도달 가능성), `Example2`(순환 참조), `Example3`(STW/로그).
- 생존 여부 관찰에는 `WeakReference`를 쓴다 — `get()`이 객체를 돌려주면 생존, null이면 회수된 것.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_Reachability` | GC는 무엇을 garbage로 보나? | strong 참조 유지 → 끊기 |
| `Example2_CircularReference` | 순환 참조는 회수되나? | A⇄B 만들고 외부 참조 끊기 |
| `Example3_StopTheWorldLog` | GC는 실제로 일어나나? | 단명 객체 대량 생성 + `-Xlog:gc` |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part03_gc.s01_gc_basics.Example1_Reachability
java -cp build/classes/java/main com.study.part03_gc.s01_gc_basics.Example2_CircularReference
```

Example3는 GC 로그 옵션을 줘야 GC가 보인다(아래 별도 명령):

```bash
java -Xlog:gc -cp build/classes/java/main com.study.part03_gc.s01_gc_basics.Example3_StopTheWorldLog
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (도달 가능성)** — 가설 1.

| 상태 | `weakRef.get()` | 의미 |
|---|---|---|
| strong 참조 유지 + gc | 객체 반환(non-null) | 루트에서 도달 가능 → 생존 |
| strong 참조 끊기 + gc | `null` | 도달 불가 → garbage로 회수 |

→ GC는 참조 카운트가 아니라 "루트에서 도달 가능한가"로 판단한다. ✅

**예시 2 (순환 참조)** — 가설 2.

| 상태 | `weakA.get()` / `weakB.get()` |
|---|---|
| 외부 참조 끊고 gc 후 | 둘 다 `null` (A, B 모두 회수됨) |

→ 서로를 가리키는 순환 참조인데도 둘 다 회수됐다. 참조 카운팅이라면 카운트가 1로 남아 누수됐을
것이므로, JVM은 도달 가능성 기반임이 증명된다. ✅

**예시 3 (STW / GC 로그)** — 가설 3. `-Xlog:gc`로 실행한 실제 로그:

```
[0.026s][info][gc] Using G1                                        <- 기본 GC가 G1 (3.4에서 다룸)
[0.098s][info][gc] GC(0) Pause Young (Normal) ... 23M->7M(512M) 3.252ms
[0.210s][info][gc] GC(2) Pause Young (Normal) ... 312M->11M(512M) 1.597ms
[1.092s][info][gc] GC(12) Pause Young (Normal) ... 1507M->14M(2504M) 1.685ms
...
```
읽는 법:
- `GC(0), GC(2)...` — GC가 여러 번 반복 발생했다.
- `23M->7M(512M)` — GC 전 사용량 23M → GC 후 7M (전체 힙 512M). 단명 객체가 대량 회수됨(약한 세대 가설).
- `3.252ms` — 그 GC의 **Pause(STW) 시간**. 이 동안 애플리케이션이 멈춰 있었다.

→ GC는 실측 가능한 이벤트이고, 각 GC마다 STW(Pause)가 있다. GC 튜닝은 이 Pause를 줄이는 것. ✅

### 세 예시를 관통하는 결론
GC의 출발점은 "무엇이 쓰레기인가"이고, 그 답이 **도달 가능성**(예시1)이다. 이 방식은 참조 카운팅이
못 푸는 순환 참조 누수를 자연스럽게 해결한다(예시2). 그리고 GC는 약한 세대 가설(대부분 금방 죽음)에
기대어 효율적으로 동작하지만, 그 대가로 매번 STW(예시3)가 발생한다 — 이 STW를 어떻게 줄이느냐가
이후 GC 알고리즘·세대 구조(3.2~3.5)의 핵심 주제다.

---

## 3. 자기 점검

- **Q. JVM이 참조 카운팅을 안 쓰는 이유는?**
  - 내 답: 순환 참조에서 카운트가 0이 되지 않아 메모리 누수가 생기고, 참조 변경마다 카운터 갱신
    비용이 든다. 그래서 도달 가능성 분석을 쓴다. (Example2가 순환 참조도 회수됨을 증명)

- **Q. STW란 무엇이고 왜 중요한가?**
  - 내 답: GC가 도는 동안 모든 앱 스레드가 멈추는 구간. 응답 지연의 원인이라 GC 튜닝의 핵심
    목표가 이 시간을 줄이는 것. (Example3 로그의 `Xms`가 STW)

- **Q. (추가 실험) `-Xlog:gc*`(별표)로 실행하면?**
  - `-Xlog:gc`보다 훨씬 상세한 단계별 로그(Young/Old 영역, 각 phase 시간 등)가 나온다.
    `java -Xlog:gc* ...`로 실행해 차이를 보고, 어떤 정보가 더 보이는지 정리해본다. (3.2 세대 구조와 연결)
