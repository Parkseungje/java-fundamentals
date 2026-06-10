package com.study.part07_concurrency.s07_communication;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 예시 1 / 3 — wait/notify로 생산자-소비자: "저수준 스레드 통신의 기본."
 *
 * 이 예시가 답하려는 질문: 한 스레드가 '조건이 충족될 때까지' 기다렸다가 다른 스레드가 깨우는,
 * 스레드 간 통신은 어떻게 하나? (생산자-소비자 문제)
 *
 * 왜 이 시나리오인가: 용량이 정해진 버퍼(bounded buffer)에 생산자는 넣고(put) 소비자는 꺼낸다(take).
 *   - 버퍼가 가득 차면 생산자는 자리가 날 때까지 '기다려야' 하고,
 *   - 버퍼가 비면 소비자는 데이터가 들어올 때까지 '기다려야' 한다.
 * 이 '기다림/깨움'을 객체의 모니터로 구현하는 게 wait()/notify()다.
 *   - wait(): 가진 락을 '놓고' 대기 상태로 들어간다(다른 스레드가 락을 쓸 수 있게).
 *   - notify()/notifyAll(): 그 객체에서 wait 중인 스레드를 깨운다(notifyAll은 전부).
 * ★ 두 가지 주의: ① wait/notify는 반드시 synchronized 블록(그 객체의 락을 쥔 상태) 안에서 호출.
 *   ② 조건 검사는 if가 아니라 'while'로 — 깨어났어도 조건이 또 안 맞을 수 있고(spurious wakeup,
 *   다른 스레드가 먼저 가져감), 그래서 깨면 조건을 '다시' 확인해야 한다. notifyAll로 깨우는 이유도
 *   생산자/소비자가 한 모니터를 공유해 '아무나' 깨우면 엉뚱한 쪽만 깨울 수 있어서다.
 *
 * 예상 결과:
 *   - 생산자가 0~9를 넣고 소비자가 0~9를 꺼낸다. 버퍼 용량(3)이 작아 중간중간 '가득참/비어있음'으로
 *     서로를 기다렸다 깨우며 진행된다. 최종적으로 10개 모두 생산·소비된다.
 * -> wait/notify는 모니터 기반의 가장 기본적인 스레드 통신이다. 단 while 재확인·notifyAll·synchronized
 *    같은 규칙을 직접 지켜야 해서 손이 많이 간다(예시2 Condition, 예시3 BlockingQueue가 이를 개선).
 */
public class Example1_WaitNotify {

    static class BoundedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        BoundedBuffer(int capacity) { this.capacity = capacity; }

        synchronized void put(int value) throws InterruptedException {
            while (queue.size() == capacity) { // if가 아니라 while! 깨어나도 다시 확인
                wait();                         // 가득 참 -> 락을 놓고 대기
            }
            queue.add(value);
            System.out.println("  [생산] " + value + " (버퍼=" + queue.size() + ")");
            notifyAll();                        // 기다리던 소비자(들)를 깨움
        }

        synchronized int take() throws InterruptedException {
            while (queue.isEmpty()) {           // 비어 있으면
                wait();                         // 락을 놓고 대기
            }
            int value = queue.poll();
            System.out.println("        [소비] " + value + " (버퍼=" + queue.size() + ")");
            notifyAll();                        // 기다리던 생산자(들)를 깨움
            return value;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] wait/notify 생산자-소비자 (버퍼 용량 3)");
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
        System.out.println("=> wait()는 락을 놓고 대기, notify/notifyAll은 깨운다. synchronized 안에서만 호출.");
        System.out.println("   조건은 while로 재확인(spurious wakeup 대비), 보통 notifyAll. 규칙이 많아 손이 간다.");
    }
}
