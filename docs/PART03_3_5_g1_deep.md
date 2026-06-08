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
각 리전을 그때그때 Eden/Survivor/Old로 **동적으로** 사용한다.
```
기존:  [ Eden | Survivor | Old ]      ← 크기·위치 고정 (통째로 회수 → 큰 힙에서 STW 폭발)
G1  :  [E][ ][S][O][ ][O][E]...       ← 같은 크기 리전, 역할은 동적 (리전 단위 부분 회수 가능)
```
리전 단위로 쪼개니 **전체가 아니라 일부 리전만 골라 회수**할 수 있고, 그래서 정지시간을 통제할 수 있다.

### 거대 리전 (Humongous)
리전 **절반보다 큰 객체**는 일반 리전에 넣기 곤란하다. 이런 객체는 **Humongous(거대) 객체**로
분류되어 연속된 Humongous 리전에 따로 저장되고 주로 Old처럼 취급된다. Humongous 할당은 비싸고
단편화를 유발할 수 있어, **큰 배열을 자주 만드는 코드는 G1에서 성능 함정**이 될 수 있다.

### Garbage First (이름의 유래)
G1은 회수할 때 **쓰레기가 가장 많은 리전부터** 고른다. 정지시간 한도 안에서 "회수 효과가 가장 큰
곳"을 우선 처리하는 것이다. "Garbage First" = "쓰레기 많은 곳 먼저" → 이름의 유래다.

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

### 세 예시를 관통하는 결론
G1이 "대부분의 서버 기본"인 이유는 **Region 모델**(예시1) 덕분이다. 힙을 작은 리전으로 쪼개니
전체가 아니라 일부만 골라 회수할 수 있고, 그래서 **정지시간을 예측·통제**(예시2)할 수 있다.
큰 힙에서도 STW가 폭발하지 않는다. 다만 리전보다 큰 객체는 Humongous로 따로 처리되므로(예시3)
큰 배열 남발은 주의해야 한다. "쓰레기 많은 리전부터(Garbage First)" 회수하는 전략이 이 모든 것을
가능하게 한다.

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
