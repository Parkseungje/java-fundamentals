# PART 5 — 제네릭 · 비교 · 함수형: 5.1 제네릭과 와일드카드 (PECS)

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.1 제네릭과 와일드카드(PECS)**를 다룬다.
> 제네릭의 불공변성에서 출발해, 와일드카드(? extends / ? super)와 PECS 원칙을 본다.

---

## 1. 학습 내용 — 불공변성, 와일드카드, PECS

### 제네릭 타입 파라미터의 관례적 이름 (T, E, K, V ...)
제네릭의 `T` 자리에 들어가는 이름은 사실 아무 글자나 가능하다(`<Banana>`도 컴파일된다). 하지만
가독성을 위해 **역할에 따라 관례적으로 정해진 한 글자 대문자**를 쓴다. 자바 표준 라이브러리도 이 관례를 따른다.

| 글자 | 뜻(유래) | 주로 쓰는 곳 |
|---|---|---|
| `T` | Type (타입) | 일반적인 단일 타입. 가장 많이 씀 (`List<T>`, `Optional<T>`) |
| `E` | Element (원소) | 컬렉션의 원소 (`List<E>`, `Set<E>`, `Collection<E>`) |
| `K` | Key (키) | Map의 키 (`Map<K,V>`) |
| `V` | Value (값) | Map의 값 (`Map<K,V>`), 반환 값 |
| `N` | Number (숫자) | 숫자 타입 |
| `R` | Result/Return (결과) | 반환 타입 (`Function<T,R>`의 R) |
| `S`, `U` | T 다음의 추가 타입 | 두 번째·세 번째 타입 파라미터(`BiFunction<T,U,R>`) |
| `?` | 와일드카드(unknown) | "어떤 타입인지 모름"을 나타냄 (`List<?>`, `? extends T`) |

규칙 요약:
- **한 글자 대문자**를 쓴다(클래스 이름과 구분 — 클래스는 보통 여러 글자).
- 역할이 분명하면 그에 맞는 글자를 쓴다: 원소면 `E`, 맵의 키/값이면 `K`/`V`, 반환이면 `R`.
- 애매하거나 그냥 "어떤 타입"이면 `T`. 타입이 둘 이상이면 `T, U, R`처럼 알파벳을 이어 쓴다.
- 이건 **강제가 아니라 관례**다. 어겨도 컴파일은 되지만, 표준 관례를 따라야 코드를 읽는 사람이
  "아, K는 키구나" 하고 바로 이해한다.

예: `interface Function<T, R>`(T를 받아 R로 변환), `interface Map<K, V>`(K로 V를 찾음),
`class ArrayList<E>`(E 원소들의 리스트). (Function/BiFunction 등 함수형 인터페이스는 5.4 참고)

### 공변(covariant)과 불공변(invariant)이란?
"공변/불공변"은 **타입 사이의 상하 관계(부모-자식)가 그 타입을 담는 그릇(배열·제네릭)에도 그대로
이어지는가**를 가리키는 말이다.

먼저 기본 전제: `String`은 `Object`의 하위 타입이다(String is-a Object). 그래서 `Object o = "hi";`는 된다.

이 관계가 "그릇"으로도 이어지는지 보자. 세 가지 경우가 있다(Fruit ← Apple, 즉 Apple이 Fruit의 하위라고 하자).
- **공변(covariant)**: "Apple이 Fruit의 하위면, Apple 그릇도 Fruit 그릇의 하위로 친다." 즉 하위 관계가
  그릇에 **같은 방향으로 전파**된다. **배열이 공변**이라 `Fruit[] arr = new Apple[3]`이 된다
  (Apple[]을 Fruit[]의 하위로 취급).
- **반공변(contravariant)**: "Apple이 Fruit의 하위면, 그릇에서는 **방향이 뒤집혀** Fruit 그릇을 Apple
  그릇의 하위처럼 취급한다." 즉 상하 관계가 그릇으로 가면서 **반대로 전파**된다.
- **불공변(invariant)**: 하위 관계가 그릇으로 **전혀 전파되지 않는다.** "Apple이 Fruit의 하위여도
  `List<Apple>`과 `List<Fruit>`는 아무 상하 관계 없는 완전히 별개 타입." **제네릭이 기본적으로 불공변**이라
  `List<Fruit> = List<Apple>` 대입이 안 된다(컴파일 에러).

한 줄 정리(그릇 A가 그릇 B를 대신할 수 있는 방향):
- 공변 = **자식 그릇 → 부모 그릇** 자리 OK (방향 그대로)
- 반공변 = **부모 그릇 → 자식 그릇** 자리 OK (방향 반대)
- 불공변 = 어느 쪽도 안 됨 (서로 남남)

### 와일드카드로 공변/반공변을 "선택적으로" 켠다
제네릭 자체는 불공변이지만, 와일드카드로 필요할 때 공변/반공변처럼 동작하게 만들 수 있다.
- `? extends Fruit` = **공변처럼**: `List<? extends Fruit>` 자리에 `List<Apple>`을 넣을 수 있다
  (자식 그릇 → 부모 자리). 대신 꺼내 읽기만 안전(넣기 불가) → Producer.
