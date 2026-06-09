package com.study.part06_io.s03_byte_stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 3 / 3 — byte[] 버퍼 읽기 함정 + FileOutputStream append 모드.
 *
 * 이 예시가 답하려는 질문: 성능을 위해 byte[] 버퍼로 한 번에 여러 바이트를 읽을 때 주의점은?
 * FileOutputStream으로 파일에 '이어쓰기'하려면?
 *
 * 왜 이 시나리오인가: 1바이트씩 읽으면 느리므로 보통 byte[] 버퍼로 한 번에 읽는다. 이때
 * in.read(buffer)는 '실제로 읽은 바이트 수 n'을 반환한다. 함정: 버퍼가 데이터보다 크면 버퍼가 다
 * 안 채워지는데, 그래도 buffer.length 전체를 처리하면 안 된다 — 반드시 'buffer[0]..buffer[n-1]'까지만
 * 써야 한다(나머지는 이전 값/0이 남은 쓰레기). 이를 잘못 vs 올바르게 처리해 비교한다.
 * 또 FileOutputStream은 기본이 '덮어쓰기'이고, 생성자에 append=true를 주면 '이어쓰기'가 된다.
 *
 * 예상 결과:
 *   - 5바이트 데이터를 10바이트 버퍼로 읽으면 read()=5(n). buffer[0..5)만 유효, 나머지 5칸은 0.
 *   - 잘못 처리(buffer 전체 사용): 뒤에 쓰레기(널 문자 등)가 붙는다.
 *   - 올바른 처리(0..n): 정확히 "HELLO"만.
 *   - append=false(기본): 두 번 쓰면 마지막 것만 남음(덮어씀). append=true: 두 글이 누적됨.
 * -> 버퍼 읽기는 반드시 '읽은 수 n'까지만 처리한다. append 모드로 이어쓰기를 제어한다.
 */
public class Example3_BufferTrapAndAppend {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] byte[] 버퍼 읽기 함정 + append 모드");
        System.out.println();

        // ===== (A) 버퍼 읽기 함정 =====
        Path file = Files.createTempFile("buffer", ".txt");
        Files.write(file, "HELLO".getBytes(StandardCharsets.UTF_8)); // 5바이트

        try (InputStream in = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[10];          // 데이터(5)보다 큰 버퍼
            int n = in.read(buffer);                // 실제 읽은 수 반환
            System.out.println("[버퍼 읽기] 버퍼 크기=10, read()가 반환한 실제 읽은 수 n = " + n);

            // 잘못된 처리: buffer 전체(length)를 문자열로 -> 뒤 5칸(0)이 쓰레기로 붙음
            String wrong = new String(buffer, StandardCharsets.UTF_8);
            System.out.println("  잘못: new String(buffer)        = \"" + wrong + "\" (길이 " + wrong.length()
                    + ", 뒤에 널 문자 쓰레기 포함)");

            // 올바른 처리: 0..n 까지만
            String correct = new String(buffer, 0, n, StandardCharsets.UTF_8);
            System.out.println("  올바름: new String(buffer,0,n)  = \"" + correct + "\" (길이 " + correct.length() + ")");
        }
        Files.deleteIfExists(file);

        System.out.println();

        // ===== (B) FileOutputStream append 모드 =====
        Path out = Files.createTempFile("append", ".txt");

        // append=false(기본): 새로 쓸 때마다 덮어씀
        try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
            fos.write("첫번째\n".getBytes(StandardCharsets.UTF_8));
        }
        try (FileOutputStream fos = new FileOutputStream(out.toFile())) { // 기본 = 덮어쓰기
            fos.write("두번째(덮어씀)\n".getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("[append=false 기본] 두 번 썼을 때 내용:");
        System.out.println(Files.readString(out).stripTrailing() + "  <- 첫번째가 사라지고 마지막만 남음");

        // append=true: 이어쓰기
        Files.writeString(out, "A\n");
        try (FileOutputStream fos = new FileOutputStream(out.toFile(), true)) { // ★ append=true
            fos.write("B(이어씀)\n".getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("[append=true] 'A' 뒤에 이어쓰기한 내용:");
        System.out.println(Files.readString(out).stripTrailing() + "  <- A와 B가 누적됨");

        Files.deleteIfExists(out);

        System.out.println();
        System.out.println("=> 버퍼 읽기는 반드시 '읽은 수 n'까지만 처리한다(buffer.length 전체 X).");
        System.out.println("   FileOutputStream은 기본 덮어쓰기, new FileOutputStream(path, true)면 이어쓰기.");
    }
}
