# PART 4 — 문자열과 컬렉션: 4.1 String과 Constant Pool

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.1 String과 Constant Pool**을 다룬다.
> PART 2.7에서 본 "상수 풀"이 문자열에서 어떻게 동작하는지, 그리고 String 불변성을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **String Pool(문자열 풀)**: 같은 내용의 문자열 리터럴을 한 번만 만들어 재사용하는 JVM의 저장 공간(Heap 안).
- **리터럴(literal)**: 코드에 직접 적은 값. `"abc"`처럼 따옴표로 쓴 문자열이 문자열 리터럴.
- **불변(immutable)**: 한 번 만들어지면 내용을 바꿀 수 없는 것. String은 불변이라 "수정"하면 새 객체가 생긴다.
- **`==` vs `equals()`**: `==`는 '같은 객체(주소)인가', `equals()`는 '내용이 같은가'를 비교. 문자열 비교는 `equals()`를 써야 한다.
- **`intern()`**: 문자열을 String Pool에 등록(또는 풀의 동일 문자열 참조를 반환)하는 메서드.
- **`new String("abc")`**: 풀과 별개로 Heap에 새 객체를 강제로 만든다(그래서 `==`가 false가 됨).

한 줄 그림: **`"abc"` 리터럴은 풀에서 공유돼 `==`가 true지만, `new String("abc")`는 새 객체라 false다. 그래서 내용 비교는 항상 `equals()`. String은 불변이다.**

---

## 1. 학습 내용 — String Pool과 불변성

### 문자열 리터럴과 String Constant Pool
문자열 리터럴(`"hello"`)은 **String Constant Pool**이라는 특별한 영역에 저장된다. 같은 값의
리터럴을 또 쓰면 새로 만들지 않고 **풀에 있는 것을 재사용**한다. 그래서 `"abc" == "abc"`는 같은
객체라 `true`다.

반면 `new String("abc")`는 풀을 무시하고 **Heap에 무조건 새 객체**를 만든다. 값은 같지만 다른
객체이므로 `"abc" == new String("abc")`는 `false`다. `intern()`을 호출하면 "이 문자열을 풀에
등록(이미 있으면 풀의 것 반환)"하므로 풀 객체와 `==`가 된다.

### 보충 0 — `a="abc"; b="abc";`일 때 왜 `a == b`가 true인가? (컴파일·런타임 원리)
헷갈리는 핵심 질문: 둘 다 내가 따로 할당했는데 왜 같은 객체일까? "a의 주소를 b에 복사해주는"
것일까? **아니다.** 정확한 원리는 "두 리터럴이 결국 **같은 하나의 풀 객체**로 해석된다"는 것이다.
단계로 보면:

**1) 컴파일 시점 — 같은 리터럴은 상수 풀의 '한 항목'으로 합쳐진다.**
`javac`가 컴파일하면 `"abc"`라는 문자열 상수는 클래스 파일의 **상수 풀(Constant Pool)** 에 단 한 번만
등록된다(2.7 참고). 소스에 `"abc"`를 두 번 썼어도, 둘 다 **같은 상수 풀 항목(#번호)** 을 가리키도록
컴파일된다. 실제 바이트코드를 보면:

```
0: ldc #7   // String abc   <- a = "abc"  (상수 풀 7번을 로드)
2: astore_1
3: ldc #7   // String abc   <- b = "abc"  (역시 상수 풀 7번을 로드, 같은 #7!)
5: astore_2
```
`a`를 위한 `ldc`도 `#7`, `b`를 위한 `ldc`도 `#7` — **둘이 똑같은 상수 풀 항목**을 참조한다.
(`ldc` = "상수 풀의 값을 스택에 로드"하는 명령)

**2) 런타임 시점 — 그 상수 풀 항목이 풀의 String 객체 '하나'로 해석된다.**
프로그램이 실행되며 `ldc #7`을 처음 만나면, JVM이 상수 풀 7번을 **String Constant Pool에 있는 실제
String 객체로 해석(resolve)** 한다. 이때 "abc"가 풀에 없으면 만들어 넣고, 그 풀 객체의 참조를 준다.
두 번째 `ldc #7`은 **이미 해석된 같은 풀 객체**를 그대로 준다. 그래서 `a`와 `b`에는 **동일한 풀
객체의 참조**가 들어간다 → `a == b`가 true.

**즉 "a의 주소를 b에 복사"한 게 아니라, a와 b가 처음부터 같은 풀 객체를 가리키도록 만들어진
것이다.** 같은 내용의 리터럴은 메모리를 아끼려고 풀에서 단 하나만 유지하기 때문이다(이것이
문자열 인터닝(interning)). 반면 `new String("abc")`는 이 풀 메커니즘을 무시하고 Heap에 별도 객체를
강제로 만들기 때문에(`ldc`가 아니라 `new`) 풀 객체와 다른 주소가 되어 `==`가 false인 것이다.

