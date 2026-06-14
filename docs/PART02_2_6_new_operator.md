# PART 2 — JVM 메모리 모델과 실행 원리: 2.6 new 연산자가 실제로 하는 일

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.6 new 연산자가 실제로 하는 일**을 다룬다.
> new가 내부적으로 무엇을 하는지, 그리고 new 없이 객체를 만드는 방법(Reflection·clone·역직렬화)을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **`new` 연산자**: Heap에 객체를 위한 메모리를 할당하고, 생성자를 호출해 초기화한 뒤 그 참조를 돌려주는 것.
- **인스턴스화(instantiation)**: 클래스로 실제 객체를 만드는 일.
- **생성자(constructor)**: 객체를 초기화하는 특별한 메서드(`new`가 내부적으로 호출).
- **Reflection(리플렉션)**: 런타임에 클래스 정보를 보고 객체 생성·메서드 호출을 하는 기능(`new` 없이 객체 생성 가능 — PART 5.3).
- **clone() / 역직렬화(deserialization)**: 기존 객체 복제 / 바이트에서 객체 복원 — 둘 다 `new` 없이 객체를 만들고 **생성자를 거치지 않는다**(PART 6.6 연결).

한 줄 그림: **`new`는 'Heap 할당 → 생성자 호출 → 참조 반환'을 한다. 그런데 Reflection·clone·역직렬화는 생성자를 안 거치고도 객체를 만든다(그래서 생성자 검증이 적용 안 될 수 있다).**

---

## 1. 학습 내용 — new의 4단계와, new 없이 객체 만들기

### new가 실제로 하는 일 (4단계)
`new Widget(7)` 한 줄은 JVM 내부에서 다음 단계를 거친다.

1. **Class Metadata Zone에서 클래스 정보 찾기** — 아직 로딩 안 된 클래스면 먼저 로딩한다.
   이때 static 초기화(static 필드/블록)가 **딱 한 번** 실행된다(2.2의 Method Area와 연결).
2. **Heap에 객체 메모리 확보** — 객체가 들어갈 공간을 Heap에 할당한다.
3. **멤버 변수 초기화** — 인스턴스 필드와 인스턴스 초기화 블록이 **소스에 적힌 순서대로** 실행된다.
4. **생성자 호출** — 마지막으로 생성자 본문이 실행된다.

그래서 한 번의 new 안에서 초기화 순서는 항상 **"필드/초기화 블록(3단계) → 생성자(4단계)"** 이고,
static 초기화(1단계)는 클래스당 한 번만 일어난다.

### new 없이 객체를 만드는 방법
`new` 키워드 외에도 객체를 만드는 방법이 있다. 그리고 그 방법들은 **생성자를 호출하는지** 여부로 갈린다.

| 방법 | 생성자 호출? | 설명 |
|---|---|---|
| `new` | ✅ | 가장 일반적인 방법 |
| **Reflection** (`newInstance`) | ✅ | 런타임에 Class를 얻어 생성자를 찾아 호출 |
| **clone()** | ❌ | 기존 객체의 메모리를 복사 (생성자 거치지 않음) |
| **역직렬화** | ❌ | 바이트 스트림에서 객체를 복원 (생성자 거치지 않음) |

- **Reflection**: `Class.forName(...).getDeclaredConstructor(...).newInstance(...)`. new 키워드는
  없지만 내부적으로 **생성자를 호출**한다. Spring DI·JPA 엔티티·Jackson JSON 매핑이 모두 이 방식으로
  객체를 만든다(PART 5에서 심화).
- **clone() / 역직렬화**: 생성자를 **건너뛰고** 객체를 만든다. 그래서 "생성자에서만 하던 검증·초기화"가
  이렇게 만든 객체에는 적용되지 않을 수 있다(함정). 직렬화는 PART 6에서 심화한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: new는 "static 초기화(1번) → 필드/블록 초기화 → 생성자" 순서로 동작한다.
