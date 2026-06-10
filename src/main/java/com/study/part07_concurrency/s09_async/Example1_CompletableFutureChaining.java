package com.study.part07_concurrency.s09_async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * 예시 1 / 3 — CompletableFuture 콜백 체이닝: "get()으로 안 막고, 끝나면 다음 단계가 이어진다."
 *
 * 이 예시가 답하려는 질문: 7.8의 Future는 결과를 get()으로 기다려야 했다(블로킹). 비동기 작업을
 * '막지 않고' 이어붙이려면?
 *
 * 왜 이 시나리오인가: Future.get()은 결과가 올 때까지 호출자를 멈춘다(7.2 Async-Blocking). 또 "작업이
 * 끝나면 자동으로 다음 작업"을 이어붙이지 못한다. CompletableFuture는 이를 해결한다 — 작업이 끝나면
 * 등록해둔 콜백이 '알아서' 실행되도록 체이닝한다(7.2 Async-Non-blocking).
 *   - supplyAsync(작업): 결과를 내는 비동기 작업 시작.
 *   - thenApply(f): 앞 결과를 받아 '변환'(결과 -> 새 결과). 논블로킹으로 이어짐.
 *   - thenAccept(c): 앞 결과를 받아 '소비'(출력 등, 반환 없음).
 * 호출자는 get()으로 멈추지 않고 다음 줄로 진행하며, 단계들은 작업이 끝나는 대로 콜백으로 실행된다.
 *
 * 예상 결과:
 *   - "main은 안 막히고 다음 일 진행" 이 먼저 찍히고, 잠시 뒤 비동기 체인(데이터 가져오기 ->
 *     대문자 변환 -> 출력)이 순서대로 실행된다.
 * -> CompletableFuture는 supplyAsync로 시작해 thenApply/thenAccept로 단계를 '논블로킹으로' 잇는다.
 *    Future.get()처럼 멈추지 않고, 각 단계는 앞 단계가 끝나는 즉시 이어진다. (CountDownLatch는 데모 종료용)
 */
public class Example1_CompletableFutureChaining {

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] CompletableFuture 콜백 체이닝 (논블로킹)");
        System.out.println();

        CountDownLatch done = new CountDownLatch(1); // 데모: 비동기 체인 끝까지 main 종료 방지

        CompletableFuture
                .supplyAsync(() -> {                       // ① 비동기로 데이터 가져오기
                    sleep(200);
                    System.out.println("  [1] 데이터 가져옴: \"hello\" by " + Thread.currentThread().getName());
                    return "hello";
                })
                .thenApply(data -> {                       // ② 결과를 받아 '변환'(논블로킹으로 이어짐)
                    System.out.println("  [2] 변환(대문자) by " + Thread.currentThread().getName());
                    return data.toUpperCase();
                })
                .thenAccept(result -> {                    // ③ 결과를 '소비'(출력)
                    System.out.println("  [3] 최종 결과: " + result);
                    done.countDown();
                });

        // 호출자는 get()으로 멈추지 않고 바로 다음 일을 한다
        System.out.println("  [main] 안 막히고 다음 일 진행 (체인은 끝나는 대로 콜백 실행)");

        done.await(); // 데모 종료용 대기
        System.out.println();
        System.out.println("=> supplyAsync로 시작 -> thenApply(변환) -> thenAccept(소비)로 단계를 논블로킹 체이닝.");
        System.out.println("   Future.get()처럼 멈추지 않고, 각 단계는 앞 단계가 끝나는 즉시 자동으로 이어진다.");
    }
}
