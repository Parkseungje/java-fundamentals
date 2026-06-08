# PART 4 — 문자열과 컬렉션: 4.4 List 3형제 + 내부 구조

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.4 List 3형제 + 내부 구조**를 다룬다.
> ArrayList/LinkedList/Vector의 내부 구조와, "삽입/삭제 효율의 진짜 이유"를 실측으로 본다.

---

## 1. 학습 내용 — ArrayList, LinkedList, Vector

| 구현체 | 내부 구조 | Thread Safe | 비고 |
|---|---|---|---|
| **ArrayList** | 배열 (1.5배 확장) | ❌ | 가장 많이 씀 (기본 선택) |
| **LinkedList** | 이중 연결 리스트 | ❌ | Queue도 구현 |
| **Vector** | 배열 (=ArrayList) | ✅ (synchronized) | 느려서 거의 안 씀 |

### ArrayList의 동적 확장
ArrayList의 내부는 사실 **일반 배열(Object[])** 이다. 배열은 크기가 고정인데(4.3) 어떻게 자동으로
늘어날까? 처음엔 작은 용량(**기본 10**)으로 시작하고, 다 차면 **더 큰 배열(약 1.5배)을 새로 만들어
기존 내용을 통째로 복사**한 뒤 그 배열로 교체한다. 그래서 add 도중 확장+복사가 여러 번 일어난다.

→ 담을 개수를 미리 알면 `new ArrayList<>(n)`으로 처음부터 충분한 크기를 줘서 이 확장·복사를 없앨 수
있고, 그만큼 빨라진다(StringBuilder의 초기 용량과 같은 원리 — 4.2).

### 삽입/삭제 효율의 "진짜" 이유 (★ 면접 단골 오해)
"LinkedList는 삽입/삭제가 O(1)이라 빠르다"는 흔한 오해다. 정확히는 삽입/삭제 비용을 **두 단계로
나눠** 봐야 한다.

| 작업 단계 | ArrayList | LinkedList |
|---|---|---|
| **위치 찾기** | O(1) (인덱스로 주소 계산) | O(n) (노드를 처음부터 따라감) |
| **실제 삽입/삭제** | O(n) (원소들을 한 칸씩 밀어 복사) | O(1) (참조 몇 개만 변경) |

그래서 결과는 "어디에, 어떻게" 삽입하느냐에 따라 갈린다:
- **맨 앞/뒤에 삽입**: 위치 찾기가 즉시. 그럼 실제 조작에서 LinkedList(O(1))가 ArrayList(O(n) 복사)를 압도.
- **인덱스로 중간 삽입**: ArrayList는 위치 즉시+복사(O(n)), LinkedList는 위치 찾기 O(n)+조작 O(1) →
  **둘 다 O(n)** 이라 LinkedList의 'O(1) 삽입' 장점이 탐색 비용에 묻힌다.

즉 **"LinkedList 삽입이 O(1)"은 위치를 이미 알 때(맨 앞/뒤, Iterator 위치)에만 참**이다. 또한
ArrayList는 메모리가 연속이라 **CPU 캐시 효율**도 좋아, 실무에서는 대부분 ArrayList가 기본 선택이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: ArrayList capacity는 10에서 시작해 1.5배씩 늘고, 초기 크기 지정이 더 빠르다.
> - **가설 2**: 임의 get은 ArrayList O(1)이 LinkedList O(n)보다 압도적으로 빠르다.
> - **가설 3**: 맨 앞 삽입은 LinkedList가 빠르지만, 이는 "위치를 알 때"에 한정된다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ArrayListGrowth` | 어떻게 자동 확장? | capacity 관찰 + 초기 크기 성능 |
| `Example2_RandomAccess` | get 속도 차이? | 임의 인덱스 get 시간 비교 |
| `Example3_InsertDelete` | 삽입 빠른 건 항상? | 맨 앞 삽입 + 비용 분해 표 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s04_list.Example2_RandomAccess
java -cp build/classes/java/main com.study.part04_collections.s04_list.Example3_InsertDelete
```

Example1의 capacity 증가를 직접 보려면 모듈 접근 옵션이 필요하다(내부 배열은 JDK가 막아둠):

