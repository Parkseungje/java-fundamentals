package com.study.part07_concurrency.s01_process_thread;

/**
 * 예시 3 / 3 — 컨텍스트 스위칭 오버헤드: "스레드가 많다고 무조건 빨라지지 않는다."
 *
 * 이 예시가 답하려는 질문: CPU를 많이 쓰는 작업(CPU 바운드)을 스레드 수만 늘리면 계속 빨라지나?
 * 스레드를 과도하게 만들면 무슨 일이 생기나?
 *
 * 왜 이 시나리오인가: 스레드를 전환할 때 CPU는 현재 스레드의 레지스터 상태를 백업하고 다음 스레드
 * 것을 복원하며, 캐시도 무효화된다 — 이 비용을 '컨텍스트 스위칭(context switching)'이라 한다. 코어
 * 수보다 스레드가 훨씬 많으면, CPU가 '실제 작업'보다 '스레드 전환'에 시간을 더 쓰게 된다. 즉 CPU
 * 바운드 작업에서는 스레드 수가 코어 수를 넘어가면 더 이상 빨라지지 않고, 과하면 오히려 느려진다.
 * '고정된 총 계산량'을 (1) 코어 수만큼의 스레드 (2) 코어 수의 수십 배 스레드로 나눠 처리해 시간을 비교한다.
 *
 * 예상 결과:
 *   - 사용 가능한 코어 수가 출력된다.
 *   - 코어 수만큼의 스레드: 가장 효율적(병렬로 코어를 꽉 채움).
 *   - 코어 수의 수십 배 스레드: 더 빨라지지 않고, 컨텍스트 스위칭 때문에 비슷하거나 오히려 느림.
 * -> CPU 바운드 작업에서 스레드 수의 적정선은 대략 '코어 수'다. 그 이상은 스위칭 오버헤드만 늘린다.
 *    (반대로 I/O 바운드는 대기가 많아 코어 수보다 많은 스레드가 유리할 수 있다 — 6.2 NIO와 대비.)
 */
public class Example3_ContextSwitching {

    // CPU를 쓰는 더미 계산(총량을 스레드들이 나눠서 수행)
    static long crunch(long iterations) {
        long acc = 0;
        for (long i = 0; i < iterations; i++) {
            acc += (i * 31 + 7) % 13; // 적당히 CPU를 쓰는 계산
        }
        return acc;
    }

    static long runWith(int threadCount, long totalWork) throws InterruptedException {
        long perThread = totalWork / threadCount;
        Thread[] threads = new Thread[threadCount];
        long start = System.currentTimeMillis();
        for (int t = 0; t < threadCount; t++) {
            threads[t] = new Thread(() -> crunch(perThread));
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();
        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 컨텍스트 스위칭: 스레드 ≠ 무조건 빠름 (CPU 바운드)");
        System.out.println();

        int cores = Runtime.getRuntime().availableProcessors();
        long totalWork = 2_000_000_000L; // 고정된 총 계산량

        System.out.println("사용 가능한 코어 수 = " + cores);

        // JIT 워밍업: 첫 측정이 '아직 컴파일 안 된 코드'라 불리해지는 것을 막는다.
        // (워밍업 없이 측정하면 먼저 잰 쪽이 손해 보여 결과가 왜곡된다.)
        for (int i = 0; i < 3; i++) runWith(cores, totalWork);

        System.out.println("총 계산량(고정)을 스레드 수만 바꿔 나눠 처리(JIT 워밍업 후 측정):");
        System.out.println();

        long t1 = runWith(cores, totalWork);
        System.out.println("  스레드 " + cores + "개 (코어 수)        : " + t1 + " ms (코어를 병렬로 꽉 채움 = 효율적)");

        int many = cores * 50;
        long t2 = runWith(many, totalWork);
        System.out.println("  스레드 " + many + "개 (코어의 50배)    : " + t2 + " ms (더 안 빨라짐 + 스위칭 오버헤드)");

        System.out.println();
        System.out.println("=> CPU 바운드 작업은 스레드를 코어 수보다 많이 만들어도 더 빨라지지 않고,");
        System.out.println("   과하면 컨텍스트 스위칭 비용만 늘어 오히려 느려질 수 있다. (수치는 실행마다 다름)");
        System.out.println("   적정 스레드 수 ≈ 코어 수. (I/O 바운드는 대기가 많아 더 많은 스레드가 유리 — 6.2와 대비)");
    }
}
