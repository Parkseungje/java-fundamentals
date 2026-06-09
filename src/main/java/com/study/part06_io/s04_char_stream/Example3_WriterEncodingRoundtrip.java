package com.study.part06_io.s04_char_stream;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 3 / 3 — Writer와 인코딩 round-trip: "쓸 때와 읽을 때 인코딩이 같아야 한글이 보존된다."
 *
 * 이 예시가 답하려는 질문: Writer로 한글을 쓸 때도 인코딩이 중요한가? 쓰기/읽기 인코딩이 다르면?
 *
 * 왜 이 시나리오인가: Writer(OutputStreamWriter/FileWriter)는 '문자(char)'를 받아 '지정한 인코딩으로
 * 바이트로 변환(인코딩)'해 파일에 쓴다. 즉 파일에는 결국 바이트가 저장되고, '어떤 인코딩으로 썼는지'가
 * 그 바이트의 의미를 결정한다. 따라서 나중에 읽을 때 '같은 인코딩'으로 디코딩해야 원래 글자가 복원된다.
 * 같은 한글을 UTF-8로 써놓고, (1) UTF-8로 읽으면 정상, (2) EUC-KR로 읽으면 깨짐 — 이 round-trip을
 * 비교해 "쓰기 인코딩 == 읽기 인코딩"이 핵심임을 확인한다.
 *
 * 예상 결과:
 *   - OutputStreamWriter(UTF-8)로 "안녕 Writer" 쓰기
 *   - InputStreamReader(UTF-8)로 읽기   -> 정상(쓰기/읽기 인코딩 일치)
 *   - InputStreamReader(EUC-KR)로 읽기 -> 깨짐(불일치)
 * -> Writer가 한글을 쓸 수 있게 해주지만, '쓴 인코딩 = 읽는 인코딩'이어야 보존된다. 인코딩은 쓰기·읽기
 *    양쪽에서 일관되게 명시하는 게 안전하다(보통 UTF-8로 통일).
 */
public class Example3_WriterEncodingRoundtrip {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] Writer로 한글 쓰기 + 인코딩 round-trip");
        System.out.println();

        Path file = Files.createTempFile("writer", ".txt");
        String text = "안녕 Writer";

        // Writer로 UTF-8로 쓰기 (char -> UTF-8 바이트로 인코딩해 저장)
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(text);
        }
        System.out.println("OutputStreamWriter(UTF-8)로 쓴 글: \"" + text + "\"");
        System.out.println();

        // (1) 같은 인코딩(UTF-8)으로 읽기 -> 정상
        System.out.print("UTF-8로 읽기  : \"");
        try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            printAll(r);
        }
        System.out.println("\"  <- 정상 (쓰기/읽기 인코딩 일치)");

        // (2) 다른 인코딩(EUC-KR)으로 읽기 -> 깨짐
        System.out.print("EUC-KR로 읽기 : \"");
        try (Reader r = new InputStreamReader(Files.newInputStream(file), Charset.forName("EUC-KR"))) {
            printAll(r);
        }
        System.out.println("\"  <- 깨짐 (UTF-8로 썼는데 EUC-KR로 해석)");

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> Writer는 char를 '지정 인코딩으로' 바이트로 변환해 쓴다. 읽을 때 같은 인코딩이어야");
        System.out.println("   원래 글자가 복원된다. 쓰기·읽기 인코딩을 일관되게 명시(보통 UTF-8)하는 게 안전하다.");
    }

    private static void printAll(Reader r) throws Exception {
        int c;
        while ((c = r.read()) != -1) System.out.print((char) c);
    }
}