> - **가설 2**: Reflection은 new 없이 객체를 만들지만 생성자는 호출한다.
> - **가설 3**: clone과 역직렬화는 생성자를 호출하지 않고 객체를 만든다.

### 모델 코드 (`com.study.part02_jvm.s06_new_operator`)
- `LoadOrder` — static 블록 / 인스턴스 필드·블록 / 생성자에 출력문을 심어 초기화 순서 관찰(예시1).
- `Widget` — 생성자에 출력문. Serializable·Cloneable 구현. "생성자가 호출됐는지"를 출력으로 판별(예시2,3).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_NewSteps` | new의 초기화 순서? | `LoadOrder` 2번 생성 |
| `Example2_ReflectionNew` | Reflection은 생성자 호출? | `newInstance(42)` |
| `Example3_CloneAndDeserialization` | clone/역직렬화는? | clone + 직렬화 라운드트립 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s06_new_operator.Example1_NewSteps
java -cp build/classes/java/main com.study.part02_jvm.s06_new_operator.Example2_ReflectionNew
java -cp build/classes/java/main com.study.part02_jvm.s06_new_operator.Example3_CloneAndDeserialization
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (new의 단계)** — 가설 1.

| new 호출 | 출력 순서 |
|---|---|
| 첫 번째 | `[1] static` → `[3] 필드` → `[3] 블록` → `[4] 생성자` |
| 두 번째 | (`[1] 없음`) → `[3] 필드` → `[3] 블록` → `[4] 생성자` |

→ static 초기화는 클래스당 1번, 인스턴스 초기화+생성자는 new마다. 생성자는 항상 마지막. ✅

**예시 2 (Reflection)** — 가설 2.
- `newInstance(42)` 호출 시 `>> Widget 생성자 실행 (id=42)` 출력됨.
- new 키워드는 없지만 생성자가 호출된다. ✅

**예시 3 (clone / 역직렬화)** — 가설 3.

| 동작 | 생성자 출력 | 결과 객체 |
|---|---|---|
| 원본 `new Widget(7)` | ✅ 1번 찍힘 | Widget(id=7) |
| `clone()` | ❌ 안 찍힘 | Widget(id=7), 원본과 다른 객체 |
| 직렬화 → 역직렬화 | ❌ 안 찍힘 | Widget(id=7), 원본과 다른 객체 |

→ clone/역직렬화는 생성자를 건너뛰고도 정상 객체를 만든다. ✅

### 세 예시를 관통하는 결론
`new`는 "클래스 로딩(static 1번) → Heap 할당 → 멤버 초기화 → 생성자"의 정해진 절차를 밟는다(예시1).
하지만 객체를 만드는 길은 new 하나가 아니다 — Reflection은 new 없이 만들되 생성자를 호출하고(예시2),
clone·역직렬화는 생성자를 아예 건너뛴다(예시3). 이 차이는 실무에서 중요하다: 생성자에 검증 로직을
두었다면, clone·역직렬화로 만든 객체는 그 검증을 통과하지 않은 채 존재할 수 있다.

---

## 3. 자기 점검

- **Q. new 한 번의 내부 초기화 순서는?**
  - 내 답: (필요 시 클래스 로딩+static 초기화 1번) → Heap 할당 → 인스턴스 필드/초기화 블록(소스
    순서) → 생성자. (Example1의 출력 순서가 근거)

- **Q. new 없이 객체를 만드는 방법과, 그중 생성자를 호출하지 않는 것은?**
  - 내 답: Reflection(생성자 O), clone(생성자 X), 역직렬화(생성자 X). (Example2·3)

- **Q. (실무 함정) 생성자에서 필수 값 검증을 하는 클래스를 역직렬화하면?**
  - 생성자를 안 거치므로 검증이 적용되지 않는다. `Widget` 생성자에 `if (id < 0) throw ...` 같은
    검증을 넣고, 음수 id를 가진 객체를 직렬화/역직렬화해보며 검증이 우회되는지 확인한다.
    (직렬화 시 `readObject`로 검증을 다시 거는 방법은 PART 6에서 다룬다)
