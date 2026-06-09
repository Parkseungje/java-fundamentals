# PART 5 — 제네릭 · 비교 · 함수형: 5.3 Reflection

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.3 Reflection**을 다룬다.
> PART 2.6에서 "new 없이 객체 생성"으로 맛본 Reflection을 본격적으로 본다. Spring/JPA/Jackson의 기반.

---

## 1. 학습 내용 — 런타임에 클래스를 들여다보고 조작하기

### Class 객체 — 모든 것의 출발점
자바의 모든 클래스 정보는 Method Area에 **Class 객체로 단 하나** 로딩된다(2.2). 이 Class 객체를
통해 런타임에 필드/메서드/생성자/어노테이션을 들여다보고 조작하는 것이 **Reflection**이다.
Class 객체를 얻는 3가지 방법(모두 같은 객체):
- `Person.class` — 컴파일 시점에 타입을 알 때
- `person.getClass()` — 인스턴스로부터
- `Class.forName("...Person")` — **문자열 이름으로**(컴파일 시점에 타입을 몰라도 됨)

### Reflection으로 할 수 있는 것
- **런타임 인스턴스 생성**: `getDeclaredConstructor(...).newInstance(...)` (new 키워드 없이, 2.6).
- **메서드 호출**: `getMethod("name", 파라미터타입).invoke(obj, 인자)` (메서드 이름 문자열로).
- **private 접근**: `setAccessible(true)`로 private 필드/메서드 접근(캡슐화 우회).
- **어노테이션 읽기**: `getAnnotation(...)` (단 `@Retention(RUNTIME)` 이어야 런타임에 읽힘).

```java
Class<?> clazz = Class.forName("org.example.Member");
Object obj = clazz.getDeclaredConstructor().newInstance();
Method m = clazz.getMethod("hap", int.class, int.class);
m.invoke(obj, 1, 2);
```

### 실무 사용처 — 프레임워크의 마법
- **Spring (DI/AOP)**: 설정·어노테이션을 읽어 객체를 만들고(newInstance) 의존성을 주입.
- **JPA (Entity 매핑)**: private 필드에 직접 값을 넣고 빼서 DB 행 ↔ 객체 매핑(getter/setter 없어도).
- **Jackson (JSON)**: 어노테이션·필드를 읽어 객체 ↔ JSON 변환.

### 단점 (남용 금지)
- **캡슐화 위반**: private 접근을 우회한다.
- **컴파일 타임 안전성 상실**: 메서드/필드를 문자열로 다루므로 오타가 런타임 오류가 된다.
- **성능**: 일반 호출보다 느리다.

→ 강력하지만 단점이 분명하므로, **일반 애플리케이션 코드에서는 직접 쓰지 말고** 프레임워크에 맡긴다.

---

## 2. 실습으로 확인하기

> - **가설 1**: Class 객체를 3가지 방법으로 얻어도 모두 같은 객체이고, 메타데이터를 조회할 수 있다.
> - **가설 2**: 문자열 이름만으로(직접 참조 없이) 객체 생성·메서드 호출이 가능하다.
> - **가설 3**: Reflection은 private 접근을 우회하고 어노테이션을 읽는다.

### 모델 코드 (`com.study.part05_generics_functional.s03_reflection`)
- `Info`(@Retention(RUNTIME) 어노테이션) / `Person`(private 필드·메서드, @Info 부착).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ClassMetadata` | Class 얻기·조회? | 3가지 방법 + 멤버 목록 |
| `Example2_RuntimeCreateAndInvoke` | 이름만으로 생성·호출? | forName + newInstance + invoke |
| `Example3_PrivateAccessAndAnnotation` | private·어노테이션? | setAccessible + getAnnotation |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part05_generics_functional.s03_reflection.Example1_ClassMetadata
java -cp build/classes/java/main com.study.part05_generics_functional.s03_reflection.Example2_RuntimeCreateAndInvoke
java -cp build/classes/java/main com.study.part05_generics_functional.s03_reflection.Example3_PrivateAccessAndAnnotation
```
> 참고: 여기서는 **내 프로젝트의 사용자 클래스(Person)** 에 setAccessible을 하므로 `--add-opens`가
> 필요 없다. 4.4/4.8에서 `java.util`(java.base 모듈) 내부에 접근할 때만 `--add-opens`가 필요했다.

### 실행 결과 — 가설과 실제 비교

**예시 1 (Class 메타데이터)** — 가설 1.
- `Person.class == getClass() == Class.forName(...)` 모두 `true`(Method Area에 1개).
- 필드 `[name, age]`, 메서드 `[secret, toString, add]`, 생성자 1개(파라미터 `[String, int]`) 조회. ✅

**예시 2 (런타임 생성·호출)** — 가설 2.
- `newInstance("홍길동", 30)` → `Person{name=홍길동, age=30}` (new 키워드 없이).
- `getMethod("add",...).invoke(obj, 1, 2)` → `3` (메서드 이름 문자열로 호출). ✅
- 코드 어디에도 `new Person`·`.add` 직접 참조가 없다 = 런타임에 결정.

**예시 3 (private·어노테이션)** — 가설 3.
- private `name` 읽기/변경 가능(`setAccessible` 후): `홍길동` → `김변경`.
- private `secret()` 호출 가능.
- `@Info` 값 읽기: `"사람을 표현하는 클래스"`. ✅

### 세 예시를 관통하는 결론
Reflection은 "런타임에 클래스를 들여다보고(예시1) 이름만으로 생성·호출하며(예시2) 캡슐화를 우회하고
어노테이션을 읽는(예시3)" 능력이다. 이 덕분에 프레임워크는 컴파일 시점에 모르는 사용자 클래스를
다루고, private 필드를 매핑하고, 어노테이션으로 동작을 결정한다 — Spring/JPA/Jackson의 마법이 모두
여기서 나온다. 단 캡슐화 위반·타입 안전성 상실·성능 비용이 있으므로 일반 코드에선 직접 남용하지 않는다.

---

## 3. 자기 점검

- **Q. Reflection이 프레임워크(Spring/JPA/Jackson)에서 핵심인 이유는?**
  - 내 답: 컴파일 시점에 모르는 사용자 클래스를 이름만으로 생성·호출하고(DI), private 필드에 직접
    값을 매핑하며(JPA/Jackson), 어노테이션을 읽어 동작을 결정하기 때문. (Example2, Example3)

- **Q. 어노테이션을 Reflection으로 읽으려면 무엇이 필요한가?**
  - 내 답: `@Retention(RetentionPolicy.RUNTIME)`. 기본값(CLASS/SOURCE)이면 런타임에 사라져 못 읽는다.
    (Info 어노테이션이 RUNTIME이라 Example3에서 읽혔다)

- **Q. Reflection의 단점 3가지는?**
  - 내 답: 캡슐화 위반(private 우회), 컴파일 타임 안전성 상실(문자열 오타=런타임 오류), 성능 저하.
    그래서 일반 코드에선 직접 쓰지 않고 프레임워크에 맡긴다.
