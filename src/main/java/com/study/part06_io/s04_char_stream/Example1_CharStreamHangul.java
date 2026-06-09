package com.study.part06_io.s04_char_stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 1 / 3 — 문자 스트림(Reader): "char 단위로 인코딩을 해석해 한글이 깨지지 않는다."
 *
 * 이 예시가 답하려는 질문: 6.3에서 바이트 스트림으로 1바이트씩 읽으면 한글이 깨졌다. 문자 스트림
 * (Reader)은 무엇이 다르길래 한글이 정상으로 나오나?
 *
 * 왜 이 시나리오인가: Reader/Writer는 '바이트'가 아니라 '문자(char)' 단위로 다룬다. 핵심은 Reader가
 * '인코딩(charset)을 알고' 여러 바이트를 묶어 하나의 문자로 '해석(디코딩)'해준다는 점이다. 그래서
 * 한글(UTF-8 3바이트)도 read()가 한 번에 '한 글자'를 돌려준다(바이트 조각이 아니라). 6.3과 같은
 * "Hi한글"을 Reader로 읽어 정상 출력됨을 확인하고, Reader.read()도 EOF에서 -1을 반환하지만 그 값은
 * '바이트'가 아니라 '문자 코드'라는 점을 본다.
 *
 * 예상 결과:
 *   - Reader로 한 글자씩 읽으면 H, i, 한, 글 이 정상적으로 나온다(바이트 조각이 아님).
 *   - BufferedReader.readLine()으로 줄 단위로 읽어도 한글 정상.
 * -> Reader는 인코딩을 해석해 '문자' 단위로 읽으므로 다국어(한글 등)가 깨지지 않는다.
 *    (6.3의 바이트 스트림은 인코딩을 모르고 바이트만 다뤄 깨졌다 — 이것이 그 해결편이다.)
 */
public class Example1_CharStreamHangul {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] 문자 스트림(Reader): char 단위 해석 -> 한글 정상");
        System.out.println();

        Path file = Files.createTempFile("char-hangul", ".txt");
        Files.write(file, "Hi한글".getBytes(StandardCharsets.UTF_8));

        // Reader로 '한 글자씩' 읽기 — InputStreamReader가 UTF-8을 알고 디코딩한다
        System.out.print("Reader로 한 글자씩 읽기: \"");
        try (Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            int c;
            while ((c = reader.read()) != -1) { // read()가 '문자 코드'를 반환(바이트 아님)
                System.out.print((char) c);     // 한글도 한 번에 한 글자로 나온다
            }
        }
        System.out.println("\"  <- 6.3과 달리 한글이 정상! (Reader가 3바이트를 묶어 '한' 글자로 해석)");

        // BufferedReader.readLine()으로 줄 단위 읽기(실무에서 가장 흔한 방식)
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            System.out.println("BufferedReader.readLine(): \"" + br.readLine() + "\"");
        }

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> Reader/Writer는 '바이트'가 아니라 '문자(char)' 단위로, 인코딩을 해석해 다룬다.");
        System.out.println("   그래서 한글(여러 바이트)도 한 글자로 읽혀 깨지지 않는다. (6.3 깨짐의 해결편)");
    }
}
