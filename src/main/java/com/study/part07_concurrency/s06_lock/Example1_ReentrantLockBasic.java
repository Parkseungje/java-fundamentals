package com.study.part07_concurrency.s06_lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 예시 1 / 3 — ReentrantLock 기본: "synchronized 대신 명시적 락. 단 unlock은 finally에서 필수."
 *
 * 이 예시가 답하려는 질문: synchronized 말고 ReentrantLock으로 임계 구역을 보호하려면? 주의점은?
 *
 * 왜 이 시나리오인가: ReentrantLock은 synchronized와 같은 효과(상호 배제로 원자성·가시성 보장)를
 * 주면서, synchronized가 못 하는 기능(타임아웃·인터럽트·공정성 — 예시3)을 추가로 제공하는 '명시적
 * 락'이다. 사용법은 lock()으로 얻고 unlock()으로 푼다. ★ 핵심 주의: synchronized는 블록을 벗어나면
 * 자동으로 락이 풀리지만, ReentrantLock은 내가 직접 unlock()을 호출해야 한다. 그래서 임계 구역에서
 * 예외가 나도 반드시 풀리도록 'try { ... } finally { unlock() }' 패턴을 써야 한다. finally를 안 쓰면
 * 예외 시 락이 안 풀려 다른 스레드가 영원히 못 들어가는 데드락이 된다.
 * synchronized와 동일하게 2스레드 count++를 보호해 유실이 없음을 확인한다.
 *
 * 예상 결과:
 *   - ReentrantLock으로 보호 -> 2스레드 x 100만 = 정확히 2,000,000 (유실 없음).
 * -> ReentrantLock은 synchronized와 같은 보호를 하되 직접 lock/unlock 한다. unlock은 반드시 finally에서.
 *    "Reentrant(재진입 가능)"는 같은 스레드가 이미 쥔 락을 다시 lock()해도 되는 것(카운트로 관리)을 뜻한다.
 */
public class Example1_ReentrantLockBasic {

    static class Counter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;

        void increment() {
            lock.lock();          // 락 획득
            try {
                count++;          // 임계 구역(보호 대상)
            } finally {
                lock.unlock();    // ★ 예외가 나도 풀리도록 반드시 finally에서 unlock
            }
        }

        int get() { return count; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] ReentrantLock 기본 (lock/unlock + try-finally)");
        System.out.println();

        Counter counter = new Counter();
        int iterations = 1_000_000;
        Runnable task = () -> { for (int i = 0; i < iterations; i++) counter.increment(); };

        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        int expected = 2 * iterations;
        System.out.println("기대값 = " + expected + ", 실제값 = " + counter.get()
                + (counter.get() == expected ? "  <- 정확! (ReentrantLock으로 보호)" : "  <- 유실"));

        System.out.println();
        System.out.println("=> ReentrantLock은 synchronized와 같은 보호를 하되 직접 lock()/unlock() 한다.");
        System.out.println("   ★ unlock()은 반드시 finally에서 — 안 그러면 예외 시 락이 안 풀려 데드락이 된다.");
        System.out.println("   추가로 타임아웃·인터럽트·공정성 같은 기능을 제공한다(예시2, 예시3).");
    }
}
