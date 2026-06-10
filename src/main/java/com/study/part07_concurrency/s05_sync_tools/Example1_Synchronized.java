package com.study.part07_concurrency.s05_sync_tools;

/**
 * 예시 1 / 3 — synchronized: "가시성과 원자성을 둘 다 해결한다(대신 느림)."
 *
 * 이 예시가 답하려는 질문: 7.4에서 본 count++ 유실(원자성 문제)을 synchronized로 어떻게 막나?
 *
 * 왜 이 시나리오인가: synchronized는 가장 확실한 동기화 도구다. 모든 객체는 '모니터 락(intrinsic
 * lock)'을 하나씩 갖는데, synchronized 블록/메서드에 들어가려면 그 락을 얻어야 한다. 한 스레드가
 * 락을 쥐면 다른 스레드는 락이 풀릴 때까지 기다린다(상태 BLOCKED). 그래서 "읽기-증가-쓰기" 3단계가
 * 한 스레드씩 통째로 실행되어(원자성), 게다가 락 진입/해제 시 메모리도 동기화되어(가시성) 두 문제를
 * 한꺼번에 해결한다. 7.4와 같은 2스레드 count++를 synchronized로 감싸 유실이 사라짐을 확인한다.
 *
 * 예상 결과:
 *   - synchronized로 보호하면 2스레드 x 100만 = 정확히 2,000,000 (유실 없음).
 * -> synchronized는 한 번에 한 스레드만 임계 구역(critical section)에 들이는 '상호 배제'로 원자성을,
 *    락 경계의 메모리 동기화로 가시성을 함께 보장한다. 단 락 획득/대기 비용이 있어 가장 느리다.
 *    한계: 무한 대기(타임아웃 불가), 인터럽트 불가, 공정성 보장 X (-> 7.6 ReentrantLock에서 보완).
 */
public class Example1_Synchronized {

    static class Counter {
        private int count = 0;

        // synchronized 메서드: 이 인스턴스의 모니터 락을 얻어야 진입 -> 한 번에 한 스레드만
        synchronized void increment() {
            count++; // 락으로 보호되어 읽기-증가-쓰기가 통째로 원자적
        }

        int get() { return count; }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] synchronized: 원자성+가시성 둘 다 해결");
        System.out.println();

        Counter counter = new Counter();
        int iterations = 1_000_000;
        Runnable task = () -> {
            for (int i = 0; i < iterations; i++) counter.increment();
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        int expected = 2 * iterations;
        System.out.println("기대값 = " + expected + ", 실제값 = " + counter.get()
                + (counter.get() == expected ? "  <- 정확! (synchronized로 유실 없음)" : "  <- 유실"));

        System.out.println();
        System.out.println("=> synchronized는 모니터 락으로 한 번에 한 스레드만 임계 구역에 들여(원자성),");
        System.out.println("   락 경계에서 메모리를 동기화한다(가시성). 두 문제를 다 해결하지만 락 비용으로 가장 느리다.");
        System.out.println("   한계: 타임아웃/인터럽트 불가, 공정성 보장 X (-> 7.6 ReentrantLock에서 보완).");
    }
}
