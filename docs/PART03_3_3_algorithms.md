# PART 3 — GC(가비지 컬렉션): 3.3 GC 알고리즘 4가지

> 이 문서는 커리큘럼 PART 3의 소단원 중 **3.3 GC 알고리즘**을 다룬다.
> 실제 JVM은 알고리즘을 내부에 숨기므로, 여기서는 **미니 힙을 자바로 시뮬레이션**해 각 알고리즘의
> 동작과 단점을 눈으로 확인한다.

---

## 0. 들어가기 전에 — 핵심 용어
- **GC 알고리즘**: 가비지를 '어떻게' 찾아 회수하는지의 방법. 아래 네 가지가 기본 빌딩블록이다.
- **Mark(표시)**: GC 루트에서 도달 가능한(살아있는) 객체에 표시를 남기는 단계.
- **Sweep(쓸기)**: 표시 안 된 객체(가비지)를 회수해 빈 공간으로 만드는 단계. → 단점: 빈 공간이 여기저기 흩어짐(단편화).
- **단편화(fragmentation)**: 빈 공간이 조각조각 흩어져, 총량은 충분해도 '연속된 큰 공간'이 없어 할당이 어려운 상태.
- **Compact(압축)**: 살아있는 객체를 한쪽으로 모아 빈 공간을 연속되게 만드는 단계(단편화 해소, 대신 느림).
- **Copy(복사)**: 살아있는 객체만 다른 영역으로 복사하고 원래 영역을 통째로 비우는 방식(빠르고 단편화 없음, 공간 2배 필요).
- 정리: Mark-Sweep / Mark-Sweep-**Compact** / **Copy**(+Mark) 가 조합되어 실제 GC(3.4)를 이룬다.

한 줄 그림: **Mark(살아있는 것 표시) 후, Sweep(쓸기)·Compact(모으기)·Copy(복사) 중 하나로 회수한다. 각 방식은 속도·단편화·공간에서 트레이드오프가 있다.**

---

## 1. 학습 내용 — 4가지 GC 알고리즘

| 알고리즘 | 핵심 동작 | 단점 |
|---|---|---|
| Reference Counting | 객체마다 참조 카운터, 0이면 회수 | 순환 참조 누수 + 카운터 갱신 비용 |
| Mark-and-Sweep | 루트에서 마킹 후, 안 찍힌 것을 제자리 제거 | Compaction 없어 단편화 |
| Mark-and-Compact | Mark 후 살아남은 객체를 한쪽으로 모음 | 객체 이동(Compact) 오버헤드 |
| Generational (실제) | Young/Old로 나눠 관리 | 구조 복잡 |

### Reference Counting
객체마다 "나를 가리키는 참조 수" 카운터를 두고 0이 되면 즉시 회수한다. 즉시성은 장점이지만,
**순환 참조(A⇄B)** 에서 서로 때문에 카운트가 0이 안 되어 누수된다(3.1에서 본 한계).

### Mark-and-Sweep
- **Mark**: GC 루트에서 출발해 도달 가능한 객체에 표시(mark).
- **Sweep**: 표시 안 된(unmarked) 객체를 회수. 단, **살아있는 객체는 움직이지 않고 제자리에서** 빈
  공간만 회수한다.
- 단점: 회수된 빈 공간이 여기저기 흩어져 **단편화(fragmentation)** 가 생긴다. 총 빈 공간은 충분해도
  연속된 큰 공간이 없어 큰 객체를 할당하지 못할 수 있다.

### Mark-and-Compact
- Mark는 동일. 그다음 **Compact**: 살아남은 객체들을 힙의 한쪽으로 차곡차곡 **이동(복사)** 시켜
  빈 공간을 한곳에 연속으로 모은다.
- 장점: 단편화가 사라져 큰 객체도 할당 가능. 단점: 객체를 옮기고 참조를 갱신하는 **이동 비용**.

### Generational (실제 JVM이 쓰는 방식)
위 알고리즘들을 세대별로 조합한다. Young은 객체가 자주 죽으므로 복사(Copy) 기반으로 빠르게,
Old는 Mark-Sweep-Compact 계열로 관리한다(3.2의 세대 구조). 구조는 복잡하지만 약한 세대 가설을
활용해 효율이 높다.

---

## 2. 실습으로 확인하기 (미니 힙 시뮬레이션)

> - **가설 1**: Mark-Sweep은 제자리 제거라 빈 칸이 흩어져 단편화가 생긴다.
> - **가설 2**: Mark-Compact는 살아남은 객체를 모아 단편화를 없앤다(대신 이동 비용).
> - **가설 3**: 참조 카운팅은 순환 참조를 못 풀지만, 도달 가능성은 푼다.

