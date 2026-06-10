package com.study.part07_concurrency.s01_process_thread;

/**
 * 예시 1 / 3 — 프로세스와 스레드: "한 프로세스(JVM) 안에서 여러 스레드가 동시에(번갈아) 흐른다."
 *
 * 이 예시가 답하려는 질문: 프로세스와 스레드는 무엇이 다른가? 한 프로그램(JVM) 안에서 여러 스레드가
 * 동시에 실행된다는 게 무슨 뜻인가?
 *
 * 왜 이 시나리오인가:
 *   - 프로세스 = 실행 중인 프로그램(여기선 이 JVM). 독립된 메모리를 갖고, 1개 이상의 스레드를 포함한다.
 *   - 스레드 = 프로세스 안에서 코드를 실행하는 '흐름'. main()도 'main 스레드'라는 하나의 흐름이다.
 * main 스레드 외에 새 스레드 2개를 만들어 동시에 숫자를 출력하게 한다. 세 흐름이 '번갈아' 찍히는
 * 인터리빙(interleaving)을 보면, 한 프로세스 안에서 여러 스레드가 동시에 진행됨을 직접 확인할 수 있다.
 *   - 멀티태스킹: 코어 1개가 시분할로 번갈아 (동시처럼 보임)
 *   - 멀티프로세싱: 여러 코어가 진짜 동시에. (둘은 함께 쓰인다)
 *
 * 예상 결과:
 *   - 현재 프로세스 PID와 main 스레드 이름이 출력된다.
 *   - worker-1, worker-2, main의 출력이 '뒤섞여(순서 보장 X)' 나온다 = 동시 실행의 증거.
 * -> 한 프로세스(JVM) 안에 여러 스레드가 있고, 그들이 동시에(또는 번갈아) 코드를 실행한다.
 *    출력 순서가 실행마다 달라지는 것이 '동시성'의 특징이다.
 */
public class Example1_ProcessAndThreads {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 1] 한 프로세스(JVM) 안의 여러 스레드");
        System.out.println();

        System.out.println("프로세스 PID = " + ProcessHandle.current().pid()
                + " (이 JVM이 하나의 프로세스)");
        System.out.println("현재 스레드 = " + Thread.currentThread().getName() + " (main도 하나의 스레드)");
        System.out.println();

        // 새 스레드 2개 생성 (작업은 Runnable 람다)
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            for (int i = 1; i <= 3; i++) {
                System.out.println("  [" + name + "] " + i);
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        };
        Thread worker1 = new Thread(task, "worker-1");
        Thread worker2 = new Thread(task, "worker-2");

        worker1.start(); // start() -> 새 스레드에서 task 실행
        worker2.start();

        // main 스레드도 동시에 자기 일을 한다
        for (int i = 1; i <= 3; i++) {
            System.out.println("  [main] " + i);
            Thread.sleep(10);
        }

        worker1.join(); // 두 스레드가 끝날 때까지 대기
        worker2.join();

        System.out.println();
        System.out.println("=> 한 프로세스(JVM) 안에서 main/worker-1/worker-2 세 스레드가 동시에 흐른다.");
        System.out.println("   출력이 뒤섞이고 순서가 매번 달라지는 것이 '동시 실행(동시성)'의 증거다.");
    }
}
