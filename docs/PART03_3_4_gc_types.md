# PART 3 — GC(가비지 컬렉션): 3.4 GC 종류의 진화

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.4 GC 종류의 진화**를 다룬다.
> 3.3의 알고리즘들이 실제 GC 구현으로 어떻게 조합·발전했는지, 같은 코드를 GC만 바꿔 실행하며 비교한다.

---

## 1. 학습 내용 — Serial → Parallel → CMS → G1 → ZGC

메모리가 커지고 코어가 늘면서, GC도 "한 스레드로 다 멈추고 청소"에서 "정지시간을 예측·통제"하는
방향으로 진화했다.

| GC | 등장 | 특징 | 적합 |
|---|---|---|---|
| Serial GC | 초기 | 싱글 스레드로 GC | CPU 1개, 작은 메모리 |
| Parallel GC | Java 7~8 기본 | 멀티 스레드 GC, 처리량 우선 | 배치·처리량 중시 |
| CMS | (Java 9 deprecated, 14 제거) | STW 최소화 시도 | 응답성(과거) |
| **G1 GC** | Java 9+ 기본 | Region 단위, 정지시간 예측 | 대부분의 서버 |
| **Z GC** | Java 11+ (15 정식) | STW 1ms 이하, 대용량 힙 | 초저지연 |

핵심은 **"더 좋은 GC"가 아니라 "다른 절충"** 이라는 점이다.
- **Serial**: 단순하지만 싱글 스레드라 느림(작은 앱/단일 CPU에 적합).
- **Parallel**: 여러 스레드로 빠르게 청소해 **처리량(throughput)** 을 높임. 단 STW는 길 수 있음.
- **CMS**: STW를 줄이려 동시 수집을 시도했으나 단편화·복잡성으로 폐기됨(G1로 대체).
- **G1**: 힙을 Region으로 쪼개 "정지시간 목표 안에서" 쓰레기 많은 곳부터 회수(3.5에서 심화).
- **ZGC**: 대부분의 작업을 애플리케이션과 **동시(concurrent)** 로 수행해 STW를 1ms 이하로. 대신
  동시 수집의 CPU/메모리 오버헤드를 감수. 응답 지연이 치명적인 서비스에 적합.

내 GC 확인: `java -XX:+PrintCommandLineFlags -version` 출력에 `-XX:+UseG1GC` 같은 플래그가 보인다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 현재 GC는 컬렉터 이름으로 식별되며, 플래그로 바꾸면 이름이 달라진다.
> - **가설 2**: 같은 워크로드라도 GC 종류에 따라 수집 횟수·시간이 다르다(처리량 vs 지연 절충).
> - **가설 3**: ZGC의 STW(Pause)는 G1보다 훨씬 짧다(초저지연).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 방법 |
|---|---|---|
| `Example1_WhichGC` | 지금 어떤 GC? | MXBean 컬렉터 이름 |
| `Example2_GCStatsByCollector` | GC별 통계 차이? | 같은 워크로드 + count/time |
| `Example3_PauseComparison` | G1 vs ZGC 정지시간? | `-Xlog:gc` Pause 비교 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

GC를 바꿔가며 같은 클래스를 실행하는 것이 이 단원의 핵심이다.

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part03_gc.s04_gc_types.Example1_WhichGC
java -XX:+UseSerialGC -cp build/classes/java/main com.study.part03_gc.s04_gc_types.Example1_WhichGC
java -XX:+UseParallelGC -cp build/classes/java/main com.study.part03_gc.s04_gc_types.Example1_WhichGC
java -XX:+UseZGC -cp build/classes/java/main com.study.part03_gc.s04_gc_types.Example1_WhichGC
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (GC 식별)** — 가설 1. GC별 컬렉터 이름(실측):

