# PART 7 — 멀티스레딩과 동시성: 7.3 스레드 만들고 다루기

> 이 문서는 커리큘럼 PART 7의 소단원 중 **7.3 스레드 만들고 다루기**를 다룬다.
> 스레드 상태, 생성 방법(Thread vs Runnable), start()/run() 차이, 데몬, join을 본다.

---

## 1. 학습 내용 — 스레드 생애와 다루는 법

### 스레드 상태 (getState())
- **NEW**: 생성됐지만 아직 `start()` 전.
- **RUNNABLE**: `start()` 후 실행 중(또는 실행 대기).
- **BLOCKED**: 다른 스레드가 쥔 락(모니터)을 기다리는 중 (7.5).
- **WAITING**: `wait()`/`join()`처럼 **무기한** 대기.
- **TIMED_WAITING**: `sleep(ms)`/`wait(ms)`/`join(ms)`처럼 **시간 제한** 대기.
- **TERMINATED**: 실행 종료.

전형적 흐름: `NEW → RUNNABLE → (대기 상태들) → TERMINATED`.

> ★ 헷갈리기 쉬운 점 — `Thread.sleep()`은 '누가' 자나? **그 줄을 실행 중인 스레드 자신**이 잔다.
> `Thread.sleep(...)`은 `Thread` 클래스의 **static 메서드**라, 특정 스레드 객체를 재우는 게 아니라
> "지금 이 코드를 실행하는 현재 스레드"를 재운다. 코드에 "main"이라고 안 적혀 있어도, main 메서드에서
> 부르면 **main이** 자고, worker의 작업(run/람다) 안에서 부르면 **worker가** 잔다. 같은 `Thread.sleep`
> 줄이라도 누가 실행하느냐로 자는 주체가 갈린다. (그래서 main에서 `Thread.sleep`을 불러도 다른 스레드
> worker는 안 멈춘다 — 7.2의 "제어권은 호출당한 함수/현재 스레드 기준"과 같은 맥락.)
> - 현재 스레드 이름이 궁금하면 `Thread.currentThread().getName()`으로 확인할 수 있다(보통 "main").

### 스레드 만드는 두 방법 — Thread 상속 vs Runnable 구현
- **Thread 상속**: `extends Thread` 후 `run()` 오버라이드. → 단일 상속을 써버려 권장 안 됨.
- **Runnable 구현**: `Runnable`(함수형 인터페이스, 람다 가능)을 만들어 `new Thread(runnable)`. → 권장.

**Runnable이 권장되는 이유 3가지**:
1. **단일 상속 제약 회피** — Thread를 상속하면 다른 클래스를 못 상속한다. Runnable은 인터페이스라 자유롭다.
2. **작업과 실행 수단의 분리** — '무엇을 할지(Runnable)'와 '어떻게 실행할지(Thread/Executor)'를 분리.
3. **메모리 효율** — 같은 Runnable을 여러 스레드가 공유할 수 있다.

#### 핵심 차이 — "할 일(run의 내용)을 어디에 두느냐"
둘 다 `start()`하면 새 스레드가 `run()`을 실행한다(결과는 비슷). 다른 건 **할 일을 어디에 두느냐**다.
- **Thread 상속**: 할 일(run)을 **Thread 클래스 '안'에** 직접 넣는다(오버라이드). 그래서 객체 하나가
  '스레드이자 할 일'을 겸한다. `Thread t = new MyThread();`
- **Runnable 주입**: 할 일(task)을 **Thread '밖'에** 따로 두고, 생성자로 건네준다. Thread는 평범한
  Thread이고 작업은 별도 Runnable이다. `Thread t = new Thread(task);`

| | Thread 상속 (`new MyThread()`) | Runnable 주입 (`new Thread(task)`) |
|---|---|---|
| 할 일 위치 | Thread **안**(run 오버라이드) | Thread **밖**(별도 Runnable) |
| 구성 | 스레드+할 일 한 몸 | 스레드 / 할 일 분리(2개) |
| 다른 클래스 상속 | ❌ (단일 상속을 Thread에 써버림) | ✅ (Runnable은 인터페이스) |
| 작업 재사용 | ❌ | ✅ 같은 task를 여러 Thread·ExecutorService에 넘김 |

비유: Runnable 주입 = **빈 일꾼(Thread)에게 업무 지시서(Runnable)를 건넴**. Thread 상속 = **일까지
직접 하는 만능 직원**(일을 바꾸려면 직원을 새로 만들어야 함). 그래서 유연한 Runnable(+람다/Executor)을 권장한다.

