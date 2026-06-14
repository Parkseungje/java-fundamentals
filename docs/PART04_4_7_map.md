# PART 4 — 문자열과 컬렉션: 4.7 Map 5형제

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.7 Map 5형제**를 다룬다.
> Map은 Collection과 별개 계층(4.3)이며, key-value를 저장한다. 다섯 구현의 순서·null·스레드 안전성을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **Map**: 키(key)→값(value) 쌍을 저장하는 자료구조. 키로 값을 빠르게 찾는다(사전: 단어→뜻).
- **키 / 값 / 엔트리(Entry)**: 찾는 기준(키), 저장된 데이터(값), 그리고 (키,값) 한 쌍이 엔트리.
- **HashMap**: 해시로 저장. 가장 빠름·순서 없음·키와 값에 null 허용. 가장 많이 씀.
- **LinkedHashMap**: HashMap + 입력 순서 유지.
- **TreeMap**: 키를 정렬해 저장(범위 검색·정렬 순회에 유리).
- **HashTable**: HashMap의 옛 버전(synchronized, null 불가, 느림 — 잘 안 씀).
- **ConcurrentHashMap**: 여러 스레드가 안전하게 동시 사용하도록 만든 Map(HashTable보다 효율적, PART 7).
- **hashCode()/equals()**: 키의 동일성 판단에 사용(Set과 동일 — 4.8).

한 줄 그림: **Map은 키-값 쌍을 저장한다. 기본은 HashMap(빠름·순서X), 순서는 LinkedHashMap, 정렬은 TreeMap, 멀티스레드는 ConcurrentHashMap.**

---

## 1. 학습 내용 — HashMap, LinkedHashMap, TreeMap, HashTable, ConcurrentHashMap

| 구현체 | 순서 | null | Thread Safe |
|---|---|---|---|
| **HashMap** | ❌ | key 1개 null + value null OK | ❌ |
| **LinkedHashMap** | ✅ 삽입순 | HashMap과 동일 | ❌ |
| **TreeMap** | ✅ key 정렬 | value만 null OK (key 불가) | ❌ |
| **HashTable** | ❌ | ❌ (둘 다 불가) | ✅ (메서드 전체 동기화) |
| **ConcurrentHashMap** | ❌ | ❌ (둘 다 불가) | ✅ (락 단위 최적화) |

### 순서 (Set 3형제와 같은 구조)
Map 구현은 Set 3형제(4.5)와 1:1 대응한다(Set이 내부적으로 Map을 쓴다).
- HashMap: 해시 테이블 → 순서 보장 X (가장 빠름).
- LinkedHashMap: 해시 + 연결 리스트 → 삽입 순서 유지.
- TreeMap: Red-Black Tree → key가 항상 정렬. `put/get/remove` 모두 **O(log n)**.
  정렬 기준은 유니코드 순서: **숫자 → 대문자 → 소문자 → 한글**.

### null 정책 (구현마다 다름)
- HashMap: null key **1개** 허용(특별 처리), null value 허용.
- TreeMap: key는 정렬 위해 서로 **비교**해야 하므로 null key 불가(NPE). value는 정렬과 무관해 OK.
- HashTable / ConcurrentHashMap: null key·value **둘 다 불가**. (ConcurrentHashMap이 막는 이유:
  멀티스레드에서 `get()==null`이 "키 없음"인지 "값이 null"인지 구분 불가 → 모호성 제거를 위해.)

### 스레드 안전성과 락 단위
- HashMap은 동기화가 없어 멀티스레드 동시 수정 시 **값 유실(lost update)** 이나 심하면 **resize 중
  무한 루프**까지 발생한다. 멀티스레드 공유에 절대 쓰면 안 된다.
- HashTable은 스레드 안전하지만 **모든 메서드가 synchronized(전체 잠금)** 이라 한 번에 한 스레드만
  접근 가능 → 느리다. 설계도 낡아 거의 안 쓴다.
- ConcurrentHashMap은 **락 단위를 작게(버킷/노드)** 쪼개 여러 스레드가 동시에 작업할 수 있어 빠르다.
  → 멀티스레드 공유 Map은 **ConcurrentHashMap이 표준 권장**(HashTable 대체).

---

## 2. 실습으로 확인하기