- `? super Fruit` = **반공변처럼**: `List<? super Fruit>` 자리에 `List<Object>`(상위 타입 그릇)를
  넣을 수 있다 (부모 그릇 → 더 좁은 자리, 방향 반대). 대신 넣기만 안전(꺼내면 Object) → Consumer.

즉 PECS의 extends/super가 바로 **공변/반공변을 코드로 켜는 스위치**이고, 그래서 "Producer-Extends(공변,
읽기), Consumer-Super(반공변, 쓰기)"로 외워진다(아래 PECS 섹션과 직결).

### 불공변성(Invariance) — 배열(공변)은 왜 위험한가
- **배열은 공변(covariant)**: `Object[] arr = new String[3]`이 된다. 하지만 위험하다 — `arr[0] = 123`
  같은 잘못된 대입이 컴파일은 통과하고 **런타임에 `ArrayStoreException`** 으로 터진다.
- **제네릭은 불공변(invariant)**: `List<String>`은 `List<Object>`의 하위 타입이 **아니다.**
  `List<Object> = List<String>` 대입 자체가 **컴파일 에러**다. 잘못된 대입을 **컴파일 시점에** 막아
  타입 안전성을 보장한다.

문제는, 불공변이라 너무 엄격해서 "Fruit이거나 그 하위 타입의 리스트를 유연하게 받고 싶다"가 안 된다.
그 해법이 **와일드카드**다.

### 헷갈리는 지점 ① — `super`가 무슨 뜻이고, 왜 `? super Fruit` 어순인가
`? super Fruit`이 어색하게 느껴진다면 대개 **`super`라는 단어의 뜻**과 **어순**때문이다.

**`super`의 뜻 = "위에 있는, 상위의"** (supervisor=감독자, superior=상급자 처럼 "위"의 느낌).
자바에서도 일관되게 "상위(부모)"를 뜻한다 — 상속에서 쓴 `super()`도 "부모(상위 클래스) 생성자"였다(1.4).
와일드카드의 `super`도 똑같이 "상위"다.

**어순 규칙: 항상 `? [관계] 기준타입` 이고, 주어는 언제나 `?`(빈칸)다.**
`extends`/`super`는 자바 어디서나 "왼쪽이 오른쪽을 ~한다"로 읽는다(클래스 선언 `class Apple extends Fruit`와 동일).
```
? extends Fruit   →  "?가 Fruit를 extends" = ?는 Fruit의 '하위'(자식 쪽)
? super   Fruit   →  "?가 Fruit를 super"   = ?는 Fruit의 '상위'(부모 쪽)
```
그래서 `Fruit super ?`처럼 쓰면 안 된다 — 그러면 주어가 Fruit가 되어 어순이 깨진다. 자바는 **제한받는
쪽(?)을 항상 앞에** 둔다. `? super Fruit`은 "**?가 Fruit의 상위(super)다**"라고 읽으면 자연스럽다.

**방향 그림** (extends=아래로 뻗는 자식, super=위에 있는 부모):
```
        Object         ← super 방향 (위/상위)
          |
        Fruit  ←─────── 기준타입
        /   \
     Apple  Banana      ← extends 방향 (아래/하위)

? extends Fruit  →  Fruit '이하'  : {Fruit, Apple, Banana}   (Fruit가 천장)
? super   Fruit  →  Fruit '이상'  : {Fruit, Object}          (Fruit가 바닥)
```
정리: **extends = 아래(자식 쪽), super = 위(부모 쪽)**. 단어 뜻만 알면 방향이 안 헷갈린다.

#### 흔한 오독 — 주어를 Fruit로 뒤집어 읽기 (★ 가장 많이 헷갈리는 지점)
`extends`는 동사("확장한다")라 자연스러운데, `super`는 명사("상위")처럼 느껴져서 어순을 뒤집어
읽기 쉽다. 둘 다 **주어는 `?`** 인데, 아래처럼 읽으면 함정에 빠진다.

| 문법 | ❌ 잘못된 읽기 (Fruit가 주어) | ✅ 올바른 읽기 (?가 주어) | 결과 |
|---|---|---|---|
| `? extends Fruit` | "Fruit을 ?로 확장한다" | "**?가** Fruit를 확장한다" | ? = Fruit의 자식 |
| `? super Fruit` | "Fruit의 상위는 ?" | "**?가** Fruit의 상위다" | ? = Fruit의 부모 |

핵심은 **`super`도 동사처럼 "~의 상위다"로 읽는 것**이다. 그러면 둘 다 "**? 가 ___ 다**"로 어순이
같아진다 — 주어는 항상 `?`이고, Fruit는 천장(extends)이냐 바닥(super)이냐의 기준선일 뿐이다.
- `? extends Fruit` = "?가 Fruit를 확장한다" → **?는 Fruit의 자식** {Fruit, Apple, Banana}
- `? super Fruit` = "?가 Fruit의 상위다" → **?는 Fruit의 부모** {Fruit, Object}

