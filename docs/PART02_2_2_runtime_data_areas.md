# PART 2 — JVM 메모리 모델과 실행 원리: 2.2 JVM 런타임 데이터 영역

> 이 문서는 커리큘럼 PART 2의 소단원 중 **2.2 JVM 런타임 데이터 영역**을 다룬다.
> 2.1(변수 저장 위치)에서 본 Stack/Heap/Method Area를, 이번에는 "영역 전체의 역할"과
> Method Area 내부의 3개 존까지 들여다본다.

---

## 1. 학습 내용 — JVM 메모리는 어떤 영역으로 나뉘는가

JVM은 실행 중 메모리를 용도별 영역으로 나눠 관리한다.

| 영역 | 무엇을 담나 | 스레드 |
|---|---|---|
| **Method Area** | 클래스 정보, static 변수, 상수 풀(Constant Pool), 메서드 바이트코드 | 공유 |
| **Heap** | 모든 객체(`new`로 만든 것) — GC 대상 | 공유 |
| **Stack** | 메서드 호출 프레임, 지역 변수 | 스레드별 |
| **PC Register** | 현재 실행 중인 명령의 위치 | 스레드별 |
| **Native Method Stack** | 네이티브(C 등) 메서드 호출용 | 스레드별 |

- **Heap**과 **Method Area**는 모든 스레드가 공유한다(그래서 동시성 문제의 무대 — PART 7).
- **Stack**은 스레드마다 따로 있다(그래서 지역 변수가 스레드 안전 — 2.1, 2.3).

### Method Area의 3개 존 (심화)
Method Area는 내부적으로 세 영역으로 나뉜다고 보면 이해가 쉽다.

```
Method Area
├─ Class Metadata Zone : 클래스명, 부모, 메서드 시그니처, 필드 선언 정보 ("무엇이 있는가")
├─ Static Zone         : static 메서드/필드의 실제 바이트코드·값
└─ Non-Static Zone     : 인스턴스 메서드의 실제 바이트코드
```

- **Class Metadata Zone**: "이 클래스에 무엇이 있는가"(설계 정보). 클래스당 1번만 로딩되어
  모든 객체가 공유한다. 그래서 객체 100만 개를 만들어도 클래스 메타데이터는 1벌뿐이다.
- **Static Zone**: static 메서드/필드. `main()`이 여기에 있어야 객체 없이 실행될 수 있다.
- **Non-Static Zone**: 인스턴스 메서드. 이 메서드는 "특정 객체의 행동"이라 `this`(객체)가 필요하다.

### 왜 static 메서드는 인스턴스 메서드를 직접 못 부르나
static 메서드는 객체 없이 호출되므로 `this`(어떤 객체)라는 정보가 없다. 반면 인스턴스 메서드는
반드시 "특정 객체의" 행동이라 그 객체가 정해져야 한다. 그래서 **Static Zone 메서드는 Non-Static
Zone 메서드를 직접 호출할 수 없고, 반드시 객체(Heap)를 만들어 그 객체를 통해야** 한다.

이것이 **`main()`이 static인 이유**이기도 하다. 프로그램 시작 시점에는 아직 아무 객체도 없으므로,
객체 없이 호출 가능한 static 메서드여야 진입점이 될 수 있다. main이 인스턴스 메서드였다면
"그 객체를 누가 먼저 만드는가?"라는 모순이 생긴다.

---

## 2. 실습으로 확인하기

> - **가설 1**: static 메서드는 인스턴스 메서드를 직접 못 부르고 객체를 거쳐야 한다(Static/Non-Static Zone 분리).
> - **가설 2**: `new` 객체는 Heap에 쌓여 사용량을 늘리고, 참조가 끊기면 GC로 회수된다.
> - **가설 3**: 클래스 정보는 Method Area에 1번만 로딩되어 모든 객체가 공유한다(getClass()가 동일).

