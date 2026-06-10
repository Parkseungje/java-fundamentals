# PART 6 — I/O와 직렬화: 6.5 보조 스트림 (Buffered / Data) + try-with-resources

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.5 보조 스트림 + try-with-resources**를 다룬다.
> 자원 자동 정리(try-with-resources)와, 기존 스트림을 감싸 기능을 더하는 보조 스트림(Buffered/Data)을 본다.

---

## 1. 학습 내용 — 자원 정리와 보조 스트림

### try-with-resources (Java 7+)
스트림/소켓 등은 OS 자원을 쓰므로 사용 후 반드시 `close`해야 한다.
- **로우레벨의 불편함**: 옛날엔 try 안에서 close를 호출했는데, try 도중 예외가 나면 close 줄에 도달
  못 해 자원이 샜다. 그래서 `finally`에서 close하는 장황한 코드(+ close 자체의 예외 처리)가 필수였다.
- **해결**: `try (자원 선언) { ... }` 형태로 쓰면, 블록을 벗어날 때(**정상이든 예외든**) 자동으로 close
  된다. 자원이 여러 개면 **선언의 역순**으로 닫는다(나중에 연 것을 먼저). 단 자원은 **AutoCloseable**을
  구현해야 한다(스트림류는 모두 구현돼 있음).

### 보조 스트림 = 기존 스트림을 감싸 기능을 더한다 (데코레이터)
보조 스트림은 단독으로 못 쓰고, 기존 스트림을 **감싸서(wrapping)** 기능을 추가한다. PART 12 AOP나
PART 4.2 I/O의 데코레이터 패턴과 같은 구조다.

- **BufferedInputStream / BufferedOutputStream**: 내부에 버퍼(기본 8KB)를 둔다. 기본 스트림은
  `write`마다 **OS 시스템 콜**이 일어나 1바이트씩 많이 쓰면 매우 느린데, Buffered는 버퍼에 모아
  두었다가 버퍼가 차거나 flush/close될 때 **한 번에** 내보내 시스템 콜 횟수를 확 줄인다 → 성능 ↑.
- **DataInputStream / DataOutputStream**: 기본 타입(int/double/boolean/String)을 **타입별 메서드**로
  저장/복원한다(`writeInt`/`readInt`, `writeUTF`/`readUTF` ...). CSV처럼 문자열로 바꿔 저장 후 파싱할
  필요가 없다. 단 **쓴 순서/타입 그대로** 읽어야 한다.

#### DataStream 안 쓸 때(수동 파싱) vs 쓸 때 — 코드로 비교
회원 정보(이름 String, 나이 int, 키 double, 활성여부 boolean)를 파일에 저장했다 읽는 상황을 보자.

**안 쓸 때 — 문자열(CSV)로 저장하고 직접 파싱 (불편)**
```java
// 저장: 모든 값을 문자열로 바꿔 콤마로 연결
String line = "홍길동" + "," + 30 + "," + 175.5 + "," + true;
Files.writeString(file, line);   // "홍길동,30,175.5,true"

// 읽기: 직접 쪼개고(split), 타입마다 일일이 변환(parse)해야 한다
String[] parts = Files.readString(file).split(",");
String name = parts[0];
int age = Integer.parseInt(parts[1]);       // 문자열 -> int (실패하면 NumberFormatException)
double height = Double.parseDouble(parts[2]); // 문자열 -> double
boolean active = Boolean.parseBoolean(parts[3]);
```
불편한 점:
- **타입마다 수동 변환**(`Integer.parseInt`/`Double.parseDouble`...)이 필요하고, 값이 이상하면 예외.
- **구분자 충돌**: 이름에 콤마(`,`)가 들어가면 split이 깨진다(`"홍,길동"` → 파싱 어긋남).
- **순서·개수 실수**에 취약: `parts[1]`/`parts[2]`를 헷갈리면 엉뚱한 값을 엉뚱한 타입으로 파싱.
- 숫자가 **문자열로 저장**되어 정밀도/포맷 문제(예: double 표기)나 용량 비효율이 생길 수 있다.

