# PART 5 — 제네릭 · 비교 · 함수형: 5.2 Comparable & Comparator

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.2 Comparable & Comparator**를 다룬다.
> 4.5/4.7에서 미뤘던 TreeSet/TreeMap의 "정렬 기준"이 여기서 깊이 다뤄진다.

---

## 1. 학습 내용 — 객체를 어떻게 비교/정렬하는가

객체는 `<`, `>`로 비교할 수 없다(어떤 필드 기준인지 모호하므로). 그래서 자바는 두 가지 비교 도구를 제공한다.

### Comparable — 자기 자신의 자연 순서 (클래스 내부, 1개)
클래스가 `Comparable<T>`를 구현하고 `compareTo(T o)` 하나를 정의한다. 이것이 그 클래스의
**기본 정렬 기준(natural ordering, 자연 순서)** 이다.
- 반환값 약속: this가 o보다 **작으면 음수, 같으면 0, 크면 양수**.
- `Collections.sort()`, `TreeSet`, `TreeMap`이 별도 기준 없이 **자동으로** 이 compareTo를 쓴다.

### Comparator — 외부에서 주입하는 비교 기준 (외부, 무제한)
`compare(o1, o2)`를 정의하는 별도 객체. 클래스 **외부에서** 정의하므로:
- **여러 정렬 기준**을 만들 수 있다(나이순/이름순/역순...).
- 클래스 **수정 권한이 없어도** 정렬 기준을 추가할 수 있다.
- 콤비네이터로 조합: `Comparator.comparing(...)`, `comparingInt(...)`, `reversed()`, `thenComparing(...)`.

```java
Comparator<Member> byAge  = Comparator.comparingInt(m -> m.age);
Comparator<Member> byName = Comparator.comparing(m -> m.name);
list.sort(byAge.reversed());                  // 나이 내림차순
list.sort(byName.thenComparing(byAge));       // 이름순, 같으면 나이순
```

| | Comparable | Comparator |
|---|---|---|
| 위치 | 클래스 내부 | 외부 |
| 메서드 | `compareTo(o)` | `compare(o1, o2)` |
| 기준 수 | 1개(기본 정렬) | 무제한 |
| 자동 사용처 | sort/TreeSet/TreeMap | 명시적으로 주입 시 |

TreeSet/TreeMap에 Comparator를 주면 **그 Comparator가 자연 순서를 덮어쓴다**(우선).

### 'a - b' 오버플로 함정 (★ 실무 주의)
정수 비교를 `(a, b) -> a - b`로 짜면, 빼기 결과가 int 범위를 넘을 때(예: `MAX - MIN`) **오버플로로
부호가 뒤집혀** 잘못된 비교가 된다. **`Integer.compare(a, b)`** (또는 `Comparator.comparingInt`)는
빼지 않고 비교만 하므로 안전하다.

---

## 2. 실습으로 확인하기

> - **가설 1**: Comparable의 compareTo(자연 순서)를 sort/TreeSet이 자동 사용한다.
> - **가설 2**: Comparator로 클래스 수정 없이 여러 기준(나이/이름/역순/다단계)을 만든다.
> - **가설 3**: TreeSet에 Comparator를 주면 자연 순서를 덮어쓴다. 'a-b' 비교는 오버플로로 깨진다.

### 모델 코드 (`com.study.part05_generics_functional.s02_comparable_comparator`)
- `Member`(name, age) — Comparable 구현(자연 순서 = 나이순).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_Comparable` | 자연 순서 자동 사용? | compareTo + sort/TreeSet |
| `Example2_Comparator` | 외부 여러 기준? | byAge/byName/reversed/thenComparing |
| `Example3_NaturalVsComparatorAndOverflow` | 우선순위? 오버플로? | Comparator 주입 + a-b 함정 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part05_generics_functional.s02_comparable_comparator.Example1_Comparable
java -cp build/classes/java/main com.study.part05_generics_functional.s02_comparable_comparator.Example2_Comparator
java -cp build/classes/java/main com.study.part05_generics_functional.s02_comparable_comparator.Example3_NaturalVsComparatorAndOverflow
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Comparable)** — 가설 1.
- `compareTo`: 25.compareTo(30)=-1(앞), 30.compareTo(25)=1(뒤), 같으면 0.
- `Collections.sort`/`TreeSet` 모두 별도 기준 없이 나이 오름차순 정렬: `[철수(25), 민수(28), 영희(30)]`. ✅

**예시 2 (Comparator)** — 가설 2.

| 기준 | 결과 |
|---|---|
| byAge | 나이 오름차순 |
| byName | 이름 사전순 |
| byAge.reversed() | 나이 내림차순 |
| byName.thenComparing(byAge) | 이름순, 같으면 나이순 |

→ 클래스 수정 없이 외부에서 여러 기준을 만들고 조합. ✅

**예시 3 (우선순위 + 오버플로)** — 가설 3.
- 같은 데이터: 자연 순서 TreeSet은 나이순, 이름순 Comparator 주입 TreeSet은 이름순 → **Comparator가 우선**.
- 오버플로: `big > small`이라 양수여야 하는데
  - `(x - y)` 방식 → **-1** (오버플로로 부호 뒤집힘, 버그!)
  - `Integer.compare` → **1** (정상)
  ✅

### 세 예시를 관통하는 결론
Comparable은 클래스의 **기본 정렬 기준 하나**를 내부에 두고(예시1), Comparator는 **외부에서 무제한의
기준**을 만든다(예시2). TreeSet/TreeMap에 Comparator를 주면 자연 순서를 덮어쓴다(예시3). 그리고
정수 비교는 반드시 `Integer.compare`/`comparingInt`로 — `a - b`는 오버플로 버그를 부른다(예시3). 이것이
4.5/4.7에서 본 "TreeSet은 비교 기준이 필요하다"의 완성된 그림이다.

---

## 3. 자기 점검

- **Q. Comparable과 Comparator의 차이를 표로?**
  - 내 답: Comparable=클래스 내부·compareTo·기본 기준 1개(sort/TreeSet 자동 사용), Comparator=외부·
    compare·무제한 기준(주입). (Example1 vs Example2)

- **Q. TreeSet에 Comparator를 주면 자연 순서는?**
  - 내 답: Comparator가 우선해 자연 순서를 덮어쓴다. (Example3의 이름순 TreeSet)

- **Q. (★ 실무) 정수 비교를 `a - b`로 하면 안 되는 이유는?**
  - 내 답: 빼기 결과가 int 범위를 넘으면(예: MAX - MIN) 오버플로로 부호가 뒤집혀 잘못된 정렬이
    된다. `Integer.compare(a, b)`나 `Comparator.comparingInt`를 쓴다. (Example3의 -1 vs 1)
