# PART 6 — I/O와 직렬화: 6.3 바이트 스트림 + 한글 문제

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.3 바이트 스트림 + 한글 문제**를 다룬다.
> 바이트 단위 I/O의 동작과 한계(한글 깨짐), read()의 int 반환·EOF, 버퍼 읽기 함정·append를 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **바이트(byte)**: 데이터의 최소 단위(0~255). 모든 파일·데이터는 결국 바이트의 나열이다.
- **바이트 스트림**: 바이트 단위로 읽고 쓰는 통로. `InputStream`(읽기)·`OutputStream`(쓰기)이 기본, 파일용은 `FileInputStream`/`FileOutputStream`.
- **인코딩(charset)**: 문자 ↔ 바이트 변환 규칙(UTF-8 등). 한글은 UTF-8에서 1글자=3바이트.
- **EOF(End Of File)**: 더 읽을 데이터가 없는 '파일 끝'. `read()`는 이때 -1을 반환.
- **버퍼(buffer)**: 한 번에 여러 바이트를 담는 임시 배열(`byte[]`). 1바이트씩보다 빠르게 읽기 위함.
- **append(이어쓰기)**: 기존 내용 뒤에 덧붙이기. `new FileOutputStream(path, true)`. 기본은 덮어쓰기.

한 줄 그림: **바이트 스트림은 '바이트'만 안다. 그래서 한글(여러 바이트)을 1바이트씩 다루면 깨진다 — 문자는 6.4의 문자 스트림으로. read()는 끝에서 -1을 준다.**

---

## 1. 학습 내용 — 바이트 스트림의 동작과 함정

### 바이트 스트림과 한글
바이트 스트림(`InputStream`/`OutputStream`)은 이름 그대로 **바이트(byte) 단위**로만 다룬다.
- 영어 알파벳: UTF-8에서 1글자 = **1바이트** → 1바이트를 char로 바꿔도 맞다.
- 한글: UTF-8에서 1글자 = **3바이트** → 1바이트씩 끊어 char로 만들면 한 글자가 3개의 깨진 조각이 된다.

그래서 `System.in`(InputStream)을 `read()`로 1바이트씩 읽으면 영어는 되지만 **한글은 깨진다.**
문자를 제대로 다루려면 인코딩을 아는 **문자 스트림(Reader/Writer)** 이 필요하다(6.4).

### InputStream / OutputStream 사용법
바이트 스트림은 `InputStream`(읽기)과 `OutputStream`(쓰기) 두 추상 클래스가 기본이고, 파일용 구현이
`FileInputStream` / `FileOutputStream`이다. **Input은 "외부→JVM(읽기)", Output은 "JVM→외부(쓰기)"** (6.1).

**OutputStream (쓰기) — 핵심 메서드**

| 메서드 | 의미 |
|---|---|
| `write(int b)` | 1바이트 쓰기 (int의 하위 8비트) |
| `write(byte[] b)` | 바이트 배열 통째로 쓰기 |
| `write(byte[] b, int off, int len)` | 배열의 일부(off부터 len개)만 쓰기 |
| `flush()` | 버퍼에 남은 것을 강제로 내보내기(Buffered일 때 중요 — 6.5) |
| `close()` | 닫기(try-with-resources가 자동 호출) |

```java
// 쓰기: 바이트를 파일로 내보냄
try (OutputStream out = new FileOutputStream("a.txt")) {
    out.write("HELLO".getBytes(StandardCharsets.UTF_8)); // 문자열 -> byte[] (6.3 보충 참고)
    out.write(65);                                        // 1바이트(65 = 'A')
} // 블록 끝에서 자동 close
```

**InputStream (읽기) — 핵심 메서드**

| 메서드 | 의미 |
|---|---|
| `read()` | 1바이트 읽어 0~255로 반환, 끝이면 -1 |
| `read(byte[] buf)` | buf에 최대 buf.length만큼 읽고, **실제 읽은 수 n** 반환(끝이면 -1) |
| `readAllBytes()` | 끝까지 전부 읽어 byte[]로 (Java 9+) |
| `close()` | 닫기(try-with-resources가 자동 호출) |

```java
// 읽기 방법 1: 1바이트씩 (전형적 루프)
try (InputStream in = new FileInputStream("a.txt")) {
    int b;
    while ((b = in.read()) != -1) {   // -1(EOF)까지
        // b를 처리 (0~255)
    }
}

// 읽기 방법 2: 버퍼로 한 번에 (빠름, 단 'n까지만' 처리 — 아래 함정 참고)
try (InputStream in = new FileInputStream("a.txt")) {
    byte[] buf = new byte[1024];
    int n;
    while ((n = in.read(buf)) != -1) {
        // buf[0..n) 만 처리!
    }
}
```