```bash
java --add-opens java.base/java.util=ALL-UNNAMED -cp build/classes/java/main com.study.part04_collections.s04_list.Example1_ArrayListGrowth
```
(옵션 없이 실행하면 capacity 관찰은 건너뛰고 안내 메시지 + 초기 크기 성능 비교만 나온다.)

### 실행 결과 — 가설과 실제 비교

**예시 1 (확장 정책)** — 가설 1. `--add-opens`로 capacity 관찰(실측):

| size | capacity | 배율 |
|---|---|---|
| 1 | 10 | (기본) |
| 11 | 15 | ×1.5 |
| 16 | 22 | ×1.5 |
| 23 | 33 | ×1.5 |
| 34 | 49 | ×1.5 |
| 50 | 73 | ×1.5 |

→ capacity가 10에서 시작해 **정확히 약 1.5배씩** 증가한다. 초기 크기 지정(`new ArrayList<>(n)`)이
미지정(283ms→200ms)보다 빠르다(확장·복사 없음). ✅

**예시 2 (임의 접근)** — 가설 2. (크기 10만, 임의 get 5만회, 실측)

| | 시간 |
|---|---|
| ArrayList | 2 ms (인덱스로 즉시 O(1)) |
| LinkedList | 2233 ms (매번 노드 따라감 O(n)) |

→ 약 **1000배 차이**. 인덱스 조회가 잦으면 ArrayList가 압도적. ✅

**예시 3 (삽입/삭제)** — 가설 3. (맨 앞 10만번 삽입, 실측)

| | 시간 |
|---|---|
| ArrayList `add(0, x)` | 368 ms (매번 전체 복사 O(n)) |
| LinkedList `addFirst` | 3 ms (head 참조만 변경 O(1)) |

→ 맨 앞 삽입에선 LinkedList가 **약 120배 빠르다**. 단 이는 "위치를 아는(맨 앞)" 경우다. 인덱스로
중간에 넣으면 LinkedList도 탐색 O(n)이 붙어 장점이 사라진다. ✅

### 세 예시를 관통하는 결론
ArrayList는 내부가 배열이라 인덱스 접근이 O(1)로 빠르고(예시2), 차면 1.5배로 확장한다(예시1).
LinkedList는 연결 리스트라 임의 접근은 느리지만(예시2) "위치를 아는" 삽입/삭제는 빠르다(예시3).
핵심은 **"LinkedList 삽입이 O(1)"이 '위치 찾기 O(n)'을 빼고 한 말**이라는 점이다. 임의 접근·캐시
효율까지 고려하면 실무 기본은 ArrayList이고, LinkedList는 "양 끝에서 자주 넣고 빼는 큐/덱" 용도에
주로 쓴다. Vector는 ArrayList와 같지만 모든 메서드가 synchronized라 느려서 거의 안 쓴다(필요하면
`Collections.synchronizedList`나 동시성 컬렉션을 쓴다).

---

## 3. 자기 점검

- **Q. "LinkedList는 삽입이 O(1)이라 ArrayList보다 빠르다"는 어디가 틀렸나?**
  - 내 답: 삽입 비용 = 위치 찾기 + 실제 조작. LinkedList의 O(1)은 '실제 조작'만이고, 인덱스로
    접근하면 '위치 찾기 O(n)'이 붙는다. 맨 앞/뒤처럼 위치를 알 때만 진짜 빠르다. (Example3)

- **Q. ArrayList에 담을 개수를 안다면 어떻게 생성하는 게 좋나?**
  - 내 답: `new ArrayList<>(예상크기)`로 초기 용량을 줘서 확장·복사를 없앤다. (Example1의 200ms vs 283ms)

- **Q. (추가 실험) LinkedList로 임의 인덱스 중간 삽입을 반복하면 ArrayList와 비슷하게 느려질까?**
  - `linkedList.add(size/2, x)`를 반복하는 코드를 만들어 ArrayList와 시간을 비교해본다. LinkedList도
    중간 위치를 찾느라 O(n)이 들어 장점이 사라지는지 확인하고, "위치를 안다 vs 모른다"의 차이를 정리한다.
