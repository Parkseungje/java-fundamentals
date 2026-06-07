# PART 1 — 객체지향(OOP) 기초: 1.7 Nested / Inner / Anonymous 클래스

> 이 문서는 커리큘럼 PART 1의 소단원 중 **1.7 Nested / Inner / Anonymous 클래스**만 다룬다.
> 익명 클래스는 람다의 전신으로, PART 5(함수형)와 이어진다.

---

## 1. 학습 내용 — 클래스 안의 클래스, 그 종류와 차이

클래스 안에 또 다른 클래스를 정의할 수 있다. 이를 **중첩 클래스(nested class)** 라 하며,
"바깥 인스턴스에 묶이는가"에 따라 성격이 갈린다.

### static nested class (정적 중첩 클래스)
- 바깥 클래스의 **이름공간 안에 있을 뿐, 바깥 인스턴스와는 독립**적이다.
- `new Outer.Nested()` 처럼 바깥 객체 없이 바로 생성한다.
- 바깥의 **static 멤버만** 접근 가능(인스턴스 멤버는 "누구의 것인지" 알 수 없어 접근 불가).
- 용도: 바깥 인스턴스 상태가 필요 없는 헬퍼/빌더 등.

### inner class (내부 클래스, non-static)
- 항상 **특정 바깥 인스턴스에 소속**된다.
- `outerInstance.new Inner()` 처럼 바깥 객체를 통해서만 생성한다.
- 자신을 만든 바깥 인스턴스를 **숨은 참조로 들고 있어, 그 인스턴스 필드에 직접 접근**한다.
- 용도: 바깥 객체의 상태와 긴밀히 협력하는 보조 객체.

### local class / anonymous class
- **지역 클래스(local class)**: 메서드 안에서 정의하는 이름 있는 클래스(잘 안 쓰임).
- **익명 클래스(anonymous class)**: 이름 없이, 인터페이스/추상클래스를 **그 자리에서 즉석
  구현하면서 동시에 인스턴스화**한다. "한 번만 쓸 구현"을 따로 파일로 만들지 않고 인라인으로 작성할 때 쓴다.

  ```java
  Greeting g = new Greeting() {          // 이름 없는 클래스를 즉석 정의 + 생성
      @Override public String greet(String n) { return "안녕, " + n; }
  };
  ```

### 익명 클래스 = 람다의 전신
추상 메서드가 **딱 1개**인 인터페이스(함수형 인터페이스)를 익명 클래스로 구현하는 코드는
대부분 보일러플레이트(`new ... () { @Override ... }`)다. 자바 8은 이를 **람다**로 축약했다.

```java
Greeting g = name -> "안녕, " + name;   // 위 익명 클래스와 동일한 일을 더 짧게
```

즉 람다는 "함수형 인터페이스에 한정된 익명 클래스의 축약형"이다. 그래서 익명 클래스를
**람다의 전신**이라 부른다. (람다·함수형 인터페이스의 본격 학습은 PART 5)

---

## 2. 실습으로 확인하기

> - **가설 1**: static nested 클래스는 바깥 인스턴스 없이 생성되고, static 멤버만 접근한다.
> - **가설 2**: inner 클래스는 바깥 인스턴스를 통해서만 생성되며, 그 인스턴스의 필드에 접근한다.
> - **가설 3**: 익명 클래스로 인터페이스를 즉석 구현할 수 있고, 함수형 인터페이스면 람다로 축약된다(결과 동일).

### 모델 코드 (`com.study.part01_oop.s07_nested_classes`)
- `University`(바깥) — static nested `Building` + inner `Student`를 모두 포함.
- `Greeting` — 추상 메서드 1개짜리 인터페이스(익명 클래스/람다 비교용).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_StaticNestedClass` | 바깥 인스턴스 없이 생성되나? | `new University.Building(...)` |
| `Example2_InnerClass` | 바깥 인스턴스에 어떻게 묶이나? | 서울대/연세대로 각각 `univ.new Student(...)` |
| `Example3_AnonymousClass` | 익명 클래스와 람다 관계? | 같은 Greeting을 익명클래스/람다로 |

### 실행
```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part01_oop.s07_nested_classes.Example1_StaticNestedClass
java -cp build/classes/java/main com.study.part01_oop.s07_nested_classes.Example2_InnerClass
java -cp build/classes/java/main com.study.part01_oop.s07_nested_classes.Example3_AnonymousClass
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (static nested)** — 가설 1.

| 확인 | 결과 |
|---|---|
| `new University.Building("공학관")` (University 인스턴스 없이) | ✅ 생성 성공 |
| `info()` 접근 멤버 | `country`(static)는 접근 / `name`(인스턴스)은 접근 불가(주석 처리) |

→ static nested는 바깥 인스턴스와 독립. 이름공간만 안에 있을 뿐.

**예시 2 (inner)** — 가설 2.

| 학생 | 생성 방식 | `info()` 소속 | 의미 |
|---|---|---|---|
| 홍길동 | `snu.new Student(...)` | 서울대 | 만든 인스턴스의 name을 봄 |
| 김연세 | `yonsei.new Student(...)` | 연세대 | 다른 인스턴스의 name을 봄 |

→ 같은 Student 클래스인데 만든 University에 따라 보이는 name이 다르다 = 바깥 인스턴스에 묶여 있다는 증거.

**예시 3 (익명 클래스 = 람다 전신)** — 가설 3.

| 방식 | `greet("철수")` 결과 |
|---|---|
| 익명 클래스 `new Greeting(){...}` | 안녕하세요, 철수님 (익명 클래스) |
| 람다 `name -> ...` | 안녕하세요, 철수님 (람다) |

→ 둘이 같은 일을 한다. 추상 메서드 1개 인터페이스라 람다로 축약 가능. 익명 클래스가 람다의 전신.

### 세 예시를 관통하는 결론
중첩 클래스를 가르는 핵심 질문은 **"바깥 인스턴스에 묶이는가"** 다. static nested(예시1)는 독립이고,
inner(예시2)는 특정 바깥 인스턴스에 묶여 그 상태에 접근한다. 익명 클래스(예시3)는 "한 번 쓸 구현을
즉석에서" 만드는 또 다른 형태이며, 함수형 인터페이스에 한해 람다로 진화한다 — 이 줄기가 PART 5로 이어진다.

---

## 3. 자기 점검

- **Q. 왜 inner 클래스는 메모리 누수의 원인이 될 수 있나?**
  - inner 클래스 인스턴스는 바깥 인스턴스를 숨은 참조로 들고 있다. 그래서 inner 객체가 살아있는
    한 바깥 객체도 GC되지 못한다. 바깥 상태가 필요 없다면 static nested로 두는 게 안전하다는
    점을 정리해본다. (GC는 PART 3에서 심화)

- **Q. (추가 실험) 익명 클래스 안에서 캡처한 지역변수를 수정해보자.**
  - `Example3`의 `suffix`를 익명 클래스 안에서 `suffix = "??"`로 바꿔보면 컴파일 에러
    (`variable used in lambda/anonymous class should be final or effectively final`)가 난다.
    왜 캡처 변수는 "사실상 final"이어야 하는지 정리해본다. (PART 5 람다 캡처와 직결)
