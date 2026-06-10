package com.study.part07_concurrency.s07_communication;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 예시 3 / 3 — BlockingQueue: "생산자-소비자를 wait/notify 없이 한 줄로."
 *
 * 이 예시가 답하려는 질문: 생산자-소비자 같은 흔한 패턴을, 락이나 wait/notify를 직접 다루지 않고
 * 안전하게 구현하는 표준 도구는?
 *
 * 왜 이 시나리오인가: 예시1(wait/notify)과 예시2(Condition)에서 직접 만든 '용량 제한 버퍼 + 대기/깨움'
 * 로직은 java.util.concurrent의 BlockingQueue에 이미 다 들어 있다. BlockingQueue는 동시성 안전한 큐로:
 *   - put(x): 큐가 가득 차면 자리가 날 때까지 '알아서' 블록(대기)한다.
 *   - take(): 큐가 비면 데이터가 들어올 때까지 '알아서' 블록(대기)한다.
 * 즉 락·wait·notify·while 재확인을 우리가 한 줄도 안 써도, 내부적으로 그것들을 정확히 해준다.
 * ArrayBlockingQueue(용량 3)로 같은 생산자-소비자를 구현해, 코드가 얼마나 간단해지는지 비교한다.
 *
 * 예상 결과:
 *   - 예시1·2와 동일하게 0~9 생산/소비가 진행되지만, 동기화 코드가 사라지고 put/take 호출만 남는다.
 * -> 실무 생산자-소비자는 보통 BlockingQueue를 쓴다. 저수준 wait/notify를 직접 다루는 건 원리 이해용이고,
 *    실제로는 검증된 고수준 도구(BlockingQueue)로 버그(빠뜨린 notify, if 오용 등) 위험을 없앤다.
 *    (Executor 스레드풀도 내부적으로 작업 큐로 BlockingQueue를 쓴다 -> 7.8)
 */
public class Example3_BlockingQueue {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] BlockingQueue (put/take가 알아서 블록)");
        System.out.println();

        // 용량 3짜리 동시성 안전 큐. 가득 차면 put이, 비면 take가 알아서 대기한다.
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);
        int count = 10;

        Thread producer = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                try {
                    queue.put(i); // 가득 차면 자동 대기 (wait/notify 직접 안 씀)
                    System.out.println("  [생산] " + i + " (버퍼=" + queue.size() + ")");
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
            }
        });
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                try {
                    int v = queue.take(); // 비면 자동 대기
                    System.out.println("        [소비] " + v + " (버퍼=" + queue.size() + ")");
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();

        System.out.println();
        System.out.println("=> BlockingQueue는 put(가득 차면 대기)/take(비면 대기)로 생산자-소비자를 안전하게 처리한다.");
        System.out.println("   락·wait·notify를 직접 안 써도 돼 코드가 간단하고 버그가 적다(실무 표준). Executor도 내부에서 사용(7.8).");
    }
}
