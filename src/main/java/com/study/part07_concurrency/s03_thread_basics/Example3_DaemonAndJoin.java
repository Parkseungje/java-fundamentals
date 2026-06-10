package com.study.part07_concurrency.s03_thread_basics;

/**
 * 예시 3 / 3 — 데몬 스레드와 join().
 *
 * 이 예시가 답하려는 질문: 데몬 스레드는 일반 스레드와 무엇이 다른가? join()은 무엇을 하나?
 *
 * 왜 이 시나리오인가:
 *   - join(): "대상 스레드가 끝날 때까지 현재 스레드를 대기"시킨다. worker.join()을 부르면 main은
 *     worker가 완전히 끝날 때까지 멈췄다가 이어간다(예시1·2에서 이미 결과 수집에 사용).
 *   - 데몬 스레드(daemon): '보조' 스레드다. JVM은 '일반(user) 스레드'가 모두 끝나면, 데몬 스레드가
 *     아직 돌고 있어도 기다리지 않고 그냥 종료한다(데몬도 같이 죽음). 그래서 데몬은 끝까지 일을
 *     마친다는 보장이 없다. 작업 완료가 꼭 필요하면 데몬으로 만들면 안 된다.
 * 두 가지를 보여준다: (A) join으로 worker 완료를 기다리는 것, (B) 데몬 스레드가 작업 중인데도
 * main이 끝나면 JVM이 종료되며 데몬이 일을 다 못 마치고 죽는 것.
 *
 * 예상 결과:
 *   - (A) join: worker가 1,2,3을 다 출력할 때까지 main이 기다린 뒤 "worker 완료 확인" 출력.
 *   - (B) daemon: 데몬은 10번 출력하려 하지만, main이 곧 끝나 JVM이 종료되며 '몇 번 못 찍고' 죽는다.
 * -> join은 "끝날 때까지 기다림", 데몬은 "일반 스레드 다 끝나면 같이 죽는 보조 스레드(완료 보장 X)".
 */
public class Example3_DaemonAndJoin {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] 데몬 스레드 vs join()");
        System.out.println();

        // (A) join: worker가 끝날 때까지 main이 대기
        System.out.println("(A) join() — worker 완료까지 main 대기:");
        Thread worker = new Thread(() -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println("    [worker] " + i);
                sleep(50);
            }
        }, "worker");
        worker.start();
        worker.join(); // worker가 1,2,3 다 찍을 때까지 main 멈춤
        System.out.println("    worker 완료 확인(join 덕분에 1,2,3 다 본 뒤 이 줄 실행)");

        System.out.println();

        // (B) 데몬: main이 끝나면 JVM 종료 -> 데몬은 일을 다 못 마치고 죽는다
        System.out.println("(B) daemon — main이 끝나면 같이 죽음(완료 보장 X):");
        Thread daemon = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {        // 10번 찍으려 하지만...
                System.out.println("    [daemon] " + i + " (계속 일하려는 중)");
                sleep(80);
            }
            System.out.println("    [daemon] 완료!"); // 여기까지 도달 못 할 것
        }, "daemon");
        daemon.setDaemon(true); // ★ 데몬으로 지정 (start 전에 호출해야 함)
        daemon.start();

        sleep(250);  // main은 잠깐만 머물다 종료 -> 그 순간 JVM이 데몬을 끊는다
        System.out.println("    main 종료 -> 일반 스레드가 없으니 JVM 종료, 데몬은 10번 못 채우고 죽음");
        // main이 여기서 끝나면, 남은 건 데몬뿐이라 JVM이 종료된다(데몬도 강제 종료).
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
