# PART 3 — GC(가비지 컬렉션): 3.1 GC 기본 + 약한 세대 가설 + 참조 카운팅의 한계

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.1 GC 기본**을 다룬다.
> PART 2(메모리 영역)에서 본 Heap을, 이번엔 "어떻게 자동으로 비우는가"의 관점으로 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **GC(Garbage Collection)**: 더 이상 안 쓰는 객체를 JVM이 자동으로 찾아 메모리를 비우는 기능. (C처럼 직접 free 안 해도 됨)
- **가비지(garbage)**: 어디서도 더 이상 닿을(참조할) 수 없게 된 객체. GC의 회수 대상.
- **도달 가능성(reachability)**: 'GC 루트'에서 참조를 따라가 닿을 수 있으면 살아있는 객체, 못 닿으면 가비지.
- **GC 루트(GC root)**: 도달성 판단의 출발점(실행 중인 스택의 지역 변수, static 변수 등).
- **참조 카운팅(reference counting)**: "참조 수를 세어 0이면 회수"하는 방식. 순환 참조를 못 푸는 한계가 있어 자바는 안 씀.
- **약한 세대 가설(weak generational hypothesis)**: "대부분의 객체는 금방 죽는다"는 경험칙. 세대별 GC(3.2)의 근거.
- **Mark-and-Sweep**: 살아있는 것을 표시(mark)하고 나머지를 쓸어담는(sweep) GC의 기본 원리.

한 줄 그림: **GC는 'GC 루트에서 못 닿는 객체(가비지)'를 자동 회수한다. 참조 수가 아니라 '도달 가능성'으로 판단하므로 순환 참조도 처리된다.**

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

### 보충 — WeakReference란? (실습에서 생존 여부를 들여다보는 도구)
실습 코드에서 객체가 "회수됐는지"를 확인하려고 `WeakReference`를 쓴다. 처음 보면 헷갈리기 쉬워서
개념을 정리해 둔다.

**참조에는 "세기(strength)"가 있다.** 같은 "객체를 가리킨다"라도 GC에게 주는 명령의 강도가 다르다.
- **강한 참조(Strong Reference)** — 우리가 평소 쓰는 모든 참조(`Object o = new Object();`).
  의미는 "이 객체 **절대 회수하지 마**". 강한 참조가 하나라도 있으면 GC는 그 객체를 회수하지 않는다.
- **약한 참조(WeakReference)** — `new WeakReference<>(obj)`. 의미는 "가리키긴 하지만 **GC가
  회수하고 싶으면 회수해도 돼**". 그래서 어떤 객체를 **약한 참조만** 가리키고 강한 참조가 하나도
  없으면, 다음 GC 때 그 객체는 회수된다.

**핵심: "가리키는 것"과 "GC를 막는 것"은 보통 같이 가지만(강한 참조), WeakReference는 이 둘을
분리한다 — 가리키되 GC는 막지 않는다.** JVM이 도달 가능성을 따질 때 약한 참조는 도달 경로로
쳐주지 않기 때문이다.

**왜 실습에 쓰나 (딜레마 해결).** 회수 실험에는 모순이 있다.
- 객체를 일반 변수(강한 참조)로 잡으면 → GC가 회수를 안 해서 실험이 안 된다.
- 변수로 안 잡으면 → 회수됐는지 확인할 방법이 없다.

`WeakReference`는 **회수를 막지 않으면서 객체가 살아있는지 엿보는 창** 역할을 해서 이 딜레마를 푼다.
```java
Object strong = new Object();                          // 강한 참조 (회수 막음)
WeakReference<Object> weakRef = new WeakReference<>(strong); // 엿보는 창 (회수 안 막음)

weakRef.get();   // 객체가 살아있으면 -> 객체 반환 / 회수됐으면 -> null 반환
```
- `strong`이 살아있는 동안: 강한 참조 때문에 회수 안 됨 -> `get()`은 객체를 돌려줌.
- `strong = null`로 끊으면: 이제 **약한 참조만** 남음 -> GC가 회수 -> `get()`은 `null`.

**자주 헷갈리는 점 1 — "WeakReference의 인자가 null이라서 회수하는 것"이 아니다.**
순서가 반대다. "객체를 가리키는 강한 참조가 모두 사라지고 약한 참조만 남았다 -> GC가 그 객체를
회수했다 -> 그래서 `weakRef.get()`이 null을 돌려준다." get()의 null은 원인이 아니라 **결과**다.

**자주 헷갈리는 점 2 — 일반 클래스 필드에 담으면 왜 회수가 안 되나?**
평범한 클래스의 필드는 전부 강한 참조다. 그래서 그 안에 객체를 담아두면 GC가 회수하지 못한다.
```java
class Holder { Object ref; Holder(Object o){ this.ref = o; } }

Object strong = new Object();
Holder holder = new Holder(strong);
strong = null;          // strong은 끊었지만...
// holder.ref(강한 참조)가 여전히 객체를 붙잡고 있다 -> 루트에서 도달 가능 -> GC 회수 안 함 ❌
```
같은 "필드에 담기"인데 결과가 다른 이유는, `Holder.ref`는 **강한 참조**라 도달 경로로 인정되고,
`WeakReference`의 내부 참조는 **약한 참조**라 도달 경로에서 제외되기 때문이다.

| | `Holder.ref` (일반 필드) | `WeakReference` |
|---|---|---|
| 참조 강도 | 강한 참조 | 약한 참조 |
| GC가 도달 경로로 인정? | 인정함 | 무시함 |
| `strong=null` 후 객체 | 생존(필드가 붙잡음) | 회수(약한 참조만 남음) |

> 참고: 참조 세기는 4단계(강→약) **Strong > Soft > Weak > Phantom**. `SoftReference`는 "메모리
> 부족할 때만 회수"(캐시에 적합), `PhantomReference`는 "회수 직전 정리 작업용". 지금은 Weak만
> 이해하면 충분하다. 실무에서 Weak는 캐시(`WeakHashMap`)나 리스너 누수 방지 등에 쓰인다.

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
