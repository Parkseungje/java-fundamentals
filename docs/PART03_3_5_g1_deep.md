# PART 3 — GC(가비지 컬렉션): 3.5 G1 GC 심화

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.5 G1 GC 심화**를 다룬다.
> (커리큘럼에서 "★ 면접·실무 직결"로 표시된 핵심 구간)
> PART 3의 마지막 소단원. 3.4에서 "G1이 대부분의 서버 기본"이라고 한 이유를 Region 모델로 깊이 본다.

---

## 1. 학습 내용 — G1의 Region 모델과 정지시간 예측

### 등장 배경
멀티프로세서 + 멀티 기가바이트 힙 시대가 오자, 기존 GC의 한계가 드러났다. 기존 GC는 Young/Old를
**크기·위치가 고정된 큰 덩어리**로 보고 통째로 회수했는데, 힙이 커질수록 한 번에 회수할 양이
많아져 **STW가 폭발**했다.

### 목표 — 정지시간 예측 모델 (Pause Prediction Model)
G1의 목표는 **"한 번의 GC를 목표 시간 안에 끝내겠다"** 이다. 기본 목표는 `MaxGCPauseMillis=200`(200ms).
이는 절대 보장이 아니라 **노력 목표**이며, G1은 이 목표에 맞추려고 "한 번에 회수할 양"을 조절한다.

### Region 모델
G1은 힙을 **동일 크기의 작은 리전(Region)** 으로 쪼갠다(1~32MB, 힙 크기에 따라 자동 결정). 그리고
각 리전을 그때그때 **역할을 동적으로** 부여해서 사용한다. 리전이 가질 수 있는 역할:
- **Eden / Survivor** (둘을 합쳐 Young Generation 역할)
- **Old** (장수 객체)
- **Humongous** (리전 절반보다 큰 거대 객체 전용 — 아래 별도 설명)
- **Available(Free)** (아직 어떤 역할도 안 받은 빈 리전 — 필요할 때 위 역할 중 하나로 배정됨)

```
기존:  [ Eden | Survivor | Old ]          ← 크기·위치 고정, 같은 역할끼리 물리적으로 붙어 있어야 함
G1  :  [E][O][S][_][H][O][E][_][O]...      ← 같은 크기 리전, 역할은 동적 (_=Available, H=Humongous)
```

핵심 특징 2가지:

1. **역할이 동적이다.** 같은 리전이 이번엔 Eden, 다음엔 비워졌다가 Old, 또는 Humongous로 역할이
   바뀔 수 있다. 그래서 "Young이 더 필요하면 빈 리전을 Young으로", "Old가 부족하면 Old로"처럼
   상황에 맞게 유연하게 배분된다.

2. **리전이 물리적으로 연속될 필요가 없다 (★ 면접 핵심).** 전통적 힙은 Young끼리, Old끼리 메모리상
   **붙어 있어야** 했다(`[ Eden | Survivor | Old ]`처럼 한 덩어리). G1의 리전들은 흩어져 있어도 된다 —
   메모리 곳곳의 리전이 같은 역할(예: Old)을 맡을 수 있다. **이 "물리적 비연속성"이 바로 위의 '역할을
   동적으로 바꿀 수 있는 유연성'의 근거**다. 붙어 있을 필요가 없으니, 아무 빈 리전이나 골라 원하는
   역할을 줄 수 있는 것이다. (단 Humongous는 예외 — 큰 객체가 한 리전에 안 들어가면 여러 리전에
   걸쳐 저장되므로 그 리전들은 연속이어야 한다.)

리전 단위로 쪼개니 **전체가 아니라 일부 리전만 골라 회수**할 수 있고, 그래서 정지시간을 통제할 수 있다.

### 거대 리전 (Humongous)
리전 **절반보다 큰 객체**는 일반 리전에 넣기 곤란하다. 이런 객체는 **Humongous(거대) 객체**로
분류되어 연속된 Humongous 리전에 따로 저장되고 주로 Old처럼 취급된다. Humongous 할당은 비싸고
단편화를 유발할 수 있어, **큰 배열을 자주 만드는 코드는 G1에서 성능 함정**이 될 수 있다.

### Garbage First (이름의 유래)
G1은 회수할 때 **쓰레기가 가장 많은 리전부터** 고른다. 정지시간 한도 안에서 "회수 효과가 가장 큰
곳"을 우선 처리하는 것이다. "Garbage First" = "쓰레기 많은 곳 먼저" → 이름의 유래다.
단순히 "쓰레기 많은 리전 먼저"가 아니라, **아래 동작 사이클의 Space Reclamation 단계에서 Mixed GC가
Old 리전 중 쓰레기 많은 것을 우선 수집하는 것**이 Garbage First의 구체적 맥락이다.

