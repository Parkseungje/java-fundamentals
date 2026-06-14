package com.study.part07_concurrency.s02_sync_async;

import java.util.concurrent.CompletableFuture;

/**
 * 예시 1 / 3 — 제어권 축: Blocking vs Non-blocking ("호출이 제어권을 바로 돌려주는가?")
 *
 * 이 예시가 답하려는 질문: Blocking과 Non-blocking을 가르는 기준은 무엇인가?
 *
 * 왜 이 시나리오인가: Blocking/Non-blocking은 '제어권(control)' 축이다. 여기서 제어권 = '다음에
 * 실행할 코드를 정하는 권한', 곧 실행 흐름 자체다. 함수를 호출하면 제어권이 그 함수로 넘어가고,
 * 함수가 return하면 호출자에게 돌아온다. 그래서 '제어권을 안 돌려주는 주체'는 '호출당한 함수'다 —
 * 호출당한 함수가 일을 끝낼 때까지 return을 미뤄 제어권을 안 돌려주면 Blocking, 일이 안 끝났어도
 * 일단 return해서 제어권을 즉시 돌려주면 Non-blocking이다.
 * 0.5초 걸리는 작업을 두 방식으로 호출해 '호출자(main)가 그동안 다른 일을 할 수 있는가'로 구분한다.
 *   - Blocking: blockingFetch()를 부르면 결과가 나올 때까지 main이 그 줄에서 멈춘다(다른 일 못 함).
 *   - Non-blocking: 호출이 즉시 핸들을 돌려주고 작업은 백그라운드로 진행 -> main은 그동안 다른 일을
 *     하며 가끔 "다 됐나?"를 확인(polling)한다.
 *
 * 예상 결과:
 *   - Blocking: 호출 줄에서 약 500ms 멈춘 뒤 결과를 받는다(그동안 출력 없음).
 *   - Non-blocking: 호출은 즉시 반환되고, main이 "다른 일 하는 중..."을 여러 번 출력하다가 완료 확인.
 * -> Blocking은 호출자를 멈춰 세우고, Non-blocking은 제어권을 바로 돌려줘 호출자가 다른 일을 할 수 있다.
 *    (이것이 '제어권' 축. 다음 예시의 '결과 처리(Sync/Async)' 축과는 별개다.)
 */
public class Example1_BlockingVsNonBlocking {

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // Blocking: 결과가 나올 때까지 제어권을 안 돌려준다(호출자가 멈춤)
    static String blockingFetch() {
        sleep(500);
        return "데이터";
    }

    // Non-blocking: 즉시 핸들을 돌려주고 작업은 백그라운드로 진행
    static CompletableFuture<String> nonBlockingFetch() {
        return CompletableFuture.supplyAsync(() -> { sleep(500); return "데이터"; });
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] 제어권 축: Blocking vs Non-blocking");
        System.out.println();

        // (A) Blocking: 호출 줄에서 멈춘다
        System.out.println("(A) Blocking 호출:");
        long start = System.currentTimeMillis();
        String r1 = blockingFetch(); // 여기서 ~500ms 멈춤 (main이 다른 일 못 함)
        System.out.println("    blockingFetch() 반환까지 " + (System.currentTimeMillis() - start)
                + "ms 멈춰 있었음, 결과 = " + r1);

        System.out.println();

        // (B) Non-blocking: 즉시 반환 -> main은 다른 일을 하며 폴링
        System.out.println("(B) Non-blocking 호출:");
        CompletableFuture<String> handle = nonBlockingFetch(); // 즉시 반환
        System.out.println("    호출 즉시 제어권 돌아옴(작업은 백그라운드 진행)");
        int polls = 0;
        while (!handle.isDone()) {        // 다 됐나? 확인(polling)
            System.out.println("    main은 다른 일 하는 중... (아직 작업 안 끝남)");
            sleep(100);
            polls++;
        }
        System.out.println("    " + polls + "번 확인 후 완료, 결과 = " + handle.get());

        System.out.println();
        System.out.println("=> Blocking은 호출자를 멈춰 세우고, Non-blocking은 제어권을 즉시 돌려줘 다른 일을 할 수 있다.");
        System.out.println("   이것이 '제어권' 축이다(결과를 어떻게 받느냐는 다음 예시의 Sync/Async 축).");
    }
}
