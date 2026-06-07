# PART 1 — 객체지향(OOP) 기초: 1.5 다형성 + instanceof + 형변환

> 이 문서는 커리큘럼 PART 1의 소단원 중 **1.5 다형성 + instanceof + 형변환**만 다룬다.

---

## 1. 학습 내용 — 다형성, 컴파일 타임 타입과 런타임 타입, 안전한 형변환

### 다형성과 동적 바인딩
**다형성(polymorphism)** 은 "하나의 부모 타입 변수가 여러 종류의 자식 객체를 가리킬 수 있고,
같은 메서드 호출이 실제 객체에 따라 다르게 동작하는" 성질이다.

```java
Animal a = new Dog();
a.makeSound();  // 실행되는 것은 Dog.makeSound() — 변수 타입(Animal)이 아니라 실제 객체 기준
```

여기서 핵심은 **동적 바인딩(dynamic binding)** 이다. 자바는 `a.makeSound()`를 호출할 때
변수 a의 타입(Animal)이 아니라 **a가 실제로 가리키는 객체(Dog)** 의 오버라이딩된 메서드를
실행한다. 이 결정은 컴파일 시점이 아니라 **런타임에** 이루어진다.

다형성의 가치는 **호출하는 쪽이 구체 타입을 몰라도 된다**는 데 있다. `Animal[]`에 Dog든
Cat이든 담아 두고 `makeSound()`만 호출하면, 각 객체가 알아서 자기 방식으로 동작한다.
새로운 동물(예: Bird)을 추가해도 호출 코드는 바뀌지 않는다 — 이것이 1.8에서 다룰 OCP의 뿌리다.

### 컴파일 타임 타입 ≠ 런타임 타입
변수에는 두 가지 타입이 있다.

- **컴파일 타임 타입**: 변수를 선언한 타입(`Animal a`의 Animal). 컴파일러는 이 타입만 보고
  "이 변수로 어떤 메서드를 호출할 수 있는가"를 검사한다.
- **런타임 타입**: 실제로 담긴 객체의 타입(`new Dog()`의 Dog). 실제로 어떤 구현이 실행되는지를 결정한다.

이 둘이 다르기 때문에 다음과 같은 일이 생긴다.

- `a.makeSound()` → Animal에 선언돼 있으므로 컴파일 OK. 실행은 동적 바인딩으로 Dog 것.
- `a.fetch()` → fetch()는 Dog에만 있고 Animal에는 없다. 변수 타입이 Animal이라 **컴파일러가
  "Animal에는 fetch()가 없다"며 막는다.** 실제 객체는 Dog지만, 컴파일러는 변수 타입만 본다.

### instanceof와 안전한 형변환
자식 고유 메서드(fetch)를 호출하려면 변수를 **자식 타입으로 형변환(캐스팅)** 해야 한다.
그런데 캐스팅은 위험하다. 같은 상속 계열이면 컴파일러가 `(Dog) a`를 문법적으로 허용하지만,
실제 객체가 Dog가 아니면 **런타임에 `ClassCastException`** 이 터진다.

그래서 캐스팅 전에 **`instanceof`로 실제 타입을 먼저 확인**한다.

```java
if (a instanceof Dog d) {   // Java 16+ 패턴 매칭: 검사 + 캐스팅을 한 번에
    d.fetch();              // a가 정말 Dog일 때만 이 블록에 들어옴 -> 안전
}
```

`instanceof Dog d`는 "a가 Dog면 true이고, 동시에 a를 Dog로 캐스팅한 변수 d를 만들어준다."
검사에 통과한 경우에만 블록 안으로 들어오므로 ClassCastException을 원천 예방한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 부모 타입 변수로 호출해도 실제 객체의 오버라이딩된 메서드가 실행된다(동적 바인딩).
> - **가설 2**: 부모 타입 변수로는 자식 고유 메서드를 못 부른다(컴파일 타임 타입 기준). instanceof+캐스팅으로 가능.
> - **가설 3**: instanceof 없이 잘못 캐스팅하면 런타임 ClassCastException, 검사하면 안전하게 회피.

