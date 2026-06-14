# PART 4 — 문자열과 컬렉션: 4.5 Set 3형제

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.5 Set 3형제**를 다룬다.
> Set 공통 성질(중복 제거)과 세 구현의 차이, 그리고 중복 판정이 hashCode/equals에 의존함을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **Set**: 중복을 허용하지 않는 모음. 같은 원소를 두 번 넣어도 하나만 유지된다.
- **HashSet**: 해시(hashCode)로 저장. 가장 빠름(O(1)), 순서는 보장 안 됨.
- **TreeSet**: 정렬된 상태로 저장(이진 탐색 트리). 자동 정렬되지만 약간 느림(O(log n)). 정렬 기준 필요(Comparable/Comparator — 5.2).
- **LinkedHashSet**: HashSet + 입력 순서 유지(넣은 순서대로 순회).
- **hashCode() / equals()**: Set의 '중복 판정'이 이 둘에 의존한다. hashCode로 후보를 찾고 equals로 최종 비교 — 그래서 둘을 같이 올바르게 재정의해야 한다(4.8).

한 줄 그림: **Set은 중복을 막는다. 빠른 건 HashSet(순서 X), 정렬은 TreeSet, 입력 순서 유지는 LinkedHashSet. 중복 판정은 hashCode+equals로 한다.**

---

## 1. 학습 내용 — HashSet, TreeSet, LinkedHashSet

Set의 공통 성질은 **중복 불허**다. 셋의 차이는 "순회 순서"와 "내부 구조"다.

| 구현체 | 특징 | 내부 구조 |
|---|---|---|
| **HashSet** | 순서 X, 중복 X | HashMap 기반 (hashCode + equals로 중복 검사) |
| **TreeSet** | 자동 정렬, 중복 X | Red-Black Tree (삽입/조회 O(log n)) |
| **LinkedHashSet** | 삽입 순서 유지 | HashMap + 연결 리스트 |

### HashSet의 중복 판정 — hashCode + equals (★ 핵심)
HashSet은 사실 내부적으로 HashMap을 쓴다(값을 key로 저장). 중복을 검사할 때:
1. 원소의 **hashCode()** 로 버킷(저장 위치)을 찾고,
2. 그 버킷에 이미 있는 원소와 **equals()** 로 비교해 같으면 중복으로 보고 안 넣는다.

그래서 **사용자 정의 객체**를 HashSet에 넣을 때, equals/hashCode를 재정의하지 않으면(기본=주소 비교)
값이 같아도 "다른 객체"로 취급되어 **중복이 제거되지 않는다.** 사용자 정의 객체를 Set/Map에 쓰려면
equals와 hashCode를 **반드시 둘 다** 재정의해야 한다(규약은 4.8에서 심화).

### TreeSet의 자동 정렬 — 비교 기준이 필요
TreeSet은 Red-Black Tree로 원소를 항상 정렬 상태로 유지한다. 정렬하려면 "무엇이 더 큰지" 비교
기준이 있어야 한다.
- 원소가 **Comparable**을 구현하면(Integer, String 등은 내장) 그 자연 순서로 정렬.
- 생성 시 **Comparator**를 넘기면 원하는 기준(역순 등)으로 정렬.
- 둘 다 없으면 비교할 수 없어 add 시 **ClassCastException**이 난다.

#### TreeSet의 "기본 순서"란? = 타입의 자연 순서(natural ordering)
"숫자는 오름차순, 문자열은 사전순"이 따로 정해진 게 아니라, **TreeSet은 원소의 `compareTo()`
(Comparable의 자연 순서)를 그대로 따른다**. 즉 기본 순서 = "각 타입이 자기 `compareTo()`에 정의해
둔 자연 순서"이고, 그 자연 순서가 타입마다 이미 정해져 있을 뿐이다.

| 타입 | 자연 순서(compareTo가 정의한 것) | 결과 |
|---|---|---|
| Integer, Long, Double 등 숫자 | 값의 대소 비교 | **오름차순** (작은 수 먼저) |
| String | 한 글자씩 **유니코드 값** 비교 | **사전순** (숫자 < 대문자 < 소문자 < 한글) |
| Character | 유니코드 값 비교 | 오름차순 |
| 사용자 정의 클래스 | `Comparable`을 구현해 직접 정의 | 내가 정한 기준 |

정리하면 TreeSet에 "오름차순/사전순"이라는 별도 규칙이 박혀 있는 게 아니다. **그냥 원소의
`compareTo()`를 호출해 정렬**할 뿐이고, Integer의 compareTo는 오름차순으로, String의 compareTo는
유니코드 순(사전순)으로 동작하게 만들어져 있어서 그렇게 보이는 것이다. 그래서:
- 다른 순서를 원하면(역순 등) 생성 시 **Comparator를 넘겨** 자연 순서를 덮어쓴다.
  예: `new TreeSet<>(Comparator.reverseOrder())` → 내림차순.