"Fruit의 상위는 ?"처럼 Fruit를 주어로 뒤집어 읽으면 방향이 꼬인다. **항상 `?`를 주어로** 읽자.

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

#### 헷갈리는 지점 ② — produce(생산)/consume(소비)의 '주어'는 컬렉션이다 (★)
`produce`=생산하다, `consume`=소비하다가 맞다. 헷갈리는 건 **"누가" 생산/소비하느냐**인데, PECS에서
그 주어는 **내(코드)가 아니라 컬렉션(자료구조)** 이다.

- **Producer(생산자) = 컬렉션이 데이터를 내놓는다(공급)** → 우리는 거기서 **꺼내 읽는다** → `extends`
- **Consumer(소비자) = 컬렉션이 데이터를 받아들인다(흡수)** → 우리는 거기에 **넣는다** → `super`

내 입장에서 생각하면 거꾸로 느껴진다("꺼내는 건 내가 받는 거니 소비 아냐?", "넣는 건 내가 주는 거니
생산 아냐?"). 하지만 **주어를 컬렉션으로 두면** 자연스럽다.

| 내가 하는 일 | 컬렉션 관점(주어=컬렉션) | 역할 | 와일드카드 |
|---|---|---|---|
| 꺼내 읽기 | 컬렉션이 원소를 **생산(공급)** | Producer | `extends` |
| 넣기 | 컬렉션이 원소를 **소비(흡수)** | Consumer | `super` |

비유: 컬렉션을 자판기/창고로 보자.
- 자판기에서 음료를 **꺼낸다** → 자판기가 음료를 **생산(공급)** = Producer = `extends`
- 창고에 물건을 **넣는다** → 창고가 물건을 **소비(받아들임)** = Consumer = `super`

즉 "꺼낸다=Producer-Extends, 넣는다=Consumer-Super"로 외우되, produce/consume의 주체는 **데이터를
담고 있는 컬렉션**이라는 점만 기억하면 된다.

### PECS는 "지켜야 하는 규칙"인가, "안 지키면 컴파일 오류"인가?
**둘 다 맞다.** 정확히는 **"PECS를 안 지키면 필요한 연산이 컴파일되지 않는다"** 가 핵심이다.
PECS는 자바가 강제로 검사하는 문법이 아니라, **컴파일러가 와일드카드에 자동으로 거는 제약을
거꾸로 정리한 가이드**다. 그래서 PECS를 어기면 "그 자리에서 PECS 위반!" 같은 에러가 뜨는 게 아니라,
**그 와일드카드로 하려던 동작(읽기 또는 쓰기)이 컴파일 에러로 막힌다.** 두 방향으로 보자.

1. **`? extends T`로 선언해 놓고 "넣으려(쓰려)" 하면 → 컴파일 에러.**
   `List<? extends Fruit>`는 "Fruit이거나 그 하위 타입의 리스트"인데, 컴파일러는 그게 정확히
   List\<Apple\>인지 List\<Banana\>인지 모른다. 그래서 add를 허용하면 List\<Banana\>에 Apple을 넣는
   타입 사고가 날 수 있어, **컴파일러가 add를 아예 금지**한다(null만 허용). → "꺼내기만 할 거면
   extends"라는 PECS의 P가 여기서 나온다.

2. **`? super T`로 선언해 놓고 "꺼내서 T로 쓰려" 하면 → 컴파일 에러.**
   `List<? super Fruit>`는 "Fruit이거나 그 상위 타입(Object 등)의 리스트"라, 꺼낸 원소가 Fruit이라는
   보장이 없다(Object일 수도). 그래서 **꺼낸 것을 Fruit 변수에 담는 코드가 컴파일 에러**가 되고,
   Object로만 받을 수 있다. → "넣기만 할 거면 super"라는 PECS의 C가 여기서 나온다.

즉 PECS는 **"컴파일러가 막는 방향을 피해서, 하려는 동작이 되도록 와일드카드를 고르는 법"** 이다.
- 잘못된 와일드카드를 골라도 "선언" 자체는 컴파일된다. **문제는 그 안에서 하려는 연산(add 또는
  꺼내 쓰기)이 막힌다**는 것. 그래서 "안 지키면 컴파일 오류"라는 말과 "지켜야 하는 규칙"이라는 말이
  같은 얘기다.
- **왜 컴파일러가 이렇게 막는가?** 제네릭의 존재 이유인 **타입 안전성을 컴파일 시점에 보장**하기
  위해서다(불공변과 같은 동기). 만약 막지 않으면, 배열의 `ArrayStoreException`(예시1)처럼 런타임에야
  터지는 타입 사고가 제네릭에서도 생긴다. PECS를 따르면 그 사고를 컴파일 단계에서 원천 차단하면서도
  최대한 유연한 API를 만들 수 있다.

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
