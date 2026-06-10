package com.study.part07_concurrency.s08_executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 예시 1 / 3 — ExecutorService 기본: "미리 만든 스레드를 재사용한다."
 *
 * 이 예시가 답하려는 질문: 스레드를 매번 new Thread로 만드는 대신, 풀(pool)을 쓰면 무엇이 좋은가?
 *
 * 왜 이 시나리오인가: 스레드 생성은 공짜가 아니다(스택 ~1MB 할당, OS 등록 등). 작업마다 새 스레드를
 * 만들면 비용도 크고 무제한 생성 위험도 있다(7.1). ExecutorService는 '스레드풀'로, 미리 만들어 둔
 * 소수의 스레드를 '재사용'하며 제출된 작업들을 처리한다.
 *   - Executors.newFixedThreadPool(3): 스레드 3개짜리 풀.
 *   - submit(작업): 작업을 풀의 작업 큐(내부적으로 BlockingQueue — 7.7)에 넣으면, 노는 스레드가 꺼내 실행.
 *   - shutdown(): 더 받지 않고, 남은 작업을 마치면 풀을 닫는다(반드시 호출 — 안 하면 JVM이 안 끝남).
 * 작업 6개를 스레드 3개 풀에 제출해, 실행 스레드 '이름'이 3종류로 '재사용'됨을 확인한다.
 *
 * 예상 결과:
 *   - 6개 작업이 실행되지만, 실행 스레드 이름은 pool-1-thread-1~3 의 3종류뿐(재사용).
 *     -> 작업마다 새 스레드를 만든 게 아니라, 3개 스레드가 6개 작업을 나눠 처리.
 * -> 스레드풀은 소수의 스레드를 재사용해 생성 비용을 없애고 동시 실행 수를 제어한다. shutdown 필수.
 */
public class Example1_ThreadPoolBasic {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] ExecutorService 기본 (스레드 3개 풀에 작업 6개)");
        System.out.println();

        ExecutorService pool = Executors.newFixedThreadPool(3); // 스레드 3개짜리 풀

        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            pool.submit(() -> {  // Runnable 작업 제출 -> 작업 큐에 들어가 노는 스레드가 처리
                String name = Thread.currentThread().getName();
                System.out.println("  작업 " + taskId + " 실행 by " + name);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            });
        }

        pool.shutdown(); // 더 받지 않음. 남은 작업을 마치면 닫힘
        pool.awaitTermination(5, TimeUnit.SECONDS); // 다 끝날 때까지 대기

        System.out.println();
        System.out.println("=> 실행 스레드 이름이 pool-1-thread-1~3 의 3종류뿐 = 3개 스레드가 6개 작업을 '재사용'하며 처리.");
        System.out.println("   작업마다 새 스레드를 만들지 않아 생성 비용↓·동시 실행 수 제어. shutdown()은 필수.");
    }
}
