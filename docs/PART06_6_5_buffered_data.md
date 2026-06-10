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
