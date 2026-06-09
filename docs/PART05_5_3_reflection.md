# PART 5 — 제네릭 · 비교 · 함수형: 5.3 Reflection

> 이 문서는 커리큘럼 PART 5의 소단원 중 **5.3 Reflection**을 다룬다.
> PART 2.6에서 "new 없이 객체 생성"으로 맛본 Reflection을 본격적으로 본다. Spring/JPA/Jackson의 기반.

---

## 0. 왜 배우는가 — "직접 쓸 일은 거의 없는데 왜?"

솔직히 **애플리케이션 코드에서 Reflection을 직접 쓸 일은 거의 없다.** 그런데도 배우는 이유는,
**우리가 매일 쓰는 프레임워크(Spring, JPA, Jackson, JUnit 등)가 전부 Reflection으로 동작하기
때문**이다. Reflection을 모르면 그 프레임워크들이 "어떻게 마법처럼 동작하는지"가 영원히 블랙박스로
남는다. 반대로 Reflection을 이해하면 다음이 한 번에 풀린다.

- **"`@Autowired`만 붙였는데 어떻게 객체가 자동으로 주입되지?"** → Spring이 Reflection으로 필드/생성자를
  찾아 객체를 만들어 넣기 때문.
- **"`@Entity` 클래스에 getter/setter도 없는데 어떻게 DB 값이 필드에 채워지지?"** → JPA가 Reflection으로
  private 필드에 직접 값을 넣기 때문(`setAccessible`).
- **"JSON 문자열이 어떻게 내 객체로 변하지?"** → Jackson이 Reflection으로 클래스의 필드를 보고 매핑하기 때문.
- **"JUnit은 어떻게 `@Test` 붙은 메서드만 골라 실행하지?"** → Reflection으로 어노테이션을 읽어
  해당 메서드를 invoke하기 때문.

### Reflection이 푸는 근본 문제 — "컴파일 시점에 모르는 것을 런타임에 다루기"
일반 코드는 **컴파일 시점에 모든 타입·메서드를 알아야** 한다(`new Member()`, `member.getName()`처럼
이름을 코드에 박아야 함). 그런데 프레임워크는 정반대 상황에 있다 — **"사용자가 앞으로 어떤 클래스를
만들지 컴파일 시점엔 전혀 모른다."** Spring을 만들 때 우리가 짤 `UserService`는 존재하지도 않았다.

그럼에도 프레임워크가 우리의 클래스를 객체로 만들고 메서드를 호출하려면, **"이름(문자열)이나 타입
정보만으로 런타임에 클래스를 다루는" 능력**이 필요하다. 그게 Reflection이다. 즉 Reflection은
**"미리 모르는 코드를 나중에 다루는" 문제를 푸는 도구**이고, 그래서 "범용 프레임워크"를 만들 수 있게 해준다.

### 원리 한 줄 — 클래스 정보도 '객체'다
자바는 클래스를 로딩할 때 그 클래스의 설계 정보(필드·메서드·생성자·어노테이션)를 `Class`라는 **객체**로
만들어 Method Area에 둔다(2.2). Reflection은 이 `Class` 객체에게 "너 어떤 필드 있어? 어떤 메서드 있어?
이 생성자로 객체 하나 만들어줘"라고 **런타임에 물어보고 시키는** 것이다. 코드가 코드(자기 자신의 구조)를
들여다본다고 해서 'reflection(반사/성찰)'이라 부른다.

### 그래서 무엇을 알아야 하나 (학습 목표)
1. `Class` 객체를 얻는 법(이름 문자열로도 얻을 수 있다는 게 핵심).
2. 그것으로 생성자/메서드/필드/어노테이션을 조회하고, 객체 생성·메서드 호출·필드 접근을 하는 법.
3. private 우회(`setAccessible`)와 어노테이션 읽기 — 프레임워크 마법의 정체.
4. 단점(성능·타입 안전성·캡슐화)을 알고, **직접 쓰기보다 "프레임워크가 이렇게 동작하는구나"를 이해**하는 용도.

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

### 자주 쓰는 Reflection API (★ 레퍼런스)

**(1) Class 객체 얻기**

| 방법 | 언제 |
|---|---|
| `Member.class` | 컴파일 시점에 타입을 알 때 |
| `obj.getClass()` | 인스턴스로부터 |
| `Class.forName("패키지.클래스")` | **문자열 이름으로**(타입을 몰라도 됨) — 프레임워크가 주로 사용 |

**(2) 멤버 조회** — `getXxx`는 public + 상속받은 것, `getDeclaredXxx`는 (private 포함) 그 클래스가
직접 선언한 것만. 프레임워크는 보통 private까지 봐야 해서 `getDeclared...`를 많이 쓴다.

| 메서드 | 반환 |
|---|---|
| `getFields()` / `getDeclaredFields()` | 필드 목록(`Field[]`) |
| `getMethods()` / `getDeclaredMethods()` | 메서드 목록(`Method[]`) |
| `getDeclaredConstructor(파라미터타입...)` | 특정 생성자(`Constructor`) |
| `getDeclaredField("이름")` / `getDeclaredMethod("이름", 파라미터타입...)` | 특정 필드/메서드 |
| `getAnnotation(Xxx.class)` / `isAnnotationPresent(Xxx.class)` | 어노테이션 읽기/존재 확인 |

**(3) 실제 조작**

| 메서드 | 용도 |
|---|---|
| `constructor.newInstance(인자...)` | 객체 생성(new 없이) |
| `method.invoke(객체, 인자...)` | 메서드 호출(객체가 static이면 첫 인자 null) |
| `field.get(객체)` / `field.set(객체, 값)` | 필드 읽기/쓰기 |
| `accessibleObject.setAccessible(true)` | private 접근 제어 우회(Field/Method/Constructor 공통) |

```java
// 전형적 흐름: 이름 -> Class -> 생성자로 객체 -> 메서드 호출 -> private 필드 접근
Class<?> clazz = Class.forName("org.example.Member");
Object obj = clazz.getDeclaredConstructor(String.class).newInstance("홍길동");

Method greet = clazz.getMethod("greet");
greet.invoke(obj);                       // obj.greet()

Field name = clazz.getDeclaredField("name");
name.setAccessible(true);                // private 우회
System.out.println(name.get(obj));       // private 필드 읽기

if (clazz.isAnnotationPresent(Entity.class)) { ... }  // 어노테이션 기반 분기(프레임워크 패턴)
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
