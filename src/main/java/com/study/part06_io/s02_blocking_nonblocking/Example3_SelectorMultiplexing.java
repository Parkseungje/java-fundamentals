package com.study.part06_io.s02_blocking_nonblocking;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * 예시 3 / 3 — Selector: "한 스레드가 여러 채널을 감시한다(1만 접속 문제의 해법)."
 *
 * 이 예시가 답하려는 질문: Non-blocking 채널을 어떻게 활용하면 '소수 스레드로 다수 연결'을 처리할 수
 * 있나? Blocking 모델의 '연결당 스레드 1개' 문제를 어떻게 푸는가?
 *
 * 왜 이 시나리오인가: Blocking IO에서는 연결마다 스레드가 read()에 묶여 멈추므로, 동시 접속 1만 명이면
 * 스레드도 1만 개가 필요하다(스레드 1개 ≈ 1MB + 컨텍스트 스위칭 비용 -> 비현실적). NIO의 Selector는
 * '멀티플렉서'로, 여러 Non-blocking 채널을 한 곳에 등록해두고 select()로 "지금 읽을 데이터가 있는
 * 채널"만 골라낸다. 그래서 '한 스레드'가 수천~수만 채널을 감시하며 준비된 것만 처리할 수 있다.
 * JVM 내부 Pipe 2개를 한 Selector에 등록하고, 단 하나의 selector 스레드가 둘 다 처리함을 보인다.
 *
 * 예상 결과:
 *   - selector 스레드 1개가 PIPE-1, PIPE-2 두 채널의 데이터를 모두 처리한다.
 *   - 메인이 시차를 두고 두 파이프에 쓰면, select()가 준비된 채널을 깨워 같은 스레드가 순서대로 처리.
 * -> 한 스레드가 여러 채널을 감시(멀티플렉싱)하므로, 연결 수만큼 스레드를 만들 필요가 없다.
 *    이것이 동시 접속 1만 명을 소수 스레드로 처리하는 NIO의 핵심이다.
 *    주의: CPU 바운드(계산 위주) 작업에는 오히려 손해 — Non-blocking은 'I/O 대기'가 많을 때 이득이다.
 */
public class Example3_SelectorMultiplexing {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] Selector: 한 스레드가 여러 채널 감시(멀티플렉싱)");
        System.out.println();

        Selector selector = Selector.open();

        // 채널 2개(파이프 2개)를 하나의 Selector에 등록 — 각각 이름표(attachment)를 붙임
        Pipe pipe1 = Pipe.open();
        pipe1.source().configureBlocking(false);
        pipe1.source().register(selector, SelectionKey.OP_READ, "PIPE-1");

        Pipe pipe2 = Pipe.open();
        pipe2.source().configureBlocking(false);
        pipe2.source().register(selector, SelectionKey.OP_READ, "PIPE-2");

        // ★ 단 하나의 selector 스레드가 두 채널을 모두 감시·처리
        Thread selectorThread = new Thread(() -> {
            try {
                int handled = 0;
                while (handled < 2) {
                    selector.select(); // 준비된 채널이 생길 때까지 대기(생기면 깨어남)
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove();
                        if (key.isReadable()) {
                            Pipe.SourceChannel ch = (Pipe.SourceChannel) key.channel();
                            ByteBuffer buf = ByteBuffer.allocate(16);
                            ch.read(buf);
                            System.out.println("  [selector 스레드] " + key.attachment()
                                    + " 에 데이터 도착 -> 처리 (이 스레드 하나가 둘 다 담당)");
                            handled++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "selector-thread");

        selectorThread.start();

        // 메인이 시차를 두고 두 파이프에 데이터를 보냄 -> selector가 준비된 채널을 골라 처리
        Thread.sleep(200);
        System.out.println("  [main] PIPE-1에 쓰기");
        pipe1.sink().write(ByteBuffer.wrap(new byte[]{1}));
        Thread.sleep(200);
        System.out.println("  [main] PIPE-2에 쓰기");
        pipe2.sink().write(ByteBuffer.wrap(new byte[]{2}));

        selectorThread.join();

        System.out.println();
        System.out.println("=> 단 하나의 selector 스레드가 PIPE-1, PIPE-2 두 채널을 모두 처리했다.");
        System.out.println("   연결 수만큼 스레드를 만들 필요 없이, 한 스레드가 다수 채널을 감시(멀티플렉싱).");
        System.out.println("   = 동시 접속 1만 명을 소수 스레드로 처리하는 NIO의 핵심. (단 CPU 바운드엔 손해)");
    }
}
