# PART 6 — I/O와 직렬화: 6.1 I/O 큰 그림 (IO vs NIO 진화)

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.1 I/O 큰 그림**을 다룬다.
> Input/Output의 기준, IO→NIO→NIO.2 진화, File→Files/Path 개선을 본다.

---

## 0. 들어가기 전에 — 핵심 용어
- **I/O(Input/Output)**: 프로그램이 외부(파일·네트워크·키보드·화면)와 데이터를 주고받는 것.
- **Input ↔ Output**: 항상 **JVM(내 프로그램) 기준**. 외부→JVM = 입력(읽기), JVM→외부 = 출력(쓰기). (`System.out.println`은 JVM→화면이라 Output)
- **스트림(stream)**: 데이터가 한 줄로 흐르는 통로. 바이트 단위(InputStream/OutputStream) 또는 문자 단위(Reader/Writer)가 있다.
- **IO(java.io)**: 옛 방식. `File`, 스트림 기반. **NIO(java.nio, Java 4+)**: 채널·버퍼 기반 고성능. **NIO.2(Java 7+)**: `Path`/`Files`로 파일 API 개선.
- **Path vs File**: 파일 경로를 나타내는 객체 — `Path`(현대, NIO.2) vs `File`(옛). 둘 다 '경로'일 뿐 실제 파일과는 별개.
- **시스템 콜(system call)**: 앱이 하드웨어(디스크 등)를 만지려고 OS 커널에 보내는 요청(비용이 있어 I/O 성능의 핵심).

한 줄 그림: **I/O는 JVM 기준으로 입력(읽기)/출력(쓰기)으로 나뉘고, API는 IO(java.io)→NIO/NIO.2(java.nio)로 발전했다. 신규 코드는 Path/Files를 권장한다.**

---

## 1. 학습 내용 — Input/Output의 기준과 I/O API의 진화

### I/O의 기준점은 JVM
Input/Output은 항상 **JVM(내 프로그램)을 기준**으로 나뉜다.
- **Input**: 외부(파일/네트워크/키보드) → JVM 으로 들어옴. 예: 파일 읽기, DB 조회, 키보드 입력.
- **Output**: JVM → 외부(파일/화면/네트워크) 로 나감. 예: 파일 쓰기, **콘솔 출력(System.out)**.

헷갈리기 쉬운 점: `System.out.println`은 "JVM → 화면"이므로 **Output**이다.

### 보충 — 시스템 콜(system call)이란? (I/O 성능을 이해하는 열쇠)
보조 스트림(Buffered, 6.5) 설명에서 "시스템 콜 횟수를 줄여 빨라진다"는 말이 자주 나온다. 시스템 콜을
알아야 그게 왜 중요한지 이해된다.

운영체제(OS)는 보안·안정성을 위해 메모리를 **유저 모드(일반 앱이 도는 곳)** 와 **커널 모드(OS 핵심이
도는 곳, 하드웨어 직접 제어)** 로 나눈다. 파일·네트워크·디스크 같은 **하드웨어 자원은 커널만** 직접
만질 수 있다. 그래서 우리 앱(JVM, 유저 모드)이 "파일에 1바이트 써줘" 같은 일을 하려면, **OS 커널에게
대신 해달라고 요청**해야 하는데 이 요청이 **시스템 콜**이다.

```
[내 앱(유저 모드)]  --- 시스템 콜(요청) --->  [OS 커널(커널 모드)]  ---> 실제 디스크/네트워크
                  <--- 결과 ---
```

**왜 비싼가**: 시스템 콜 한 번에는 "유저 모드 → 커널 모드 전환 → 작업 → 다시 유저 모드 복귀"라는
**모드 전환 비용**이 든다. 이 전환 자체가 오버헤드라, 횟수가 많아지면 느려진다.

**그래서 버퍼가 중요하다**: `FileOutputStream`으로 1바이트씩 100만 번 쓰면 시스템 콜도 ~100만 번 →
모드 전환만 100만 번 → 매우 느리다(6.5에서 실측 70배 차이). `BufferedOutputStream`은 1바이트 쓰기를
일단 메모리 버퍼(기본 8KB)에 모았다가 **버퍼가 찰 때 한 번의 시스템 콜로** 8KB를 통째로 내보낸다 →
시스템 콜 횟수가 수백~수천 배 줄어 빨라진다. (네트워크 read의 "커널 소켓 버퍼"도 같은 맥락 — 6.2)

