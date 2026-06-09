package com.study.part06_io.s03_byte_stream;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 1 / 3 — 바이트 스트림과 한글 깨짐: "1바이트씩 읽으면 영어는 OK, 한글은 깨진다."
 *
 * 이 예시가 답하려는 질문: 바이트 스트림(InputStream)으로 1바이트씩 읽어 문자로 만들면 왜 한글이 깨지나?
 *
 * 왜 이 시나리오인가: 바이트 스트림은 이름 그대로 '바이트(byte) 단위'로만 다룬다. 영어 알파벳은
 * UTF-8에서 1글자 = 1바이트라, 1바이트를 그대로 char로 바꿔도 맞다. 하지만 한글은 UTF-8에서
 * 1글자 = 3바이트다. 그래서 1바이트씩 끊어 char로 만들면, 한글 한 글자가 3개의 깨진 조각으로
 * 쪼개진다(각 바이트가 의미 없는 문자가 됨). "Hi한글"을 써놓고 1바이트씩 읽어 깨짐을 직접 확인하고,
 * 반대로 전체 바이트를 UTF-8로 한꺼번에 디코딩하면 제대로 나온다는 것도 비교한다(문자 스트림은 6.4).
 *
 * 예상 결과:
 *   - "Hi한글"의 UTF-8 바이트 수 = 2(영어) + 6(한글 2자 x 3바이트) = 8바이트
 *   - 1바이트씩 (char)로 변환: H, i 는 정상, 한글 부분은 깨진 문자들로 출력
 *   - 전체 바이트를 new String(bytes, UTF_8)로 디코딩: "Hi한글" 정상
 * -> 바이트 스트림은 '바이트'만 안다. 한글처럼 여러 바이트로 된 문자를 1바이트씩 쪼개면 깨진다.
 *    문자를 제대로 다루려면 '인코딩을 아는' 문자 스트림(Reader/Writer)이 필요하다(6.4 예고).
 */
public class Example1_ByteStreamHangul {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] 바이트 스트림 1바이트씩 -> 한글 깨짐");
        System.out.println();

        Path file = Files.createTempFile("byte-hangul", ".txt");
        String original = "Hi한글";
        Files.write(file, original.getBytes(StandardCharsets.UTF_8));

        // 각 글자가 UTF-8에서 몇 바이트인지 확인
        System.out.println("원본: \"" + original + "\"");
        System.out.println("UTF-8 총 바이트 수 = " + original.getBytes(StandardCharsets.UTF_8).length
                + " (영어 H,i = 1바이트씩, 한글 한 글자 = 3바이트씩)");

        // 1바이트씩 읽어 char로 변환 -> 한글 깨짐
        System.out.print("1바이트씩 읽어 (char) 변환: \"");
        try (InputStream in = new FileInputStream(file.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                System.out.print((char) b); // 1바이트를 그대로 문자로 -> 한글은 깨진다
            }
        }
        System.out.println("\"  <- H,i는 정상이지만 한글은 깨짐(3바이트가 3조각으로 쪼개짐)");

        // 비교: 전체 바이트를 UTF-8로 한꺼번에 디코딩하면 정상
        byte[] all = Files.readAllBytes(file);
        String decoded = new String(all, StandardCharsets.UTF_8);
        System.out.println("전체 바이트를 UTF-8로 디코딩  : \"" + decoded + "\"  <- 정상(인코딩을 알고 묶어서 해석)");

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> 바이트 스트림은 '바이트'만 안다. 한글(3바이트)을 1바이트씩 쪼개면 깨진다.");
        System.out.println("   문자를 제대로 다루려면 인코딩을 아는 '문자 스트림(Reader/Writer)'이 필요하다(6.4).");
    }
}
