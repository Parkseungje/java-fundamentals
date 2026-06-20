package com.study.part07_concurrency.s03_thread_basics;

/**
 * 예시 4 / 4 — 스레드를 '멈추는' 정석(interrupt)과 CPU '양보' 힌트(yield).
 *
 * 이 예시가 답하려는 질문:
 *   - 돌고 있는 스레드를 어떻게 안전하게 멈추나? (stop()은 왜 쓰면 안 되나?)
 *   - interrupt()는 무엇을 하나? sleep 중 interrupt하면 왜 예외가 터지나?
 *   - yield()는 sleep과 무엇이 다른가?
 *
 * 왜 이 시나리오인가:
 *   (A) 자바엔 스레드를 '강제로' 죽이는 안전한 방법이 없다(stop()은 deprecated — 락을 쥔 채 죽어 데이터가
 *       깨질 수 있어서). 대신 interrupt()로 "이제 그만해 달라"는 '신호'만 보내고, 받는 쪽이 그 신호를
 *       확인해 스스로 정리하고 멈추는 '협력적 취소'가 정석이다. 여기서는 무한 루프 worker에게 interrupt를
 *       보내 isInterrupted()로 빠져나오게 한다.
 *   (B) sleep 같은 블로킹 중에 interrupt가 오면 InterruptedException이 '즉시' 터진다(잠을 깨워 신호를
 *       알린다). 그래서 sleep/wait/join은 이 예외 처리를 강제한다 — 지금까지 try-catch로 감쌌던 이유다.
 *   (C) yield()는 "지금 CPU를 다른 스레드에 양보하면 좋겠다"는 '힌트'일 뿐 강제가 아니다(무시될 수 있음).
 *       sleep과 달리 시간을 정해 자는 게 아니라 상태가 RUNNABLE로 유지되며 곧 다시 실행될 수 있다.
 *
 * 예상 결과:
 *   (A) worker가 카운트를 돌다가 main의 interrupt 신호를 받고 루프를 빠져나와 스스로 종료.
 *   (B) sleep 중인 worker에 interrupt -> InterruptedException 발생 -> 잠에서 깨어 종료.
 *   (C) yield 호출은 동작하지만 순서를 '보장'하진 않는다(힌트라서).
 */
public class Example4_InterruptAndYield {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 4] interrupt(협력적 취소) / yield(양보 힌트)");
        System.out.println();

        // (A) 협력적 취소: 무한 루프를 interrupt 신호로 멈춘다 (stop() 대신)
        System.out.println("(A) interrupt로 무한 루프 worker 멈추기 (강제 종료 stop() 대신):");
        Thread counter = new Thread(() -> {
            int n = 0;
            // isInterrupted(): "나에게 멈춰달라는 신호가 왔나?"를 매 바퀴 확인 -> 협력적으로 빠져나옴
            while (!Thread.currentThread().isInterrupted()) {
                n++;
            }
            System.out.println("    [counter] interrupt 신호 감지 -> " + n + "까지 세고 스스로 종료");
        }, "counter");
        counter.start();
        sleep(50);                 // 잠깐 돌게 두고
        counter.interrupt();       // "이제 그만" 신호만 보낸다(강제로 죽이는 게 아님)
        counter.join();

        System.out.println();

        // (B) sleep 중 interrupt -> InterruptedException 즉시 발생
        System.out.println("(B) sleep(블로킹) 중 interrupt -> InterruptedException으로 깨어남:");
        Thread sleeper = new Thread(() -> {
            try {
                System.out.println("    [sleeper] 10초 잘 예정...");
                Thread.sleep(10_000);
                System.out.println("    [sleeper] (정상적으로 다 잤다면 이 줄) — 도달 못 할 것");
            } catch (InterruptedException e) {
                // sleep/wait/join이 InterruptedException을 강제하는 이유가 바로 이것:
                // 블로킹 중에도 interrupt 신호를 '즉시' 받아 깨어나 정리할 수 있게 하기 위함.
                System.out.println("    [sleeper] 자는 중 interrupt 받음 -> 깨어나 종료");
            }
        }, "sleeper");
        sleeper.start();
        sleep(100);                // 확실히 sleep에 들어가게 한 뒤
        sleeper.interrupt();       // 자고 있는 스레드를 깨운다(예외 발생)
        sleeper.join();

        System.out.println();

        // (C) yield: CPU 양보 '힌트'(강제 아님)
        System.out.println("(C) yield — CPU 양보 힌트(보장 X, sleep과 달리 시간 지정 없음):");
        Runnable task = () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("    [" + Thread.currentThread().getName() + "] " + i + " (출력 후 yield로 양보 시도)");
                Thread.yield(); // "다른 스레드 있으면 먼저 하세요"라는 힌트. 무시될 수도 있어 순서는 보장 안 됨.
            }
        };
        Thread y1 = new Thread(task, "Y1");
        Thread y2 = new Thread(task, "Y2");
        y1.start();
        y2.start();
        y1.join();
        y2.join();
        System.out.println("    => 두 스레드가 번갈아 나오는 '경향'은 있으나 yield는 힌트라 매번 같은 순서를 보장하지 않는다.");

        System.out.println();
        System.out.println("정리: 멈춤은 interrupt(협력적 취소)로, 강제 종료 stop()은 금지. yield는 양보 '힌트'일 뿐.");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
