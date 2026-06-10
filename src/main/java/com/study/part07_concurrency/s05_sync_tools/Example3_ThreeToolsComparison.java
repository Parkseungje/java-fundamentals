package com.study.part07_concurrency.s05_sync_tools;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 예시 3 / 3 — 세 도구 비교: synchronized vs volatile vs Atomic (정확성 + 성능).
 *
 * 이 예시가 답하려는 질문: synchronized/volatile/Atomic은 각각 무엇을 보장하고 성능은 어떤가? 언제 무엇을?
 *
 * 왜 이 시나리오인가: 같은 작업(2스레드 x 100만 count++)을 세 방식으로 돌려, '정확성'과 '성능'을
 * 한 화면에서 비교한다.
 *   - volatile: 가시성만 보장, 원자성 ❌ -> count++ 유실(틀린 결과). 단 빠르다.
 *   - synchronized: 가시성+원자성 ✅ -> 정확. 단 락 비용으로 느린 편.
 *   - Atomic(CAS): 가시성+원자성 ✅ -> 정확. 논블로킹이라 보통 synchronized보다 빠르다.
 * 진화 서사: 안전하지만 느린 synchronized -> 가시성만 싸게 주는 volatile -> 락 없이 원자성을 얻는 Atomic.
 *
 * 예상 결과:
 *   - volatile: 결과 < 200만 (유실) — 빠르지만 틀림.
 *   - synchronized: 정확히 200만 — 느린 편.
 *   - Atomic: 정확히 200만 — synchronized보다 보통 빠름.
 * -> 단순 카운터/플래그엔 Atomic(또는 volatile-읽기전용)이 효율적이고, 여러 변수를 한 묶음으로 보호해야
 *    하면 synchronized(락)가 필요하다. (수치는 실행/환경마다 다름)
 */
public class Example3_ThreeToolsComparison {

    static volatile int volatileCount = 0;
    static int syncCount = 0;
    static final Object lock = new Object();
    static AtomicInteger atomicCount = new AtomicInteger(0);

    static final int ITER = 1_000_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] synchronized vs volatile vs Atomic 비교");
        System.out.println();

        // volatile: 가시성만 -> 유실(틀림), 빠름
        long tV = run(() -> { for (int i = 0; i < ITER; i++) volatileCount++; });
        System.out.printf("volatile     : 결과 %d / 기대 %d  | %d ms  -> %s%n",
                volatileCount, 2 * ITER, tV, (volatileCount == 2 * ITER ? "정확" : "유실(원자성 X)"));

        // synchronized: 둘 다 -> 정확, 느린 편
        long tS = run(() -> { for (int i = 0; i < ITER; i++) synchronized (lock) { syncCount++; } });
        System.out.printf("synchronized : 결과 %d / 기대 %d  | %d ms  -> %s%n",
                syncCount, 2 * ITER, tS, (syncCount == 2 * ITER ? "정확" : "유실"));

        // Atomic(CAS): 둘 다 -> 정확, 보통 빠름
        long tA = run(() -> { for (int i = 0; i < ITER; i++) atomicCount.incrementAndGet(); });
        System.out.printf("Atomic(CAS)  : 결과 %d / 기대 %d  | %d ms  -> %s%n",
                atomicCount.get(), 2 * ITER, tA, (atomicCount.get() == 2 * ITER ? "정확" : "유실"));

        System.out.println();
        System.out.println("[정리]  도구       | 가시성 | 원자성 | 방식           | 성능");
        System.out.println("        synchronized| ✅     | ✅     | 락(블로킹)      | 가장 느림");
        System.out.println("        volatile    | ✅     | ❌     | 메모리 동기화   | 빠름(단 원자성 X)");
        System.out.println("        Atomic      | ✅     | ✅     | CAS(논블로킹)   | 빠름");
        System.out.println();
        System.out.println("=> 단순 카운터/플래그면 Atomic(읽기전용 플래그면 volatile)이 효율적,");
        System.out.println("   여러 변수를 한 묶음으로 보호해야 하면 synchronized(락)가 필요하다. (수치는 실행마다 다름)");
    }

    // 같은 task를 2스레드로 돌리고 시간(ms) 반환
    static long run(Runnable task) throws InterruptedException {
        Thread t1 = new Thread(task), t2 = new Thread(task);
        long start = System.currentTimeMillis();
        t1.start(); t2.start(); t1.join(); t2.join();
        return System.currentTimeMillis() - start;
    }
}
