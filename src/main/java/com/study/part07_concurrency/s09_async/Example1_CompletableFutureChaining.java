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

        // ★★ 여기가 '논블로킹의 의미'를 보여주는 핵심 구간이다.
        //   위의 체인(supplyAsync...)은 작업을 백그라운드로 '던지고 즉시 반환'했다. 그래서 그 데이터(200ms
        //   걸림)가 도착하기를 '기다리지 않고' main은 곧장 여기로 와서 자기 일을 한다.
        //   - 만약 동기(블로킹)였다면: 데이터를 받는 그 줄에서 200ms '묶여서' 아래 작업을 시작도 못 했다.
        //   - 논블로킹이라: 데이터가 오는 200ms '동안' main이 아래 doMainWork를 진행한다(시간 활용!).
        //   즉 논블로킹의 의미 = "결과를 결국 기다리느냐"가 아니라 "기다리는 시간 동안 main이 자유로우냐"이다.
        System.out.println("  [main] 안 막히고 내 일을 시작함 (백그라운드 작업과 '동시에' 진행)");
        for (int i = 1; i <= 3; i++) {
            System.out.println("  [main] 내 작업 " + i + " 처리 중... (이때 백그라운드 체인도 같이 돌고 있다)");
            sleep(80);   // main이 '다른 일'을 하는 시간 — 동기였다면 이 일은 데이터 받은 뒤에야 가능했다
        }
        System.out.println("  [main] 내 일 다 끝남. 이제 비동기 결과가 필요하니 그때서야 기다린다(await).");

        // done.await()는 '데모 종료용'이기도 하지만(콜백이 별도 스레드라 main이 먼저 끝나면 출력을 못 봄),
        //   의미상으로는 "내 할 일을 다 한 뒤, 마지막에 결과가 필요해지면 그때 합류해 기다린다"는 지점이다.
        //   (이 await 한 줄 자체는 블로킹이지만, 그 전까지 main은 묶이지 않고 자유로웠다 = 논블로킹의 이득)
        done.await(); // 콜백 완료까지 대기

        // ↓ 아래 두 줄은 'await가 풀린 뒤(= 콜백이 모두 끝난 뒤)'에야 실행된다. 그래서 출력의 맨 마지막에 나온다.
        System.out.println();
        System.out.println("=> supplyAsync로 시작 -> thenApply(변환) -> thenAccept(소비)로 단계를 논블로킹 체이닝.");
        System.out.println("   Future.get()처럼 멈추지 않고, 각 단계는 앞 단계가 끝나는 즉시 자동으로 이어진다.");
    }
}
