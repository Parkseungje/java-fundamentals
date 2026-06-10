package com.study.part07_concurrency.s01_process_thread;

/**
 * 예시 2 / 3 — 메모리 공유와 스레드 안전: "공유되는가? ≈ 스레드 안전한가?"
 *
 * 이 예시가 답하려는 질문: 여러 스레드가 같이 돌 때 어떤 변수는 안전하고 어떤 변수는 위험한가?
 * 그 기준은 무엇인가?
 *
 * 왜 이 시나리오인가: 스레드는 메모리를 일부는 공유하고 일부는 따로 갖는다(2.2/2.3).
 *   - 코드/데이터/Heap/Method Area : 모든 스레드가 공유  -> 같이 만지면 위험
 *   - Stack(지역 변수)             : 스레드마다 따로      -> 안전(공유 안 됨)
 * 그래서 "이 변수가 스레드 안전한가?"는 사실상 "이 변수가 공유되는가?"와 같은 질문이다. 같은 작업을
 *   (A) 지역 변수에 누적 -> 스레드마다 독립 -> 항상 정확
 *   (B) 공유 static 변수에 누적 -> 같이 만짐 -> 동기화 없으면 갱신 유실
 * 으로 비교해 이 원칙을 확인한다. (2.3 Stack 예제의 7장 관점 재확인)
 *
 * 예상 결과:
 *   - (A) 지역 변수: 4개 스레드 모두 정확히 100000 (서로 독립, 동기화 불필요)
 *   - (B) static 공유: 기대 400000보다 작게 나오기 쉬움(경쟁 상태로 갱신 유실, 실행마다 다름)
 * -> 스레드 안전성의 핵심 질문은 "공유되는가". 지역 변수(Stack)는 공유 안 돼 안전하고, 인스턴스/
 *    static 변수(Heap/Method Area)는 공유돼 동기화가 필요하다(synchronized/Atomic 등 — 7.5).
 */
public class Example2_SharedVsLocal {

    static final int THREADS = 4;
    static final int ITERATIONS = 100_000;

    static int sharedCounter = 0; // 모든 스레드가 공유하는 static 변수

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] 공유되는가? ≈ 스레드 안전한가?");
        System.out.println();

        // (A) 지역 변수: 각 스레드의 Stack에만 존재 -> 독립 -> 안전
        System.out.println("(A) 지역 변수에 누적 (Stack은 스레드별 -> 안전):");
        Thread[] local = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            final int id = t;
            local[t] = new Thread(() -> {
                int localSum = 0; // 이 스레드의 Stack에만 있는 변수
                for (int i = 0; i < ITERATIONS; i++) localSum++;
                System.out.println("    스레드 " + id + " localSum = " + localSum + " (항상 정확)");
            });
        }
        for (Thread th : local) th.start();
        for (Thread th : local) th.join();

        System.out.println();

        // (B) 공유 static 변수: 모든 스레드가 같이 만짐 -> 동기화 없으면 위험
        System.out.println("(B) 공유 static 변수에 누적 (동기화 없음 -> 경쟁 상태):");
        Thread[] shared = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            shared[t] = new Thread(() -> {
                for (int i = 0; i < ITERATIONS; i++) sharedCounter++; // 공유 변수 동시 수정
            });
        }
        for (Thread th : shared) th.start();
        for (Thread th : shared) th.join();

        int expected = THREADS * ITERATIONS;
        System.out.println("    기대값 = " + expected + ", 실제 sharedCounter = " + sharedCounter
                + (sharedCounter == expected ? " (우연히 일치)" : "  <- 유실! (공유 변수라 위험)"));

        System.out.println();
        System.out.println("[정리] 스레드 안전성 = '공유되는가'의 문제");
        System.out.println("  지역 변수   | Stack(스레드별)      | 안전(공유 X)");
        System.out.println("  인스턴스 변수| Heap                | 공유 -> 동기화 필요");
        System.out.println("  static 변수 | Method Area         | 공유 -> 동기화 필요");
        System.out.println();
        System.out.println("=> 지역 변수는 스레드마다 따로라 안전. 공유되는 인스턴스/static 변수는 동기화 필요(7.5).");
    }
}
