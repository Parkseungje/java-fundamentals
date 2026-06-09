# PART 5 — 제네릭 · 비교 · 함수형: 5.1 제네릭과 와일드카드 (PECS)

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.1 제네릭과 와일드카드(PECS)**를 다룬다.
> 제네릭의 불공변성에서 출발해, 와일드카드(? extends / ? super)와 PECS 원칙을 본다.

---

## 1. 학습 내용 — 불공변성, 와일드카드, PECS

### 불공변성(Invariance)
- **배열은 공변(covariant)**: `Object[] arr = new String[3]`이 된다. 하지만 위험하다 — `arr[0] = 123`
  같은 잘못된 대입이 컴파일은 통과하고 **런타임에 `ArrayStoreException`** 으로 터진다.
- **제네릭은 불공변(invariant)**: `List<String>`은 `List<Object>`의 하위 타입이 **아니다.**
  `List<Object> = List<String>` 대입 자체가 **컴파일 에러**다. 잘못된 대입을 **컴파일 시점에** 막아
  타입 안전성을 보장한다.

문제는, 불공변이라 너무 엄격해서 "Fruit이거나 그 하위 타입의 리스트를 유연하게 받고 싶다"가 안 된다.
그 해법이 **와일드카드**다.

### 와일드카드 — 상한(? extends)과 하한(? super)
- **`? extends T` (상한, Producer)**: "T이거나 그 하위 타입". 꺼내면 최소 T임이 보장되어 **읽기 안전**.
  하지만 정확한 타입을 모르므로(Apple 리스트일 수도 Banana 리스트일 수도) **넣기 불가**(null만 가능).
- **`? super T` (하한, Consumer)**: "T이거나 그 상위 타입". T(과 그 하위)를 **넣기 안전**(어떤 상위
  타입 리스트든 T는 담을 수 있으므로). 하지만 꺼내면 타입이 **Object로만** 나온다.
- **무제한 `?`**: 어떤 타입이든 OK, 꺼낸 원소는 Object로만, add 불가.

### PECS 원칙
한 메서드가 한 컬렉션에서 꺼내(produce) 다른 컬렉션에 넣을(consume) 때:
- **꺼낸다(Produce) → `extends`**
- **넣는다(Consume) → `super`**
- **둘 다면 → `T` 자체**

이것이 **PECS(Producer-Extends, Consumer-Super)**. `copy(List<? extends T> src, List<? super T> dst)`처럼
선언하면, src로 `List<Apple>`을, dst로 `List<Fruit>`나 `List<Object>`를 받을 수 있어 가장 유연하다.
(자바 표준 `Collections.copy`도 이 패턴.)

---

## 2. 실습으로 확인하기

> - **가설 1**: 배열은 공변(런타임 위험), 제네릭은 불공변(컴파일 차단).
> - **가설 2**: ? extends는 읽기 안전·쓰기 불가, ? super는 쓰기 안전·읽기는 Object.
> - **가설 3**: PECS로 선언하면 copy가 다양한 src/dst 타입을 유연하게 받는다.

### 모델 코드 (`com.study.part05_generics_functional.s01_generics_pecs`)
- `Fruit` ← `Apple`, `Banana` 상속 계층.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_Invariance` | 제네릭은 공변? | 배열 vs 제네릭 대입 |
| `Example2_ExtendsVsSuper` | extends/super 차이? | 읽기/쓰기 가능 여부 |
| `Example3_PECS` | 둘 다 쓰려면? | copy(src, dst) |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part05_generics_functional.s01_generics_pecs.Example1_Invariance
java -cp build/classes/java/main com.study.part05_generics_functional.s01_generics_pecs.Example2_ExtendsVsSuper
java -cp build/classes/java/main com.study.part05_generics_functional.s01_generics_pecs.Example3_PECS
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (불공변성)** — 가설 1.

| | 배열(공변) | 제네릭(불공변) |
|---|---|---|
| 상위 타입 대입 | `Object[] = String[]` OK | `List<Object> = List<String>` **컴파일 에러** |
| 잘못된 사용 | `arr[0]=123` → **런타임 ArrayStoreException** | 애초에 컴파일 단계에서 차단 |

→ 제네릭은 타입 오류를 런타임이 아니라 컴파일 시점에 막는다(그래서 불공변). ✅

**예시 2 (extends/super)** — 가설 2.

| 와일드카드 | 읽기(get) | 쓰기(add) |
|---|---|---|
| `? extends Fruit` | Fruit로 읽기 OK | **컴파일 에러** (넣기 불가) |
| `? super Fruit` | Object로만 | Apple/Banana 넣기 OK |

→ extends=읽기 안전, super=쓰기 안전. 이 비대칭이 PECS의 근거. ✅

**예시 3 (PECS)** — 가설 3.
- `copy(List<Apple>, List<Fruit>)` → `[Apple, Apple]` 복사 성공.
- `copy(List<Apple>, List<Object>)` → 성공(Object도 Fruit 상위라 super 만족).

→ src=`? extends T`(Producer), dst=`? super T`(Consumer)로 선언해 다양한 타입을 유연하게 받음. ✅

### 세 예시를 관통하는 결론
제네릭은 타입 안전성을 위해 불공변이다(예시1). 그 엄격함을 유연하게 풀되 안전성을 유지하는 도구가
와일드카드이고(예시2), 그 사용 규칙이 PECS다(예시3). **"꺼내면 extends, 넣으면 super, 둘 다면 T"** —
이 한 줄이 제네릭 API 설계의 핵심이다.

---

## 3. 자기 점검

- **Q. PECS를 한 문장으로?**
  - 내 답: Producer-Extends, Consumer-Super. 꺼내(생산) 읽을 컬렉션은 `? extends T`, 넣을(소비)
    컬렉션은 `? super T`. (Example3의 copy 시그니처)

- **Q. 제네릭이 배열과 달리 불공변인 이유는?**
  - 내 답: 타입 오류를 런타임(ArrayStoreException)이 아니라 컴파일 시점에 막기 위해. `List<Object> =
    List<String>`를 허용하면 List<String>에 Integer를 넣는 사고가 가능해지므로 아예 대입을 금지한다. (Example1)

- **Q. (추가) `? extends Fruit` 리스트에 왜 add를 못 하나?**
  - 그 리스트가 List<Apple>인지 List<Banana>인지 컴파일러가 모르기 때문. Apple을 넣으려 해도 실제가
    List<Banana>면 타입 위반이라, 안전을 위해 add 자체를 막는다(null만 허용). Example2에서 직접 확인.
