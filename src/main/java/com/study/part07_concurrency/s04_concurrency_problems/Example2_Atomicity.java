package com.study.part07_concurrency.s04_concurrency_problems;

/**
 * 예시 2 / 3 — 원자성(Atomicity) 문제: "count++는 사실 3단계라 동시에 하면 서로 덮어쓴다."
 *
 * 이 예시가 답하려는 질문: 여러 스레드가 공유 변수를 count++ 하면 왜 일부가 사라지나?
 *
 * 왜 이 시나리오인가: `count++`는 한 번에 끝나는 '원자적' 연산처럼 보이지만, 실제로는 3단계다.
 *   1) 읽기: 현재 count 값을 읽어온다
 *   2) 증가: 읽은 값에 +1
 *   3) 쓰기: 결과를 count에 저장
 * 두 스레드가 이 3단계를 동시에 진행하면, 둘 다 같은 옛 값(예: 100)을 읽고 → 둘 다 101을 써서,
 * 두 번 증가했는데 한 번만 반영되는 '갱신 유실(lost update)'이 생긴다. 이것이 '원자성 문제'다.
 * 2개 스레드가 공유 count를 각각 많이 증가시켜, 최종값이 기대값보다 작아짐을 확인한다.
 *
 * 예상 결과:
 *   - 2스레드 x 1,000,000 = 기대 2,000,000 이지만, 실제는 그보다 작다(유실). 실행마다 값이 다르다.
 * -> count++는 원자적이지 않다(읽기-증가-쓰기 3단계). 그 사이 다른 스레드가 끼어들면 갱신이 유실된다.
 *    이것이 '원자성' 문제이고, 가시성(예시1)과는 다른 별개의 문제다. (해결: synchronized/Atomic — 7.5)
 */
public class Example2_Atomicity {

    static int count = 0; // 공유 변수

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] 원자성 문제: count++는 3단계라 동시에 하면 유실");
        System.out.println();

        int iterations = 1_000_000;
        Runnable task = () -> {
            for (int i = 0; i < iterations; i++) {
                count++; // 읽기 -> +1 -> 쓰기 (원자적이지 않음!)
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        int expected = 2 * iterations;
        System.out.println("기대값 = " + expected + " (2스레드 x " + iterations + ")");
        System.out.println("실제값 = " + count
                + (count == expected ? " (우연히 일치)" : "  <- 유실! (count++가 원자적이지 않음)"));
        System.out.println("유실된 증가 횟수 = " + (expected - count));

        System.out.println();
        System.out.println("[count++의 3단계] 읽기 -> +1 -> 쓰기");
        System.out.println("  두 스레드가 같은 옛 값을 읽고 각자 +1해서 쓰면, 2번 증가가 1번으로 덮어써진다.");
        System.out.println();
        System.out.println("=> count++는 원자적이지 않아 동시 수정 시 갱신이 유실된다(원자성 문제).");
        System.out.println("   가시성(예시1)과는 다른 별개 문제. 해결은 synchronized/Atomic(7.5).");
    }
}