### 모델 코드 (`com.study.part03_gc.s03_algorithms`)
- `MiniObject` — 시뮬레이션용 가짜 객체(marked, refs, refCount 보유). 8칸짜리 `MiniObject[]`를 미니 힙으로 사용.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 알고리즘 | 시나리오 |
|---|---|---|
| `Example1_MarkSweep` | Mark-Sweep | 짝수=live/홀수=garbage 배치 후 sweep → 단편화 |
| `Example2_MarkCompact` | Mark-Compact | 같은 힙에 compact → 연속 공간 확보 |
| `Example3_RefCountVsReachability` | 참조 카운팅 vs 도달 가능성 | A⇄B 순환 참조에 둘 다 적용 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part03_gc.s03_algorithms.Example1_MarkSweep
java -cp build/classes/java/main com.study.part03_gc.s03_algorithms.Example2_MarkCompact
java -cp build/classes/java/main com.study.part03_gc.s03_algorithms.Example3_RefCountVsReachability
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Mark-Sweep)** — 가설 1.
```
초기 힙 : [live0, garbage1, live2, garbage3, live4, garbage5, live6, garbage7]
Sweep 후: [live0, _, live2, _, live4, _, live6, _]
총 빈 칸 = 4, 연속 빈 칸 최대 길이 = 1
연속 2칸 필요한 객체 할당 가능? false
```
→ 빈 칸이 4개나 되는데 연속이 아니라 크기 2 객체를 못 넣는다 = 단편화. ✅

**예시 2 (Mark-Compact)** — 가설 2. (같은 초기 힙)
```
Compact 후: [live0, live2, live4, live6, _, _, _, _]
총 빈 칸 = 4, 연속 빈 칸 최대 길이 = 4
연속 2칸 필요한 객체 할당 가능? true
```
→ 살아남은 객체를 앞으로 모으니 빈 칸이 뒤에 연속으로 생겨 할당 성공(Example1과 정반대). ✅

**예시 3 (참조 카운팅 vs 도달 가능성)** — 가설 3. (A⇄B, 루트는 A/B 안 가리킴)

| 알고리즘 | A 회수? | B 회수? | 판정 |
|---|---|---|---|
| 참조 카운팅 (refCount==0?) | false | false | 둘 다 1이라 누수! |
| 도달 가능성 (루트 mark) | true | true | 정확히 회수 |

→ 참조 카운팅은 순환 참조를 못 푼다(카운트가 0이 안 됨). 도달 가능성은 "루트에서 닿는가"만
보므로 순환이어도 끊겼으면 회수 = JVM이 도달 가능성을 택한 이유(3.1 결론을 메커니즘으로 재확인). ✅

### 세 예시를 관통하는 결론
GC 알고리즘은 "무엇을 garbage로 보는가(참조 카운팅 vs 도달 가능성)"와 "회수 후 공간을 어떻게
정리하는가(Sweep vs Compact)"의 두 축으로 갈린다. 참조 카운팅은 순환 참조에서 무너지고(예시3),
Mark-Sweep은 단순하지만 단편화가 남으며(예시1), Mark-Compact는 단편화를 없애되 이동 비용이
든다(예시2). 실제 JVM은 이들을 세대별로 조합한 Generational 방식을 쓴다(3.2). 어느 하나가 완벽한
게 아니라 트레이드오프의 조합이며, 그 조합을 어떻게 하느냐가 GC 종류(3.4)를 가른다.

---

## 3. 자기 점검

- **Q. Mark-Sweep과 Mark-Compact의 차이와 트레이드오프는?**
  - 내 답: 둘 다 마킹은 같지만, Sweep은 제자리 제거(단순·빠름·단편화), Compact는 살아남은 객체를
    모음(단편화 없음·이동 비용). (Example1 vs Example2의 힙 레이아웃)

- **Q. 참조 카운팅이 순환 참조에서 실패하는 이유를 메커니즘으로?**
  - 내 답: A는 B가, B는 A가 가리켜 서로의 카운트를 1로 유지시킨다. 외부 참조가 끊겨도 카운트가
    0이 안 되어 회수되지 않는다. (Example3의 refCount 출력)

- **Q. (추가 정리) 실제 JVM의 Generational이 위 알고리즘들을 어떻게 조합하나?**
  - Young은 객체가 금방 죽어 살아남는 게 적으므로 복사(Copy) 위주로 빠르게, Old는 객체가 많이
    살아남아 Mark-Sweep-Compact 계열로 관리한다. 3.2의 Survivor 복사와 연결해 정리해본다.
