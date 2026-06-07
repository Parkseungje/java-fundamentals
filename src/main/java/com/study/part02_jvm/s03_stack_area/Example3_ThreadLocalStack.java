package com.study.part02_jvm.s03_stack_area;

/**
 * 예시 3 / 3 — 스레드별 Stack: "Stack은 스레드마다 따로라서, 지역변수는 스레드 안전하다."
 *
 * 이 예시가 답하려는 질문: 여러 스레드가 '같은 메서드'를 동시에 실행하면, 그 메서드의 지역변수는
 * 서로 섞이는가? 반대로 공유되는 static 변수는 어떻게 되는가?
 *
 * 왜 이 시나리오인가: 4개의 스레드가 동시에 같은 작업을 한다 — 각자 10만 번 1씩 더하기.
 *   (A) 지역변수 버전: 합을 '메서드 안의 지역변수'에 누적한다. Stack은 스레드별로 따로이므로
 *       각 스레드의 지역변수는 완전히 독립적이다 -> 모든 스레드가 정확히 100000을 얻어야 한다.
 *   (B) 공유 static 버전: 합을 'static 변수'에 누적한다. static은 Method Area에 1개라 모든
 *       스레드가 공유한다 -> 동기화가 없으면 갱신이 서로 덮어써져(lost update) 합이 400000보다
 *       작게 나오는 경우가 많다(실행마다 다름).
 *
 * 예상 결과:
 *   - (A) 지역변수: 4개 스레드 모두 100000 (항상 정확, 서로 간섭 없음)
 *   - (B) static 공유: 기대값 400000보다 작게 나오기 쉬움(경쟁 상태로 일부 갱신 유실)
 * -> "Stack은 스레드별"이기 때문에 지역변수는 동기화 없이도 안전하다. 반대로 Heap/Method Area에
 *    있는 공유 변수(인스턴스/static)는 여러 스레드가 함께 만지면 위험하다.
 *    이것이 PART 7 동시성의 출발점이다. (B의 결과는 실행할 때마다 달라질 수 있다 = 경쟁 상태의 특징)
 */
public class Example3_ThreadLocalStack {

    static final int THREADS = 4;
    static final int ITERATIONS = 100_000;

    // (B) 모든 스레드가 공유하는 static 변수 — 동기화 없이 더하면 위험하다.
    static int sharedCounter = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 스레드별 Stack: 지역변수는 안전, 공유 static은 위험");
        System.out.println();

        // ===== (A) 지역변수 버전: 각 스레드가 자기 Stack의 지역변수에 누적 =====
        System.out.println("(A) 지역변수에 누적 (Stack은 스레드별 -> 독립):");
        Thread[] localThreads = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            localThreads[t] = new Thread(() -> {
                int localSum = 0; // 이 변수는 '이 스레드의 Stack'에만 존재 -> 다른 스레드와 무관
                for (int i = 0; i < ITERATIONS; i++) {
                    localSum++;
                }
                System.out.println("    스레드 " + id + "의 localSum = " + localSum + " (항상 정확)");
            });
        }
        for (Thread th : localThreads) th.start();
        for (Thread th : localThreads) th.join();

        System.out.println();

        // ===== (B) 공유 static 버전: 모든 스레드가 같은 static 변수에 누적 =====
        System.out.println("(B) 공유 static 변수에 누적 (동기화 없음 -> 경쟁 상태):");
        Thread[] sharedThreads = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            sharedThreads[t] = new Thread(() -> {
                for (int i = 0; i < ITERATIONS; i++) {
                    sharedCounter++; // 읽기-증가-쓰기가 원자적이지 않아 갱신이 유실될 수 있음(PART 7)
                }
            });
        }
        for (Thread th : sharedThreads) th.start();
        for (Thread th : sharedThreads) th.join();

        int expected = THREADS * ITERATIONS;
        System.out.println("    기대값 = " + expected + ", 실제 sharedCounter = " + sharedCounter
                + (sharedCounter == expected ? " (이번엔 우연히 일치)" : "  <- 유실 발생!"));

        System.out.println();
        System.out.println("=> 지역변수(A)는 스레드별 Stack에 있어 항상 정확하다(동기화 불필요).");
        System.out.println("   공유 static(B)은 여러 스레드가 함께 만져 값이 유실될 수 있다(실행마다 다름).");
        System.out.println("   '무엇이 공유되는가'가 동시성 안전의 핵심 -> PART 7로 이어진다.");
    }
}
