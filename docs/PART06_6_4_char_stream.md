# PART 6 — I/O와 직렬화: 6.4 문자 스트림 (Reader/Writer) — 한글 해결

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.4 문자 스트림**을 다룬다.
> 6.3에서 본 한글 깨짐의 해결편. Reader/Writer가 인코딩을 해석하는 원리와 인코딩 명시의 중요성을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **문자 스트림(character stream)**: '문자(char)' 단위로 읽고 쓰는 통로. `Reader`(읽기)·`Writer`(쓰기). 인코딩을 해석해 한글을 제대로 다룬다.
- **인코딩/디코딩**: 문자→바이트(인코딩) / 바이트→문자(디코딩). Reader는 디코딩, Writer는 인코딩을 한다.
- **charset(문자셋)**: 인코딩 규칙의 이름. UTF-8(권장), EUC-KR/MS949(옛 한국어) 등. `StandardCharsets.UTF_8`.
- **FileReader vs InputStreamReader**: FileReader는 인코딩을 못 정함(플랫폼 기본 charset) / InputStreamReader는 **charset을 명시** 가능(권장).
- **BufferedReader.readLine()**: 한 줄(String) 단위로 읽는 메서드(끝이면 null). 내부적으로 read()로 문자를 모으다 줄바꿈에서 끊는다.

한 줄 그림: **문자 스트림(Reader/Writer)은 인코딩을 해석해 한글을 깨지지 않게 다룬다. 단 '쓴 인코딩=읽은 인코딩'이 맞아야 하고, 인코딩은 InputStreamReader로 항상 명시(UTF-8)하는 게 안전하다.**

---

## 1. 학습 내용 — 문자 스트림과 인코딩

### Reader/Writer = 문자(char) 단위 + 인코딩 해석
바이트 스트림(6.3)이 '바이트'만 다룬 것과 달리, **Reader/Writer는 '문자(char)' 단위**로 다룬다.
핵심은 **인코딩(charset)을 알고 여러 바이트를 묶어 하나의 문자로 해석(디코딩)** 한다는 점이다.
그래서 한글(UTF-8 3바이트)도 `read()`가 한 번에 '한 글자'를 돌려준다(바이트 조각이 아니라).
→ 6.3의 한글 깨짐이 해결된다.

### read() vs readLine() — "한 글자씩"이냐 "한 줄씩"이냐
같은 읽기지만 단위가 다르다.

| 메서드 | 소속 | 단위 | 반환 | 끝(EOF) |
|---|---|---|---|---|
| `read()` | Reader | **문자 1개** | int (문자 코드 0~65535) | `-1` |
| `read(char[] buf)` | Reader | 문자 여러 개 | 읽은 문자 수 n | `-1` |
| `readLine()` | BufferedReader | **한 줄(String)** | String (줄바꿈 제외) | `null` |

- **`read()`** 는 `Reader`의 기본 메서드로, **문자 하나**를 int(문자 코드)로 반환한다. EOF면 -1.
  바이트 스트림의 `read()`(0~255 바이트)와 형태는 같지만, 이쪽은 **'바이트'가 아니라 '문자'** 를
  돌려준다는 점이 핵심이다(인코딩 해석 후이므로 한글도 한 글자로 옴).
- **`readLine()`** 은 `BufferedReader`가 제공하는 메서드로, **한 줄 전체를 String**으로 돌려준다.
  더 읽을 줄이 없으면 `null`을 반환한다(read()의 -1과 다름!).

### readLine()의 원리 — "내부에서 read()를 줄바꿈까지 모아준다"
`readLine()`이 마법으로 한 줄을 읽는 게 아니다. **내부적으로 `read()`로 문자를 하나씩 읽다가,
줄바꿈 문자(`\n`, `\r`, `\r\n`)를 만나면 거기까지 모은 문자들을 String으로 만들어 반환**한다.
개념적으로 이런 동작이다:

```java
// readLine()이 내부에서 하는 일(개념 단순화)
String readLine() {
    StringBuilder sb = new StringBuilder();
    int c;
    while ((c = read()) != -1) {     // 문자를 하나씩 읽다가
        if (c == '\n') return sb.toString();  // 줄바꿈을 만나면 거기까지를 한 줄로 반환
        if (c == '\r') { /* \r\n 처리: 다음이 \n이면 같이 소비 */ continue; }
        sb.append((char) c);          // 줄바꿈 전까지 모은다
    }
    return sb.length() > 0 ? sb.toString() : null; // 더 없으면 null
}
```

포인트:
- 반환되는 String에는 **줄바꿈 문자가 포함되지 않는다**(구분자로만 쓰고 버린다).
- 그래서 줄 단위로 읽을 땐 `while ((line = br.readLine()) != null)` 패턴을 쓴다(끝이 **null**).
- `readLine()`은 `BufferedReader`에만 있다(내부 버퍼가 있어야 줄바꿈까지 효율적으로 모을 수 있으므로).
  그래서 보통 `new BufferedReader(new InputStreamReader(in, UTF_8))`로 감싼다.

언제 무엇을: **줄 단위(로그·CSV·텍스트 파일)** 면 `readLine()`이 편하고, **문자 단위 정밀 처리**가
필요하면 `read()`를 쓴다.