한 줄 요약: **시스템 콜 = 유저 모드 앱이 하드웨어를 만지려고 OS 커널에 보내는 요청**이고, 모드 전환
비용이 있어 비싸다. I/O 최적화의 상당 부분은 "시스템 콜 횟수를 줄이는 것"(버퍼링 등)이다.

### IO → NIO → NIO.2 진화
| 구분 | IO (Java 1.0~) | NIO (1.4+) | NIO.2 (7+) |
|---|---|---|---|
| 단위 | 스트림(1바이트씩) | 채널 + 버퍼(블록) | 채널 + 버퍼 |
| 방향 | 단방향 | 양방향 | 양방향 |
| Blocking | 항상 Blocking | Non-blocking 가능 | Non-blocking 가능 |
| 파일 API | `File` | `FileChannel` | `Files`(static), `Path` |

- **IO(스트림)**: 데이터를 1바이트(혹은 byte[])씩 흘려보낸다. 방향이 고정(InputStream=읽기 전용).
- **NIO(채널+버퍼)**: 데이터는 항상 **Buffer(블록)** 를 거쳐 **Channel(양방향)** 로 오간다.
  Buffer는 `flip()`으로 읽기/쓰기 모드를 전환한다.
- **NIO.2**: `File`을 대체하는 `Files`(static 메서드) + `Path`.

### File → Files/Path 개선
옛 `java.io.File`은 설계가 낡았다. 대표적으로 `File.delete()`는 **boolean만** 반환한다 — false가
나오면 "실패"라는 것만 알 뿐 **왜**(없어서? 권한? 잠김?)는 알 수 없다. NIO.2의 `Files.delete(Path)`는
실패 시 **구체적인 예외**(없으면 `NoSuchFileException` 등)를 던져 원인을 알 수 있다. Files API는
메서드가 모두 static이고 Path를 인자로 받는다. 그래서 신규 코드는 Files/Path를 쓴다.

#### 왜 NIO.2는 File을 버리고 Path + Files 구조로 갔나 — File의 5가지 문제
NIO.2(Java 7)가 `File`을 `Path`(경로 표현) + `Files`(조작 메서드)로 쪼갠 건 `File`의 누적된 결함을
해결하기 위해서였다. `File`이 안고 있던 문제들:

1. **실패 원인을 알 수 없다 (가장 큰 문제).** `File`의 조작 메서드는 대부분 **boolean을 반환**한다
   (`delete()`, `mkdir()`, `renameTo()` ...). false가 와도 "없어서/권한 없어서/잠겨서" 중 무엇인지 모른다.
   → `Files`는 **구체적 예외**(`NoSuchFileException`, `AccessDeniedException` 등)를 던져 원인을 알고 대처할 수 있다.

2. **'경로 표현'과 '파일 조작'이 한 클래스에 뒤섞임 (책임 과다 = SRP 위반).** `File`은 경로를 나타내는
   값이면서 동시에 delete/mkdir 같은 동작도 하는 거대 클래스였다. → NIO.2는 **`Path`(경로라는 '값'만 표현)**
   와 **`Files`(조작을 모은 유틸)** 로 책임을 분리했다. 그래서 새 기능을 `Files`에 추가하기도 쉽다.

3. **메타데이터·심볼릭 링크·권한을 제대로 못 다룸.** `File`은 파일 권한, 소유자, 생성/수정 시간,
   심볼릭 링크 같은 OS 파일 시스템 기능을 빈약하게만 지원했다. → `Files`는 `getPosixFilePermissions`,
   `readAttributes`, 심볼릭 링크 처리 등 **현대 파일 시스템 기능**을 제대로 제공한다.

4. **대용량 디렉터리 순회가 비효율적.** `File.listFiles()`는 모든 항목을 **배열에 한꺼번에** 담아
   메모리를 많이 쓰고, 큰 디렉터리에서 느렸다. → `Files.list()`/`Files.walk()`는 **Stream으로 지연
   순회**(필요한 만큼만)해 메모리·성능이 낫다.