> 💡 **"쓰고 나서 왜 또 읽지?" (예제에서 자주 헷갈리는 점)** — `Files.write(file, ...)`로 파일에 쓴
> 다음 `new FileInputStream(file.toFile())`로 읽는 코드를 보면 "이미 썼는데 왜 또?"라고 생각하기 쉽다.
> 쓰기(Output)와 읽기(Input)는 **별개의 작업**이다. 예제는 "읽기 동작/함정"을 보여주는 게 목적이라,
> 먼저 `write`로 **읽을 대상 파일을 준비**해 두고, 그다음 `InputStream`으로 **그 파일을 다시 읽으며**
> 시연하는 것이다. (시험문제를 칠판에 적어놓고 → 그걸 보고 푸는 것과 같다.)

### read()는 왜 int를 반환하나 (EOF -1)
`InputStream.read()`는 1바이트를 읽는데 반환 타입이 `byte`가 아니라 `int`다. 이유는 두 가지를
한 반환값으로 구분해야 하기 때문이다.
- 읽은 바이트 값: **0 ~ 255** (부호 없는 바이트)
- 더 읽을 게 없음(EOF): **-1**

만약 byte(-128~127)였다면, 정상 데이터 `0xFF`(=byte로 -1)와 EOF 신호 -1이 구분되지 않는다. 그래서
범위가 넓은 int로 "0~255는 데이터, -1은 EOF"를 명확히 나눈다. 읽기 루프는 항상 `while ((b = read()) != -1)`.
스트림은 OS 자원을 쓰므로 **사용 후 close 필수**(try-with-resources, 6.5).

### byte[] 버퍼 읽기 함정
1바이트씩은 느리므로 `byte[]` 버퍼로 한 번에 읽는다. `in.read(buffer)`는 **실제로 읽은 바이트 수 n**을
반환한다. 함정: 버퍼가 데이터보다 크면 다 안 채워지는데, **`buffer.length` 전체가 아니라 `buffer[0..n)`
까지만** 처리해야 한다(나머지는 이전 값/0이 남은 쓰레기).

### FileOutputStream append
`FileOutputStream`은 기본이 **덮어쓰기**다. 생성자에 `true`를 주면 **이어쓰기**:
`new FileOutputStream(path, true)`.

### 보충 — `"HELLO".getBytes(UTF_8)`은 뭐고, 꼭 써야 하나?
예제에 자주 나오는 `Files.write(file, "HELLO".getBytes(StandardCharsets.UTF_8))`이 헷갈릴 수 있다.

**`getBytes(charset)` = 문자열을 그 인코딩 규칙으로 byte[]로 변환**한다.
- `"HELLO".getBytes(UTF_8)` → `[72, 69, 76, 76, 79]` (H,E,L,L,O — 영어라 1글자=1바이트)
- 문자(글자)와 바이트(파일에 저장되는 숫자)는 다른 개념이라, "글자→바이트"로 바꾸려면 인코딩이 필요하다.

**왜 변환하나? → `Files.write`가 byte[]만 받기 때문이다.**

```java
Files.write(file, byte[])          // 시그니처: byte[]만 받음
Files.write(file, "HELLO".getBytes(UTF_8));  // ✅ 문자열 -> byte[] 변환 필요
Files.write(file, "HELLO");                  // ❌ 컴파일 에러(String은 byte[]가 아님)
```

**문자열을 바로 쓰고 싶으면 `Files.writeString`(Java 11+)을 쓰면 된다 — 변환 불필요:**

```java
Files.writeString(file, "HELLO");                       // 기본 UTF-8 (getBytes 불필요)
Files.writeString(file, "HELLO", StandardCharsets.UTF_8);
```

| 쓰려는 것 | 메서드 | 인코딩 |
|---|---|---|
| 문자열 (간단) | `Files.writeString(file, s)` | 내부 자동(기본 UTF-8) |
| 문자열 (byte API로) | `Files.write(file, s.getBytes(UTF_8))` | 수동 변환 |
| 바이트(이미지 등) | `Files.write(file, byteArray)` | 변환 불필요(이미 바이트) |

이 단원 예제가 `Files.write` + getBytes를 쓴 건 **"문자열도 결국 바이트로 저장된다"는 바이트 단원의
취지**를 명시적으로 보여주기 위해서다. 의미상 `Files.writeString(file, "HELLO")`와 같다.

> ⚠️ `getBytes()`를 **인자 없이** 쓰면 플랫폼 기본 charset(Windows 한국어=MS949 등)을 써서 환경마다
> 결과가 달라질 수 있다(6.4 인코딩 함정). **항상 `getBytes(StandardCharsets.UTF_8)`처럼 인코딩을 명시**하자.

### 보충 — `file.toFile()`은 뭐고 왜 필요한가?
예제에 나오는 `new FileInputStream(file.toFile())`의 `file.toFile()`도 헷갈릴 수 있다.

자바에는 파일 경로를 나타내는 타입이 **두 가지**다.
- **`java.nio.file.Path`** (NIO.2, Java 7+) — 현대적 API. `Files`, `Path.of(...)`, `Files.createTempFile(...)`이 돌려주는 타입.
- **`java.io.File`** (옛 API) — `FileInputStream`/`FileOutputStream`/`FileReader` 같은 **옛 스트림 클래스들이 받는 타입.**