**쓸 때 — DataStream으로 타입 그대로 저장/복원 (편함)**
```java
// 저장: 타입별 메서드로 그대로 기록
try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
    out.writeUTF("홍길동");   // String
    out.writeInt(30);        // int
    out.writeDouble(175.5);  // double
    out.writeBoolean(true);  // boolean
}

// 읽기: '쓴 순서대로' 타입별 메서드로 바로 복원 (split도 parse도 불필요)
try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
    String name = in.readUTF();        // 바로 String
    int age = in.readInt();            // 바로 int
    double height = in.readDouble();   // 바로 double
    boolean active = in.readBoolean(); // 바로 boolean
}
```
편한 점:
- **파싱·타입 변환 불필요**: `readInt()`가 곧바로 int를 준다(NumberFormatException 걱정 없음).
- **구분자 문제 없음**: 콤마로 나누는 게 아니라 타입별 길이/포맷으로 저장되므로, 이름에 콤마가 있어도 안전.
- **타입이 코드로 명확**: `readInt`/`readDouble`처럼 읽는 타입이 메서드에 드러나 실수가 준다.
- 숫자가 **이진 형태**로 저장돼 정밀도/용량에 유리.

단점/주의: DataStream 파일은 **사람이 열어 읽기 어렵다**(바이너리). 또 **쓴 순서·타입과 정확히 같은
순서·타입으로** 읽어야 한다(`writeInt` 했으면 `readInt`). 사람이 읽어야 하거나 다른 시스템과 교환하는
포맷이 필요하면 CSV/JSON이 낫고, **자바끼리 기본 타입을 간편·정확히 주고받을 땐 DataStream이 편하다.**

---

## 1-B. 실전 — 웹 백엔드에서 파일을 저장/읽는 시스템은 어떻게 만드나

지금까지 배운 스트림이 실제 서비스에서 어떻게 쓰이는지 큰 그림을 본다. "사람이 파일을 직접 옮기는 게
아니라, 코드가 한다"는 게 핵심이다.

### ★ 먼저 오해 풀기 — "파일 저장에는 DataStream을 안 쓴다"
가장 헷갈리는 지점부터 짚자. **DataStream(writeInt/writeUTF...)은 파일 업로드/다운로드와 무관하다.**
둘은 상황이 완전히 다르다.

- **DataStream을 쓰는 경우** = *내가 데이터의 구조를 직접 설계*할 때. "이름(String) → 나이(int) →
  키(double) 순서로 기록한다"처럼 **내가 만든 데이터를 내가 정한 포맷으로** 쓰는 것이다. 그러니까
  `writeInt`/`writeUTF`로 타입을 하나하나 박는다.
- **업로드된 파일(jpg, pdf, zip, mp4...)을 저장하는 경우** = 그 파일은 **이미 자기만의 내부 포맷**을
  갖고 있다(JPEG 헤더, PDF 구조 등). 백엔드는 그 내용을 **해석하지도, 타입별로 쪼개지도 않는다.**
  그냥 **들어온 바이트를 그대로(raw) 복사해서** 저장할 뿐이다.

비유: 택배 회사(백엔드)는 상자(파일) 안에 뭐가 들었는지 안 뜯어본다. 그냥 **상자째 그대로** 창고에
옮긴다. 안의 내용물 구조(JPEG든 PDF든)는 그 파일을 만든 프로그램(카메라, 워드 등)의 일이지 저장하는
쪽의 일이 아니다.

**핵심: 파일은 결국 그냥 '바이트 덩어리(byte[])'다.** 그래서 저장은 타입 해석 없이 바이트를 그대로
읽어다(InputStream) 그대로 쓰면(OutputStream) 끝이다. "어떻게 타입별로 저장하지?"가 아니라
**"바이트를 있는 그대로 복사한다"** 가 정답이다.

```java
// 업로드된 jpg를 저장 — 내용 해석 X, 바이트를 '그대로' 복사할 뿐
byte[] raw = uploadedFile.getBytes();   // 파일 = 바이트 덩어리
Files.write(targetPath, raw);            // 그대로 디스크에 씀
// 끝. writeInt/writeUTF 같은 타입별 기록은 등장하지 않는다.
```

그럼 나중에 "이게 jpg인지 pdf인지"는 어떻게 아냐고? 그건 **파일 내용이 아니라 별도 메타데이터**
(원본 파일명·확장자·MIME 타입 `image/jpeg` 등)로 관리한다 — 보통 DB에 경로와 함께 적어둔다.
바이트 자체는 그냥 보관할 뿐, 해석은 그 파일을 쓸 프로그램(브라우저의 이미지 뷰어 등)이 한다.

