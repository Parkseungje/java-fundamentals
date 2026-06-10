package com.study.part07_concurrency.s04_concurrency_problems;

/**
 * 예시 1 / 3 — 가시성(Visibility) 문제: "한 스레드의 변경이 다른 스레드에 안 보인다."
 *
 * 이 예시가 답하려는 질문: 한 스레드가 공유 변수를 바꿨는데, 다른 스레드가 그 변경을 즉시 못 보는
 * 일이 왜 생기나?
 *
 * 왜 이 시나리오인가: CPU는 성능을 위해 각 코어의 캐시에 변수 값을 들고 일한다. 그리고 JIT 컴파일러는
 * `while(running){}`처럼 루프 안에서 안 바뀌어 보이는 변수를 '루프 밖으로 빼서 캐시'하는 최적화를 한다.
 * 그 결과, main 스레드가 메인 메모리의 running을 false로 바꿔도, worker 스레드는 자기 캐시에 든 옛 값
 * (true)만 보고 무한 루프를 돈다 — 이것이 '가시성 문제'다. (volatile/동기화로 해결 — 7.5, 예시3)
 *
 * worker를 데몬으로 둬서, 무한 루프에 빠져도 main이 끝나면 JVM이 정리하게 한다. main이 running=false로
 * 바꾼 뒤 worker.join(2000)으로 2초 기다려보고, 그래도 worker가 살아있으면 "가시성 문제 재현"이다.
 *
 * 예상 결과:
 *   - main이 running=false로 바꿔도 worker가 변경을 못 봐서, 2초 뒤에도 worker가 '살아있음(무한 루프)'.
 *     -> "worker alive after 2s? true" = 가시성 문제 재현.
 * 주의: 이 문제는 JVM/CPU/최적화에 의존한다. 환경에 따라 worker가 변경을 (늦게라도) 볼 수도 있다.
 *      (이 프로젝트 JDK 21에서는 안정적으로 재현된다.)
 * -> 가시성 = "내가 바꾼 값이 다른 스레드에 보이느냐"의 문제. 동기화 없이 공유 변수를 읽으면 옛 값을
 *    볼 수 있다. (원자성과는 별개 문제 — 예시2, 예시3에서 구분)
 */
public class Example1_Visibility {

    static boolean running = true; // volatile 아님 -> 가시성 보장 안 됨

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] 가시성 문제: 변경이 다른 스레드에 안 보임");
        System.out.println();

        Thread worker = new Thread(() -> {
            long count = 0;
            while (running) {  // running이 false로 바뀌어도 캐시된 옛 값(true)만 보면 무한 루프
                count++;
            }
            System.out.println("  [worker] running=false를 봐서 멈춤, count=" + count);
        });
        worker.setDaemon(true); // 무한 루프에 빠져도 main 종료 시 JVM이 정리
        worker.start();

        Thread.sleep(100);
        running = false; // main이 메인 메모리의 running을 false로
        System.out.println("  [main] running = false 로 변경함");

        worker.join(2000); // worker가 변경을 보고 끝나면 2초 안에 종료될 것
        boolean stillRunning = worker.isAlive();
        System.out.println("  2초 뒤 worker 살아있음? " + stillRunning
                + (stillRunning ? "  <- 가시성 문제! (변경을 못 보고 무한 루프)" : "  (변경을 봐서 종료됨)"));

        System.out.println();
        System.out.println("=> main이 running=false로 바꿔도 worker는 캐시된 옛 값(true)을 봐서 멈추지 않는다.");
        System.out.println("   이것이 가시성 문제다(원자성과는 별개). volatile/동기화로 해결한다(예시3, 7.5).");
        // main 종료 -> worker는 데몬이라 JVM이 함께 종료(무한 루프 정리)
    }
}
