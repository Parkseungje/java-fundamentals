package com.study.part07_concurrency.s03_thread_basics;

/**
 * 예시 2 / 3 — Thread 상속 vs Runnable 구현 + start() vs run().
 *
 * 이 예시가 답하려는 질문: 스레드를 만드는 두 방식(Thread 상속 / Runnable 구현)의 차이는?
 * start()와 run()을 직접 부르는 건 어떻게 다른가? 왜 Runnable이 권장되나?
 *
 * 왜 이 시나리오인가:
 *   - 스레드를 만드는 방법 두 가지: ① Thread를 상속해 run() 오버라이드 ② Runnable을 구현(람다 가능).
 *   - ★ start() vs run(): start()는 '새 스레드'를 만들어 그 위에서 run()을 실행한다. 반면 run()을
 *     '직접' 호출하면 새 스레드가 안 생기고 '지금 스레드(main)에서 그냥 메서드 호출'일 뿐이다.
 *     이를 실행 스레드 이름으로 구분한다(직접 호출=main, start()=새 스레드 이름).
 *   - Runnable 권장 이유 3가지: ① 자바는 단일 상속이라 Thread를 상속하면 다른 클래스를 못 상속한다
 *     (Runnable은 인터페이스라 자유롭다) ② '작업(Runnable)'과 '실행 수단(Thread)'을 분리한다
 *     ③ 같은 Runnable을 여러 스레드가 공유할 수 있다(메모리 효율). 람다로 간단히 만들 수 있다.
 *
 * 예상 결과:
 *   - task.run() 직접 호출 -> 실행 스레드 = main (새 스레드 아님)
 *   - new Thread(task).start() -> 실행 스레드 = 새 스레드 이름 (진짜 별도 스레드)
 * -> start()만이 새 스레드를 만든다. run() 직접 호출은 평범한 메서드 호출일 뿐이다(초보자 흔한 실수).
 *    스레드 생성은 Runnable(인터페이스/람다) 방식이 권장된다.
 */
public class Example2_ThreadVsRunnable {

    // 방법 ① Thread 상속 (단일 상속을 써버림 -> 권장되지 않음)
    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("  [Thread 상속] 실행 스레드 = " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] Thread 상속 vs Runnable + start() vs run()");
        System.out.println();

        // 방법 ② Runnable 구현 (람다) — 권장
        Runnable task = () -> System.out.println("  [Runnable] 실행 스레드 = " + Thread.currentThread().getName());

        // ★ run() 직접 호출: 새 스레드 안 생김, 지금 스레드(main)에서 그냥 메서드 호출
        System.out.println("(A) task.run() 직접 호출:");
        task.run();
        System.out.println("    -> 위 실행 스레드가 'main'이면, 새 스레드가 아니라 그냥 메서드 호출이었다는 뜻");

        System.out.println();

        // ★ start(): 새 스레드 생성 -> 그 위에서 run() 실행
        // 아래 (B)와 (C)는 둘 다 '새 스레드를 만들어 run()을 실행'한다(결과 비슷). 차이는 '할 일을 어디에 두느냐'다.
        //   (B) Runnable 주입: 할 일(task)은 Thread '밖'에 따로 있고, Thread에 생성자로 건네준다.
        //       -> Thread는 평범한 Thread, 작업은 별도 Runnable. 역할 분리(스레드 + 할 일 = 2개).
        //   (C) Thread 상속: 할 일(run)을 Thread 클래스 '안'에 직접 넣었다(MyThread가 run을 오버라이드).
        //       -> MyThread 객체가 '스레드이자 할 일'을 한 몸에 합친 것.
        // 비유: (B)는 빈 일꾼(Thread)에게 업무 지시서(Runnable)를 건네는 것, (C)는 일까지 직접 하는 만능 직원.
        System.out.println("(B) new Thread(task).start()  [Runnable 주입 — 할 일이 Thread 밖]:");
        Thread t1 = new Thread(task, "runnable-thread"); // task(할 일)를 외부에서 주입
        t1.start();
        t1.join();

        System.out.println();
        System.out.println("(C) Thread 상속 방식 start()  [할 일이 Thread 안]:");
        Thread t2 = new MyThread();      // 할 일(run)이 MyThread 안에 내장됨
        t2.setName("subclass-thread");
        t2.start();
        t2.join();

        System.out.println();
        System.out.println("=> start()만 새 스레드를 만든다. run() 직접 호출은 그냥 메서드 호출(main에서 실행).");
        System.out.println("   Runnable 권장: ①단일 상속 제약 회피 ②작업/스레드 분리 ③Runnable 공유(메모리 효율).");
    }
}