### 모델 코드 (`com.study.part01_oop.s05_polymorphism`)
- `Animal`(추상, `makeSound()`) / `Dog`(오버라이딩 + 고유 `fetch()`) / `Cat`(오버라이딩 + 고유 `scratch()`).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_DynamicBinding` | 부모 타입 호출 시 어느 메서드 실행? | `Animal[]`에 Dog/Cat 섞어 `makeSound()` 반복 |
| `Example2_CompileTimeVsRuntimeType` | 자식 고유 메서드는 어떻게 부르나? | `a.fetch()` 직접 불가 → instanceof 패턴 매칭 |
| `Example3_WrongCastWithoutCheck` | instanceof 검사가 왜 필요? | Cat을 Dog로 잘못 캐스팅 (자기 점검) |

### 실행
```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part01_oop.s05_polymorphism.Example1_DynamicBinding
java -cp build/classes/java/main com.study.part01_oop.s05_polymorphism.Example2_CompileTimeVsRuntimeType
java -cp build/classes/java/main com.study.part01_oop.s05_polymorphism.Example3_WrongCastWithoutCheck
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (동적 바인딩)** — 가설 1.

| 객체 | 같은 호출 `a.makeSound()` 결과 | 가설 부합 |
|---|---|---|
| 초코(Dog) | 멍멍 | ✅ |
| 나비(Cat) | 야옹 | ✅ |

→ 똑같은 `a.makeSound()`인데 객체에 따라 결과가 갈린다. 변수 타입(Animal)이 아니라 실제 객체의 메서드가 실행된다.

**예시 2 (컴파일 타임 vs 런타임 타입)** — 가설 2.

| 호출 | 결과 | 이유 |
|---|---|---|
| `a.makeSound()` | 멍멍 | Animal에 선언됨 → 컴파일 OK + 동적 바인딩 |
| `a.fetch()` (직접) | 컴파일 에러 | 변수 타입 Animal엔 fetch() 없음 |
| `if (a instanceof Dog d) d.fetch()` | 초코가 공을 물어온다 | 런타임 타입 확인 후 캐스팅 |

→ 컴파일 타임 타입은 "호출 가능 범위"를, 런타임 타입은 "실제 실행"을 결정한다. 둘은 별개.

**예시 3 (잘못된 캐스팅 = 자기 점검)** — 가설 3.

| 방식 | 결과 |
|---|---|
| 검사 없이 `(Dog) a` (실제 Cat) | **ClassCastException**: `Cat cannot be cast to Dog` |
| `if (a instanceof Dog d)` | false → 캐스팅 시도 안 함, 안전하게 건너뜀 |

→ **자기 점검 답**: 캐스팅은 같은 계열이면 컴파일러가 막지 못하므로(문법상 허용), instanceof로
"실제 그 타입이 맞는지"를 런타임에 먼저 확인해야 ClassCastException(런타임 크래시)을 예방할 수 있다.

### 세 예시를 관통하는 결론
다형성은 "부모 타입으로 묶어도 실제 객체가 알아서 동작한다(예시1)"는 강력함을 주지만,
그 대가로 "변수 타입과 실제 타입이 다를 수 있다(예시2)"는 복잡성이 따라온다. 자식 고유 기능에
접근하려고 캐스팅할 때는 이 간극 때문에 위험이 생기므로, instanceof로 런타임 타입을 먼저
확인하는 것이 안전한 형변환의 기본(예시3)이다.

---

## 3. 자기 점검

- **Q. 캐스팅 전에 instanceof 검사가 왜 필요한가?**
  - 내 답: 캐스팅은 같은 상속 계열이면 컴파일러가 통과시키지만 실제 객체가 다르면 런타임에
    ClassCastException이 난다. instanceof로 실제 타입을 미리 확인하면 이 크래시를 예방할 수 있다.
  - 근거 코드: `Example3_WrongCastWithoutCheck` — 검사 없이 캐스팅 시 예외, instanceof 가드 시 안전.

- **Q. (추가) 다형성이 OCP(개방-폐쇄 원칙)와 어떻게 연결되나?**
  - `Example1`의 `Animal[]` 반복 코드는 새로운 동물(Bird 등)을 추가해도 바뀌지 않는다.
    직접 `Bird extends Animal`을 만들어 배열에 추가해보고, 호출 코드를 한 줄도 안 고쳐도
    동작하는지 확인한다. (이 성질이 1.8 OCP의 핵심 — "확장에 열리고 수정에 닫힘")
