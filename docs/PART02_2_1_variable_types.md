# PART 2 — JVM 메모리 모델과 실행 원리: 2.1 자바 변수 3종류와 저장 위치

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.1 자바 변수 3종류와 저장 위치**를 다룬다.
> PART 2부터는 `javap`로 바이트코드를 직접 까보며 "저장 위치 차이"가 실제 명령어로 드러나는 것을 확인한다.

---

## 0. 들어가기 전에 — 핵심 용어
- **JVM(Java Virtual Machine)**: 자바 코드를 실행하는 '가상 컴퓨터'. 메모리를 영역별로 나눠 관리한다.
- **지역 변수(local variable)**: 메서드 안에서 선언한 변수. 메서드가 끝나면 사라진다. → **Stack**에 저장.
- **인스턴스 변수(instance variable, 필드)**: 객체(인스턴스)마다 하나씩 갖는 변수. 객체가 살아 있는 동안 유지. → **Heap**에 저장.
- **클래스 변수(static variable)**: `static` 붙은 변수. 클래스당 하나만 존재(모든 객체가 공유). → **Method Area**에 저장.
- **Stack / Heap / Method Area**: JVM 메모리의 세 영역(2.2에서 자세히). "변수가 어디 저장되나"의 답이 이 셋 중 하나다.
- **javap**: 컴파일된 `.class`의 바이트코드를 사람이 읽게 보여주는 도구. "저장 위치 차이"를 명령어로 확인할 때 쓴다.

한 줄 그림: **변수는 종류(지역/인스턴스/static)에 따라 저장 위치(Stack/Heap/Method Area)와 수명이 다르다.**

---

## 1. 학습 내용 — 변수는 어디에 저장되는가

자바의 변수는 **선언 위치와 종류에 따라 JVM 메모리의 서로 다른 영역**에 저장된다.
이 저장 위치 차이가 곧 변수의 "수명"과 "공유 여부"를 결정한다.

| 변수 종류 | 저장 영역 | 생성 시점 | 소멸 시점 | 공유 여부 |
|---|---|---|---|---|
| 지역 변수 (Local) | **Stack** | 메서드 호출 | 메서드 종료 | 호출(스택 프레임)마다 독립 |
| 인스턴스 변수 (Instance) | **Heap** (객체와 함께) | `new`로 객체 생성 | GC가 객체 회수 | 객체마다 독립 |
| 클래스 변수 (static) | **Method Area** | 클래스 로딩 | JVM 종료 | 클래스당 1개, 모든 객체 공유 |

### 지역 변수 (Stack)
메서드 안에서 선언한 변수. 메서드가 호출되면 **스택 프레임**이 생기고 그 안에 지역 변수가
자리 잡는다. 메서드가 끝나면 스택 프레임이 통째로 제거되며 지역 변수도 함께 사라진다.
그래서 같은 메서드를 여러 번 호출해도 이전 호출의 값이 남지 않는다(호출마다 새로 태어남).
**메서드 매개변수도 지역 변수에 속한다**(호출 시 생기고 종료 시 사라진다).

### 인스턴스 변수 (Heap)
`new`로 객체를 만들 때 그 객체와 함께 Heap에 생성된다. **객체마다 자기 전용 공간**을 가지므로,
같은 클래스에서 만든 객체라도 인스턴스 변수 값은 서로 독립적이다. 객체가 더 이상 참조되지 않으면
GC가 회수하면서 함께 사라진다(GC는 PART 3).

### 클래스 변수 (Method Area)
`static`으로 선언한 변수. 클래스가 로딩될 때 **Method Area에 클래스당 단 1개** 생성된다.
객체가 몇 개든 이 변수는 하나뿐이라 **모든 객체가 같은 것을 공유**한다. 그래서 "지금까지
만들어진 객체 수" 같은 전역 상태에 쓴다. **클래스 변수가 모든 객체에 공유되는 이유가 바로
"Method Area에 단 1개만 존재하기 때문"** 이다. 객체 없이 `클래스명.변수`로 접근할 수 있는 것도
객체가 아니라 클래스에 속하기 때문이다.

---

## 2. 실습으로 확인하기

> - **가설 1**: 지역 변수는 메서드 호출마다 새로 생겼다 사라진다(값이 누적되지 않는다).
> - **가설 2**: 인스턴스 변수는 객체마다 따로라서, 한 객체를 바꿔도 다른 객체는 그대로다.
> - **가설 3**: 클래스 변수는 1개뿐이라 모든 객체가 같은 값을 보고, 객체 없이도 접근된다.