> 정리: 리터럴 문자열은 "자동으로 풀에 등록되고 재사용된다". `a`, `b`가 같은 객체인 이유는
> 대입의 순서(누가 먼저냐)와 무관하게, **둘 다 같은 상수 풀 항목 → 같은 풀 객체**로 귀결되기 때문이다.

### == vs equals()
- **`==`** : 두 변수가 **같은 객체(주소)** 를 가리키는지 비교.
- **`equals()`** : 두 문자열의 **값(내용)** 이 같은지 비교.

문자열 비교는 거의 항상 `equals()`를 써야 한다. `==`로 비교하면 "리터럴이냐 new냐"에 따라 결과가
달라져 버그가 된다.

### 보충 1 — `equals()`는 어떻게 "값이 같은지" 확인하나? (String Pool과 무관!)
헷갈리기 쉬운 점: `a.equals(c)`가 `true`인 것은 **String Pool과 아무 상관이 없다.** `equals()`는
풀을 조회하지 않는다. String 클래스가 `equals()`를 오버라이드해서, **두 문자열의 글자를 하나하나
직접 비교**할 뿐이다. 동작을 단순화하면 이렇다:

```java
// String.equals()의 핵심 로직 (개념 단순화)
public boolean equals(Object other) {
    if (this == other) return true;            // 1) 같은 객체면 당연히 true (빠른 경로)
    if (!(other instanceof String s)) return false; // 2) String이 아니면 false
    if (this.length() != s.length()) return false;  // 3) 길이 다르면 false
    for (int i = 0; i < length(); i++) {       // 4) 글자를 하나씩 비교
        if (this.charAt(i) != s.charAt(i)) return false;
    }
    return true;                               // 5) 전부 같으면 true
}
```

즉 `a("abc")`와 `c(new String("abc"))`는 **서로 다른 객체**(`a == c`는 false)지만, `equals()`가
글자를 차례로 비교(`a`==`a`, `b`==`b`, `c`==`c`)해서 모두 같으니 `true`를 돌려준다. 풀에 들어
있는지 여부는 전혀 보지 않는다. **"== 는 주소 비교(또는 풀 재사용 여부), equals는 글자 비교"** 라고
기억하면 된다.

> 참고: 모든 클래스의 기본 `equals()`(Object의 것)는 사실 `==`와 똑같이 주소만 비교한다. String은
> 이를 오버라이드해 "글자 비교"로 바꾼 것이다. 그래서 우리가 만든 클래스도 값 비교가 필요하면
> equals()를 직접 오버라이드해야 한다(이건 4.8 hashCode/equals에서 다룬다).

### 보충 2 — `intern()`은 풀을 조회해서 "같은 내용의 풀 객체"를 돌려준다
`a == d` (단, `d = c.intern()`)가 `true`인 과정을 차근차근 보자. `c`는 `new String("abc")`로 만든
**Heap의 새 객체**다(`a == c`는 false). 여기에 `c.intern()`을 호출하면:

1. intern()이 **String Pool을 뒤져** "abc"와 **글자가 같은** 문자열이 풀에 이미 있는지 찾는다.
2. 이미 있으면(있다! 리터럴 `a = "abc"`를 쓸 때 "abc"가 풀에 등록됐으므로) → **그 풀에 있는 객체의
   참조를 반환**한다.
3. 만약 풀에 없었다면 → c의 내용을 풀에 새로 등록하고 그 참조를 반환했을 것이다.

여기서는 2번 경로다. 풀에 있던 "abc"는 `a`가 가리키는 바로 그 객체이므로, `d`도 **`a`와 똑같은 풀
객체**를 가리키게 된다. 그래서 `a == d`가 `true`다. 정리하면:

```
a = "abc"            -> 풀의 "abc" 객체(주소 예: 0x100)
c = new String("abc")-> Heap의 새 객체(주소 예: 0x500), 내용만 같음
d = c.intern()       -> 풀에서 "abc" 찾음 -> 풀의 0x100 반환
결과: a(0x100) == d(0x100)  -> true,  a(0x100) == c(0x500) -> false
```

**핵심**: intern()은 "c가 가진 글자(내용)"를 기준으로 풀을 조회해 **같은 내용의 풀 객체를 돌려주는**
것이지, c 객체 자체를 풀에 넣어 a로 바꾸는 게 아니다. c는 여전히 Heap에 따로 존재하고, d만 풀의
객체를 가리킨다. (intern은 풀 메모리를 늘릴 수 있어 남용은 금물 — 보통 직접 쓸 일은 드물고
"리터럴은 자동으로 intern된 상태"라는 점만 알면 된다.)

### String은 불변(immutable)
String은 한 번 만들어지면 **내부 내용을 절대 바꿀 수 없다.** `toUpperCase()`, `concat()`,
`replace()` 같은 "수정처럼 보이는" 메서드는 사실 원본을 그대로 두고 **새 String을 만들어 반환**한다.