### 전체 흐름 — 업로드(쓰기)와 다운로드(읽기)
```
[업로드] 브라우저 --HTTP(파일 바이트)--> 백엔드 --스트림으로 저장--> 저장소(디스크/클라우드/DB)
[다운로드] 브라우저 <--HTTP(파일 바이트)-- 백엔드 <--스트림으로 읽음-- 저장소
```
브라우저가 `multipart/form-data`로 파일 바이트를 HTTP 본문에 실어 보내면, 백엔드는 그 **InputStream을
받아** 저장소에 **OutputStream으로 쓴다.** 다운로드는 그 반대. 즉 6.3~6.5에서 배운 InputStream/
OutputStream(+버퍼)이 그대로 등장한다. 사람이 파일 탐색기로 옮기는 게 아니라 이 코드가 자동으로 한다.

### 핵심 패턴 — "입력 스트림에서 출력 스트림으로 복사"
파일 처리의 90%는 결국 **"한 스트림에서 읽어 다른 스트림으로 쓰는 복사"** 다. 직접 짜면:

```java
// 입력(업로드된 데이터) -> 출력(저장 위치)로 버퍼 복사 (6.5의 버퍼 개념 그대로)
try (InputStream in = uploadedFile.getInputStream();
     OutputStream out = new BufferedOutputStream(new FileOutputStream(targetPath))) {
    byte[] buffer = new byte[8192];   // 8KB 버퍼
    int n;
    while ((n = in.read(buffer)) != -1) {  // 읽은 만큼(n)만
        out.write(buffer, 0, n);           // 그만큼만 쓴다 (6.3의 버퍼 함정 회피)
    }
}
```

실무에서는 이 복사 루프를 매번 손으로 안 짜고 **헬퍼 한 줄**로 끝낸다(내부는 위와 같은 버퍼 복사다):

```java
// 자바 표준 (NIO.2) — 입력 스트림을 그대로 파일로 복사
Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

// 스트림끼리 복사 (Java 9+)
in.transferTo(out);
```

### Spring 백엔드에서의 모습 (가장 흔한 형태)
스프링에서는 업로드 파일이 `MultipartFile`로 들어오고, 저장은 보통 `transferTo`/`Files.copy`로 한다.
(스프링은 PART 8~ 에서 본격적으로 다루지만, 파일 처리 흐름만 미리 본다.)

```java
@PostMapping("/upload")
public String upload(@RequestParam("file") MultipartFile file) throws IOException {
    // 파일명에 경로 조작 문자가 없게 정리하고, 저장 경로를 정함
    Path target = Paths.get("/data/uploads").resolve(sanitize(file.getOriginalFilename()));
    // 업로드된 바이트를 그 경로에 저장 — 내부적으로 스트림 복사
    file.transferTo(target);          // 또는: Files.copy(file.getInputStream(), target)
    return "saved: " + target;
}

@GetMapping("/download/{name}")
public ResponseEntity<Resource> download(@PathVariable String name) {
    Path path = Paths.get("/data/uploads").resolve(sanitize(name));
    Resource resource = new FileSystemResource(path); // 파일을 스트림으로 흘려보낼 준비
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
            .body(resource); // 스프링이 파일 InputStream을 응답 OutputStream으로 복사해 전송
}
```

### 어디에 저장하나 — 3가지 선택지
1. **로컬/마운트 디스크**: 위 예시처럼 서버의 경로에 저장. 가장 단순하지만 서버가 여러 대면 파일 공유가
   어렵고, 서버가 죽으면 유실 위험.
2. **클라우드 오브젝트 스토리지(AWS S3 등)**: 실무에서 가장 흔하다. 백엔드는 받은 InputStream을 S3
   SDK로 올린다(`s3.putObject(bucket, key, inputStream, ...)`). 확장·내구성·CDN 연동에 유리.
3. **DB에 BLOB로 저장**: 작은 파일엔 가능하지만 큰 파일엔 DB 부담이 커 잘 안 쓴다. 보통은 **파일은
   스토리지에, DB에는 경로/URL·메타데이터(이름·크기·타입)만** 저장하는 방식이 정석이다.

### 대용량일 때 — "통째로 메모리에 올리지 말고 스트리밍"
`Files.readAllBytes()`로 큰 파일을 통째 메모리에 올리면 OutOfMemory 위험이 있다. 그래서 업/다운로드는
**버퍼로 조금씩 흘려보내는 스트리밍**(위 복사 루프/`transferTo`)으로 처리한다. 6.5의 "버퍼로 시스템 콜을
줄인다"가 여기서 성능과 직결된다.