### G1의 동작 방식 — 3단계 사이클 (★★ 면접에서 구조만큼 중요)
구조(Region 모델)만 알고 "동작 방식"을 모르면 면접에서 막힌다. G1은 다음 사이클을 돈다.

```
[Young-only 단계]  Young GC 반복  →  Old 비율이 임계치 초과
        ↓
[Concurrent Marking]  앱과 '동시에' Old 리전들의 살아있는 객체(liveness) 측정
        ↓
[Space Reclamation]  Mixed GC로 (Young 전부 + 쓰레기 많은 Old 일부) 수집
        ↓  (다시 Young-only로)
```

1. **Young-only 단계** — 평소에는 **Young GC만** 반복한다. (G1에서는 Minor GC를 특별히 **Young GC**라
   부른다 — 의미는 Minor GC와 동일.) Eden이 차면 Young GC로 살아남은 객체를 Survivor/Old로 옮긴다.
   이게 반복되다 **Old 영역 비율이 임계치(기본 45%, `InitiatingHeapOccupancyPercent`)를 넘으면** 다음 단계로.

2. **Concurrent Marking(동시 마킹)** — Young GC와 병행하면서, **애플리케이션 스레드와 동시에(concurrent)**
   Old 리전들을 훑어 "각 리전에 살아있는 객체가 얼마나 되는지(liveness)"를 측정한다. 동시에 실행되므로
   이 단계 자체는 STW를 거의 일으키지 않는다(아주 짧은 STW 몇 단계만 — 3.4 ZGC 실습에서 본 Pause Mark 같은 것).
   이 마킹으로 "어느 Old 리전이 쓰레기가 많은지"를 알게 된다.

3. **Space Reclamation(공간 회수)** — **Mixed GC**를 수행한다. "Mixed"인 이유는 **Young 리전 전부 +
   쓰레기가 많은 Old 리전 일부**를 **섞어서(mixed)** 회수하기 때문이다. 이때 2단계에서 측정한 liveness를
   근거로 **쓰레기가 많은(=회수 효과가 큰) Old 리전부터** 정지시간 목표 안에서 고른다 → 이것이 Garbage
   First 전략의 실제 동작이다. 다 끝나면 다시 Young-only로 돌아간다.

**STW 정리**: Concurrent Marking은 앱과 동시에 돌아 STW를 최소화하지만, **Young GC와 Mixed GC
자체는 STW가 발생**한다(객체를 실제로 옮기는 작업이라). 즉 "마킹은 동시에, 실제 회수(이동)는 STW로".

**용어 한눈에**:
| 용어 | 의미 |
|---|---|
| Young GC | G1의 Minor GC. Young 리전만 수집 (STW) |
| Concurrent Marking | 앱과 동시에 Old 리전의 liveness 측정 (STW 최소) |
| Mixed GC | Young 전부 + 쓰레기 많은 Old 일부를 섞어 수집 (STW) |
| Full GC | 위로도 못 버티면 전체 수집 (G1에선 '실패 시'에 가까움, 매우 느림 — 피해야 할 신호) |

---

## 2. 실습으로 확인하기