5. **다른 파일 시스템으로 확장 불가.** `File`은 OS 기본 파일 시스템에 박혀 있었다. → NIO.2는
   `FileSystem`/`Path` 추상화를 도입해, ZIP 안의 경로나 메모리 파일 시스템 등 **다양한 파일 시스템을
   같은 API로** 다룰 수 있게 했다(`FileSystems.newFileSystem(...)`).

한 줄 요약: NIO.2는 `File`의 **① 실패 원인 불명(boolean) ② 경로/조작 책임 혼재 ③ 빈약한 메타데이터·
링크 지원 ④ 비효율적 대용량 순회 ⑤ 확장 불가**를 해결하려고, **경로(Path)와 조작(Files)을 분리하고
예외 기반·Stream 기반·플랫폼 추상화**로 다시 설계한 것이다.

### ★ 헷갈리는 지점 — "Path는 파일이 아니다" (실제 파일 vs 가리키는 객체)
`Path out = Files.createTempFile("append", ".txt")`를 보면 "Path인데 append.txt는 파일 아닌가?
파일을 만들었는데 왜 또 `toFile()`로 파일화하지?" 하고 헷갈리기 쉽다. 핵심은 **"실제 파일"과
"그 파일을 가리키는 객체"는 다른 개념**이라는 것이다.

| 개념 | 정체 |
|---|---|
| `append.txt` (디스크의 실제 파일) | OS/디스크에 존재하는 진짜 파일 |
| `Path` | 그 파일을 **가리키는 자바 객체**(경로 정보) — NIO.2 양식 |
| `File` | 그 파일을 가리키는 **또 다른 종류**의 자바 객체 — 옛 양식 |

비유: **집(실제 파일) vs 집 주소가 적힌 종이(Path/File)**. Path와 File은 둘 다 "같은 집을 가리키는
주소 종이"인데 **양식만 다르다**(NIO.2 양식=Path, 옛 양식=File).

**`Files.createTempFile`은 두 가지를 한다.**
1. 디스크에 실제 빈 파일을 **생성**하고, 2. 그 경로를 **Path로 반환**한다. 그래서 "파일이 만들어졌고,
그걸 가리키는 주소가 Path 타입"인 상태다.

**`toFile()`은 파일을 또 만드는 게 아니다.** 디스크 파일은 그대로 두고, **가리키는 객체의 타입만
Path → File로 변환**한다(주소 종이를 NIO.2 양식 → 옛 양식으로 옮겨 적기). 왜? `FileInputStream` 같은
옛 스트림 클래스가 `Path`를 못 받고 `File`만 받기 때문이다.

**경로만 만들기 vs 파일까지 만들기 — 둘은 다르다.**

| 코드 | 실제 파일 생성? | 의미 |
|---|---|---|
| `Path.of("a.txt")` / `Paths.get("a.txt")` | ❌ 안 함 | "a.txt를 가리키는 주소"만 만든다(디스크엔 없을 수 있음) |
| `Files.createTempFile("x", ".txt")` | ✅ 함 | 실제 파일 생성 + 그 Path 반환 |
| `Files.createFile(path)` / `Files.write(path, ...)` | ✅ 함 | Path가 가리키는 곳에 실제 파일 생성/기록 |

```java
Path p = Path.of("a.txt");     // 주소만 — 디스크엔 아직 파일 없음
Files.exists(p);                // false
Files.createFile(p);            // 이제 실제 파일 생성
Files.writeString(p, "내용");    // 또는 쓰면서 생성
```

정리: **`Path`는 "경로(주소)를 나타내는 값"일 뿐, 그 자체로는 파일 존재와 무관**하다. 실제 파일을
만지는 건 `Files`의 메서드(`createTempFile`/`createFile`/`write` 등)다. `toFile()`은 파일이 아니라
'주소 객체의 타입'을 바꾸는 변환일 뿐이다.

### ★ IO vs NIO API 구분 — 패키지·클래스로 알아본다
"어떤 게 옛 IO고 어떤 게 NIO냐"는 **패키지 이름**으로 거의 구분된다.
- **옛 IO** = `java.io.*` (클래스명에 `File`, `Stream`, `Reader`, `Writer`가 붙음)
- **NIO/NIO.2** = `java.nio.*` (`Path`, `Files`, `Channel`, `ByteBuffer`)