### 모델 코드 (`com.study.part02_jvm.s02_runtime_data_areas`)
- `Robot` — static 필드/메서드 + 인스턴스 필드/메서드를 모두 보유. 세 영역 요소를 한 클래스에 담음.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 검증 영역 | 시나리오 |
|---|---|---|
| `Example1_StaticZoneVsNonStatic` | Static/Non-Static Zone | static에서 인스턴스 메서드 호출 시도 |
| `Example2_HeapGrowth` | Heap | 객체 100만 개 할당 전/후/GC 후 메모리 측정 |
| `Example3_MethodAreaClassInfo` | Method Area(Class Metadata) | 두 객체의 `getClass()` 비교 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part02_jvm.s02_runtime_data_areas.Example1_StaticZoneVsNonStatic
java -cp build/classes/java/main com.study.part02_jvm.s02_runtime_data_areas.Example2_HeapGrowth
java -cp build/classes/java/main com.study.part02_jvm.s02_runtime_data_areas.Example3_MethodAreaClassInfo
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (Static/Non-Static Zone)** — 가설 1.
- `Robot.factoryInfo()`(static)는 객체 없이 호출 성공.
- `Robot.staticCallsInstance()`는 내부에서 객체를 만들어서야 `greet()`(인스턴스) 호출 성공.
- 직접 `greet()` 호출은 컴파일 에러(주석 처리). → static은 this가 없어 인스턴스 메서드를 직접 못 부른다. ✅

**예시 2 (Heap)** — 가설 2. (수치는 실행/JVM마다 다름, 방향성에 주목)

| 시점 | 사용 힙 (예시 실행값) |
|---|---|
| 할당 전 | 2 MB |
| 객체 100만 개 할당 후 | 71 MB (크게 증가 — Heap에 쌓임) |
| 참조 해제 + `System.gc()` 후 | 1 MB (감소 — GC 회수) |

→ `new` 객체는 Heap에 저장되어 사용량을 늘리고, 참조가 끊기면 GC 대상이 된다. ✅
(`System.gc()`는 강제가 아닌 요청 — GC 메커니즘은 PART 3)

**예시 3 (Method Area)** — 가설 3.

| 비교 | 결과 | 의미 |
|---|---|---|
| `r1 == r2` | false | 객체는 Heap에 각각 따로 |
| `r1.getClass() == r2.getClass()` | true | 클래스 정보는 Method Area에 단 1개(공유) |

→ 객체는 객체마다 따로지만, 클래스 설계 정보는 한 번만 로딩되어 공유된다. ✅

### 터미널로 더 보기 (선택)
실행 중인 JVM의 메모리/클래스 로딩 상태는 JDK 도구로 관찰할 수 있다.
```bash
jps                        # 실행 중인 JVM의 PID 확인
jcmd <PID> GC.heap_info    # 힙 영역 상태
jcmd <PID> VM.class_stats  # 로딩된 클래스 통계 (Method Area 관련)
```
(Example2를 오래 도는 버전으로 바꿔 실행한 뒤 다른 터미널에서 관찰하면 좋다.)

### 세 예시를 관통하는 결론
JVM 메모리는 "무엇을, 얼마나 오래, 누구와 공유하느냐"에 따라 영역이 갈린다. Stack(스레드별·호출
단위), Heap(공유·객체 단위·GC 대상), Method Area(공유·클래스 단위). 특히 Method Area의
Static/Non-Static Zone 분리는 "static은 객체 없이, 인스턴스는 객체를 통해"라는 호출 규칙을
만들고, 이것이 main()이 static인 근본 이유다.

---

## 3. 자기 점검

- **Q. main()이 static이어야 하는 이유는?**
  - 내 답: 프로그램 시작 시점엔 객체가 하나도 없으므로, 객체 없이 호출 가능한 static 메서드여야
    JVM이 진입점으로 부를 수 있다. (Example1)

- **Q. Static Zone 메서드가 Non-Static Zone 메서드를 직접 호출 못 하는 이유는?**
  - 내 답: 인스턴스 메서드는 `this`(특정 객체)가 필요한데 static 컨텍스트엔 this가 없다. 그래서
    객체(Heap)를 만들어 그 객체를 통해야만 호출 가능. (Example1의 staticCallsInstance가 근거)

- **Q. (추가) 객체를 100만 개 만들어도 클래스 메타데이터가 1벌인 이유는?**
  - Class Metadata Zone(Method Area)에 클래스 정보가 1번만 로딩되기 때문. Example3의
    `getClass() ==` 결과와 연결해 정리하고, 이것이 메모리 효율에 어떤 의미인지 생각해본다.