불변성의 이점:
- **스레드 안전 / 안전한 공유**: 아무도 내용을 못 바꾸니 여러 변수·스레드가 공유해도 간섭이 없다.
- **보안**: 한 번 검증한 문자열(파일 경로·URL 등)이 나중에 몰래 바뀔 수 없다.
- **캐싱**: 값이 안 변하니 `hashCode`를 한 번 계산해 캐싱해두고 재사용한다.
- **HashMap 키로 안전**: key의 내용이 안 바뀌어 hashCode가 고정 → 넣은 값을 항상 다시 찾을 수 있다.

단점은, 루프에서 `+=`로 문자열을 이어붙이면 매번 새 객체가 생겨 비효율적이라는 것 — 이를 푸는
StringBuilder는 4.2에서 다룬다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 리터럴은 풀에서 재사용(==true), new String은 새 객체(==false), equals는 값 비교(true).
> - **가설 2**: String 수정 메서드는 원본을 안 바꾸고 새 객체를 반환한다.
> - **가설 3**: 불변성 덕분에 안전한 공유·HashMap 키 안정성·hashCode 캐싱이 가능하다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_StringPoolAndIdentity` | ==와 equals 차이? 풀 재사용? | 리터럴 / new / intern 비교 |
| `Example2_Immutability` | 수정 메서드는 원본을 바꾸나? | toUpperCase / concat |
| `Example3_ImmutabilityBenefits` | 불변이면 뭐가 좋나? | 공유 / Map 키 / hashCode |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s01_string_pool.Example1_StringPoolAndIdentity
java -cp build/classes/java/main com.study.part04_collections.s01_string_pool.Example2_Immutability
java -cp build/classes/java/main com.study.part04_collections.s01_string_pool.Example3_ImmutabilityBenefits
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (String Pool / 동일성)** — 가설 1.

| 비교 | 결과 | 의미 |
|---|---|---|
| `a == b` (둘 다 리터럴 "abc") | true | 풀에서 재사용 → 같은 객체 |
| `a == c` (c = new String) | false | c는 Heap의 새 객체 |
| `a.equals(c)` | true | 값은 같음 |
| `a == d` (d = c.intern()) | true | intern이 풀 객체 반환 |

→ **자기 점검 답**: `a="abc"; b=new String("abc")`일 때 `a==b`는 false(다른 객체), `a.equals(b)`는
true(값 동일). 문자열 비교는 equals()로! ✅

**예시 2 (불변성)** — 가설 2.

| 동작 | 반환값 | 원본 s |
|---|---|---|
| `s.toUpperCase()` | "HELLO" (새 객체, `s==upper` false) | "hello" (그대로) |
| `s.concat(" world")` | "hello world" | "hello" (그대로) |

→ 수정 메서드는 원본을 안 바꾸고 새 객체를 반환한다. String은 불변. ✅

**예시 3 (불변성 이점)** — 가설 3.

| 이점 | 확인 결과 |
|---|---|
| 안전한 공유 | 메서드에 넘겨 "수정"해도 원본 `shared`는 훼손 안 됨 |
| HashMap 키 안정성 | `put("user:1",100)` → `get("user:1")` = 100 (키 내용 불변이라 항상 찾힘) |
| hashCode 캐싱 | 같은 문자열 hashCode 두 번 호출 → 동일값 (`-1439763676`) |

→ 불변성이 공유 안전성·자료구조 신뢰성·성능 이점을 만든다. ✅

### 세 예시를 관통하는 결론
String은 리터럴을 풀에서 재사용해 메모리를 아끼고(예시1), 한 번 만들면 내용을 바꿀 수 없는
불변 객체다(예시2). 이 불변성이 안전한 공유·HashMap 키 안정성·hashCode 캐싱이라는 광범위한
이점을 만든다(예시3). 단 불변이라 "수정"이 매번 새 객체를 만드므로, 반복적인 문자열 조립에는
StringBuilder가 필요하다(4.2). 그리고 문자열 비교는 객체 동일성(==)이 아니라 값(equals())으로 해야 한다.

---

## 3. 자기 점검

- **Q. `String a="abc"; String b=new String("abc");`일 때 `a.equals(b)`와 `a==b`의 결과는?**
  - 내 답: `a==b`는 false(b는 new로 만든 Heap의 다른 객체), `a.equals(b)`는 true(값이 같음).
    (Example1의 a==c, a.equals(c) 결과가 근거)

- **Q. String이 불변이라서 좋은 점 3가지는?**
  - 내 답: ① 안전한 공유(스레드 안전·보안) ② HashMap 키 안정성(hashCode 고정) ③ hashCode 캐싱.
    (Example3)

- **Q. (추가 실험) `"a" + "b" + "c"` (리터럴만 연결)는 컴파일 시 어떻게 될까?**
  - 컴파일러가 상수 폴딩으로 `"abc"` 하나로 합쳐 풀에 넣는다. `("a"+"b"+"c") == "abc"`가 true인지
    직접 확인하고, 변수로 연결할 때(`x + "b"`)와 어떻게 다른지 javap로 살펴본다. (2.7 상수 풀과 연결)
