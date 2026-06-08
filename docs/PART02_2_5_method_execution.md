# PART 2 — JVM 메모리 모델과 실행 원리: 2.5 메서드 실행 메커니즘

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.5 메서드 실행 메커니즘**을 다룬다.
> 2.2에서 본 Method Area의 존 구조 위에서, "메서드 호출 한 줄"이 어떻게 실행되는지 본다.

---

## 1. 학습 내용 — 메서드 호출의 2단계와 "무엇을 vs 어디에"

### 메서드 호출의 2단계
JVM이 메서드 호출을 처리하는 과정은 크게 두 단계다.

1. **"무엇을 호출하나"** — 컴파일러가 Class Metadata Zone(2.2)의 정보를 바탕으로 메서드
   시그니처를 확정해 상수 풀에 기록한다. (어떤 이름·매개변수·반환타입의 메서드인지)
2. **"실제 코드는 어디"** — JVM이 Static/Non-Static Zone에서 실제 바이트코드를 찾아 실행한다.

핵심은 이 둘이 **분리**되어 있다는 점이다. "무엇을 호출할지"를 정하는 시점과 "실제 어느 코드를
실행할지"를 정하는 시점이 다를 수 있고, 바로 이 분리가 **다형성/오버라이딩**을 가능하게 한다.

### "무엇을(컴파일 시점)" vs "어디에(런타임)" — 오버로딩 vs 오버라이딩
이 분리를 가장 선명하게 보여주는 것이 오버로딩과 오버라이딩의 차이다.

- **오버로딩(overloading) = "무엇을" = 컴파일 시점 결정**: 같은 이름의 메서드가 여러 개일 때,
  어느 것을 부를지는 **인자의 정적 타입(변수 선언 타입)** 으로 컴파일 시점에 정해진다.
  실제 객체가 무엇이든 상관없다.
- **오버라이딩(overriding) = "어디에" = 런타임 결정**: 부모 타입 변수로 호출해도, 실제로 실행되는
  코드는 **런타임의 실제 객체 타입**으로 정해진다.

그래서 같은 Dog 객체라도, 오버로딩 선택은 변수 타입에 따라 갈리고, 오버라이딩된 메서드는 항상
실제 객체(Dog)의 것이 실행된다.

### 호출 경로 (2.2 존 구조와 연결)
- **static ↔ static**: Static Zone 내부에서 직접 호출.
- **static → 인스턴스**: this가 없으므로 객체(Heap)를 만들어 그 객체를 통해 호출(2.2에서 확인).
- **인스턴스끼리**: 같은 객체 안에서 자유롭게 호출.

### 바이트코드 레벨 — invoke 명령
"메서드 실행 메커니즘"은 추상적 개념이 아니라 JVM 명령으로 구현돼 있다. 호출 종류마다 javac가
다른 invoke 명령을 생성한다.

| invoke 명령 | 언제 |
|---|---|
| `invokestatic` | static 메서드 호출 |
| `invokespecial` | 생성자, private, super 호출 |
| `invokevirtual` | 일반 인스턴스 메서드 (오버라이딩 → 런타임에 실제 코드 결정 = "어디에") |
| `invokeinterface` | 인터페이스 참조를 통한 호출 |

`invokevirtual`이 "런타임에 실제 객체의 코드를 찾는" 동적 디스패치를 담당한다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 오버라이딩은 런타임에 실제 객체로 결정된다(부모 타입으로 불러도 자식 코드 실행).
> - **가설 2**: 오버로딩은 컴파일 시점에 변수의 정적 타입으로 결정된다(실제 객체와 무관).
> - **가설 3**: 호출 종류별로 javac가 다른 invoke 명령을 생성한다(javap로 확인).