### ★ start() vs run() (초보자 흔한 실수)
- **`start()`**: **새 스레드**를 만들어 그 위에서 `run()`을 실행한다.
- **`run()` 직접 호출**: 새 스레드가 **안 생기고**, 그냥 **현재 스레드(main)에서 메서드 호출**일 뿐이다.

→ 멀티스레드로 돌리려면 반드시 `start()`. `run()`을 직접 부르면 동시 실행이 아니다.

### 데몬 스레드와 join()
- **join()**: "대상 스레드가 끝날 때까지 현재 스레드를 대기"시킨다. `worker.join()` → worker가 끝나야 다음 줄 실행.
- **데몬 스레드(daemon)**: '보조' 스레드. JVM은 **일반(user) 스레드가 모두 끝나면** 데몬이 돌고 있어도
  기다리지 않고 종료한다(데몬도 같이 죽음). 그래서 데몬은 **작업 완료가 보장되지 않는다.** 끝까지 마쳐야
  하는 작업은 데몬으로 만들면 안 된다. (`setDaemon(true)`는 `start()` 전에 호출)

---

## 2. 실습으로 확인하기

> - **가설 1**: 스레드는 NEW → RUNNABLE → TIMED_WAITING → TERMINATED로 상태가 전이된다.
> - **가설 2**: start()는 새 스레드, run() 직접 호출은 현재 스레드(main)에서 실행.
> - **가설 3**: join은 대상이 끝날 때까지 대기. 데몬은 일반 스레드가 끝나면 작업을 못 마치고 죽는다.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_ThreadStates` | 상태 전이? | getState() 시점별 출력 |
| `Example2_ThreadVsRunnable` | start vs run, 권장? | 직접 호출 vs start() 스레드명 |
| `Example3_DaemonAndJoin` | join, 데몬? | join 대기 + 데몬 강제 종료 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part07_concurrency.s03_thread_basics.Example1_ThreadStates
java -cp build/classes/java/main com.study.part07_concurrency.s03_thread_basics.Example2_ThreadVsRunnable
java -cp build/classes/java/main com.study.part07_concurrency.s03_thread_basics.Example3_DaemonAndJoin
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (상태)** — 가설 1.
- start() 전: NEW / 실행 직후: RUNNABLE / sleep 중: TIMED_WAITING / join 후: TERMINATED. ✅
- (상태는 타이밍에 의존하므로 샘플 시점에 따라 조금 다를 수 있다.)

**예시 2 (start vs run)** — 가설 2.

| 호출 | 실행 스레드 |
|---|---|
| `task.run()` 직접 | **main** (새 스레드 아님) |
| `new Thread(task).start()` | runnable-thread (새 스레드) |
| `MyThread().start()` | subclass-thread (새 스레드) |

→ start()만 새 스레드. run() 직접 호출은 그냥 메서드 호출. ✅

**예시 3 (join/daemon)** — 가설 3.
- join: worker가 1,2,3을 다 찍을 때까지 main 대기 후 "완료 확인". ✅
- daemon: 10번 찍으려 했지만 main이 250ms 뒤 끝나자 **3번만 찍고 강제 종료**("완료!" 도달 못 함). ✅

### 세 예시를 관통하는 결론
스레드는 NEW에서 시작해 RUNNABLE·대기 상태를 거쳐 TERMINATED로 끝난다(예시1). 만들 땐 Runnable(인터페이스/
람다)이 권장되고, **반드시 start()로 새 스레드를 띄워야** 한다(run() 직접 호출은 동시 실행이 아님 — 예시2).
join은 완료를 기다리는 도구이고, 데몬은 일반 스레드가 끝나면 같이 죽는 보조 스레드라 완료 보장이 없다(예시3).
이 기본기 위에서 7.4(동시성 문제)와 7.8(Executor)로 나아간다.

---

## 3. 자기 점검

- **Q. start()와 run()을 직접 호출하는 것의 차이는?**
  - 내 답: start()는 새 스레드를 만들어 run()을 실행하고, run() 직접 호출은 새 스레드 없이 현재
    스레드에서 메서드처럼 실행된다. 동시 실행하려면 start(). (Example2의 실행 스레드 이름)

- **Q. Runnable 구현이 Thread 상속보다 권장되는 이유 3가지는?**
  - 내 답: ①단일 상속 제약 회피 ②작업(Runnable)과 실행 수단(Thread) 분리 ③Runnable 공유로 메모리 효율.

- **Q. 데몬 스레드를 쓰면 안 되는 경우는?**
  - 내 답: 끝까지 반드시 마쳐야 하는 작업(예: 파일 저장, 로그 flush). 일반 스레드가 끝나면 JVM이
    데몬을 작업 도중 죽여버리기 때문. (Example3의 daemon이 10을 못 채우고 종료)
