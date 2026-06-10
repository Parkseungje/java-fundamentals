# PART 6 — I/O와 직렬화: 6.1 I/O 큰 그림 (IO vs NIO 진화)

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.1 I/O 큰 그림**을 다룬다.
> Input/Output의 기준, IO→NIO→NIO.2 진화, File→Files/Path 개선을 본다.

---

## 1. 학습 내용 — Input/Output의 기준과 I/O API의 진화

### I/O의 기준점은 JVM
Input/Output은 항상 **JVM(내 프로그램)을 기준**으로 나뉜다.
- **Input**: 외부(파일/네트워크/키보드) → JVM 으로 들어옴. 예: 파일 읽기, DB 조회, 키보드 입력.
- **Output**: JVM → 외부(파일/화면/네트워크) 로 나감. 예: 파일 쓰기, **콘솔 출력(System.out)**.

헷갈리기 쉬운 점: `System.out.println`은 "JVM → 화면"이므로 **Output**이다.

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
