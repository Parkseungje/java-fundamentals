package com.study.part07_concurrency.s07_communication;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 예시 2 / 3 — ReentrantLock + Condition: "조건을 나눠 정확한 쪽만 깨운다."
 *
 * 이 예시가 답하려는 질문: wait/notify의 불편(한 모니터에 모두 묶여 notifyAll로 다 깨워야 함)을
 * 어떻게 개선하나?
 *
 * 왜 이 시나리오인가: wait/notify는 객체 하나의 모니터를 쓰므로 '대기 줄'이 하나뿐이다. 그래서
 * 생산자와 소비자가 같은 줄에서 기다리고, 깨울 때 notifyAll로 '전부' 깨워야 한다(엉뚱한 쪽이 깨면
 * 다시 자느라 비효율). ReentrantLock의 Condition은 한 락에 '여러 개의 대기 줄'을 만들 수 있다.
 *   - notFull  : "자리가 났다"를 기다리는 생산자 줄
 *   - notEmpty : "데이터가 들어왔다"를 기다리는 소비자 줄
 * put은 notEmpty.signal()로 '소비자만', take는 notFull.signal()로 '생산자만' 정확히 깨운다.
 * 대응: wait()->await(), notify()->signal(), notifyAll()->signalAll(). 역시 락을 쥔 상태에서 호출하며
 * unlock은 finally에서(7.6).
 *
 * 예상 결과:
 *   - 예시1과 같은 생산자-소비자가 동작하되, 조건을 분리해 '필요한 쪽만' 깨운다.
 * -> Condition은 한 락에서 여러 대기 조건을 분리해, notifyAll의 낭비 없이 정확한 스레드만 깨운다.
 *    (wait/notify보다 명확하고 효율적. 단 여전히 락/await/signal을 직접 다뤄야 한다 -> 예시3이 더 간단.)
 */
public class Example2_ConditionVariable {

    static class BoundedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();  // 생산자 대기 줄
        private final Condition notEmpty = lock.newCondition(); // 소비자 대기 줄

        BoundedBuffer(int capacity) { this.capacity = capacity; }

        void put(int value) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == capacity) notFull.await(); // 가득 참 -> 생산자 줄에서 대기
                queue.add(value);
                System.out.println("  [생산] " + value + " (버퍼=" + queue.size() + ")");
                notEmpty.signal();   // '소비자'만 콕 집어 깨움 (notifyAll 낭비 없음)
            } finally {
                lock.unlock();
            }
        }

        int take() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) notEmpty.await();         // 비어 있음 -> 소비자 줄에서 대기
                int value = queue.poll();
                System.out.println("        [소비] " + value + " (버퍼=" + queue.size() + ")");
                notFull.signal();    // '생산자'만 콕 집어 깨움
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] ReentrantLock + Condition (notFull/notEmpty 분리)");
        System.out.println();

        BoundedBuffer buffer = new BoundedBuffer(3);
        int count = 10;

        Thread producer = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                try { buffer.put(i); Thread.sleep(20); } catch (InterruptedException ignored) {}
            }
        });
        Thread consumer = new Thread(() -> {
            for (int i = 0; i < count; i++) {
                try { buffer.take(); Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();

        System.out.println();
        System.out.println("=> Condition은 한 락에서 대기 줄(notFull/notEmpty)을 나눠 '필요한 쪽만' signal로 깨운다.");
        System.out.println("   wait/notify의 notifyAll 낭비를 없앤다(대응: wait->await, notify->signal).");
    }
}
