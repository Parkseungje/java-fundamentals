package com.study.part06_io.s03_byte_stream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 2 / 3 — read()의 int 반환과 EOF -1: "왜 byte가 아니라 int를 반환하나?"
 *
 * 이 예시가 답하려는 질문: InputStream.read()는 1바이트를 읽는데 왜 반환 타입이 byte가 아니라 int인가?
 * EOF(파일 끝)는 어떻게 알리나?
 *
 * 왜 이 시나리오인가: read()는 두 가지를 한 번에 알려야 한다.
 *   (1) 읽은 1바이트의 값: 0 ~ 255 (부호 없는 바이트, unsigned)
 *   (2) "더 읽을 게 없다(EOF)": -1
 * 만약 반환 타입이 byte였다면(-128 ~ 127), EOF 신호로 쓸 -1이 정상 데이터 바이트 0xFF(=-1)와
 * 구분되지 않는다. 그래서 범위를 넓힌 int를 써서 "0~255는 데이터, -1은 EOF"로 명확히 구분한다.
 * 이 때문에 읽기 루프는 항상 'while ((b = in.read()) != -1)' 패턴이다. 또 스트림은 OS 자원을
 * 쓰므로 사용 후 반드시 close해야 한다(여기선 try-with-resources로 자동 close — 6.5에서 심화).
 *
 * 예상 결과:
 *   - read()가 바이트를 0~255 범위의 int로 반환하고, 파일 끝에서 -1을 반환해 루프가 종료된다.
 *   - 읽은 바이트 개수와 마지막 반환값(-1)을 출력.
 * -> read()가 int인 이유: "데이터(0~255)와 EOF(-1)"를 한 반환값으로 구분하기 위해서다.
 */
public class Example2_ReadEofAndIntReturn {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] read()의 int 반환 + EOF(-1)");
        System.out.println();

        Path file = Files.createTempFile("read-eof", ".bin");
        Files.write(file, new byte[]{65, 66, (byte) 0xFF}); // 'A'(65), 'B'(66), 그리고 0xFF

        System.out.println("파일 내용(바이트): 65('A'), 66('B'), 0xFF(255)");
        System.out.println();

        try (InputStream in = new FileInputStream(file.toFile())) {
            int b;
            int count = 0;
            while ((b = in.read()) != -1) {   // -1(EOF)이 나올 때까지
                count++;
                System.out.println("  read() = " + b + " (0~255 범위의 데이터 바이트)");
            }
            System.out.println("  read() = -1 (EOF, 더 읽을 게 없음 -> 루프 종료). 총 " + count + "바이트 읽음");
        }

        System.out.println();
        System.out.println("주목: 세 번째 바이트 0xFF가 255로 읽혔다. 만약 read()가 byte였다면 이 값이 -1이 되어");
        System.out.println("      EOF 신호(-1)와 구분이 안 됐을 것이다. 그래서 int(0~255 + EOF -1)로 반환한다.");

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> read()가 int인 이유 = 데이터(0~255)와 EOF(-1)를 한 반환값으로 구분하려고.");
        System.out.println("   읽기 루프는 항상 while((b=read())!=-1). 스트림은 사용 후 close 필수(try-with-resources).");
    }
}