문제는 옛 스트림 클래스들이 **`Path`를 직접 못 받고 `File`만 받는다**는 것이다. 그래서 `Path`를
`File`로 바꿔주는 변환 메서드가 `Path.toFile()`이다(반대 방향은 `File.toPath()`).

```java
Path file = Files.createTempFile("x", ".txt");   // Path를 얻음
new FileInputStream(file)            // ❌ 컴파일 에러 (FileInputStream은 Path를 못 받음)
new FileInputStream(file.toFile())   // ✅ Path -> File 로 변환해 전달
```

| 변환 | 의미 |
|---|---|
| `path.toFile()` | NIO.2 `Path` → 옛 `java.io.File` |
| `file.toPath()` | 옛 `File` → NIO.2 `Path` |

> 참고: 사실 NIO.2에는 Path를 바로 받는 `Files.newInputStream(path)` / `Files.newOutputStream(path)`가
> 있어서, 이걸 쓰면 `toFile()` 없이도 된다(현대 코드 권장). 이 단원이 `FileInputStream(...toFile())`을
> 쓴 건 '전통 바이트 스트림 클래스'를 직접 보여주기 위해서다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 1바이트씩 읽으면 영어는 정상, 한글(3바이트)은 깨진다. UTF-8로 묶어 디코딩하면 정상.
> - **가설 2**: read()는 데이터를 0~255 int로, EOF를 -1로 반환한다(그래서 byte가 아닌 int).
> - **가설 3**: 버퍼 읽기는 읽은 수 n까지만 처리해야 한다. append=true면 이어쓰기.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ByteStreamHangul` | 왜 한글이 깨지나? | "Hi한글" 1바이트씩 vs UTF-8 디코딩 |
| `Example2_ReadEofAndIntReturn` | read()는 왜 int? | 0xFF=255, EOF=-1 |
| `Example3_BufferTrapAndAppend` | 버퍼 함정·이어쓰기? | n까지만 처리 + append 모드 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s03_byte_stream.Example1_ByteStreamHangul
java -cp build/classes/java/main com.study.part06_io.s03_byte_stream.Example2_ReadEofAndIntReturn
java -cp build/classes/java/main com.study.part06_io.s03_byte_stream.Example3_BufferTrapAndAppend
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (한글 깨짐)** — 가설 1.

| 방식 | 결과 |
|---|---|
| 1바이트씩 (char) 변환 | `Hi????¸?` (H,i 정상, 한글 깨짐) |
| 전체 UTF-8 디코딩 | `Hi한글` (정상) |

→ "Hi한글" = 8바이트(영어 2 + 한글 2자×3). 바이트 스트림으로 1바이트씩 쪼개면 한글이 깨진다. ✅

**예시 2 (int 반환·EOF)** — 가설 2.
- read() = 65('A'), 66('B'), **255(0xFF)**, 그리고 **-1(EOF)** 로 루프 종료.
- 0xFF가 255로 읽혔다 → byte였다면 -1이 되어 EOF와 충돌. 그래서 int(0~255 + EOF -1). ✅

**예시 3 (버퍼 함정·append)** — 가설 3.
- 버퍼 크기 10, 데이터 5바이트 → `read()`=**5**(n). `new String(buffer)`는 길이 10(뒤 쓰레기),
  `new String(buffer, 0, n)`은 길이 5("HELLO"). → **n까지만 처리해야 함**.
- append=false(기본): 두 번 쓰면 마지막만 남음(덮어씀). append=true: A 뒤에 B가 누적됨. ✅

### 세 예시를 관통하는 결론
바이트 스트림은 가장 기본적이고 모든 데이터(이미지·파일 등)에 쓸 수 있지만 **'바이트'만 안다.**
그래서 한글처럼 여러 바이트로 된 문자를 1바이트씩 다루면 깨진다(예시1) — 문자는 6.4의 문자 스트림으로.
read()가 int인 건 데이터(0~255)와 EOF(-1)를 구분하기 위함이고(예시2), 성능을 위한 버퍼 읽기는 읽은
수 n까지만 처리해야 한다(예시3). append 모드로 덮어쓰기/이어쓰기를 제어한다.

---

## 3. 자기 점검

- **Q. 바이트 스트림으로 한글을 1바이트씩 읽으면 왜 깨지나?**
  - 내 답: 한글은 UTF-8에서 1글자가 3바이트인데, 1바이트씩 char로 변환하면 한 글자가 3조각으로
    쪼개져 의미 없는 문자가 된다. 전체 바이트를 UTF-8로 묶어 디코딩해야 정상. (Example1)

- **Q. read()가 byte가 아니라 int를 반환하는 이유는?**
  - 내 답: 데이터 값(0~255)과 EOF(-1)를 한 반환값으로 구분하려고. byte였다면 0xFF(데이터)와 -1(EOF)이
    겹친다. (Example2)

- **Q. byte[] 버퍼로 읽을 때 반드시 지켜야 할 것은?**
  - 내 답: `read(buffer)`가 반환한 실제 읽은 수 n까지만(`buffer[0..n)`) 처리한다. 버퍼 전체를 쓰면
    안 채워진 뒷부분 쓰레기가 섞인다. (Example3)
