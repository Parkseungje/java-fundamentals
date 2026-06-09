# PART 6 — I/O와 직렬화: 6.3 바이트 스트림 + 한글 문제

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.3 바이트 스트림 + 한글 문제**를 다룬다.
> 바이트 단위 I/O의 동작과 한계(한글 깨짐), read()의 int 반환·EOF, 버퍼 읽기 함정·append를 본다.

---

## 1. 학습 내용 — 바이트 스트림의 동작과 함정

### 바이트 스트림과 한글
바이트 스트림(`InputStream`/`OutputStream`)은 이름 그대로 **바이트(byte) 단위**로만 다룬다.
- 영어 알파벳: UTF-8에서 1글자 = **1바이트** → 1바이트를 char로 바꿔도 맞다.
- 한글: UTF-8에서 1글자 = **3바이트** → 1바이트씩 끊어 char로 만들면 한 글자가 3개의 깨진 조각이 된다.

그래서 `System.in`(InputStream)을 `read()`로 1바이트씩 읽으면 영어는 되지만 **한글은 깨진다.**
문자를 제대로 다루려면 인코딩을 아는 **문자 스트림(Reader/Writer)** 이 필요하다(6.4).

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
