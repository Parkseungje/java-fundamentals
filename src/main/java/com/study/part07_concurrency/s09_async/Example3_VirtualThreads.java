package com.study.part07_concurrency.s09_async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 예시 3 / 3 — 가상 스레드(Virtual Thread, Java 21): "수만 개를 싸게, I/O 블로킹에 강하게."
 *
 * 이 예시가 답하려는 질문: 플랫폼 스레드(OS 스레드 1:1)는 수가 많아지면 비싸다(7.1). I/O 대기가 많은
 * 작업을 수만 개 동시에 처리하려면? 가상 스레드는 무엇이 다른가?
 *
 * 왜 이 시나리오인가: 기존 자바 스레드(플랫폼 스레드)는 OS 스레드와 1:1이라 스택 ~1MB를 쓰고, 수만
 * 개를 만들면 메모리·스위칭 비용이 폭증한다(7.1). 가상 스레드(Java 21)는 JVM이 관리하는 '경량 스레드'로,
 * 수십만~수백만 개를 만들 수 있다. 핵심: 가상 스레드가 I/O 등으로 '블로킹'되면, 그 가상 스레드는 실제
 * OS 스레드(캐리어)를 '놓아주고(언마운트)' 잠들고, 그 OS 스레드는 다른 가상 스레드를 실행한다. 그래서
 * 적은 수의 OS 스레드로 엄청나게 많은 블로킹 작업을 동시에 처리한다(6.2 Non-blocking의 효과를 '블로킹
 * 코드 스타일' 그대로 얻는 셈).
 *   - (A) 같은 일(I/O 대기 100ms x N개)을 '플랫폼 풀(고정)' vs '가상 스레드'로 비교.
 *   - (B) 가상 스레드로 100,000개 작업이 얼마나 빨리 끝나는지(플랫폼으론 비현실적).
 * Executors.newVirtualThreadPerTaskExecutor()로 작업마다 가상 스레드를 띄운다.
 *
 * 예상 결과:
 *   - (A) N=2000, 각 100ms 대기: 플랫폼 풀(200개)은 2000/200*100 ≈ 1000ms+, 가상 스레드는 모두 동시에
 *     블로킹해 ~100~300ms로 훨씬 빠름.
 *   - (B) 가상 스레드 100,000개: 모두 동시에 잠들었다 깨어 1초 안팎에 완료(플랫폼이면 수 GB 메모리 필요).
 * -> I/O 바운드 대량 동시성에서 가상 스레드는 적은 OS 자원으로 엄청난 수를 처리한다. 블로킹 코드를
 *    그대로 쓰면서도 논블로킹에 준하는 확장성을 얻는다(Java 21). (CPU 바운드는 코어 수 한계 그대로 — 7.1)
 */
public class Example3_VirtualThreads {

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

    // I/O 대기를 흉내내는 작업(100ms 블로킹)을 count개 실행하고 걸린 시간(ms) 반환
    static long runTasks(ExecutorService executor, int count) throws InterruptedException {
        AtomicInteger done = new AtomicInteger();
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            executor.submit(() -> { sleep(100); done.incrementAndGet(); });
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 가상 스레드 (Java 21) — I/O 대량 동시성");
        System.out.println();

        int n = 2000; // 각 작업은 100ms 'I/O 대기'

        // (A) 플랫폼 스레드 고정 풀(200개) — 한 번에 200개씩만 처리
        long platformTime = runTasks(Executors.newFixedThreadPool(200), n);
        System.out.println("(A) 플랫폼 스레드 풀(200): " + n + "개 작업(각 100ms) -> " + platformTime
                + " ms (200개씩 나눠 처리 -> 여러 번 반복)");

        // (B) 가상 스레드 — 작업마다 가상 스레드, 모두 동시에 블로킹
        long virtualTime = runTasks(Executors.newVirtualThreadPerTaskExecutor(), n);
        System.out.println("(B) 가상 스레드        : " + n + "개 작업(각 100ms) -> " + virtualTime
                + " ms (2000개가 모두 동시에 대기 -> 한 번에)");

        System.out.println();

        // (C) 가상 스레드로 100,000개 — 플랫폼으론 비현실적인 규모
        int big = 100_000;
        long bigTime = runTasks(Executors.newVirtualThreadPerTaskExecutor(), big);
        System.out.println("(C) 가상 스레드 " + big + "개(각 100ms) -> " + bigTime
                + " ms 완료 (플랫폼 스레드 10만 개면 수 GB 메모리 필요 -> 비현실적)");

        System.out.println();
        System.out.println("=> 가상 스레드는 I/O 블로킹 시 OS 스레드(캐리어)를 놓아줘, 적은 OS 자원으로 수만~수십만");
        System.out.println("   동시 작업을 처리한다. 블로킹 코드 그대로 쓰면서 높은 확장성을 얻는다(Java 21).");
        System.out.println("   (CPU 바운드는 코어 수가 한계라 가상 스레드여도 더 빨라지지 않는다 — 7.1)");
    }
}