> - **가설 1**: HashMap 순서 X, LinkedHashMap 삽입순, TreeMap key 정렬(숫자<대문자<소문자<한글).
> - **가설 2**: null 정책이 다르다 — HashMap(관대), TreeMap(key 금지), ConcurrentHashMap(둘 다 금지).
> - **가설 3**: HashMap은 동시 갱신 시 값 유실, ConcurrentHashMap은 안전.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_MapOrder` | 순서 차이? | 같은 put 순서 + TreeMap 정렬 기준 |
| `Example2_NullPolicy` | null 허용? | 세 Map에 null key/value |
| `Example3_HashMapVsConcurrent` | 스레드 안전? | 4스레드 동시 값 증가 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s07_map.Example1_MapOrder
java -cp build/classes/java/main com.study.part04_collections.s07_map.Example2_NullPolicy
java -cp build/classes/java/main com.study.part04_collections.s07_map.Example3_HashMapVsConcurrent
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (순서)** — 가설 1. put `banana, apple, cherry`:

| Map | keySet | 순서 |
|---|---|---|
| HashMap | (구현 의존) | 보장 X |
| LinkedHashMap | `[banana, apple, cherry]` | 삽입순 |
| TreeMap | `[apple, banana, cherry]` | key 정렬 |

TreeMap에 `"a","2","가","B"` → `[2, B, a, 가]` (숫자<대문자<소문자<한글). ✅

**예시 2 (null 정책)** — 가설 2.

| Map | `put(null, 1)` | `put("k", null)` |
|---|---|---|
| HashMap | OK | OK |
| TreeMap | **NPE** (key 비교 불가) | OK |
| ConcurrentHashMap | **NPE** | **NPE** |

→ HashMap 관대 / TreeMap key만 금지 / ConcurrentHashMap 둘 다 금지. ✅

**예시 3 (스레드 안전)** — 가설 3. (4스레드 × 10만 증가, 기대 합계 400000, 실측)

| Map | 값 합계 |
|---|---|
| HashMap | **390115** (유실 발생) |
| ConcurrentHashMap | **400000** (정확) |

→ HashMap은 동기화 없는 read-modify-write라 갱신이 덮어써져 유실. ConcurrentHashMap은 `merge`로
원자적 갱신해 정확. ✅ (HashMap을 동시에 put하며 크기를 늘리면 resize 중 무한 루프 위험도 있어,
이 예시는 key를 고정해 그 위험을 피하면서 유실만 관찰한다.)

### 세 예시를 관통하는 결론
Map도 "순서/정렬이 필요한가(예시1), null을 담아야 하나(예시2), 멀티스레드 공유인가(예시3)"로 구현을
고른다. 단일 스레드 기본은 **HashMap**(가장 빠름), 삽입순 필요 시 LinkedHashMap, 정렬 필요 시
TreeMap. 멀티스레드 공유는 **ConcurrentHashMap**(HashTable은 전체 락이라 느려 대체됨). HashMap을
공유 상태로 쓰면 값 유실·무한 루프 같은 심각한 버그가 난다는 점이 핵심이다.

---

## 3. 자기 점검

- **Q. HashTable 대신 ConcurrentHashMap을 권장하는 이유는?**
  - 내 답: 둘 다 스레드 안전하지만 HashTable은 메서드 전체에 락(전체 잠금)이라 동시성이 낮아 느리다.
    ConcurrentHashMap은 락 단위를 버킷/노드로 작게 쪼개 여러 스레드가 동시에 작업해 빠르다. (Example3)

- **Q. TreeMap에 null key를 못 넣는 이유는?**
  - 내 답: TreeMap은 key를 정렬하려고 서로 compareTo로 비교하는데, null은 비교할 수 없어 NPE가 난다.
    value는 정렬과 무관해 허용된다. (Example2)

- **Q. (실무) 멀티스레드에서 HashMap을 공유하면 생기는 두 가지 문제는?**
  - ① 값 유실(lost update) ② resize 중 무한 루프(CPU 100% 점유). Example3는 key를 고정해 ②를
    피했는데, 동시 put으로 크기를 키우면 ②가 재현될 수 있다. 왜 ConcurrentHashMap이 답인지 정리해본다.
