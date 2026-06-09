package com.study.part06_io.s01_io_overview;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * 예시 3 / 3 — IO 스트림 vs NIO 채널+버퍼: "1바이트 단방향 vs 블록 양방향."
 *
 * 이 예시가 답하려는 질문: 전통 IO(스트림)와 NIO(채널+버퍼)는 같은 파일을 읽어도 무엇이 다른가?
 *
 * 왜 이 시나리오인가: 같은 파일을 두 방식으로 읽어 '읽는 방법(메커니즘)'의 차이를 본다.
 *   - 전통 IO(java.io): Stream 기반. read()로 데이터를 흘려보내듯 읽는다. 방향이 정해져 있고
 *     (InputStream은 읽기 전용), 한 번에 1바이트(혹은 byte[])씩 순차적으로 흐른다.
 *   - NIO(java.nio): Channel + Buffer 기반. 데이터는 항상 'Buffer'라는 블록을 거쳐 Channel로
 *     오간다. Channel은 양방향이고(읽기·쓰기 모두), Buffer 단위(블록)로 한꺼번에 다룬다.
 * 두 방식 모두 같은 내용을 읽지만, "스트림으로 흘리기" vs "버퍼라는 블록에 담아 채널로 옮기기"라는
 * 모델이 다르다. (Blocking/Non-blocking·Selector 등 NIO의 진짜 강점은 6.2에서 다룬다.)
 *
 * 예상 결과:
 *   - IO 스트림 읽기 결과 == NIO 채널 읽기 결과 (같은 내용)
 *   - 단 IO는 InputStream.read()로 순차, NIO는 ByteBuffer에 담아 읽는다(코드 모델이 다름)
 * -> 결과는 같아도 모델이 다르다: IO=스트림(1바이트·단방향), NIO=채널+버퍼(블록·양방향).
 */
public class Example3_StreamVsChannel {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] IO 스트림(1바이트·단방향) vs NIO 채널+버퍼(블록·양방향)");
        System.out.println();

        Path file = Files.createTempFile("io-channel", ".txt");
        Files.writeString(file, "Hello NIO");

        // (A) 전통 IO: InputStream(스트림)으로 1바이트씩 순차적으로 읽기
        StringBuilder ioResult = new StringBuilder();
        try (InputStream in = new FileInputStream(file.toFile())) {
            int b;
            while ((b = in.read()) != -1) { // read()가 1바이트씩 반환, EOF면 -1
                ioResult.append((char) b);
            }
        }
        System.out.println("(A) IO 스트림(InputStream.read 1바이트씩): \"" + ioResult + "\"");

        // (B) NIO: Channel + Buffer로 블록 단위로 읽기 (데이터가 항상 Buffer를 거침)
        StringBuilder nioResult = new StringBuilder();
        try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(64); // 블록(버퍼)
            int read = channel.read(buffer);             // 채널 -> 버퍼로 한 번에 담기
            buffer.flip();                                // 쓰기모드 -> 읽기모드 전환(양방향 버퍼의 특징)
            while (buffer.hasRemaining()) {
                nioResult.append((char) buffer.get());
            }
        }
        System.out.println("(B) NIO 채널+버퍼(ByteBuffer 블록)       : \"" + nioResult + "\"");

        System.out.println();
        System.out.println("두 결과 같은가? " + ioResult.toString().equals(nioResult.toString()));

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> 같은 내용을 읽어도 모델이 다르다: IO=스트림(흘려보냄, 단방향), NIO=채널+버퍼");
        System.out.println("   (블록 단위, 양방향, buffer.flip으로 읽기/쓰기 모드 전환). NIO의 진짜 강점(Non-blocking,");
        System.out.println("   Selector)은 6.2에서 다룬다.");
    }
}
