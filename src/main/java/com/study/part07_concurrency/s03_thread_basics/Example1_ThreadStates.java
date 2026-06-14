package com.study.part07_concurrency.s03_thread_basics;

/**
 * 예시 1 / 3 — 스레드 상태 전이: "NEW → RUNNABLE → TIMED_WAITING → TERMINATED."
 *
 * 이 예시가 답하려는 질문: 스레드는 만들어져서 끝날 때까지 어떤 상태들을 거치나?
 *
 * 왜 이 시나리오인가: 스레드는 생애 동안 여러 상태를 거치고, Thread.getState()로 확인할 수 있다.
 *   - NEW          : 생성됐지만 아직 start() 전
 *   - RUNNABLE     : start() 후 실행 중(또는 실행 대기)
 *   - TIMED_WAITING: sleep(ms)/wait(ms)/join(ms)처럼 '시간 제한 대기' 중
 *   - WAITING      : wait()/join()처럼 '무기한 대기' 중 (곁가지)
 *   - BLOCKED      : 다른 스레드가 쥔 락을 기다리는 중 (곁가지, 7.5)
 *   - TERMINATED   : 실행이 끝남
 * worker 스레드가 잠깐 CPU 작업(RUNNABLE) 후 sleep(TIMED_WAITING)하도록 만들고, main이 시점을 달리해
 * getState()를 찍어 상태가 바뀌는 것을 관찰한다.
 *
 * 예상 결과:
 *   - start() 전: NEW
 *   - 실행 직후 샘플: RUNNABLE (CPU 작업 중) — 타이밍에 따라 다를 수 있음
 *   - sleep 중 샘플: TIMED_WAITING
 *   - join 후: TERMINATED
 * -> 스레드는 NEW로 태어나 RUNNABLE로 실행되고, 대기(TIMED_WAITING/WAITING/BLOCKED)를 거쳐
 *    TERMINATED로 끝난다. (상태 전이는 타이밍에 의존하므로 샘플 시점에 따라 조금 다를 수 있다.)
 */
public class Example1_ThreadStates {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] 스레드 상태 전이");
        System.out.println();

        // 주의: 아래 'worker'(변수 이름)와 "worker-thread"(스레드의 런타임 이름)는 서로 다른 것이다.
        //   - worker        : 자바 변수 이름. 내 코드가 이 Thread 객체를 가리킬 때 쓴다(worker.start() 등). 컴파일 타임.
        //   - "worker-thread": 스레드의 '이름'. 실행 중 Thread.currentThread().getName()이나 스레드 덤프에 찍힌다. 런타임.
        //   둘은 독립적이라 우연히 같은 단어로 둘 필요가 없다(여기선 구분되게 일부러 다르게 지었다).
        Thread worker = new Thread(() -> {
            long end = System.currentTimeMillis() + 100;
            while (System.currentTimeMillis() < end) { /* CPU 작업(RUNNABLE) */ }
            try { Thread.sleep(300); } catch (InterruptedException ignored) {} // TIMED_WAITING
        }, "worker-thread");

        System.out.println("start() 전        : " + worker.getState() + "   (생성만 됨)");

        worker.start();

        // ★ 주의: 아래 Thread.sleep은 'worker'가 아니라 '지금 이 코드를 실행하는 main'이 잔다.
        //   - Thread.sleep(...)은 'Thread 클래스의 static 메서드'다. 특정 객체(worker)를 재우는 게 아니라,
        //     '이 줄을 실행 중인 스레드 자신'을 재운다. 코드에 "main"이라고 안 적혀 있어도, 이 줄을 main이
        //     실행하므로 결과적으로 main이 잔다. (worker 람다 안의 Thread.sleep(300)은 worker가 실행하니 worker가 잔다.)
        //   - 즉 worker를 멈추는 게 아니라, main이 "언제 worker 상태를 관찰할지" 시점을 미루는 타이머일 뿐이다.
        //   worker의 자체 일정(start 기준):  0~100ms = while 루프(RUNNABLE),  100~400ms = sleep(300)(TIMED_WAITING).
        //   그래서 main이 관찰하는 두 시점에 worker가 '하고 있는 일'이 달라 상태가 다르게 찍힌다.

        Thread.sleep(30);  // main이 30ms 잠 -> start 후 약 30ms 시점에 관찰: worker는 아직 while 루프 안(RUNNABLE)
        System.out.println("실행 직후 샘플    : " + worker.getState() + " (실행 중)");

        Thread.sleep(200); // main이 200ms 더 잠 -> 누적 약 230ms 시점에 관찰: worker는 루프(100ms)를 끝내고 sleep(300) 중
        System.out.println("sleep 중 샘플     : " + worker.getState() + "  (시간 제한 대기)");

        worker.join();     // worker 끝날 때까지 대기
        System.out.println("join 후           : " + worker.getState() + "  (실행 종료)");

        System.out.println();
        System.out.println("=> NEW -> RUNNABLE -> (TIMED_WAITING 등 대기) -> TERMINATED 로 상태가 전이된다.");
        System.out.println("   getState()로 확인. 대기 종류: TIMED_WAITING(sleep/시간제한), WAITING(무기한), BLOCKED(락 대기).");
    }
}
