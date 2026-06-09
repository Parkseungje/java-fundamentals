package com.study.part06_io.s05_buffered_data;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 2 / 3 — 보조 스트림 Buffered: "버퍼가 OS 호출 횟수를 줄여 훨씬 빠르다."
 *
 * 이 예시가 답하려는 질문: BufferedOutputStream(보조 스트림)으로 감싸면 왜 빨라지나?
 *
 * 왜 이 시나리오인가: 기본 스트림(FileOutputStream)으로 write(int)를 1바이트씩 호출하면, 매번
 * OS 시스템 콜이 일어난다(디스크/OS와의 통신). 호출 1번당 오버헤드가 있어, 1바이트씩 100만 번이면
 * 시스템 콜도 100만 번 → 매우 느리다. BufferedOutputStream은 내부에 버퍼(기본 8KB)를 두고, write를
 * 호출하면 일단 버퍼에 모아 두었다가 '버퍼가 차거나 flush/close될 때' 한 번에 OS로 내보낸다. 그래서
 * 시스템 콜 횟수가 확 줄어 빨라진다. 같은 양을 두 방식으로 1바이트씩 써서 시간을 비교한다.
 * (보조 스트림 = 기존 스트림을 '감싸서' 기능을 더하는 데코레이터 — PART 12 AOP와 같은 패턴)
 *
 * 예상 결과:
 *   - FileOutputStream(unbuffered)로 1바이트씩 쓰기: 느림(매 write가 시스템 콜)
 *   - BufferedOutputStream로 감싸서 쓰기: 훨씬 빠름(버퍼에 모아 한 번에)
 * -> 같은 작업인데 버퍼 유무로 성능이 크게 갈린다. 그래서 파일 I/O는 보통 Buffered로 감싼다.
 */
public class Example2_BufferedStream {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] Buffered 보조 스트림: 시스템 콜 횟수 ↓ -> 성능 ↑");
        System.out.println();

        int n = 1_000_000; // 1바이트씩 100만 번 쓰기

        // (A) 버퍼 없음: 매 write가 OS 시스템 콜
        Path f1 = Files.createTempFile("nobuf", ".bin");
        long t1 = System.currentTimeMillis();
        try (OutputStream out = new FileOutputStream(f1.toFile())) {
            for (int i = 0; i < n; i++) {
                out.write(i & 0xFF);     // 1바이트씩 -> 매번 시스템 콜
            }
        }
        long unbuffered = System.currentTimeMillis() - t1;

        // (B) 버퍼 있음: 내부 8KB 버퍼에 모아 한 번에
        Path f2 = Files.createTempFile("buf", ".bin");
        long t2 = System.currentTimeMillis();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(f2.toFile()))) {
            for (int i = 0; i < n; i++) {
                out.write(i & 0xFF);     // 버퍼에 쌓였다가 한꺼번에 flush
            }
        }
        long buffered = System.currentTimeMillis() - t2;

        System.out.println(n + "바이트를 1바이트씩 쓰기:");
        System.out.println("  FileOutputStream (버퍼 X)     : " + unbuffered + " ms (매 write가 시스템 콜)");
        System.out.println("  BufferedOutputStream (버퍼 O) : " + buffered + " ms (버퍼에 모아 한 번에)");
        if (buffered > 0) {
            System.out.println("  -> 버퍼 사용이 약 " + (unbuffered / Math.max(1, buffered)) + "배 빠름");
        }

        Files.deleteIfExists(f1);
        Files.deleteIfExists(f2);

        System.out.println();
        System.out.println("=> 기본 스트림은 write마다 OS 시스템 콜 -> 느림. Buffered는 버퍼(8KB)에 모아");
        System.out.println("   한 번에 내보내 시스템 콜을 줄인다. 파일 I/O는 보통 Buffered로 감싼다.");
    }
}
