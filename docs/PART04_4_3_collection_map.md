# PART 4 — 문자열과 컬렉션: 4.3 컬렉션 전체 지도

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.3 컬렉션 전체 지도**를 다룬다.
> 개별 자료구조(4.4~4.7)를 보기 전에, 전체 구조와 네 갈래의 성격을 먼저 잡는다.

---

## 0. 들어가기 전에 — 핵심 용어
- **컬렉션(Collection)**: 여러 값을 담는 자료구조들의 통합 프레임워크. 배열의 한계(크기 고정 등)를 넘는다.
- **List**: 순서가 있고 중복을 허용하는 목록(예: ArrayList). 인덱스로 접근.
- **Set**: 중복을 허용하지 않는 모음(예: HashSet). 순서는 보통 없음.
- **Queue**: 한쪽으로 넣고 다른 쪽으로 빼는 줄(FIFO 등). 대기열.
- **Map**: 키→값 쌍의 모음(예: HashMap). 키로 값을 찾는다. (엄밀히는 Collection 인터페이스 밖이지만 컬렉션 프레임워크의 일부)
- **인터페이스 vs 구현체**: `List`(약속) ↔ `ArrayList`/`LinkedList`(실제 구현). 보통 변수는 인터페이스 타입으로 선언(다형성).
- **제네릭(`<T>`)**: 담을 원소의 타입을 지정해 타입 안전을 얻는 문법(`List<String>`). PART 5.1에서 자세히.

한 줄 그림: **컬렉션은 List(순서·중복) / Set(중복 X) / Queue(줄) / Map(키-값) 네 갈래다. 용도에 맞는 인터페이스를 고르고 적절한 구현체를 쓴다.**

---

## 1. 학습 내용 — 배열의 한계와 컬렉션 계층

### 배열의 한계 → 컬렉션의 등장
배열은 가장 기본적인 자료구조지만 두 가지 근본 한계가 있다.
- **크기 고정**: `new int[3]`은 한 번 만들면 길이를 못 늘린다. 더 담으려면 더 큰 배열을 새로 만들어
  복사해야 한다(수동).
- **중간 삽입/삭제 메서드 없음**: "인덱스 1에 끼워넣기" 같은 메서드가 없어, 원소를 직접 한 칸씩
  밀어서(복사) 처리해야 한다.

이 불편함을 해결하려고 **컬렉션(Collection)** 이 등장했다. 컬렉션은 **"자료구조 + 그것을 다루는
알고리즘"을 클래스로 묶어** 제공한다(`add`, `remove`, `add(index, ...)` 등). 크기도 자동으로 늘어난다.

### 컬렉션 계층 — Collection과 Map
자바 컬렉션은 크게 두 갈래다.

```
Collection ─┬─ List  (ArrayList, LinkedList, Vector)        순서 O, 중복 O
            ├─ Set   (HashSet, TreeSet, LinkedHashSet)      중복 X
            └─ Queue (ArrayDeque, LinkedList, PriorityQueue) FIFO 등

Map (Collection 아님) ─┬─ HashMap
                       ├─ LinkedHashMap
                       ├─ TreeMap
                       └─ ConcurrentHashMap                  key-value
```

- **List / Set / Queue** 는 모두 `Collection` 인터페이스를 상속한다. 그래서 `add()`, `size()`,
  `iterator()` 같은 공통 메서드를 가지며, **`Collection` 타입으로 묶어 똑같이 다룰 수 있다**(다형성).
- **Map** 은 단일 원소가 아니라 **key-value 쌍**을 다루므로 구조가 달라 **Collection을 상속하지
  않는다**(별개 계층).

### 네 갈래의 성격 (선택 기준)
| 자료구조 | 순서 | 중복 | 핵심 용도 |
|---|---|---|---|
| **List** | 유지 | 허용 | 순서 있는 목록 |
| **Set** | (구현마다) | 불허 | 중복 제거 |
| **Queue** | FIFO | 허용 | 먼저 온 것 먼저 처리 |
| **Map** | (구현마다) | key 불가 | key로 값 조회 |