### 모델 코드 (`com.study.part02_jvm.s01_variable_types`)
- `Counter` — 클래스 변수 `totalCreated`(static) + 인스턴스 변수 `instanceCount`를 함께 보유.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 변수 종류 / 저장 영역 | 시나리오 |
|---|---|---|
| `Example1_LocalVariable` | 지역 변수 / Stack | 같은 메서드 3번 호출 → 값 누적 여부 |
| `Example2_InstanceVariable` | 인스턴스 변수 / Heap | c1만 증가 → c2 영향 여부 |
| `Example3_StaticVariable` | 클래스 변수 / Method Area | 객체 3개 생성 → 공유 여부 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s01_variable_types.Example1_LocalVariable
java -cp build/classes/java/main com.study.part02_jvm.s01_variable_types.Example2_InstanceVariable
java -cp build/classes/java/main com.study.part02_jvm.s01_variable_types.Example3_StaticVariable
```

### 실행 결과 — 가설과 실제 비교

| 예시 | 결과 | 가설 부합 |
|---|---|---|
| 지역 변수 | `countLocal(1)` 3번 호출 → 매번 `1` (누적 안 됨) | ✅ 호출마다 새로 생성·소멸 |
| 인스턴스 변수 | c1=3, c2=0 (c1만 올렸는데 c2 영향 없음) | ✅ 객체마다 독립 |
| 클래스 변수 | 생성 전 0 → 3개 생성 후 c1·c2·`Counter.totalCreated` 모두 3 | ✅ 1개를 공유 |

### 터미널로 직접 확인 — `javap`로 바이트코드 들여다보기
저장 위치 차이는 단순히 "동작"만이 아니라 **바이트코드 명령어 자체**로도 구분된다.

프로젝트 루트에서 실행한다 (`-p`: private 포함, `-c`: 바이트코드 출력).
명령은 아래 한 줄만 복사해 붙여넣는다(설명용 주석 줄은 함께 붙여넣지 말 것).

```bash
javap -p -c build/classes/java/main/com/study/part02_jvm/s01_variable_types/Counter.class
```

`Counter()` 생성자 부분을 보면:

```
6: putfield      #7    // Field instanceCount:I   <- 인스턴스 변수는 putfield/getfield
9: getstatic     #13   // Field totalCreated:I    <- 클래스 변수는 getstatic/putstatic
14: putstatic    #13   // Field totalCreated:I
```

- 인스턴스 변수(`instanceCount`)는 **`getfield` / `putfield`** — "특정 객체(Heap)의 필드"를 읽고 쓴다.
- 클래스 변수(`totalCreated`)는 **`getstatic` / `putstatic`** — "클래스(Method Area)의 필드"를 읽고 쓴다.

즉 컴파일러가 변수 종류에 따라 **다른 명령어를 생성**한다. "instance는 Heap, static은 Method Area"라는
개념적 구분이 JVM 명령어 레벨에서 실제로 분리되어 있음을 눈으로 확인할 수 있다.
(`#7`, `#13` 같은 숫자는 상수 풀 인덱스 — 2.7에서 심화)

### 세 예시를 관통하는 결론
변수의 "저장 위치"는 단순한 분류가 아니라 **수명과 공유 범위를 결정하는 핵심**이다.
Stack(지역)은 호출 단위로 살고, Heap(인스턴스)은 객체 단위로 독립적이며, Method Area(클래스)는
프로그램 전체에서 하나로 공유된다. 이 차이가 javap의 명령어(getfield vs getstatic)로도 드러난다.
이 구분은 이후 PART 7 동시성("무엇이 공유되어 위험한가" = static/인스턴스 변수)과 직결된다.

---

## 3. 자기 점검

- **Q. 클래스 변수가 모든 객체에 공유되는 이유는?**
  - 내 답: Method Area에 클래스당 단 1개만 존재하기 때문. 객체가 몇 개든 그 변수는 하나뿐이라
    모두 같은 것을 본다. (Example3 + javap의 `putstatic`이 근거)

- **Q. (PART 7 예고) 세 변수 중 멀티스레드에서 "공유되어 위험한" 것은?**
  - 지역 변수는 스레드마다 별도 Stack에 있어 안전하고, 인스턴스/클래스 변수는 Heap/Method Area에
    있어 여러 스레드가 공유할 수 있다(동기화 필요). 왜 지역 변수가 스레드 안전한지 "Stack은
    스레드별"이라는 사실(2.3)과 연결해 미리 정리해본다.
