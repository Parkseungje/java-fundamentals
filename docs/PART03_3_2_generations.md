# PART 3 — GC(가비지 컬렉션): 3.2 Heap의 세대 구조

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.2 Heap의 세대 구조**를 다룬다.
> 3.1의 "약한 세대 가설"이 실제 Heap 구조로 어떻게 구현되는지 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **세대(generation)**: Heap을 객체 나이별로 나눈 구역. "금방 죽는 객체"와 "오래 사는 객체"를 따로 관리.
- **Young 영역(젊은 세대)**: 갓 만든 객체가 들어가는 곳. 대부분 여기서 금방 죽는다. 하위로 Eden + Survivor(S0/S1).
- **Eden**: 새 객체가 처음 놓이는 곳. **Survivor(S0/S1)**: Young GC에서 살아남은 객체가 잠시 머무는 두 칸.
- **Old 영역(늙은 세대)**: Young에서 여러 번 살아남은 '오래 산' 객체가 옮겨가는 곳.
- **Minor GC(Young GC)**: Young 영역만 청소(자주, 빠름). **Major/Full GC**: Old(또는 전체) 청소(드물게, 느림).
- **승격(promotion)**: 객체가 Young에서 충분히 살아남아 Old로 옮겨지는 것.
- **Stop-The-World(STW)**: GC가 도는 동안 애플리케이션 스레드를 잠깐 멈추는 것(3.3에서 자세히).

한 줄 그림: **객체는 Eden에서 태어나 → 살아남으면 Survivor → 더 오래 살면 Old로 승격된다. "대부분 금방 죽는다"는 가설(3.1) 덕에 Young만 자주 빠르게 청소하면 효율적이다.**

---

## 1. 학습 내용 — Young/Old 세대와 객체의 일생

3.1에서 본 약한 세대 가설("대부분 객체는 금방 죽는다")을 활용하기 위해, JVM은 Heap을 **세대
(generation)** 로 나눈다.

### 세대 구조
- **Young Generation** — 갓 만들어진 객체가 사는 곳. 세 영역으로 나뉜다.
  - **Eden** : `new`로 만든 객체가 처음 놓이는 곳.
  - **Survivor 0 (From)** / **Survivor 1 (To)** : Minor GC에서 살아남은 객체가 잠시 머무는 두 구역.
- **Old Generation** — 오래 살아남은(장수) 객체가 사는 곳.

### 객체의 일생
```
Eden에서 생성
  → (Eden이 차면) Minor GC: 살아남은 객체를 Survivor로 복사
  → From ↔ To Survivor를 오가며 N번 생존 (age 증가)
  → age가 임계치를 넘으면 Old로 Promotion(승격)
  → Old가 가득 차면 Full GC (전체 STW)
```

- **Minor GC** : Young 영역만 청소. 자주 일어나지만 영역이 작아 빠르다. 로그상 "Pause Young".
- **Full GC** : Old까지 포함한 전체 청소. 드물지만 느리고 STW가 길다. 로그상 "Pause Full".
- **Promotion** : Young에서 오래 버틴 객체가 Old로 옮겨지는 것.

### Survivor가 둘인 이유
Survivor를 둘(From/To)로 두는 것은 **단편화(fragmentation)를 막기 위해서**다. Minor GC 때
살아남은 객체를 한 Survivor에서 다른 Survivor로 **통째로 복사(Copy)** 하고 원래 쪽을 완전히 비운다.
이렇게 하면 살아남은 객체들이 한쪽에 빈틈없이 모여 메모리가 조각나지 않는다. From과 To는 GC마다
역할을 서로 바꾼다(스위칭).

---

## 2. 실습으로 확인하기

> - **가설 1**: 단명 객체는 Young에서 나고 죽어 Minor GC(Pause Young)가 자주 일어난다.
> - **가설 2**: 오래 사는 객체는 Old로 promotion되어 Old Gen 사용량이 증가한다.
> - **가설 3**: Eden/Survivor/Old 세대 구조는 실제 JVM 메모리 풀로 존재한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_MinorGC` | 단명 객체는 어떤 GC? | 1MB 배열 대량 churn + `-Xlog:gc` |
| `Example2_Promotion` | 오래 사는 객체는? | static list에 누적 + Old Gen 측정 |
| `Example3_HeapPools` | 세대 구조는 실재하나? | MXBean으로 HEAP 풀 출력 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part03_gc.s02_generations.Example3_HeapPools
```

