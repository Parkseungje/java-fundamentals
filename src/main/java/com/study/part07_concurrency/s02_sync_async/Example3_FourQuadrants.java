package com.study.part07_concurrency.s02_sync_async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 예시 3 / 3 — 4분면 종합 + Future.get vs CompletableFuture (Async-Blocking vs Async-Non-blocking).
 *
 * 이 예시가 답하려는 질문: 두 축(Sync/Async × Blocking/Non-blocking)을 합치면 4가지 조합이 나온다.
 * 특히 둘 다 'Async'인데 Future.get()과 CompletableFuture 콜백은 무엇이 다른가?
 *
 * 왜 이 시나리오인가: 두 축은 독립이라 2x2 = 4분면이 된다.
 *   |        | Blocking            | Non-Blocking                         |
 *   | Sync   | 전통 IO(단순/비효율)  | NIO Polling(예시1 B)                  |
 *   | Async  | Future.get()        | CompletableFuture 콜백(가장 효율적)     |
 * 여기선 아래 두 'Async' 조합의 차이를 직접 비교한다.
 *   - Async-Blocking: executor.submit(task)로 작업을 비동기로 던지지만(Async), 결과는 future.get()에서
 *     '기다린다'(Blocking). 즉 던질 땐 비동기지만 받을 땐 막힌다 -> 결국 호출자가 멈춘다.
 *   - Async-Non-blocking: CompletableFuture.supplyAsync(...).thenAccept(콜백). 던지고 콜백만 등록하면,
 *     호출자는 안 막히고(Non-blocking) 결과는 끝났을 때 콜백이 처리한다(Async). -> 가장 효율적.
 *
 * 예상 결과:
 *   - Future.get(): "get() 대기 중" 후 결과를 받지만, get()에서 호출자가 멈춘다(Async지만 Blocking).
 *   - CompletableFuture.thenAccept(): 호출자는 안 멈추고 바로 다음 일, 결과는 콜백이 나중에 처리.
 * -> "비동기로 던졌다"고 다 좋은 게 아니다. Future.get()은 결국 막히고(Async-Blocking),
 *    콜백 기반 CompletableFuture가 막지 않고 끝나면 알려주는 가장 효율적인 조합(Async-Non-blocking)이다.
 *    (CompletableFuture/Future의 본격 학습은 7.8/7.9)
 */
public class Example3_FourQuadrants {

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] 4분면 + Future.get(Async-Blocking) vs CompletableFuture(Async-Non-blocking)");
        System.out.println();

        // (A) Async-Blocking: submit으로 던지지만 get()에서 막힌다
        System.out.println("(A) Async-Blocking — Future.get():");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> { sleep(400); return "데이터"; }); // 비동기로 던짐
        System.out.println("    submit 직후엔 제어권 돌아옴(Async). 하지만 결과는 get()에서 기다려야 함...");
        String r = future.get(); // ★ 여기서 호출자가 멈춘다(Blocking)
        System.out.println("    get() 반환(여기까지 멈춰 있었음): " + r);
        executor.shutdown();

        System.out.println();

        // (B) Async-Non-blocking: 콜백 등록 -> 안 막히고 끝나면 콜백
        System.out.println("(B) Async-Non-blocking — CompletableFuture.thenAccept():");
        CountDownLatch done = new CountDownLatch(1);
        CompletableFuture
                .supplyAsync(() -> { sleep(400); return "데이터"; }) // 비동기로 던짐
                .thenAccept(result -> {                              // 끝나면 콜백이 처리(안 막힘)
                    System.out.println("    >> 콜백이 결과 처리(끝난 뒤): " + result);
                    done.countDown();
                });
        System.out.println("    호출자는 안 멈추고 바로 다음 일 진행(결과는 콜백이 나중에 처리)");
        // ★ done.await()는 데모용 장치다(Example2 참고). 비동기의 일부가 아니라, main이 먼저 끝나
        //   JVM이 종료되면 콜백이 실행되기도 전에 죽으므로 '콜백이 끝날 때까지만' 붙잡아두는 것이다.
        //   실무 비동기에선 이렇게 막지 않는다(웹 서버는 계속 떠 있어 불필요). docs PART07_7_2 참고.
        done.await(); // (데모 종료용) 콜백이 끝날 때까지만 main을 붙잡아둠

        System.out.println();
        System.out.println("[4분면 정리]");
        System.out.println("           | Blocking              | Non-Blocking");
        System.out.println("  Sync     | 전통 IO(단순/비효율)    | NIO Polling(예시1-B)");
        System.out.println("  Async    | Future.get()          | CompletableFuture 콜백(가장 효율적)");
        System.out.println();
        System.out.println("=> 비동기로 던져도 Future.get()은 결국 막힌다(Async-Blocking). 콜백 기반");
        System.out.println("   CompletableFuture는 막지 않고 끝나면 알려준다(Async-Non-blocking) = 가장 효율적.");
    }
}
