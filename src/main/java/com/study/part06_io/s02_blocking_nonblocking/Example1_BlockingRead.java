package com.study.part06_io.s02_blocking_nonblocking;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 예시 1 / 3 — Blocking read: "데이터가 올 때까지 read()가 스레드를 멈춰 세운다."
 *
 * 이 예시가 답하려는 질문: 전통 IO 스트림의 read()는 데이터가 아직 없으면 어떻게 동작하나?
 *
 * 왜 이 시나리오인가: 전통 IO(java.io)의 read()는 'Blocking'이다 — 읽을 데이터가 아직 없으면
 * 데이터가 올 때까지 그 스레드를 '멈춰 세운다'(반환하지 않는다). 이를 보이려고 PipedInputStream(읽기)과
 * PipedOutputStream(쓰기)을 연결하고, 읽기 스레드가 먼저 read()를 호출하게 한다. 이때 아직 아무도
 * 안 썼으므로 read()는 블록되고, 0.5초 뒤 메인이 데이터를 써야 비로소 read()가 반환한다. 읽기 스레드가
 * 얼마나 멈춰 있었는지 시간을 측정해 "스레드가 정지했다"를 확인한다.
 *
 * 예상 결과:
 *   - 읽기 스레드의 read()가 약 500ms 동안 블록되었다가, 메인이 write한 뒤에야 값을 반환.
 *   - 측정된 대기 시간 ≈ 500ms.
 * -> Blocking IO는 데이터가 올 때까지 스레드를 점유한 채 멈춘다. 이 때문에 "연결 1개 = 스레드 1개"가
 *    되어, 동시 접속이 많으면 스레드도 그만큼 필요해진다(Example3에서 그 해법). close()로만 빠져나올 수도 있다.
 */
public class Example1_BlockingRead {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] Blocking read: 데이터 올 때까지 스레드 정지");
        System.out.println();

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out); // out에 쓰면 in으로 흘러감

        Thread reader = new Thread(() -> {
            try {
                System.out.println("  [reader] read() 호출 — 데이터가 없으면 여기서 멈춘다...");
                long start = System.currentTimeMillis();
                int b = in.read();             // Blocking! 데이터 올 때까지 반환 안 함
                long waited = System.currentTimeMillis() - start;
                System.out.println("  [reader] read() 반환! 값=" + b + ", 약 " + waited + "ms 동안 멈춰 있었음");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        reader.start();
        System.out.println("  [main] 0.5초 동안 일부러 안 쓴다 (reader는 그동안 block 상태)");
        Thread.sleep(500);
        System.out.println("  [main] 이제 write(42) — 이 순간 reader의 read()가 깨어난다");
        out.write(42);
        reader.join();

        System.out.println();
        System.out.println("=> Blocking IO의 read()는 데이터가 올 때까지 스레드를 멈춰 세운다(점유).");
        System.out.println("   그래서 '연결 1개 = 스레드 1개'가 되어 동시 접속이 많으면 스레드도 폭증한다(Example3 해법).");
    }
}
