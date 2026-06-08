# PART 4 — 문자열과 컬렉션: 4.2 StringBuilder vs StringBuffer

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.2 StringBuilder vs StringBuffer**를 다룬다.
> 4.1에서 본 "String 불변성 → += 반복 시 객체 폭증" 문제를 해결하는 도구다.

---

## 1. 학습 내용 — 가변 문자열의 등장

### 로우레벨의 불편함 — String += 반복
String은 불변(4.1)이라, `s += "x"`를 할 때마다 "기존 내용 + 새 내용"을 담은 **새 String을 만들고
이전 내용을 통째로 복사**한다. 루프에서 N번 이어붙이면:
- 새 객체가 N개 생기고(객체 폭증),
- 길어질수록 복사량도 커져 전체 비용이 대략 **O(n²)** 로 폭증한다.

### 해결 — 가변 문자열 (StringBuilder / StringBuffer)
`StringBuilder`는 내부에 **char 배열**을 두고, `append()` 할 때 그 배열을 **제자리에서 늘려가며**
글자를 덧붙인다. 새 String 객체를 만들지 않으므로 전체 비용이 대략 **O(n)** 이다. 그리고 `append()`는
**자기 자신(this)을 반환**하므로 새 객체가 생기지 않고, `.append().append()...` 체이닝도 된다.

즉 String은 "수정 = 새 객체"(불변)인데, StringBuilder는 "수정 = 같은 객체를 제자리 변경"(가변)이다.

### StringBuilder vs StringBuffer
둘은 기능(가변 문자열)이 거의 같다. 차이는 **스레드 안전성과 속도**다.

| | StringBuilder | StringBuffer |
|---|---|---|
| 스레드 안전 | ❌ (동기화 없음) | ✅ (모든 메서드 synchronized) |
| 속도 | 빠름 | 느림(동기화 비용) |
| 언제 | 단일 스레드(대부분) | 여러 스레드가 하나의 가변 문자열을 공유할 때 |

- **StringBuilder**: 동기화가 없어 빠르다. 단 여러 스레드가 동시에 쓰면 갱신이 충돌해 깨진다.
- **StringBuffer**: 모든 메서드가 synchronized라 스레드 안전하지만 동기화 비용으로 느리다.

**실무 기본은 StringBuilder.** 보통 가변 문자열을 여러 스레드가 공유하는 일 자체를 피하기 때문이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: String += 반복은 StringBuilder보다 훨씬 느리다(객체 폭증·복사).
> - **가설 2**: StringBuilder의 append는 새 객체를 안 만들고 같은 객체를 수정한다(== 로 확인).
> - **가설 3**: StringBuilder는 빠르지만 멀티 스레드에서 깨지고, StringBuffer는 느리지만 안전하다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ConcatProblem` | += 가 왜 느린가? | String += vs StringBuilder 시간 측정 |
| `Example2_MutableBuilder` | append는 새 객체? | 반환값 == sb 확인 + 체이닝 |
| `Example3_BuilderVsBuffer` | Builder vs Buffer? | 단일 성능 + 멀티 안전성 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s02_stringbuilder.Example1_ConcatProblem
java -cp build/classes/java/main com.study.part04_collections.s02_stringbuilder.Example2_MutableBuilder
java -cp build/classes/java/main com.study.part04_collections.s02_stringbuilder.Example3_BuilderVsBuffer
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (concat 문제)** — 가설 1. (n=50,000, 실측)

| 방식 | 시간 | 결과 길이 |
|---|---|---|
| String += | 287 ms | 50000 |
| StringBuilder | 3 ms | 50000 |

→ 결과는 같지만 String +=가 약 **95배 느리다**. 불변이라 매번 새 객체+복사가 일어나기 때문. ✅
(n을 키울수록 차이가 더 벌어진다)

**예시 2 (가변성)** — 가설 2.

| 확인 | 결과 |
|---|---|
| `sb.append("b")` 반환값 == sb | true (같은 객체) |
| 체이닝 `sb.append("c").append("d").append("e")` | "abcde" (동작) |

→ append는 새 객체를 안 만들고 같은 객체(this)를 제자리 수정한다. String과 정반대. ✅

**예시 3 (Builder vs Buffer)** — 가설 3.

단일 스레드(5,000,000회 append, 실측):
| | 시간 |
|---|---|
| StringBuilder | 38 ms (빠름) |
| StringBuffer | 97 ms (synchronized라 느림) |

멀티 스레드(4스레드 × 100,000회, 기대 길이 400000):
| | 최종 길이 |
|---|---|
| StringBuilder | **115472** (깨짐 — 동기화 없음) |
| StringBuffer | **400000** (정확 — synchronized) |

→ 단일 스레드면 StringBuilder가 빠르고, 멀티 스레드 공유에선 StringBuffer만 안전하다. ✅
(멀티 스레드 깨짐 값은 실행마다 다름 = 경쟁 상태 — PART 7과 연결)

### 세 예시를 관통하는 결론
String의 불변성은 안전하지만, 반복 조립에서는 객체 폭증이라는 비용이 된다(예시1). 그래서 가변
문자열인 StringBuilder가 등장했고, append가 같은 객체를 제자리 수정하기에 효율적이다(예시2).
StringBuffer는 거기에 synchronized를 더해 스레드 안전을 얻는 대신 속도를 내준다(예시3). 정리하면:
**불변이 필요하면 String, 단일 스레드 조립이면 StringBuilder(기본), 여러 스레드 공유면 StringBuffer.**

---

## 3. 자기 점검

- **Q. 루프에서 String += 가 느린 이유는?**
  - 내 답: String은 불변이라 += 마다 새 객체를 만들고 이전 내용을 복사한다. N번이면 객체 N개 +
    복사 비용 누적(O(n²) 경향). StringBuilder는 내부 배열을 제자리 확장해 O(n). (Example1의 95배 차이)

- **Q. StringBuilder와 StringBuffer를 언제 각각 쓰나?**
  - 내 답: 단일 스레드(대부분) → StringBuilder(빠름), 여러 스레드가 하나의 가변 문자열을 공유 →
    StringBuffer(synchronized로 안전). (Example3의 멀티 스레드 길이 비교)

- **Q. (추가 실험) StringBuilder에 초기 용량을 지정하면(`new StringBuilder(n)`)?**
  - append 도중 내부 배열이 부족하면 더 큰 배열로 복사·확장된다(ArrayList와 유사 — 4.4에서 다룸).
    초기 용량을 충분히 주면 이 확장이 줄어 더 빨라지는지, n=5,000,000으로 `new StringBuilder()` vs
    `new StringBuilder(n)` 시간을 비교해본다.
