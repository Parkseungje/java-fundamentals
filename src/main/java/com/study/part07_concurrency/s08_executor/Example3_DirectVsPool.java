package com.study.part07_concurrency.s08_executor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 예시 3 / 3 — 스레드 직접 생성 vs 스레드풀: "작업이 많을수록 풀이 유리하다."
 *
 * 이 예시가 답하려는 질문: 작업마다 new Thread로 만드는 것과 풀을 쓰는 것은 실제로 얼마나 다른가?
 *
 * 왜 이 시나리오인가: 짧은 작업이 아주 많을 때(예: 10,000개), '작업마다 새 스레드'를 만들면 스레드를
 * 1만 개나 생성·소멸해 생성 비용과 컨텍스트 스위칭(7.1)이 폭증한다. 스레드풀은 소수의 스레드를 재사용해
 * 이 비용을 없앤다. 같은 10,000개의 짧은 작업을 두 방식으로 처리해 (1) 걸린 시간과 (2) 실제로 사용된
 * 스레드 개수를 비교한다.
 *   - 직접 생성: new Thread를 작업마다 -> 스레드 1만 개 생성.
 *   - 풀: newFixedThreadPool(코어 수) -> 소수 스레드를 재사용.
 *
 * 예상 결과:
 *   - 직접 생성: 스레드 10,000개 생성, 시간이 더 오래 걸림(생성/소멸 오버헤드).
 *   - 풀: 코어 수만큼(예: 12개)의 스레드만 재사용, 보통 더 빠름.
 * -> 작업이 많을수록 '풀로 재사용'이 생성 비용·스레드 폭증을 막아 유리하다. (수치는 환경마다 다름)
 */
public class Example3_DirectVsPool {

    static final int TASKS = 10_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 스레드 직접 생성 vs 풀 (" + TASKS + "개의 짧은 작업)");
        System.out.println();

        // (A) 작업마다 new Thread — 스레드 1만 개 생성
        AtomicInteger workDone1 = new AtomicInteger();
        Set<String> directThreads = Collections.synchronizedSet(new HashSet<>());
        long t1 = System.currentTimeMillis();
        Thread[] threads = new Thread[TASKS];
        for (int i = 0; i < TASKS; i++) {
            threads[i] = new Thread(() -> {
                directThreads.add(Thread.currentThread().getName()); // 사용된 스레드 이름 수집
                workDone1.incrementAndGet();
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        long directTime = System.currentTimeMillis() - t1;
        System.out.println("(A) 직접 생성: " + directTime + " ms, 생성된 스레드 수 ≈ "
                + directThreads.size() + " (작업마다 새로)");

        // (B) 스레드풀 — 코어 수만큼 재사용
        AtomicInteger workDone2 = new AtomicInteger();
        Set<String> poolThreads = Collections.synchronizedSet(new HashSet<>());
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(cores);
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < TASKS; i++) {
            pool.submit(() -> {
                poolThreads.add(Thread.currentThread().getName());
                workDone2.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        long poolTime = System.currentTimeMillis() - t2;
        System.out.println("(B) 스레드풀  : " + poolTime + " ms, 사용된 스레드 수 = "
                + poolThreads.size() + " (코어 수만큼 재사용)");

        System.out.println();
        System.out.println("두 방식 모두 작업 완료 수: 직접=" + workDone1.get() + ", 풀=" + workDone2.get());
        System.out.println();
        System.out.println("=> 직접 생성은 스레드를 " + TASKS + "개나 만들어 비용↑. 풀은 소수(" + poolThreads.size()
                + "개)를 재사용해 효율적.");
        System.out.println("   작업이 많을수록 풀이 생성 비용·스레드 폭증·컨텍스트 스위칭을 막아 유리하다. (수치는 환경마다 다름)");
    }
}
