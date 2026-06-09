package com.study.part06_io.s02_blocking_nonblocking;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * 예시 2 / 3 — Non-blocking read: "데이터가 없으면 멈추지 않고 즉시 0을 반환한다."
 *
 * 이 예시가 답하려는 질문: NIO 채널을 Non-blocking 모드로 두면, 데이터가 없을 때 read()가 어떻게
 * 동작하나? (Example1의 Blocking과 무엇이 다른가?)
 *
 * 왜 이 시나리오인가: NIO 채널은 configureBlocking(false)로 'Non-blocking' 모드가 된다. 이 모드에서
 * read()는 데이터가 없어도 스레드를 멈추지 않고 '즉시 0을 반환'한다(Example1처럼 기다리지 않는다).
 * 그래서 스레드는 멈추지 않고 다른 일을 하거나 나중에 다시 확인(polling)할 수 있다. JVM 내부 Pipe로
 * 결정적으로 보여준다: 데이터를 쓰기 전 read()는 0, 쓴 뒤 read()는 실제 바이트 수를 반환한다.
 *
 * 예상 결과:
 *   - 데이터 쓰기 전: source.read(buffer) = 0 (즉시 반환, 대기 안 함)
 *   - 데이터 쓴 후 : source.read(buffer) = 3 (읽은 바이트 수)
 * -> Non-blocking은 "없으면 0 주고 바로 넘어감". 스레드를 멈추지 않으므로, 한 스레드가 여러 채널을
 *    돌며 확인할 수 있다. 이 성질이 Selector(Example3)와 결합해 '소수 스레드로 다수 연결 처리'를 가능케 한다.
 */
public class Example2_NonBlockingRead {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] Non-blocking read: 데이터 없으면 즉시 0 반환(대기 안 함)");
        System.out.println();

        Pipe pipe = Pipe.open();
        Pipe.SourceChannel source = pipe.source(); // 읽기 채널
        Pipe.SinkChannel sink = pipe.sink();       // 쓰기 채널
        source.configureBlocking(false);           // ★ Non-blocking 모드

        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 데이터 쓰기 전: 데이터가 없지만 멈추지 않고 즉시 0 반환
        int before = source.read(buffer);
        System.out.println("쓰기 전 read() = " + before + "  <- 데이터 없어도 멈추지 않고 즉시 0 (Blocking과 다름!)");

        // 데이터 쓰기
        sink.write(ByteBuffer.wrap(new byte[]{1, 2, 3}));

        // 쓴 후: 이제 실제 바이트 수 반환
        buffer.clear();
        int after = source.read(buffer);
        System.out.println("쓴 후   read() = " + after + "  <- 읽은 바이트 수(3)");

        source.close();
        sink.close();

        System.out.println();
        System.out.println("=> Non-blocking은 데이터가 없으면 '기다리지 않고' 즉시 0을 준다(스레드 안 멈춤).");
        System.out.println("   그래서 한 스레드가 여러 채널을 돌며 확인 가능 -> Selector(Example3)와 결합해 위력 발휘.");
    }
}