### FileReader vs InputStreamReader — 인코딩을 명시할 수 있느냐
Reader가 한글을 제대로 읽으려면 **'파일이 저장된 인코딩'과 'Reader가 해석할 인코딩'이 일치**해야 한다.
- **FileReader(옛 API)**: 인코딩을 못 받고 **플랫폼 기본 charset**을 쓴다(Windows 한국어=MS949 등).
  그래서 UTF-8 파일을 기본이 다른 환경에서 읽으면 깨질 수 있다. (Java 11+에 charset 받는 생성자가
  추가됐지만, 핵심은 "인코딩을 항상 명시하는 습관")
- **InputStreamReader(보조 스트림)**: 생성자에 **charset을 명시**할 수 있다. 파일 인코딩에 맞춰 정확히 해석.
  → 권장. `new InputStreamReader(inputStream, StandardCharsets.UTF_8)`

### Writer도 인코딩이 중요 — round-trip
`OutputStreamWriter`/`FileWriter`는 char를 받아 **지정한 인코딩으로 바이트로 변환(인코딩)** 해 쓴다.
파일에는 결국 바이트가 저장되고, '어떤 인코딩으로 썼는지'가 그 바이트의 의미를 결정한다. 따라서
나중에 읽을 때 **같은 인코딩**으로 디코딩해야 원래 글자가 복원된다(**쓰기 인코딩 == 읽기 인코딩**).
실무에서는 보통 UTF-8로 통일한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: Reader는 char 단위로 인코딩을 해석해 한글이 깨지지 않는다(6.3 해결).
> - **가설 2**: 인코딩이 안 맞으면 Reader여도 깨진다. InputStreamReader는 인코딩을 명시해 안전.
> - **가설 3**: Writer로 쓸 때와 읽을 때 인코딩이 같아야 한글이 보존된다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_CharStreamHangul` | Reader는 왜 한글 OK? | "Hi한글" Reader로 읽기 |
| `Example2_FileReaderVsInputStreamReader` | 인코딩 명시 차이? | EUC-KR 파일을 UTF-8/EUC-KR로 |
| `Example3_WriterEncodingRoundtrip` | 쓰기/읽기 인코딩? | UTF-8로 쓰고 UTF-8/EUC-KR로 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s04_char_stream.Example1_CharStreamHangul
java -cp build/classes/java/main com.study.part06_io.s04_char_stream.Example2_FileReaderVsInputStreamReader
java -cp build/classes/java/main com.study.part06_io.s04_char_stream.Example3_WriterEncodingRoundtrip
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Reader 한글)** — 가설 1.
- Reader로 한 글자씩 읽기 → `Hi한글` (6.3 바이트 방식과 달리 한글 정상).
- `BufferedReader.readLine()` → `Hi한글`. ✅
- → Reader가 UTF-8 3바이트를 묶어 '한' 글자로 해석하기 때문.

**예시 2 (FileReader vs InputStreamReader)** — 가설 2. (EUC-KR로 저장한 "한글테스트")

| 읽기 인코딩 | 결과 |
|---|---|
| InputStreamReader(UTF-8) | `???????` (깨짐 — 파일은 EUC-KR) |
| InputStreamReader(EUC-KR) | `한글테스트` (정상 — 인코딩 일치) |

→ Reader라고 무조건 되는 게 아니라 인코딩이 맞아야 한다. InputStreamReader는 명시 가능해 안전. ✅

**예시 3 (Writer round-trip)** — 가설 3. (UTF-8로 쓴 "안녕 Writer")

| 읽기 인코딩 | 결과 |
|---|---|
| UTF-8 | `안녕 Writer` (정상, 쓰기와 일치) |
| EUC-KR | `???? Writer` (깨짐, 불일치) |

→ 쓰기 인코딩 == 읽기 인코딩이어야 한글이 보존된다. ✅

### 세 예시를 관통하는 결론
문자 스트림(Reader/Writer)은 인코딩을 해석해 '문자' 단위로 다루므로 한글을 제대로 처리한다(예시1).
단 "Reader면 무조건 OK"가 아니라, **파일 인코딩과 해석 인코딩이 일치**해야 한다(예시2). 쓸 때도
인코딩으로 바이트를 만들므로 **쓰기/읽기 인코딩이 같아야** 글자가 보존된다(예시3). 그래서 인코딩은
바이트 스트림을 문자 스트림으로 감쌀 때(InputStreamReader/OutputStreamWriter) **항상 명시**하고,
프로젝트 전체를 보통 **UTF-8로 통일**한다. (앞서 PART 2~3에서 javap/콘솔 한글 깨짐도 결국 같은
"인코딩 불일치" 문제였다.)

---

## 3. 자기 점검

- **Q. 바이트 스트림과 문자 스트림의 결정적 차이는?**
  - 내 답: 바이트 스트림은 바이트만 다뤄 인코딩을 모른다. 문자 스트림(Reader/Writer)은 인코딩을
    해석해 여러 바이트를 한 문자로 묶어 다룬다 → 한글 등 다국어 OK. (Example1)

- **Q. FileReader 대신 InputStreamReader를 권장하는 이유는?**
  - 내 답: FileReader는 플랫폼 기본 charset에 의존해 환경에 따라 깨질 수 있다. InputStreamReader는
    charset을 명시할 수 있어 파일 인코딩에 정확히 맞출 수 있다. (Example2)

- **Q. (적용) UTF-8로 저장된 CSV를 읽었더니 한글이 깨졌다. 가장 먼저 확인할 것은?**
  - 읽는 쪽의 인코딩이 UTF-8로 명시됐는지. InputStreamReader(in, UTF_8)로 읽고 있는지, 혹시
    기본 charset(FileReader/Files 기본)에 의존하고 있지 않은지 확인. (Example2, Example3과 연결)
