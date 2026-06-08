# PART 2 — JVM 메모리 모델과 실행 원리: 2.7 바이트코드와 상수 풀

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.7 바이트코드와 상수 풀**을 다룬다.
> (커리큘럼에서 "★ JVM 이해의 정점"으로 표시된 핵심 구간 / "9-섹션 깊이 학습" 권장)
> PART 2의 마지막 소단원. javap로 그동안 본 개념(객체 생성·메서드 호출·변수)이 바이트코드로 어떻게 실재하는지 종합한다.

---

## 1. 학습 내용 — 바이트코드, 상수 풀, 심볼 참조

### 바이트코드
`.java` 소스는 `javac`로 컴파일되면 `.class` 파일이 된다. 이 안에 들어 있는 것이 **바이트코드** —
JVM이 이해하는, 어셈블리 비슷한 명령어들이다. CPU가 아니라 JVM이 해석하므로 **플랫폼 독립적**이다
(같은 .class가 윈도/리눅스/맥 어디서든 동일하게 실행 = "Write Once, Run Anywhere"). 확인 도구는
`javap -c`(바이트코드)와 `javap -v`(상수 풀까지).

### 상수 풀 (Constant Pool)
컴파일 시점에 클래스 파일 내부에 만들어지는 표다. **문자열 상수, 클래스/필드/메서드 참조** 등을
담는다. 바이트코드에 자주 보이는 `#숫자`는 바로 이 **상수 풀의 인덱스**다. 예를 들어 `new #7`은
"상수 풀 7번 항목(어떤 클래스)을 가리킨다"는 뜻이다.

### 심볼 참조 (Symbolic Reference) — 정점 개념
- **로우레벨의 한계**: 컴파일 시점에는 클래스/메서드/필드의 **실제 메모리 주소를 알 수 없다.**
  아직 로딩조차 안 됐고, 주소는 런타임·플랫폼마다 다르다.
- **해결**: 주소 대신 **이름(심볼)** 으로 참조해 둔다. 예: `"java/io/PrintStream.println:(String)V"`.
  그리고 런타임에 그 심볼을 **실제 위치로 해석(resolve)** 한다.
- 그래서 `#숫자` → 상수 풀의 심볼 → 런타임에 실제 주소, 라는 2단계 참조가 된다. 주소가 아니라
  이름으로 묶여 있기 때문에 바이트코드가 특정 머신에 종속되지 않고 플랫폼 독립적일 수 있다.

### 객체 생성 바이트코드 (new / dup / invokespecial)
`new X(...)` 한 줄은 보통 세 명령으로 쪼개진다.
- `new #n` : Heap에 메모리 확보 + 아직 초기화 안 된 참조를 스택에 올림 (`#n` = 클래스 심볼)
- `dup` : 그 참조를 복제 (생성자 호출이 참조를 소비하므로, 호출 후에도 쓸 참조를 미리 복제)
- `invokespecial #m` : 생성자 `<init>` 호출 (`#m` = 생성자 심볼)

즉 "메모리 확보(new)"와 "생성자 호출(invokespecial)"이 **별개 명령**이다(2.6의 'new 단계'가 바이트코드로 분리).

### invoke 명령 구분 (2.5 복습)
`invokestatic`(static), `invokespecial`(생성자/private/super), `invokevirtual`(인스턴스 다형성),
`invokeinterface`(인터페이스).

---

## 2. 실습으로 확인하기

> - **가설 1**: `new X()` 한 줄은 바이트코드에서 new/dup/invokespecial로 나뉜다.
> - **가설 2**: 바이트코드의 `#숫자`는 상수 풀 인덱스이고, 상수 풀 항목은 이름(심볼) 참조다.
> - **가설 3**: 산술 연산은 피연산자 스택 기반 명령(iload/iadd 등)으로 컴파일된다.

### 모델/예시 코드 (`com.study.part02_jvm.s07_bytecode_constant_pool`)
- `Point` + `Example1_NewDupInvoke` — 객체 생성 바이트코드
- `Example2_ConstantPool` — 상수 풀·심볼 참조
- `Example3_StackArithmetic` — 스택 기반 산술

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s07_bytecode_constant_pool.Example1_NewDupInvoke
java -cp build/classes/java/main com.study.part02_jvm.s07_bytecode_constant_pool.Example2_ConstantPool
java -cp build/classes/java/main com.study.part02_jvm.s07_bytecode_constant_pool.Example3_StackArithmetic
```

### 터미널로 직접 확인 — javap

> **⚠️ 한글 깨짐 방지** — 아래 명령들은 출력에 한글(상수 풀의 String 등)이 섞여 있어 인코딩이
> 어긋나면 깨진다. 두 가지가 맞아야 한다: **javap가 UTF-8로 내보내기**(JDK 18+ &
> `"-J-Dstdout.encoding=UTF-8"`, 따옴표 필수) + **터미널이 UTF-8로 디코딩**. 환경별 명령:
>
> **🏢 회사 — IntelliJ 내장 터미널(PowerShell), PATH 기본 JDK 17**
> 앞 두 줄은 터미널 세션당 1회만 실행하면 된다(이후 모든 javap에 적용).
> ```powershell
> $env:PATH = "C:\Users\a0108\.jdks\dragonwell-21.0.11\bin;$env:PATH"
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> javap "-J-Dstdout.encoding=UTF-8" -v build\classes\java\main\com\study\part02_jvm\s07_bytecode_constant_pool\Example2_ConstantPool.class
> ```
> `export`(bash)·`| cat`(=Get-Content)은 PowerShell에서 안 되니 쓰지 말 것.
> `[Console]::OutputEncoding` 한 줄을 `$PROFILE`에 넣으면 영구 적용된다.
>
> **🏠 집 — Git Bash 등 UTF-8 터미널, PATH 기본 JDK 21**
> ```bash
> javap "-J-Dstdout.encoding=UTF-8" -v build/classes/java/main/com/study/part02_jvm/s07_bytecode_constant_pool/Example2_ConstantPool.class
> ```
> (위는 `-v` 예시. 아래 가설 1~3의 `-c` 명령도 동일하게 환경에 맞춰 실행한다.)
> 자세한 배경은 [2.5 method_execution 문서](./PART02_2_5_method_execution.md)의 동일 주의 박스 참고.

**가설 1: 객체 생성 (new/dup/invokespecial)** — `javap -c`로 createPoint() 보기:

```bash
javap "-J-Dstdout.encoding=UTF-8" -p -c build/classes/java/main/com/study/part02_jvm/s07_bytecode_constant_pool/Example1_NewDupInvoke.class
```

실제 출력(createPoint 부분):
```
static ...Point createPoint();
   0: new           #7    // class .../Point      <- 메모리 확보 + 참조 push
   3: dup                                          <- 참조 복제
   4: iconst_3                                     <- 인자 3 push
   5: iconst_4                                     <- 인자 4 push
   6: invokespecial #9    // Method .../Point."<init>":(II)V   <- 생성자 호출
   9: astore_0
  10: aload_0
  11: areturn