> - **가설 1**: G1은 힙을 동일 크기 리전으로 쪼개며, 리전 크기·정지시간 목표는 설정값으로 존재한다.
> - **가설 2**: MaxGCPauseMillis 목표를 바꾸면 GC 횟수/Pause 경향이 달라진다.
> - **가설 3**: 리전 절반보다 큰 객체는 Humongous로 분류되어 별도 처리된다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 방법 |
|---|---|---|
| `Example1_RegionModel` | 리전 크기·정지목표는? | HotSpotDiagnosticMXBean으로 플래그 읽기 |
| `Example2_PausePrediction` | 정지목표 바꾸면? | `-XX:MaxGCPauseMillis` 변경 비교 |
| `Example3_Humongous` | 큰 객체는? | 리전 2배 배열 할당 + `-Xlog:gc` |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part03_gc.s05_g1_deep.Example1_RegionModel
```

리전 크기를 바꿔 실행하거나(Example1), 정지목표를 바꿔 비교한다(Example2):

```bash
java -Xmx512m -XX:G1HeapRegionSize=1m -cp build/classes/java/main com.study.part03_gc.s05_g1_deep.Example1_RegionModel
java -Xmx512m -XX:MaxGCPauseMillis=20 -cp build/classes/java/main com.study.part03_gc.s05_g1_deep.Example2_PausePrediction
java -Xmx512m -XX:MaxGCPauseMillis=500 -cp build/classes/java/main com.study.part03_gc.s05_g1_deep.Example2_PausePrediction
```

Humongous는 리전을 작게 고정하고 GC 로그를 켜서 본다:

```bash
java -Xmx128m -XX:G1HeapRegionSize=1m -Xlog:gc -cp build/classes/java/main com.study.part03_gc.s05_g1_deep.Example3_Humongous
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Region 모델)** — 가설 1. MXBean 출력(실측):
```
기본:                  G1HeapRegionSize = 4 MB,  MaxHeapSize = 8180 MB,  리전 ≈ 2045개
-Xmx512m -XX:G1HeapRegionSize=1m:  G1HeapRegionSize = 1 MB, MaxHeapSize = 512 MB
MaxGCPauseMillis = 200 ms (기본 정지시간 목표)
```
→ 힙이 "동일 크기 리전 수천 개"로 관리됨을 수치로 확인. 옵션으로 리전 크기를 바꿀 수 있다. ✅

**예시 2 (정지시간 예측)** — 가설 2. 같은 워크로드(`-Xmx512m`), 목표만 변경(실측):

| MaxGCPauseMillis | 총 GC 횟수 | 총 GC 시간 |
|---|---|---|
| 200 (기본) | 57 | 85 ms |
| 20 (짧게) | 60 | 92 ms |
| 500 (길게) | 57 | 95 ms |

→ 목표를 20ms로 짧게 잡으니 GC 횟수가 늘었다(한 번에 적게 회수 → 자주). G1이 정지시간 목표에
맞춰 "한 번에 처리할 양"을 조절한다는 증거. (수치는 실행마다 다름 — 방향성에 주목) ✅

**예시 3 (Humongous)** — 가설 3. `-XX:G1HeapRegionSize=1m -Xlog:gc` 실측:
```
GC(0) Pause Young (Concurrent Start) (G1 Humongous Allocation) 59M->58M(128M) 1.707ms
GC(2) Pause Young (Concurrent Start) (G1 Humongous Allocation) 61M->60M(128M) 1.099ms
```
→ 리전(1MB)의 2배인 2MB 배열을 할당하니 GC 원인이 **"G1 Humongous Allocation"** 으로 찍힌다.
즉 큰 객체는 일반 객체와 다른 경로(Humongous)로 처리된다. ✅

### 보너스 — 동작 사이클(Young-only → Concurrent Mark → Mixed)을 로그로 보기
Old를 임계치(45%)까지 채우면 위에서 설명한 3단계 사이클이 GC 로그에 그대로 나타난다.
Old를 빠르게 채우는 워크로드(장수 객체 누적)를 작은 힙으로 돌리면 실측된다:
```
GC(19) Pause Young (Concurrent Start) ...      <- Old 임계치 초과 -> 마킹 시작 신호(Young GC에 얹힘)
GC(20) Concurrent Mark Cycle                    <- 동시 마킹 시작 (앱과 동시 실행)
GC(20) Pause Remark   98M->98M(128M) 0.531ms    <- 마킹 마무리(짧은 STW)
GC(20) Pause Cleanup  107M->107M(128M) 0.079ms  <- 정리(짧은 STW)
GC(20) Concurrent Mark Cycle 7.046ms            <- 동시 마킹 완료
GC(22) Pause Young (Prepare Mixed) ...          <- Mixed GC 준비
GC(23) Pause Young (Mixed) 120M->81M(128M) ...  <- Mixed GC! (Young 전부 + 쓰레기 많은 Old 일부)
GC(24) Pause Young (Mixed) 120M->73M(128M) ...  <- Mixed 반복하며 Old 쓰레기 회수
```
읽는 법:
- `Concurrent Start` / `Concurrent Mark Cycle` — Old 비율이 임계치를 넘어 **동시 마킹**이 시작됐다.
  `Remark`·`Cleanup`은 마킹 과정의 짧은 STW 단계다.
- `Prepare Mixed` → `Mixed` — **Space Reclamation 단계의 Mixed GC**. 사용량이 `120M->81M->73M`처럼
  단계적으로 줄어드는 것은 쓰레기 많은 Old 리전을 나눠서(정지시간 목표 안에서) 회수하기 때문이다
  = **Garbage First 전략의 실제 동작**.