**(1) 경로 표현**

| 구분 | 클래스 | 만드는 법 |
|---|---|---|
| 옛 IO | `java.io.File` | `new File("a.txt")` |
| NIO.2 | `java.nio.file.Path` | `Path.of("a.txt")` / `Paths.get("a.txt")` |

변환: `path.toFile()`(Path→File), `file.toPath()`(File→Path).

**(2) 파일 조작 — 옛 `File` 메서드 vs NIO.2 `Files`(static) 메서드**

| 작업 | 옛 IO (`File`의 인스턴스 메서드) | NIO.2 (`Files`의 static 메서드) |
|---|---|---|
| 존재 확인 | `file.exists()` | `Files.exists(path)` |
| 삭제 | `file.delete()` (boolean) | `Files.delete(path)` (실패 시 예외) |
| 생성 | `file.createNewFile()` | `Files.createFile(path)` |
| 디렉터리 생성 | `file.mkdirs()` | `Files.createDirectories(path)` |
| 크기 | `file.length()` | `Files.size(path)` |
| 목록 | `file.listFiles()` | `Files.list(path)` (Stream 반환) |

> 옛 IO는 "File 객체를 만들고 그 객체의 메서드 호출"(`file.delete()`), NIO.2는 "Files라는 유틸 클래스의
> static 메서드에 Path를 넘김"(`Files.delete(path)`)이라는 형태 차이가 있다.

**(3) 읽기/쓰기 — 가장 헷갈리는 부분**

옛 IO는 '스트림 객체를 만들어 read/write 루프'를 돌린다(저수준, 세밀한 제어).

```java
// 옛 IO — 바이트 스트림 (java.io)
try (InputStream in = new FileInputStream("a.txt")) {        // File/문자열 경로
    int b;
    while ((b = in.read()) != -1) { ... }                    // 1바이트씩
}
try (OutputStream out = new FileOutputStream("a.txt")) {
    out.write(bytes);
}
// 옛 IO — 문자 스트림 (java.io): Reader/Writer
try (BufferedReader br = new BufferedReader(new FileReader("a.txt"))) {
    String line = br.readLine();
}
```

NIO.2 `Files`는 '한 줄짜리 헬퍼'로 통째로 읽고 쓴다(고수준, 간편).

```java
// NIO.2 — 통째로 (java.nio.file.Files, 모두 static + Path)
String text = Files.readString(path);                 // 파일 전체를 문자열로 (Java 11+)
List<String> lines = Files.readAllLines(path);        // 줄 리스트로
byte[] bytes = Files.readAllBytes(path);              // 바이트 전체

Files.writeString(path, "내용");                       // 문자열 쓰기 (Java 11+)
Files.write(path, bytes);                             // 바이트 쓰기

// NIO.2 — 스트림이 필요하면 Path로 바로 여는 헬퍼 (toFile() 불필요)
try (InputStream in = Files.newInputStream(path)) { ... }
try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { ... }
```

**(4) NIO 전용 — Channel + Buffer (대용량·논블로킹)**
`FileChannel`, `ByteBuffer`, `Selector`(java.nio.channels)는 NIO만의 저수준 고성능 API다.
일반 파일 읽기엔 `Files`가 간편하고, 대용량·네트워크·논블로킹이 필요할 때 Channel을 쓴다(6.2).

**언제 무엇을 쓰나 (실무 가이드)**
- **간단한 파일 읽기/쓰기** → `Files.readString` / `Files.writeString` (NIO.2, 가장 간편) ← 신규 코드 권장
- **대용량을 조금씩(스트리밍)** → 스트림(`Files.newInputStream` 또는 옛 `FileInputStream`) + 버퍼
- **네트워크·동시접속·논블로킹** → NIO Channel + Selector (6.2)
- 옛 `java.io.File`/`FileReader`는 레거시 코드에서 보게 되지만, 신규 코드는 `Path`/`Files` 우선.