| 플래그 | 컬렉터 이름(Young / Old) |
|---|---|
| 기본(G1) | `G1 Young Generation` / `G1 Old Generation` (+ `G1 Concurrent GC`) |
| `-XX:+UseSerialGC` | `Copy` / `MarkSweepCompact` |
| `-XX:+UseParallelGC` | `PS Scavenge` / `PS MarkSweep` |
| `-XX:+UseZGC` | `ZGC Cycles` / `ZGC Pauses` |

→ 같은 코드인데 플래그에 따라 컬렉터 이름이 완전히 바뀐다 = GC는 교체 가능한 부품. ✅
`java -XX:+PrintCommandLineFlags -version` 출력에도 기본이 `-XX:+UseG1GC`로 확인된다.

**예시 2 (GC별 통계)** — 가설 2. 동일 워크로드(`-Xmx512m`) 실측:

| GC | Young 수집 | 시간 |
|---|---|---|
| Serial | `Copy` count=73 | 74ms |
| Parallel | `PS Scavenge` count=63 | 36ms |
| G1 | `G1 Young Generation` count=46 | 75ms |

→ 같은 코드인데 횟수·시간이 다르다. Parallel이 멀티 스레드로 이 워크로드에선 총 GC 시간이 가장
짧게 나왔다(처리량 우선). 절대 우열이 아니라 워크로드/힙 크기/요구사항에 따른 절충이다. ✅
(수치는 실행·머신마다 다름 — 방향성에 주목)

**예시 3 (G1 vs ZGC Pause)** — 가설 3. `-Xlog:gc`(+ZGC는 `-Xlog:gc+phases=info`) 실측:

G1:
```
GC(0) Pause Young (Normal) (G1 Evacuation Pause) 24M->2M(512M) 2.133ms
GC(1) Pause Young (Normal) (G1 Evacuation Pause) 39M->2M(512M) 1.656ms
```
ZGC:
```
GC(0) Pause Mark Start    0.007ms
GC(0) Pause Mark End      0.022ms
GC(0) Pause Relocate Start 0.011ms
```
→ G1의 Pause는 1~2ms 수준인데, ZGC의 각 Pause는 **0.005~0.022ms**(마이크로초 단위)로 100배 이상
짧다. ZGC는 대부분의 작업을 Concurrent로 처리하고 STW는 극히 짧은 몇 단계만 잡기 때문이다. ✅

### 세 예시를 관통하는 결론
GC는 "교체 가능한 부품"이며(예시1), 종류마다 처리량·지연의 절충이 다르다(예시2). 진화의 방향은
일관되게 **"STW를 줄이고 예측 가능하게"** 였고, 그 끝에 ZGC의 초저지연이 있다(예시3). 단 어느 하나가
만능이 아니다 — 배치성 처리량 중시면 Parallel, 일반 서버면 G1(기본), 응답 지연이 치명적이면 ZGC를
고른다. "왜 G1이 대부분의 서버 기본인가"는 다음 3.5에서 Region 모델로 깊이 본다.

---

## 3. 자기 점검

- **Q. 내 JVM의 현재 GC를 확인하는 두 가지 방법은?**
  - 내 답: ① `java -XX:+PrintCommandLineFlags -version`에서 `-XX:+UseXxxGC` 확인,
    ② GarbageCollectorMXBean의 컬렉터 이름 확인(Example1).

- **Q. "ZGC가 G1보다 무조건 좋다"가 틀린 이유는?**
  - 내 답: ZGC는 STW를 극단적으로 줄이는 대신 동시 수집의 CPU/메모리 오버헤드를 감수한다.
    처리량이 중요하거나 작은 힙이면 Parallel/G1이 더 나을 수 있다. "더 좋은 GC"가 아니라 "다른 절충".

- **Q. (추가 실험) `-Xmx`를 크게(예: 4g) 주고 G1과 ZGC의 Pause를 비교하면?**
  - 힙이 커질수록 G1의 Pause는 늘어나는 경향이 있지만 ZGC는 거의 일정하다(대용량 힙 초저지연이
    ZGC의 강점). 직접 `-Xmx4g`로 Example3를 두 GC로 돌려 Pause 변화를 비교해본다.