- 사용자 정의 클래스는 자연 순서가 없으므로, Comparable을 구현하거나 Comparator를 줘야 한다
  (안 그러면 ClassCastException).

(Comparable/Comparator는 PART 5.2에서 자세히 다룬다.)

### 선택 기준
- 순서가 필요 없고 가장 빠른 중복 제거 → **HashSet**
- 삽입한 순서를 유지하며 중복 제거 → **LinkedHashSet**
- 항상 정렬된 상태가 필요 → **TreeSet**

---

## 2. 실습으로 확인하기

> - **가설 1**: HashSet은 순서 보장 X, LinkedHashSet은 삽입순, TreeSet은 정렬순(셋 다 중복 제거).
> - **가설 2**: HashSet 중복 제거는 hashCode/equals에 의존한다(재정의 안 하면 중복 안 걸림).
> - **가설 3**: TreeSet은 비교 기준(Comparable/Comparator)이 있어야 정렬·저장된다.

### 모델 코드 (`com.study.part04_collections.s05_set`)
- `PointNoEquals` (equals/hashCode 재정의 X) / `PointWithEquals` (재정의 O) — 예시2에서 대비.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_SetOrder` | 세 Set의 순서 차이? | 같은 입력 순회 비교 |
| `Example2_DedupByHashCodeEquals` | 중복 판정 기준? | NoEquals vs WithEquals size |
| `Example3_TreeSetOrdering` | TreeSet 정렬 기준? | 자연순/역순/예외 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s05_set.Example1_SetOrder
java -cp build/classes/java/main com.study.part04_collections.s05_set.Example2_DedupByHashCodeEquals
java -cp build/classes/java/main com.study.part04_collections.s05_set.Example3_TreeSetOrdering
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (순서)** — 가설 1. 입력 `banana, apple, cherry, apple, date`:

| Set | 결과 | 순서 성격 |
|---|---|---|
| HashSet | `[banana, date, apple, cherry]` | 순서 보장 X (해시) |
| LinkedHashSet | `[banana, apple, cherry, date]` | 삽입 순서 |
| TreeSet | `[apple, banana, cherry, date]` | 정렬(사전순) |

→ 셋 다 중복 apple 제거. 차이는 순서뿐. ✅

**예시 2 (중복 판정)** — 가설 2.

| 클래스 | `(1,2)` 두 번 add 후 size | 의미 |
|---|---|---|
| PointNoEquals (재정의 X) | **2** | 값 같아도 다른 객체 → 중복 제거 실패 |
| PointWithEquals (재정의 O) | **1** | 값 기준 동등 → 중복 제거 성공 |

→ HashSet 중복 제거는 hashCode/equals에 의존. 사용자 정의 객체는 둘 다 재정의 필수. ✅

**예시 3 (TreeSet 정렬)** — 가설 3.

| 경우 | 결과 |
|---|---|
| Integer TreeSet (자연 순서) | `[10, 20, 30]` 오름차순 |
| `Comparator.reverseOrder()` | `[30, 20, 10]` 내림차순 |
| 비교 기준 없는 객체 add | **ClassCastException** |

→ TreeSet은 비교 기준(Comparable/Comparator)이 있어야 정렬·저장 가능. ✅

### 세 예시를 관통하는 결론
Set은 모두 중복을 제거하지만, **그 "중복"의 정의와 "순서"가 구현마다 다르다.** HashSet은 해시 기반
(순서 X, 가장 빠름)이고 중복 판정을 hashCode/equals에 맡긴다(예시2 — 그래서 사용자 정의 객체엔
재정의 필수). LinkedHashSet은 삽입순을 유지하고, TreeSet은 비교 기준으로 항상 정렬 상태를 유지한다
(예시3). "순서가 필요한가, 정렬이 필요한가"가 선택의 기준이다(예시1).

---

## 3. 자기 점검

- **Q. HashSet에 사용자 정의 객체를 넣을 때 주의할 점은?**
  - 내 답: equals와 hashCode를 둘 다 재정의해야 값 기준 중복 제거가 동작한다. 안 하면 값이 같아도
    다른 객체로 취급되어 중복이 안 걸린다. (Example2의 size 2 vs 1)

- **Q. TreeSet에 add 시 ClassCastException이 나는 이유와 해결은?**
  - 내 답: 원소가 Comparable이 아니고 Comparator도 안 줘서 비교를 못 하기 때문. Comparable 구현
    또는 생성 시 Comparator 전달로 해결. (Example3)

- **Q. (추가 실험) HashSet<Integer>에 작은 정수들을 넣으면 왜 정렬된 것처럼 보일까?**
  - Integer의 hashCode는 값 자체라, 작은 정수는 `value % capacity` 버킷에 순서대로 들어가 우연히
    정렬돼 보인다. 큰 값이나 String을 넣으면 그 규칙성이 깨진다 — 직접 `HashSet<Integer>`에 1,2,3과
    1000,500,7 등을 넣어 순서를 비교해본다. (HashSet 순서는 "보장 안 됨"이지 "무작위"가 아님)