### ★ 비슷한 이름 총정리 — 스트림 클래스/메서드 (IO vs NIO 구분)
`BufferedInputStream`, `DataInputStream`, `newInputStream`... 이름이 비슷해 헷갈린다. **세 그룹**으로
나눠서 보면 정리된다: ① 기반 스트림(java.io) ② 보조 스트림(java.io, 기반을 '감싸서' 기능 추가)
③ NIO.2 헬퍼(java.nio.file.Files의 static 메서드).

**(A) InputStream 계열 (바이트 '읽기')**

| 이름 | 분류 | 역할 |
|---|---|---|
| `InputStream` | java.io (추상) | 모든 바이트 입력의 부모 |
| `FileInputStream` | java.io (기반) | 파일에서 바이트 읽기 (직접 소스) |
| `ByteArrayInputStream` | java.io (기반) | 메모리 byte[]에서 읽기 |
| `BufferedInputStream` | java.io (보조) | 다른 InputStream을 감싸 **버퍼링**(시스템 콜 ↓, 6.5) |
| `DataInputStream` | java.io (보조) | 감싼 스트림에서 **기본 타입**(int/double/UTF) 읽기(`readInt` 등, 6.5) |
| `ObjectInputStream` | java.io (보조) | **객체 역직렬화**(`readObject`, 6.6) |
| `Files.newInputStream(path)` | **NIO.2 (헬퍼)** | Path로 InputStream을 바로 연다(`toFile()` 불필요) |

**(B) OutputStream 계열 (바이트 '쓰기')** — 위와 1:1 대응

| 이름 | 분류 | 역할 |
|---|---|---|
| `OutputStream` | java.io (추상) | 모든 바이트 출력의 부모 |
| `FileOutputStream` | java.io (기반) | 파일에 바이트 쓰기 (append 옵션 있음, 6.3) |
| `ByteArrayOutputStream` | java.io (기반) | 메모리 byte[]에 쓰기 |
| `BufferedOutputStream` | java.io (보조) | 감싸서 **버퍼링**(모아서 한 번에, 6.5) |
| `DataOutputStream` | java.io (보조) | **기본 타입** 쓰기(`writeInt`/`writeUTF`, 6.5) |
| `ObjectOutputStream` | java.io (보조) | **객체 직렬화**(`writeObject`, 6.6) |
| `Files.newOutputStream(path)` | **NIO.2 (헬퍼)** | Path로 OutputStream을 바로 연다 |

> **기반 vs 보조 구분법**: 생성자에 **다른 스트림을 받으면 보조**(`new BufferedInputStream(다른스트림)`),
> **파일/배열 등 실제 소스를 받으면 기반**(`new FileInputStream(file)`). 보조는 혼자 못 쓰고 기반을 감싼다.
> 전형적 조합: `new DataInputStream(new BufferedInputStream(new FileInputStream(f)))`
> — "파일에서(기반) → 버퍼링하며(보조) → 타입별로 읽기(보조)".

**(C) 문자 스트림 — Reader/Writer (바이트가 아니라 '문자', 6.4)**

| 이름 | 분류 | 역할 |
|---|---|---|
| `FileReader`/`FileWriter` | java.io (기반) | 파일 문자 읽기/쓰기(기본 charset — 인코딩 명시 불가가 함정) |
| `InputStreamReader`/`OutputStreamWriter` | java.io (보조/다리) | **바이트 스트림 ↔ 문자 스트림** 변환(charset 명시 가능) |
| `BufferedReader`/`BufferedWriter` | java.io (보조) | 버퍼링 + **`readLine()`** 제공(6.4) |
| `Files.newBufferedReader(path, cs)` | **NIO.2 (헬퍼)** | Path로 BufferedReader를 바로 연다 |

**메서드 빠른 참조 (이름이 헷갈릴 때)**

| 하고 싶은 것 | 옛 IO | NIO.2 (Files static) |
|---|---|---|
| 바이트 1개 읽기 | `inputStream.read()` | (스트림 열어서 동일) |
| 바이트 배열로 다 읽기 | `inputStream.readAllBytes()` | `Files.readAllBytes(path)` |
| 한 줄 읽기 | `bufferedReader.readLine()` | `Files.readAllLines(path)`(전체 줄) |
| 문자열 전체 읽기 | (직접 모아야) | `Files.readString(path)` ✅ 가장 간단 |
| 바이트 쓰기 | `outputStream.write(bytes)` | `Files.write(path, bytes)` |
| 문자열 쓰기 | `writer.write(s)` | `Files.writeString(path, s)` ✅ |
| 타입별(int/double) 저장 | `DataOutputStream.writeInt(...)` | (없음 — DataStream 사용) |
| 객체 저장 | `ObjectOutputStream.writeObject(...)` | (없음 — ObjectStream 사용) |

