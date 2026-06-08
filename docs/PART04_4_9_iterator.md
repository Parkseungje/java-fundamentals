# PART 4 — 문자열과 컬렉션: 4.9 Iterator

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.9 Iterator**를 다룬다.
> PART 4의 마지막 소단원. 컬렉션 순회의 공통 추상화와 순회 중 수정의 함정을 본다.

---

## 1. 학습 내용 — Iterator 패턴

### 로우레벨의 불편함
Iterator가 없던 시절에는 컬렉션마다 순회 방식이 달랐다. ArrayList는 인덱스(`for i; get(i)`),
HashSet은 인덱스가 없어 다른 방식... 즉 순회하려면 **내부 구조를 알아야** 했고, 그 결과 캡슐화가
깨지고 컬렉션마다 순회 코드가 중복됐다.

### 해결 — Iterator 패턴
모든 `Collection`이 `iterator()`를 제공한다. 반환된 Iterator의 세 메서드로 **내부 구조와 무관하게
동일한 코드**로 순회한다.
- `hasNext()` : 다음 원소가 있는지
- `next()` : 다음 원소를 반환하며 커서 이동
- `remove()` : 방금 next()로 꺼낸 원소를 안전하게 제거

내부가 배열이든 해시든 연결 리스트든, 순회 코드는 똑같다. 컬렉션 종류가 바뀌어도 순회 코드를
그대로 둘 수 있다(다형성).

### for-each는 Iterator다
`for (T x : collection)` 문법은 컴파일 시 **Iterator를 쓰는 코드로 바뀐다**(문법 설탕). 그래서
명시적 Iterator 순회와 동작이 같다.

### 순회 중 수정의 함정 — ConcurrentModificationException(CME)
for-each(= Iterator)로 순회하는 도중에 **컬렉션을 직접 수정**(`collection.remove(...)` 등)하면,
Iterator가 "순회 중 구조가 바뀌었다"를 감지해 **`ConcurrentModificationException`** 을 던진다.
이는 잘못된 순회 버그를 빨리 잡으라는 **fail-fast** 동작이다.

> ⚠️ 유명한 함정: 원소가 3개 `[a,b,c]`일 때 '끝에서 두 번째'인 b를 지우면 **CME가 안 난다.**
> remove 후 size=2, cursor=2가 되어 루프가 `next()`를 다시 부르기 전에 끝나버리기 때문(감지 코드가
> 실행 안 됨). 이 "우연히 안 터지는" 동작에 기대면 안 된다 — 안전한 삭제 방법을 써야 한다.

### 순회 중 안전한 삭제
- **`Iterator.remove()`**: Iterator를 통해 제거하므로 Iterator가 변경을 인지해 CME가 안 난다.
- **`removeIf(predicate)`**: 조건을 주면 컬렉션이 알아서 안전하게 일괄 제거(가장 간결, 권장).

---

## 2. 실습으로 확인하기

> - **가설 1**: Iterator로 내부 구조가 다른 컬렉션들을 같은 코드로 순회할 수 있다.
> - **가설 2**: for-each는 Iterator와 동등하고, 순회 중 컬렉션 직접 수정 시 CME가 난다.
> - **가설 3**: Iterator.remove()/removeIf()는 순회 중 안전하게 삭제한다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_UniformIteration` | 같은 코드로 순회? | List/Set/LinkedList 동일 메서드 |
| `Example2_ForEachUsesIterator` | for-each=Iterator? CME? | for-each vs Iterator + 순회 중 remove |
| `Example3_SafeRemove` | 안전한 삭제? | Iterator.remove() / removeIf() |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s09_iterator.Example1_UniformIteration
java -cp build/classes/java/main com.study.part04_collections.s09_iterator.Example2_ForEachUsesIterator
java -cp build/classes/java/main com.study.part04_collections.s09_iterator.Example3_SafeRemove
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (통일된 순회)** — 가설 1.
- 같은 `countAll(Collection)` 메서드가 ArrayList/HashSet/LinkedList 모두에서 동작(각 원소 수 3). ✅

**예시 2 (for-each=Iterator, CME)** — 가설 2.
- for-each와 명시적 Iterator 순회 결과 동일(`a b c d`).
- for-each 도중 `list.remove("b")` → **ConcurrentModificationException** 발생(fail-fast). ✅
- (함정 확인: 원소 3개에서 끝-2번째를 지우면 CME가 안 나므로, 4개로 두어 확실히 재현)

**예시 3 (안전한 삭제)** — 가설 3.

| 방법 | 결과 |
|---|---|
| `Iterator.remove()`로 홀수 제거 | `[2, 4, 6]` (CME 없음) |
| `removeIf(v -> v%2==1)` | `[2, 4, 6]` (한 줄, CME 없음) |

→ 순회 중 삭제는 컬렉션을 직접 건드리지 말고 Iterator.remove()/removeIf()로. ✅

### 세 예시를 관통하는 결론
Iterator는 "내부 구조를 숨기고 순회 방법만 통일한" 추상화다(예시1). for-each는 그 Iterator를 쓰는
문법 설탕이라(예시2), 순회 중 컬렉션을 직접 수정하면 fail-fast로 CME가 난다 — 그래서 안전한
삭제는 Iterator.remove()나 removeIf()로 한다(예시3). 이로써 PART 4(문자열·컬렉션)가 마무리된다:
자료구조를 용도에 맞게 고르고(4.3~4.7), 그 빠름의 원리(해시 4.8)를 이해하고, 어떤 컬렉션이든
일관되게 순회하는 법(4.9)까지 갖췄다.

---

## 3. 자기 점검

- **Q. for-each 도중 `list.remove(x)`를 하면 왜 예외가 나나?**
  - 내 답: for-each는 Iterator를 쓰는데, 순회 중 컬렉션이 직접 수정되면 Iterator가 modCount 변화를
    감지해 ConcurrentModificationException(fail-fast)을 던진다. (Example2)

- **Q. 순회하면서 조건에 맞는 원소를 지우는 안전한 방법 두 가지는?**
  - 내 답: ① `Iterator.remove()` ② `removeIf(predicate)`. 현대 코드는 removeIf가 간결. (Example3)

- **Q. (함정) `[a,b,c]`에서 for-each로 b를 지우면 CME가 안 나는 이유는?**
  - remove 후 size=2, cursor=2가 되어 hasNext()가 false가 되며 루프가 next() 재호출 전에 끝나,
    감지 코드(checkForComodification)가 실행되지 않기 때문. "우연히 안 터지는" 동작이라 의존 금지.
    (Example2 주석 참고 — 그래서 4개로 재현)