자료구조 선택은 결국 **"순서가 필요한가? 중복을 막아야 하나? 먼저 온 것부터 처리하나? key로
찾나?"** 에 대한 답이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 배열은 크기 고정·중간 삽입 수동이고, 컬렉션(ArrayList)은 메서드로 해결한다.
> - **가설 2**: List/Set/Queue는 Collection을 상속하고 Map은 별개다.
> - **가설 3**: 같은 입력도 List/Set/Queue/Map은 성격에 따라 다르게 동작한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ArrayLimitations` | 배열은 왜 불편한가? | 크기 확장·중간 삽입 배열 vs ArrayList |
| `Example2_CollectionHierarchy` | Map도 Collection? | instanceof + Collection 다형성 |
| `Example3_FourFamilies` | 네 갈래 성격 차이? | 같은 [A,B,A,C] 넣어 비교 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s03_collection_map.Example1_ArrayLimitations
java -cp build/classes/java/main com.study.part04_collections.s03_collection_map.Example2_CollectionHierarchy
java -cp build/classes/java/main com.study.part04_collections.s03_collection_map.Example3_FourFamilies
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (배열 한계)** — 가설 1.
- 배열: 크기 못 늘려 `Arrays.copyOf`로 새 배열 복사, 중간 삽입도 `arraycopy`로 수동 시프트.
- ArrayList: `add(40)` 자동 확장, `add(1, 99)` 중간 삽입, `remove(...)` 모두 한 줄. ✅

**예시 2 (계층)** — 가설 2.

| 타입 | instanceof Collection |
|---|---|
| ArrayList | true |
| HashSet | true |
| ArrayDeque | true |
| **HashMap** | **false** (key-value라 별개 계층) |

→ List/Set/Queue는 Collection 타입 하나로 공통 처리 가능(`fillAndCount`가 셋 다 받음). Map은 별개. ✅

**예시 3 (네 갈래 성격)** — 가설 3. 같은 입력 `[A, B, A, C]`:

| 자료구조 | 결과 | 성격 |
|---|---|---|
| List | `[A, B, A, C]` | 순서 유지 + 중복 허용 |
| Set | `[A, B, C]` | 중복 A 하나 제거 |
| Queue | poll 순서 `A, B, A, C` | FIFO |
| Map | `{A=2, B=1, C=1}` | 같은 key는 값 갱신(빈도 세기) |

→ 같은 입력도 자료구조에 따라 결과가 다르다. "무엇을 위한 자료구조인가"가 결과로 드러난다. ✅

### 세 예시를 관통하는 결론
배열의 한계(크기 고정·중간 조작 불편)가 컬렉션 등장의 이유다(예시1). 컬렉션은 List/Set/Queue가
`Collection` 공통 추상으로 묶이고 Map은 별개인 계층을 이룬다(예시2). 그리고 각 갈래는 순서·중복·
꺼내는 방식이 달라, 같은 데이터도 다르게 다룬다(예시3). 이 지도를 머리에 넣어두면, 이후 개별
자료구조(List 4.4 / Set 4.5 / Queue 4.6 / Map 4.7)를 "왜 이게 필요한가" 관점으로 이해할 수 있다.

---

## 3. 자기 점검

- **Q. Map은 왜 Collection을 상속하지 않나?**
  - 내 답: Collection은 단일 원소(add(E))를 다루는 추상인데, Map은 key-value 쌍(put(K,V))을 다뤄
    구조가 다르기 때문. 그래서 별개 계층이다. (Example2의 `HashMap instanceof Collection == false`)

- **Q. 배열 대신 컬렉션을 쓰는 이유 두 가지는?**
  - 내 답: ① 크기가 자동으로 늘어난다(고정 아님) ② 삽입·삭제·검색 등 조작 메서드를 제공한다
    (수동 복사 불필요). (Example1)

- **Q. (적용) 다음 상황에 어떤 자료구조? "방문한 URL 중복 없이 기록" / "작업을 들어온 순서대로 처리"
  / "사용자 ID로 정보 조회"**
  - 각각 Set / Queue / Map. 왜 그런지 예시3의 성격 표와 연결해 정리해본다.