→ 평소엔 Young GC만 돌다가(Young-only), Old가 차면 동시 마킹으로 liveness를 재고, Mixed GC로
쓰레기 많은 Old부터 회수하는 사이클이 로그로 확인된다. (이 로그는 장수 객체를 임계치 이상 쌓아야
나타난다 — 자기 점검의 추가 실험 참고)

### 세 예시를 관통하는 결론
G1이 "대부분의 서버 기본"인 이유는 **Region 모델**(예시1) 덕분이다. 힙을 작은 리전으로 쪼개고
역할을 동적으로 주되 물리적으로 연속될 필요가 없게 해서, 전체가 아니라 일부만 골라 회수할 수 있고
**정지시간을 예측·통제**(예시2)할 수 있다. 그 위에서 G1은 **Young-only → Concurrent Marking →
Space Reclamation(Mixed GC)** 의 사이클을 돈다(보너스 로그). 평소엔 Young GC만 싸게 돌다가, Old가
차면 앱과 동시에 마킹해 쓰레기 많은 Old 리전을 가려내고, Mixed GC로 그곳부터 회수한다 — 이것이
**Garbage First**의 실제 동작이다. 큰 객체는 Humongous로 따로 처리되므로(예시3) 큰 배열 남발은 주의.

면접 답변 키워드 4가지: ① 리전의 동적 역할 + 물리적 비연속성 ② Young-only / Concurrent Marking /
Space Reclamation 사이클 ③ Mixed GC ④ Garbage First.

---

## 3. 자기 점검

- **Q. G1이 큰 힙에 적합한 이유를 Region 모델로 설명하라.**
  - 내 답: 힙을 동일 크기 리전으로 쪼개 전체가 아닌 일부 리전만 골라 회수할 수 있어서, 큰 힙에서도
    한 번의 STW를 정지시간 목표 안에 통제할 수 있다. (Example1의 리전 개수 + Example2의 목표 조절)

- **Q. Humongous 객체란 무엇이고 왜 주의해야 하나?**
  - 내 답: 리전 절반 이상 크기의 객체. 연속 Humongous 리전에 따로 저장되어 할당이 비싸고 단편화를
    유발할 수 있다. 큰 배열을 자주 만들면 성능 함정. (Example3의 `G1 Humongous Allocation` 로그)

- **Q. (정리) "Garbage First"라는 이름의 의미는?**
  - 정지시간 한도 안에서 쓰레기가 가장 많은(=회수 효과가 큰) 리전부터 우선 회수한다는 뜻.
    Example2에서 목표시간을 바꾸면 "한 번에 몇 개 리전을 고를지"가 달라지는 것과 연결해 정리해본다.

- **Q. (★ 면접 단골) G1의 동작 방식을 사이클로 설명하라.**
  - 내 답: 평소엔 **Young-only**(Young GC만 반복)로 돌다가, Old 비율이 임계치(45%)를 넘으면
    **Concurrent Marking**(앱과 동시에 Old 리전의 liveness 측정)을 시작하고, 그다음 **Space
    Reclamation** 단계에서 **Mixed GC**(Young 전부 + 쓰레기 많은 Old 일부)로 회수한다. Concurrent
    Marking은 STW 최소지만 Young GC·Mixed GC 자체는 STW가 있다. (보너스 로그의 Concurrent Mark
    Cycle → Pause Young (Mixed) 흐름이 근거)

- **Q. (★ 면접 단골) G1 리전이 "물리적으로 연속될 필요가 없다"는 게 왜 중요한가?**
  - 내 답: 전통적 힙은 같은 역할(Young/Old)끼리 메모리상 붙어 있어야 했지만 G1 리전은 흩어져 있어도
    된다. 이 비연속성이 "빈 리전 아무거나 골라 원하는 역할을 동적으로 줄 수 있는" 유연성의 근거다.
    (단 Humongous는 한 객체가 여러 리전에 걸치면 그 리전들은 연속이어야 함)

- **Q. (추가 실험) 동작 사이클(Concurrent Mark / Mixed GC) 로그를 직접 띄워보기**
  - 장수 객체를 Old 임계치 이상으로 쌓는 워크로드를 작은 힙(`-Xmx128m`)으로 `-Xlog:gc` 실행하면
    `Concurrent Mark Cycle`, `Pause Young (Prepare Mixed)`, `Pause Young (Mixed)`가 로그에 나타난다.
    Example2는 retention이 적어 잘 안 나오니, longLived 누적을 크게 늘려 직접 유발해본다.
