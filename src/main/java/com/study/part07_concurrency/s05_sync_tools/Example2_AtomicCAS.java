package com.study.part07_concurrency.s05_sync_tools;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 예시 2 / 3 — Atomic + CAS: "락 없이 원자성을 얻는다(논블로킹)."
 *
 * 이 예시가 답하려는 질문: 락(synchronized)을 안 쓰고도 어떻게 count++를 안전하게 하나? CAS란 무엇인가?
 *
 * 왜 이 시나리오인가: AtomicInteger는 내부적으로 'CAS(Compare And Swap)'라는 CPU 명령으로 락 없이
 * 원자성을 보장한다. CAS의 3단계:
 *   ① 현재 값을 읽는다 (A)
 *   ② 새 값을 계산한다 (B = A + 1)
 *   ③ "메모리의 현재 값이 아직 A와 같으면 B로 바꿔라"를 '한 번에(원자적으로)' 시도한다.
 *      - 같으면: 그 사이 아무도 안 건드린 것 -> 교체 성공.
 *      - 다르면: 그 사이 다른 스레드가 바꾼 것 -> 실패 -> ①부터 다시(재시도).
 * 락으로 막는 게 아니라 "바뀌었으면 다시 해" 방식이라 '논블로킹'이고, 그래서 보통 더 빠르다.
 * 두 가지를 보여준다: (A) AtomicInteger.incrementAndGet()로 간단히, (B) compareAndSet으로 CAS를 직접 구현.
 *
 * 예상 결과:
 *   - (A) incrementAndGet: 2스레드 x 100만 = 정확히 2,000,000 (락 없이 유실 없음).
 *   - (B) 직접 CAS 루프(get -> +1 -> compareAndSet, 실패 시 재시도): 역시 정확히 2,000,000.
 * -> Atomic은 CAS로 락 없이 원자성을 보장한다(논블로킹, 빠름). 단 경쟁이 심하면 CAS 재시도가 잦아질 수
 *    있고, 'ABA 문제'(값이 A->B->A로 돌아오면 안 바뀐 걸로 오인)도 주의해야 한다.
 */
public class Example2_AtomicCAS {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] Atomic + CAS: 락 없이 원자성");
        System.out.println();

        int iterations = 1_000_000;

        // (A) AtomicInteger.incrementAndGet() — 내부적으로 CAS를 써서 락 없이 원자적
        AtomicInteger atomic = new AtomicInteger(0);
        Runnable simple = () -> { for (int i = 0; i < iterations; i++) atomic.incrementAndGet(); };
        Thread a1 = new Thread(simple), a2 = new Thread(simple);
        a1.start(); a2.start(); a1.join(); a2.join();
        System.out.println("(A) incrementAndGet(): " + atomic.get() + " / 기대 " + (2 * iterations)
                + (atomic.get() == 2 * iterations ? "  <- 정확(락 없이)" : "  <- 유실"));

        System.out.println();

        // (B) compareAndSet으로 CAS를 직접 구현 — "값이 안 바뀌었으면 교체, 바뀌었으면 재시도"
        AtomicInteger manual = new AtomicInteger(0);
        Runnable casLoop = () -> {
            for (int i = 0; i < iterations; i++) {
                int old, next;
                do {
                    old = manual.get();        // ① 현재 값 읽기 (A)
                    next = old + 1;            // ② 새 값 계산 (B)
                } while (!manual.compareAndSet(old, next)); // ③ old와 같으면 next로 교체, 다르면 재시도
            }
        };
        Thread b1 = new Thread(casLoop), b2 = new Thread(casLoop);
        b1.start(); b2.start(); b1.join(); b2.join();
        System.out.println("(B) 직접 CAS 루프    : " + manual.get() + " / 기대 " + (2 * iterations)
                + (manual.get() == 2 * iterations ? "  <- 정확(CAS 재시도로 유실 없음)" : "  <- 유실"));

        System.out.println();
        System.out.println("[CAS 3단계] ① 현재 값 읽기(A) ② 새 값 계산(B) ③ '아직 A면 B로 교체' 원자적 시도");
        System.out.println("  성공=아무도 안 건드림 / 실패=다른 스레드가 바꿈 -> ①부터 재시도(논블로킹).");
        System.out.println();
        System.out.println("=> Atomic은 CAS로 '락 없이' 원자성을 보장한다(논블로킹 -> 보통 빠름).");
        System.out.println("   주의: 경쟁 심하면 재시도 잦아짐, ABA 문제(A->B->A를 안 바뀐 걸로 오인) 가능.");
    }
}
