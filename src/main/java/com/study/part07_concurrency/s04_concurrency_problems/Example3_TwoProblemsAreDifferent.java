package com.study.part07_concurrency.s04_concurrency_problems;

/**
 * 예시 3 / 3 — 두 문제는 별개다: "volatile은 가시성만 해결, 원자성은 못 해결."
 *
 * 이 예시가 답하려는 질문: 가시성과 원자성은 같은 문제인가? volatile을 붙이면 둘 다 해결되나?
 *
 * 왜 이 시나리오인가: 두 문제는 별개이고, 해결 도구도 다르다. 이를 한 화면에서 보여준다.
 *   (A) volatile boolean flag: volatile은 "항상 메인 메모리에서 읽고 쓰게" 해 가시성을 보장한다.
 *       그래서 예시1에서 멈추지 않던 worker가, flag를 volatile로 바꾸면 변경을 보고 즉시 멈춘다.
 *       => volatile이 '가시성'을 해결.
 *   (B) volatile int count++: 그런데 같은 volatile을 카운터에 써도, count++의 '원자성'은 해결되지
 *       않는다. volatile은 읽기/쓰기 각각을 메인 메모리와 동기화할 뿐, "읽기-증가-쓰기" 3단계를
 *       하나로 묶어주지는 않기 때문이다. 그래서 volatile int여도 동시 증가는 여전히 유실된다.
 *       => volatile은 '원자성'을 해결하지 못한다.
 *
 * 예상 결과:
 *   - (A) volatile flag: worker가 flag 변경을 보고 빠르게 종료(가시성 해결, 무한 루프 아님).
 *   - (B) volatile count: 기대값보다 작게 나옴(원자성은 여전히 미해결).
 * -> 가시성과 원자성은 다른 문제다. volatile은 가시성만 해결한다. 원자성까지 필요하면
 *    synchronized나 Atomic(CAS)을 써야 한다 -> 7.5에서 세 도구를 비교한다.
 */
public class Example3_TwoProblemsAreDifferent {

    static volatile boolean running = true; // volatile -> 가시성 보장
    static volatile int count = 0;           // volatile -> 가시성은 되지만 원자성은 안 됨

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 가시성 vs 원자성은 별개 — volatile은 가시성만 해결");
        System.out.println();

        // (A) volatile boolean: 가시성 해결 -> worker가 변경을 보고 멈춘다
        System.out.println("(A) volatile boolean flag -> 가시성 해결:");
        Thread worker = new Thread(() -> {
            long c = 0;
            while (running) { c++; }  // volatile이라 변경을 즉시 본다
            System.out.println("    [worker] running=false를 보고 멈춤! (가시성 해결, 무한 루프 아님)");
        });
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(100);
        running = false;
        worker.join(2000);
        System.out.println("    2초 뒤 worker 살아있음? " + worker.isAlive() + " (false면 변경을 봐서 정상 종료)");

        System.out.println();

        // (B) volatile int count++: 원자성은 여전히 미해결 -> 유실
        System.out.println("(B) volatile int count++ -> 원자성은 미해결:");
        int iterations = 1_000_000;
        Runnable task = () -> { for (int i = 0; i < iterations; i++) count++; };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        int expected = 2 * iterations;
        System.out.println("    기대값 = " + expected + ", 실제값 = " + count
                + (count == expected ? " (우연히 일치)" : "  <- 유실! (volatile은 원자성을 못 해결)"));

        System.out.println();
        System.out.println("=> 가시성과 원자성은 별개 문제. volatile은 가시성만 해결하고 원자성은 못 해결한다.");
        System.out.println("   원자성까지 필요하면 synchronized 또는 Atomic(CAS)을 써야 한다(7.5).");
    }
}