핵심 한 줄: **`new Xxx...Stream`(java.io) = 기반/보조 스트림(직접 조립), `Files.newXxx`/`Files.readXxx`
(java.nio.file) = NIO.2 헬퍼(간편)**. `Data`/`Object`/`Buffered`로 시작하면 보조 스트림(뭔가를 감싸는 것)이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: Input/Output은 JVM 기준(들어옴/나감)으로 나뉘고, 콘솔 출력도 Output이다.
> - **가설 2**: File.delete()는 boolean(이유 불명), Files.delete()는 정확한 예외를 던진다.
> - **가설 3**: IO 스트림과 NIO 채널+버퍼는 결과는 같아도 모델(흐름 vs 블록)이 다르다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_InputOutputPerspective` | Input/Output 기준? | 파일 쓰기/읽기 + System.out |
| `Example2_FileVsFiles` | 실패를 어떻게 알리나? | 없는 파일 삭제 비교 |
| `Example3_StreamVsChannel` | IO vs NIO 모델? | 같은 파일 두 방식 읽기 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s01_io_overview.Example1_InputOutputPerspective
java -cp build/classes/java/main com.study.part06_io.s01_io_overview.Example2_FileVsFiles
java -cp build/classes/java/main com.study.part06_io.s01_io_overview.Example3_StreamVsChannel
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Input/Output 관점)** — 가설 1.
- `Files.writeString`=Output(JVM→파일), `Files.readString`=Input(파일→JVM), `System.out`=Output(JVM→화면). ✅

**예시 2 (File vs Files)** — 가설 2.

| API | 없는 파일 삭제 결과 |
|---|---|
| `new File(...).delete()` | `false` (이유 불명) |
| `Files.delete(path)` | **NoSuchFileException** (원인 명확) |

→ File은 실패를 boolean으로 뭉개고, Files는 정확한 예외로 알려준다. ✅

**예시 3 (IO vs NIO)** — 가설 3.
- IO 스트림(`InputStream.read` 1바이트씩) = `"Hello NIO"`
- NIO 채널+버퍼(`ByteBuffer` 블록 + `flip()`) = `"Hello NIO"`
- 두 결과 동일(true). 결과는 같지만 모델(흐름 vs 블록·양방향)이 다르다. ✅

### 세 예시를 관통하는 결론
I/O는 JVM을 기준으로 Input(들어옴)/Output(나감)을 나눈다(예시1). API는 IO(스트림·1바이트·단방향·항상
Blocking) → NIO(채널+버퍼·블록·양방향·Non-blocking 가능) → NIO.2(Files/Path)로 진화했고, File의
boolean 반환 같은 낡은 설계가 Files의 정확한 예외로 개선됐다(예시2). IO와 NIO는 같은 일을 해도
모델이 다르며(예시3), NIO의 진짜 강점인 Non-blocking·Selector는 6.2에서 다룬다.

---

## 3. 자기 점검

- **Q. System.out.println은 Input인가 Output인가?**
  - 내 답: Output. JVM → 화면(외부)으로 나가기 때문. Input/Output은 JVM 기준으로 판단한다. (Example1)

- **Q. File 대신 Files/Path를 쓰는 이유는?**
  - 내 답: File.delete()는 boolean만 반환해 실패 이유를 모르지만, Files.delete()는 구체적 예외로
    원인을 알려준다. Files는 static + Path 기반의 현대적 API. (Example2)

- **Q. (예고) IO가 "항상 Blocking"이라 1만 명 동시 접속에 불리한 이유는?**
  - 6.2에서 다룬다. IO는 read()가 데이터 올 때까지 스레드를 멈추므로 접속 수만큼 스레드가 필요하다.
    NIO는 Non-blocking + Selector로 소수 스레드가 다수 연결을 처리한다 — 그 원리를 6.2에서 본다.