### 모델 코드 (`com.study.part02_jvm.s05_method_execution`)
- `Animal`/`Dog` — `sound()` 오버라이딩. `SoundDescriber` — `describe(Animal)`/`describe(Dog)` 오버로딩.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_OverridingRuntime` | 오버라이딩은 언제 결정? | `Animal a = new Dog(); a.sound()` |
| `Example2_OverloadingCompileTime` | 오버로딩은 언제 결정? | 같은 Dog를 Animal/Dog 변수로 `describe()` |
| `Example3_InvokeInstructions` | invoke 명령은? | static/생성자/인스턴스/인터페이스 호출 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s05_method_execution.Example1_OverridingRuntime
java -cp build/classes/java/main com.study.part02_jvm.s05_method_execution.Example2_OverloadingCompileTime
java -cp build/classes/java/main com.study.part02_jvm.s05_method_execution.Example3_InvokeInstructions
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (오버라이딩 = 런타임)** — 가설 1.

| 호출 | 결과 | 의미 |
|---|---|---|
| `Animal a = new Dog(); a.sound()` | 멍멍 | 변수 타입 Animal이 아니라 실제 객체 Dog의 코드 실행 |

→ "어디에(실제 코드)"는 런타임 실제 객체로 결정된다. ✅

**예시 2 (오버로딩 = 컴파일 시점)** — 가설 2. (같은 Dog 객체를 변수 타입만 다르게)

| 호출 | 선택된 오버로드 | `sound()` 결과 |
|---|---|---|
| `describe(a)` (정적 타입 Animal) | `describe(Animal)` | 멍멍 |
| `describe(d)` (정적 타입 Dog) | `describe(Dog)` | 멍멍 |

→ 같은 객체인데 describe는 **변수 타입**에 따라 갈린다(오버로딩=컴파일 시점). 반면 그 안의
`sound()`는 항상 Dog 것(오버라이딩=런타임). "무엇을"과 "어디에"가 분리돼 있다는 직접 증거. ✅

**예시 3 (invoke 명령)** — 가설 3. `javap`로 main 바이트코드 확인:

프로젝트 루트에서 실행 (`-p`: private 포함, `-c`: 바이트코드):

```bash
javap "-J-Dstdout.encoding=UTF-8" -p -c build/classes/java/main/com/study/part02_jvm/s05_method_execution/Example3_InvokeInstructions.class
```

> **⚠️ 한글 깨짐 방지** — javap는 클래스의 문자열 상수(한글 포함)를 올바르게 읽지만, 출력 인코딩이
> 터미널과 어긋나면 한글이 깨진다. 두 가지가 맞아야 한다: **① javap가 UTF-8로 내보내기**(JDK 18+ &
> `-Dstdout.encoding=UTF-8`), **② 터미널이 UTF-8로 디코딩**. 환경별로 정리하면:
>
> **공통 규칙**
> - `-J` 플래그는 **반드시 따옴표**로 묶는다 — `"-J-Dstdout.encoding=UTF-8"`. 안 묶으면 셸(특히
>   PowerShell)이 `.` 앞에서 인자를 쪼개 `-p -c`까지 클래스명으로 오인한다(바이트코드가 안 나옴).
> - `stdout.encoding`은 **JDK 18+ 전용**. 회사 PATH 기본이 JDK 17이면 무시되므로, 이 프로젝트 빌드용
>   JDK 21 javap를 써야 한다. `javap -version`으로 먼저 확인.
>
> **🏢 회사 — IntelliJ 내장 터미널(PowerShell) + PATH 기본 JDK 17**
> PowerShell은 외부 프로그램 출력을 `[Console]::OutputEncoding`(한국어 Windows 기본 MS949)으로
> 디코딩하므로, 그것도 UTF-8로 바꿔야 한다. 세 줄(앞 두 줄은 세션당 1회):
> ```powershell
> $env:PATH = "C:\Users\a0108\.jdks\dragonwell-21.0.11\bin;$env:PATH"   # javap를 21로
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8              # PowerShell 디코딩을 UTF-8로
> javap "-J-Dstdout.encoding=UTF-8" -p -c build\classes\java\main\com\study\part02_jvm\s05_method_execution\Example3_InvokeInstructions.class
> ```
> ※ `export`(bash)·`| cat`(=Get-Content)은 PowerShell에서 안 먹으니 쓰지 말 것.
> ※ `[Console]::OutputEncoding` 한 줄을 `$PROFILE`에 넣으면 영구 적용된다.
>
> **🏠 집 — Git Bash 등 UTF-8 터미널 + PATH 기본 JDK 21**
> 터미널이 이미 UTF-8이면 따옴표만 붙이면 끝:
> ```bash
> javap "-J-Dstdout.encoding=UTF-8" -p -c build/classes/java/main/com/study/part02_jvm/s05_method_execution/Example3_InvokeInstructions.class
> ```
>
> **어느 환경이든 확실한 방법** — 파일로 빼서 에디터(UTF-8)로 본다(터미널 인코딩 무관):
> JDK 21 javap로 `... > build/javap_out.txt` 후 IntelliJ에서 열기 (`build/`는 gitignore라 커밋 안 됨).
>
> 같은 이유로 다른 단원의 `javap` 명령도 한글이 깨지면 동일하게 처리한다.

main 바이트코드에서 다음 명령들이 보인다:

```
invokestatic  #25   // Method staticGreeting:()...          (1) static 호출
invokespecial #37   // Method .../Dog."<init>":()V          (2) 생성자 호출
invokevirtual #40   // Method .../Animal.sound:()...        (3) 인스턴스(오버라이드) 호출
invokeinterface #49 // InterfaceMethod .../Speaker.speak    (4) 인터페이스 호출
```

→ 호출 종류마다 다른 invoke 명령이 생성됐다. 특히 `invokevirtual`이 오버라이딩(런타임 결정)을
담당한다. ✅
보너스: 문자열 연결("..." + 변수)이 `invokedynamic`(makeConcatWithConstants)로 컴파일된 것도 보인다
(Java 9+의 문자열 연결 최적화).

### 세 예시를 관통하는 결론
메서드 호출은 "무엇을 호출하나(컴파일 시점, 시그니처)"와 "실제 코드는 어디(런타임, 실제 객체)"의
두 단계로 분리되어 있다. 오버로딩은 전자(컴파일 시점·정적 타입), 오버라이딩은 후자(런타임·실제
객체)에 해당하며, 이 분리 덕분에 다형성이 성립한다. 그리고 그 메커니즘은 javap의 invoke 명령
(특히 invokevirtual)으로 실재함을 확인할 수 있다.

---

## 3. 자기 점검

- **Q. 오버로딩과 오버라이딩의 결정 시점이 어떻게 다른가?**
  - 내 답: 오버로딩은 컴파일 시점에 인자의 정적 타입(변수 선언 타입)으로 결정("무엇을"),
    오버라이딩은 런타임에 실제 객체 타입으로 결정("어디에"). (Example2가 둘을 한 화면에서 대비)

- **Q. `invokevirtual`이 다형성에서 하는 역할은?**
  - 내 답: 인스턴스 메서드 호출을 런타임에 실제 객체 타입으로 디스패치한다. 그래서 부모 타입으로
    호출해도 자식의 오버라이딩 코드가 실행된다. (Example3 javap의 invokevirtual)

- **Q. (추가 실험) `private` 메서드나 `super.sound()` 호출은 javap에서 어떤 명령으로 보일까?**
  - Dog에 private 메서드를 추가하거나 `super.sound()`를 호출하고 javap로 확인해본다.
    (둘 다 `invokespecial` — "재정의로 바뀌면 안 되는, 정적으로 결정되는 호출"이기 때문)
