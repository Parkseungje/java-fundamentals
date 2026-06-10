package com.study.part07_concurrency.s08_executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 예시 2 / 3 — Callable + Future: "작업의 결과(반환값)를 받는다."
 *
 * 이 예시가 답하려는 질문: 스레드 작업의 '결과값'을 어떻게 받나? Runnable과 Callable은 무엇이 다른가?
 *
 * 왜 이 시나리오인가:
 *   - Runnable: run()이 void라 '결과를 반환할 수 없고' 검사 예외(checked exception)도 못 던진다.
 *   - Callable<V>: call()이 'V를 반환'하고 예외도 던질 수 있다. 결과가 필요한 작업에 쓴다.
 * Callable을 submit하면 Future<V>를 돌려받는다. Future는 '미래에 올 결과를 담는 약속'으로:
 *   - future.get(): 결과가 준비될 때까지 '기다렸다가(블로킹)' 값을 받는다. (7.2의 Async-Blocking)
 *   - 작업 중 예외가 났으면 get()에서 ExecutionException으로 감싸 던진다.
 * 작업 3개(각각 제곱 계산)를 Callable로 제출하고, Future들로 결과를 모아 합산한다.
 *
 * 예상 결과:
 *   - 3개 작업이 1*1, 2*2, 3*3 을 계산 -> Future.get()으로 1,4,9 수집 -> 합 14.
 * -> 결과가 필요하면 Callable + Future를 쓴다. Future.get()은 결과가 올 때까지 블로킹하며,
 *    작업 예외는 get()에서 ExecutionException으로 전달된다. (반환·결과가 필요 없으면 Runnable로 충분.)
 */
public class Example2_CallableFuture {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("[예시 2] Callable + Future (결과 반환)");
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(3);

        // Callable<Integer>: 결과(Integer)를 반환하는 작업
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final int n = i;
            Callable<Integer> task = () -> {
                Thread.sleep(50);
                int result = n * n;
                System.out.println("  작업: " + n + "^2 = " + result + " by " + Thread.currentThread().getName());
                return result; // Runnable과 달리 값을 반환할 수 있다
            };
            futures.add(pool.submit(task)); // submit(Callable) -> Future<Integer> 반환
        }

        // Future.get()으로 각 결과를 받아 합산 (get은 결과가 올 때까지 블로킹)
        int sum = 0;
        for (Future<Integer> f : futures) {
            sum += f.get(); // 준비될 때까지 대기 후 결과 수령
        }
        System.out.println("결과 합 = " + sum + " (1+4+9 = 14)");

        pool.shutdown();

        System.out.println();
        System.out.println("=> Runnable은 void(결과 X), Callable<V>는 V 반환(+예외 가능). submit(Callable)->Future<V>.");
        System.out.println("   future.get()은 결과가 올 때까지 블로킹하고, 작업 예외는 ExecutionException으로 전달한다.");
    }
}
