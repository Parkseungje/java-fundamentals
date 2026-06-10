package com.study.part07_concurrency.s06_lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 예시 3 / 3 — ReentrantLock의 추가 기능: synchronized가 못 하는 것들.
 *
 * 이 예시가 답하려는 질문: ReentrantLock이 synchronized보다 나은 점(synchronized 한계 3가지의 보완)은?
 *
 * 왜 이 시나리오인가: 7.5에서 본 synchronized의 한계 3가지를 ReentrantLock이 보완한다.
 *   ① 무한 대기 -> tryLock(시간): 정해진 시간만 기다리고 못 얻으면 false(포기 가능).
 *   ② 인터럽트 불가 -> lockInterruptibly(): 락 대기 중에 인터럽트로 빠져나올 수 있음.
 *   ③ 공정성 보장 X -> new ReentrantLock(true): 먼저 기다린 스레드가 먼저 얻도록(FIFO) 보장.
 * 여기선 가장 대표적인 (A) tryLock(시간)으로 '못 얻으면 포기'를 보여주고, (B) 공정 락(fair)의 개념을
 * 출력으로 설명한다. (인터럽트 가능 락은 주석으로 정리)
 *
 * 예상 결과:
 *   - (A) 한 스레드가 락을 2초 쥐고 있는 동안, 다른 스레드가 tryLock(300ms)을 시도 -> 300ms 후 false
 *     ("락 못 얻어 포기"). synchronized였다면 풀릴 때까지 무한 대기였을 것.
 * -> ReentrantLock은 synchronized의 한계(무한 대기/인터럽트 불가/불공정)를 tryLock(시간)·
 *    lockInterruptibly()·fair 락으로 보완한다. 그래서 데드락 회피·응답성 있는 락 획득이 가능하다.
 */
public class Example3_ReentrantLockFeatures {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] ReentrantLock 추가 기능 (synchronized 한계 보완)");
        System.out.println();

        // (A) tryLock(시간): 무한 대기 대신 '정해진 시간만 시도 후 포기'
        System.out.println("(A) tryLock(시간) — 무한 대기 보완:");
        ReentrantLock lock = new ReentrantLock();

        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("    [holder] 락 획득, 2초간 점유");
                sleep(2000);
            } finally {
                lock.unlock();
                System.out.println("    [holder] 락 해제");
            }
        });
        holder.start();
        sleep(100); // holder가 먼저 락을 쥐도록

        Thread trier = new Thread(() -> {
            try {
                System.out.println("    [trier] tryLock(300ms) 시도...");
                boolean got = lock.tryLock(300, TimeUnit.MILLISECONDS);
                if (got) {
                    try { System.out.println("    [trier] 락 얻음"); } finally { lock.unlock(); }
                } else {
                    System.out.println("    [trier] 300ms 내 못 얻어 포기(false) <- synchronized면 무한 대기였을 것");
                }
            } catch (InterruptedException ignored) {}
        });
        trier.start();
        trier.join();
        holder.join();

        System.out.println();
        System.out.println("(B) 그 외 보완 기능:");
        System.out.println("    - lockInterruptibly(): 락 대기 중 인터럽트로 빠져나올 수 있음(synchronized는 불가)");
        System.out.println("    - new ReentrantLock(true): 공정 락 — 먼저 기다린 스레드가 먼저 획득(FIFO)");

        System.out.println();
        System.out.println("=> ReentrantLock은 synchronized 한계를 보완: tryLock(시간)=무한대기 회피,");
        System.out.println("   lockInterruptibly()=인터럽트 가능, fair 락=공정성. 응답성 있고 데드락 회피가 쉬워진다.");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