GC 로그가 필요한 예시는 옵션을 준다:

```bash
java -Xlog:gc -cp build/classes/java/main com.study.part03_gc.s02_generations.Example1_MinorGC
java -Xlog:gc -cp build/classes/java/main com.study.part03_gc.s02_generations.Example2_Promotion
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Minor GC)** — 가설 1. `-Xlog:gc` 실제 로그:
```
GC(0) Pause Young (Normal) (G1 Evacuation Pause) 23M->7M(512M) 2.744ms
GC(2) Pause Young (Normal) (G1 Evacuation Pause) 312M->11M(512M) 1.670ms
```
→ "Pause Young"(Minor GC)이 반복되고, GC 후 사용량이 작게 떨어진다(312M→11M). 단명 객체가
Young에서 대량 회수됨 = 약한 세대 가설 그대로. ✅

**예시 2 (Promotion)** — 가설 2.

| 시점 | Old Gen used |
|---|---|
| 시작 | 0 MB |
| 오래 사는 객체 200MB 보관 후 | **200 MB** (증가) |

`-Xlog:gc` 로그에서도 promotion 누적 후 `Pause Full` 이 등장:
```
GC(10) Pause Young (Normal) ... 1427M->257M(2280M) 13.288ms   <- 살아남은 객체가 점점 늘어남(Old로 승격)
GC(11) Pause Full (System.gc()) 427M->200M(948M) 23.996ms     <- 전체 청소(Full GC), 200M는 안 줄음(살아있음)
```
→ 죽지 않는 객체는 Old로 승격되어 Old Gen을 채운다. Full GC를 해도 살아있는 200M는 회수되지 않는다. ✅

**예시 3 (세대 구조 실재)** — 가설 3. MXBean 출력:
```
- G1 Eden Space        used=0MB, max=무제한
- G1 Survivor Space    used=0MB, max=무제한
- G1 Old Gen           used=0MB, max=8180MB
```
→ Eden / Survivor / Old 구획이 실제 JVM 메모리 풀로 존재한다. "Young = Eden + Survivor, 그리고
Old"라는 세대 구조가 개념도가 아니라 실재함을 코드로 확인. (앞의 "G1"은 현재 GC가 G1이라는 뜻 — 3.4) ✅

### 세 예시를 관통하는 결론
약한 세대 가설(3.1)은 Heap을 Young(Eden+Survivor)과 Old로 나누는 세대 구조(예시3)로 구현된다.
대부분의 객체는 Young에서 나고 Minor GC로 싸게 회수되고(예시1), 그중 오래 버틴 소수만 Old로
승격된다(예시2). 이렇게 "자주 죽는 Young만 자주, 싸게 청소"하는 것이 세대별 GC의 핵심 전략이다.
다만 Old가 차면 비싼 Full GC가 일어나므로, promotion을 적절히 관리하는 것이 GC 튜닝의 한 축이 된다.

---

## 3. 자기 점검

- **Q. Survivor 영역이 둘인 이유는?**
  - 내 답: 살아남은 객체를 한 Survivor에서 다른 Survivor로 통째로 복사(Copy)하고 원래 쪽을 비워
    단편화를 막기 위해. From/To는 GC마다 역할을 바꾼다.

- **Q. Minor GC와 Full GC의 차이는?**
  - 내 답: Minor는 Young만 청소(자주·빠름·짧은 STW), Full은 Old 포함 전체 청소(드뭄·느림·긴 STW).
    (Example1의 Pause Young vs Example2의 Pause Full)

- **Q. (추가 실험) `-Xlog:gc*` 또는 `-Xlog:gc+heap=debug`로 실행하면?**
  - Eden/Survivor/Old의 각 영역 크기 변화와 promotion 상세가 더 자세히 보인다. Example2를
    `java -Xlog:gc+heap=debug ...`로 실행해 Old 영역이 차오르는 과정을 관찰해본다. (3.3 알고리즘과 연결)
