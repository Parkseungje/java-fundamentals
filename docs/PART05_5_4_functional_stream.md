# PART 5 — 제네릭 · 비교 · 함수형: 5.4 함수형 인터페이스 + 람다 + 스트림

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.4 함수형 인터페이스 + 람다 + 스트림**을 다룬다.
> PART 5의 마지막. 1.7에서 예고한 "익명 클래스 → 람다"가 여기서 완성되고 스트림까지 이어진다.

---

## 1. 학습 내용 — 함수를 값처럼 다루기

### 함수형 인터페이스
**추상 메서드가 정확히 1개**인 인터페이스. 그래서 람다로 인스턴스화할 수 있다. `@FunctionalInterface`를
붙이면 컴파일러가 "추상 메서드 1개"인지 검증한다(필수는 아니지만 실수 방지). 자바 표준 4종:

| 인터페이스 | 시그니처 | 용도 |
|---|---|---|
| `Function<T,R>` | `R apply(T)` | 변환 (입력→출력) |
| `Predicate<T>` | `boolean test(T)` | 조건 (입력→boolean) |
| `Consumer<T>` | `void accept(T)` | 소비 (입력→void) |
| `Supplier<T>` | `T get()` | 공급 (입력 없이→값) |

### 람다와 캡처
람다는 익명 함수의 간결한 표현이자 **익명 클래스의 발전형(축약)**(1.7). 람다는 바깥 메서드의
지역변수를 **캡처**해 쓸 수 있는데, 그 변수는 **사실상 final(effectively final)** 이어야 한다. 람다가
변수의 **값을 복사해 가두기** 때문이다(지역변수는 메서드 종료 시 사라지지만 람다는 더 오래 살 수
있으므로). 캡처 변수를 재할당하려 하면 컴파일 에러.

### 스트림 (I/O 스트림과 무관)
컬렉션·배열에 **동일 연산을 선언적으로** 적용하는 도구. **한 번만 사용 가능**(최종 연산 후 닫힘).
- **중간 연산**(Stream 반환, **지연 평가**): `filter, map, sorted, distinct, limit, skip` — 쌓기만 한다.
- **최종 연산**(값/void): `forEach, collect, count, reduce, anyMatch` — 이때 비로소 실행된다.
- **중간 연산만 호출하고 최종 연산이 없으면 아무것도 실행되지 않는다**(지연 평가/lazy).

```java
List<String> result = words.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .sorted()
    .collect(Collectors.toList());

Map<Integer, List<String>> byLen =
    words.stream().collect(Collectors.groupingBy(String::length));
```

---

## 2. 실습으로 확인하기

> - **가설 1**: 추상 메서드 1개 인터페이스는 람다로 인스턴스화(익명 클래스와 동등). 표준 4종 동작.
> - **가설 2**: 람다 캡처 변수는 사실상 final(재할당 시 컴파일 에러).
> - **가설 3**: 중간 연산은 지연 평가 — 최종 연산이 없으면 실행되지 않는다.
> - **가설 4**: 스트림은 collect/groupingBy로 수집하며, 한 번 소비하면 재사용 불가(IllegalStateException).

### 모델 코드 (`com.study.part05_generics_functional.s04_functional_stream`)
- `Calculator` — `@FunctionalInterface` (추상 메서드 1개).

### 예시 4개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_FunctionalInterfaces` | 함수형 인터페이스·람다? | 익명클래스 vs 람다 + 표준 4종 |
| `Example2_LambdaCapture` | 캡처 제약? | 사실상 final |
| `Example3_StreamLazy` | 지연 평가? | peek로 실행 시점 관찰 |
| `Example4_StreamCollectors` | 수집·재사용? | collect/groupingBy + 일회용 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part05_generics_functional.s04_functional_stream.Example1_FunctionalInterfaces
java -cp build/classes/java/main com.study.part05_generics_functional.s04_functional_stream.Example2_LambdaCapture
java -cp build/classes/java/main com.study.part05_generics_functional.s04_functional_stream.Example3_StreamLazy
java -cp build/classes/java/main com.study.part05_generics_functional.s04_functional_stream.Example4_StreamCollectors
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (함수형 인터페이스)** — 가설 1.
- 익명 클래스 `calculate(2,3)`=5 == 람다 `calculate(2,3)`=5 (동등).
- Function `"hello".length()`=5 / Predicate `4 짝수?`=true / Consumer 출력 / Supplier 공급. ✅

**예시 2 (캡처)** — 가설 2.
- 캡처한 `message`(사실상 final)를 람다가 정상 사용. 재할당하면 컴파일 에러(주석). ✅

**예시 3 (지연 평가)** — 가설 3.

| 경우 | peek 출력 |
|---|---|
| (A) 중간 연산만 (최종 없음) | **없음** (실행 안 됨) |
| (B) 최종 연산(count) 추가 | peek 1~5 모두 찍힘 (실행됨) |

→ 중간 연산은 지연(lazy), 최종 연산이 방아쇠. 최종 연산이 없으면 아무것도 실행되지 않는다. ✅

**예시 4 (수집·재사용)** — 가설 4.
- `filter(>3)→map(대문자)→sorted→collect`: `[APPLE, BANANA, CHERRY, DATE, KIWI]`
- `groupingBy(길이)`: `{3=[fig], 4=[kiwi, date], 5=[apple], 6=[banana, cherry]}`
- 같은 스트림에 count 두 번 → 둘째에서 **IllegalStateException**(이미 닫힘). ✅

### 네 예시를 관통하는 결론
함수형 인터페이스 + 람다는 "동작(함수)을 값처럼 변수에 담아 전달"하게 해준다(예시1). 람다는
바깥 지역변수를 값 복사로 캡처하므로 그 변수는 사실상 final이다(예시2). 이 람다들이 스트림의
연산(filter/map 등)으로 들어가, 데이터를 선언적으로 가공한다 — 단 중간 연산은 지연되고(예시3)
최종 연산이 실행을 촉발하며, 스트림은 일회용이다(예시4). 이로써 PART 5(제네릭·비교·함수형)가
마무리된다: 타입 안전(제네릭) + 비교 기준(Comparable/Comparator) + 런타임 조작(Reflection) +
함수를 값으로 다루기(함수형/스트림).

---

## 3. 자기 점검

- **Q. 스트림이 한 번만 사용 가능하고 지연 평가되는 이유는?**
  - 내 답: 중간 연산은 '무엇을 할지' 설계도만 쌓고(지연), 최종 연산이 호출돼야 한꺼번에 실행된다.
    최종 연산을 수행하면 스트림이 닫혀 재사용 불가(IllegalStateException). (Example3, Example4)

- **Q. 람다 캡처 변수가 사실상 final이어야 하는 이유는?**
  - 내 답: 람다가 변수의 값을 복사해 가두는데(지역변수는 메서드 종료 시 사라지므로), 변수가 바뀔 수
    있으면 "어느 시점 값"인지 모호해진다. 그래서 자바가 사실상 final을 강제한다. (Example2)

- **Q. (적용) 표준 함수형 인터페이스 4종을 스트림 연산과 연결하면?**
  - `filter`는 Predicate, `map`은 Function, `forEach`는 Consumer, 그리고 값 생성엔 Supplier가 쓰인다.
    Example1의 4종과 Example3/4의 스트림 연산을 매칭해 정리해본다.
