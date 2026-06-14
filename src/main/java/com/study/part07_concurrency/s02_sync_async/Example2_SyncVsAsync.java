package com.study.part07_concurrency.s02_sync_async;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * 예시 2 / 3 — 결과 처리 축: Sync vs Async ("결과를 누가 챙기는가?")
 *
 * 이 예시가 답하려는 질문: Sync와 Async를 가르는 기준은 무엇인가?
 *
 * 왜 이 시나리오인가: Sync/Async는 '결과(완료) 처리' 축이다 — 호출한 쪽이 결과를 '직접' 받아 처리하면 Sync,
 * 호출된 쪽이 일을 끝낸 뒤 '콜백으로 알려주면(호출자는 결과를 직접 안 챙김)' Async다.
 *   - Sync: result = compute(); 처럼 호출자가 반환값을 받아 다음 줄에서 직접 처리한다.
 *   - Async(콜백): computeAsync(콜백)처럼 "끝나면 이 콜백 실행해줘"라고 등록만 하고 넘어간다. 결과는
 *     작업이 끝났을 때 그 콜백이 처리한다. 호출자는 결과를 직접 받지 않는다.
 *
 * 예상 결과:
 *   - Sync: "동기 결과 처리: 데이터" 가 호출자 흐름에서 직접 출력된다.
 *   - Async: 호출자는 "콜백 등록 후 바로 다음 일"을 먼저 찍고, 잠시 뒤 '콜백'이 결과를 처리한다
 *     (결과 처리 주체가 호출자가 아니라 콜백).
 * -> Sync는 호출자가 결과를 직접 챙기고, Async는 콜백(호출된 쪽)이 결과를 알려준다. 이것이 '결과 처리' 축.
 *    (제어권을 즉시 주느냐는 예시1의 Blocking/Non-blocking 축 — 둘은 별개다.)
 */
public class Example2_SyncVsAsync {

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // Sync: 결과를 반환값으로 직접 돌려준다(호출자가 받아 처리)
    static String computeSync() {
        sleep(300);
        return "데이터";
    }

    // Async(콜백): 끝나면 콜백을 호출해 결과를 '전달'한다(호출자는 결과를 직접 안 받음)
    static void computeAsync(Consumer<String> callback) {
        new Thread(() -> {
            sleep(300);
            callback.accept("데이터"); // 작업 완료 시 콜백이 결과를 처리
        }).start();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] 결과 처리 축: Sync vs Async");
        System.out.println();

        // (A) Sync: 호출자가 결과를 직접 받아 처리
        System.out.println("(A) Sync:");
        String result = computeSync();              // 결과를 직접 받음
        System.out.println("    동기 결과 처리(호출자가 직접): " + result);

        System.out.println();

        // (B) Async(콜백): 끝나면 콜백이 처리, 호출자는 바로 다음 일
        System.out.println("(B) Async(콜백):");
        CountDownLatch done = new CountDownLatch(1); // 데모용: 콜백 끝날 때까지 main 종료 방지
        computeAsync(r -> {
            System.out.println("    >> 콜백이 결과 처리(작업 끝난 뒤): " + r);
            done.countDown();
        });
        System.out.println("    main은 콜백 등록 후 바로 다음 일 진행(결과는 콜백이 알아서 처리)");
        // ★ done.await()는 '데모용 장치'지 비동기의 일부가 아니다.
        //   await()는 그 자체가 '블로킹'이라, 래치가 0이 될 때까지(=콜백이 countDown 할 때까지) main을
        //   여기서 멈춰 세운다. 즉 이 줄 이후 다른 일을 '못 하는' 게 아니라, 래치가 풀리면(콜백 완료)
        //   바로 다음 줄들로 정상 진행한다.
        //   왜 굳이 막나? main이 그냥 끝나면 JVM이 종료되어 별도 스레드의 콜백이 '실행되기도 전에' 죽어
        //   콜백 출력을 못 보기 때문이다. 그래서 콜백이 끝날 때까지만 붙잡아두는 것뿐이다.
        //   실무 비동기에서는 이렇게 막지 않는다(막지 않는 게 비동기의 핵심). 예: 웹 서버는 main이 끝나지
        //   않고 계속 떠 있으므로 이런 await가 불필요하다. (자세한 실무 사례는 docs PART07_7_2 참고)
        done.await(); // (데모 종료용) 콜백이 끝날 때까지만 main을 붙잡아둠

        System.out.println();
        System.out.println("=> Sync는 호출자가 결과를 직접 받아 처리, Async는 콜백(호출된 쪽)이 완료 시 결과를 처리한다.");
        System.out.println("   호출자가 결과를 '직접 챙기느냐'가 Sync/Async를 가른다(제어권 축과는 별개).");
    }
}