```
→ `new`(메모리)와 `invokespecial`(생성자)이 별개 명령, 그 사이 `dup`로 참조를 복제. ✅

**가설 2: 상수 풀·심볼 참조** — `javap -v`로 상수 풀 보기:

```bash
javap "-J-Dstdout.encoding=UTF-8" -v build/classes/java/main/com/study/part02_jvm/s07_bytecode_constant_pool/Example2_ConstantPool.class
```

실제 출력(상수 풀 일부):
```
#7  = Fieldref     #8.#9    // java/lang/System.out:Ljava/io/PrintStream;
#8  = Class        #10      // java/lang/System
#9  = NameAndType  #11:#12  // out:Ljava/io/PrintStream;
#10 = Utf8         java/lang/System
#13 = String       #14      // [예시 2] 상수 풀과 심볼 참조 (#숫자)
#15 = Methodref    #16.#17  // java/io/PrintStream.println:(Ljava/lang/String;)V
```
→ `#숫자`가 서로를 가리키며(`Fieldref → Class + NameAndType → Utf8`) 결국 **이름 문자열(Utf8)**
에 도달한다. 즉 주소가 아니라 "java/lang/System.out" 같은 **심볼(이름)** 로 참조한다. 이것이
심볼 참조이며, 런타임에 실제 주소로 해석된다. ✅

**가설 3: 스택 기반 산술** — `javap -c`로 add() 보기:

```bash
javap "-J-Dstdout.encoding=UTF-8" -p -c build/classes/java/main/com/study/part02_jvm/s07_bytecode_constant_pool/Example3_StackArithmetic.class
```

실제 출력(add 부분):
```
static int add(int, int);
   0: iload_0    // 인자 a를 스택에 push
   1: iload_1    // 인자 b를 스택에 push
   2: iadd       // 스택에서 둘을 pop -> 더함 -> 결과 push
   3: ireturn    // 스택 맨 위 값 반환
```
→ a + b가 "값을 스택에 올리고(iload) 연산이 꺼내 계산(iadd)"하는 스택 머신 방식으로 컴파일됐다.
CPU 레지스터에 의존하지 않아 플랫폼 독립적. ✅

### 실행 결과 요약
- Example1: `distanceFromOrigin = 5.0`
- Example2: `안녕하세요 JVM`
- Example3: `add(3,4)=7`, `compute()=14`

### 세 예시를 관통하는 결론
바이트코드는 JVM의 "기계어"이고, 그 안의 모든 참조는 주소가 아니라 **상수 풀의 심볼(이름)** 로
이뤄진다(예시2). 이 간접 참조 덕분에 같은 .class가 어디서든 동일하게 해석되고, 스택 기반 명령
(예시3)은 특정 CPU 레지스터에 종속되지 않아 플랫폼 독립성이 완성된다. 그리고 객체 생성·메서드
호출 같은 고수준 동작도 결국 new/dup/invoke* 같은 저수준 명령들의 조합(예시1)으로 풀린다 —
이것이 PART 2 전체("변수 저장 → 메모리 영역 → 스택 → pass by value → 메서드 실행 → new")가
바이트코드 한 곳으로 수렴하는 지점이다.

---

## 3. 자기 점검

- **Q. 바이트코드의 `#7` 같은 숫자는 무엇이며 왜 필요한가?**
  - 내 답: 상수 풀 인덱스. 컴파일 시점엔 실제 주소를 모르므로, 클래스/메서드/필드를 이름(심볼)으로
    상수 풀에 적어두고 #인덱스로 가리킨 뒤 런타임에 실제 주소로 해석(resolve)한다. (Example2 javap -v)

- **Q. `new`와 `invokespecial`이 별개 명령인 이유는?**
  - 내 답: 객체 생성은 "메모리 확보(new)"와 "생성자 호출(invokespecial)"이 분리된 단계이기
    때문(2.6). 그 사이 dup로 참조를 복제해, 생성자가 참조를 소비한 뒤에도 객체를 쓸 수 있게 한다.

- **Q. (정점 정리) 바이트코드가 플랫폼 독립적일 수 있는 두 가지 이유는?**
  - ① 참조를 실제 주소가 아니라 상수 풀의 심볼(이름)로 하기 때문(Example2),
    ② 스택 기반 명령이라 특정 CPU 레지스터 구조에 의존하지 않기 때문(Example3).
    이 둘을 묶어 "Write Once, Run Anywhere"의 바이트코드 레벨 근거로 정리해본다.