### 실무 주의점 (보안·안정성)
- **경로 조작(Path Traversal) 방지**: 업로드 파일명에 `../`가 들어오면 의도치 않은 경로에 쓰일 수 있다.
  파일명을 정리(sanitize)하거나 서버가 생성한 안전한 이름(UUID 등)을 쓴다.
- **크기·확장자·MIME 검증**: 너무 큰 파일·허용 안 된 형식을 막는다.
- **try-with-resources로 스트림 닫기**(6.5): 안 닫으면 파일 핸들이 새어 결국 서버가 파일을 못 연다.
- **파일명 중복**: 같은 이름이 덮어써지지 않게 UUID/타임스탬프를 붙이는 경우가 많다.

핵심: 웹의 파일 시스템도 결국 **InputStream에서 OutputStream으로의 버퍼 복사**다(6.3~6.5 그대로).
사람이 옮기는 게 아니라, `MultipartFile.transferTo`/`Files.copy`/`transferTo` 같은 코드가 그 복사를 한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: try-with-resources는 자동 close하고, 여러 자원은 역순으로, 예외 시에도 닫는다.
> - **가설 2**: BufferedOutputStream은 시스템 콜을 줄여 unbuffered보다 훨씬 빠르다.
> - **가설 3**: DataStream은 기본 타입을 타입별로 저장/복원한다(파싱 불필요).

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_TryWithResources` | 자원 자동 정리? | AutoCloseable 2개 + 예외 케이스 |
| `Example2_BufferedStream` | Buffered가 왜 빠르나? | 1바이트씩 100만 번 쓰기 비교 |
| `Example3_DataStream` | 타입별 저장/읽기? | writeInt/Double/UTF round-trip |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s05_buffered_data.Example1_TryWithResources
java -cp build/classes/java/main com.study.part06_io.s05_buffered_data.Example2_BufferedStream
java -cp build/classes/java/main com.study.part06_io.s05_buffered_data.Example3_DataStream
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (try-with-resources)** — 가설 1.
- A, B 순서로 열면 close는 **B → A 역순**.
- try 안에서 예외가 나도 C가 **close됨**(catch 전에 close 호출) → 자원 누수 없음. ✅

**예시 2 (Buffered)** — 가설 2. (1바이트씩 100만 번 쓰기, 실측)

| | 시간 |
|---|---|
| FileOutputStream (버퍼 X) | 2622 ms |
| BufferedOutputStream (버퍼 O) | 37 ms |

→ 약 **70배** 빠름. 기본 스트림은 write마다 시스템 콜, Buffered는 8KB 버퍼에 모아 한 번에. ✅

**예시 3 (Data)** — 가설 3.
- `writeInt(42)/writeDouble(3.14)/writeBoolean(true)/writeUTF("홍길동")`로 저장 →
  `readInt=42, readDouble=3.14, readBoolean=true, readUTF="홍길동"`으로 타입 그대로 복원(파싱 없음). ✅

### 세 예시를 관통하는 결론
try-with-resources는 자원 정리를 자동화해(예외에도 안전, 역순 close) 옛 finally 보일러플레이트를
없앤다(예시1). 보조 스트림은 기존 스트림을 감싸 기능을 더하는데, Buffered는 시스템 콜을 줄여 성능을
끌어올리고(예시2 — 70배), Data는 기본 타입을 타입 안전하게 저장/복원해 파싱 수작업을 없앤다(예시3).
이 "감싸서 기능 추가"가 데코레이터 패턴이며, 같은 사고가 PART 12 Spring AOP로 이어진다.

---

## 3. 자기 점검

- **Q. try-with-resources가 옛 finally 방식보다 나은 점은?**
  - 내 답: 블록을 벗어날 때 자동 close(예외가 나도 호출됨), 여러 자원은 역순으로 안전하게 닫음.
    finally + close 예외 처리의 장황함을 없앤다. (Example1)

- **Q. BufferedStream이 빠른 이유는?**
  - 내 답: 기본 스트림은 write마다 OS 시스템 콜이 일어나는데, Buffered는 내부 버퍼(8KB)에 모아 한
    번에 내보내 시스템 콜 횟수를 줄이기 때문. (Example2의 70배)

- **Q. (적용) 정수 1만 개를 파일에 저장했다가 다시 읽으려면 CSV vs DataStream 중 무엇이 편한가?**
  - DataStream. writeInt를 1만 번 하고 readInt를 1만 번 하면 끝(파싱 불필요). CSV는 문자열로 바꿔
    저장 후 split + parseInt 해야 한다. 단 DataStream은 쓴 순서대로 읽어야 한다. (Example3)
