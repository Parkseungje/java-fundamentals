package com.study.part06_io.s04_char_stream;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 2 / 3 — FileReader vs InputStreamReader: "인코딩을 명시할 수 있느냐."
 *
 * 이 예시가 답하려는 질문: 같은 '문자 스트림'인데 FileReader와 InputStreamReader는 무슨 차이인가?
 * 왜 InputStreamReader를 권장하나?
 *
 * 왜 이 시나리오인가: Reader가 한글을 제대로 읽으려면 '파일이 어떤 인코딩으로 저장됐는지'와
 * 'Reader가 어떤 인코딩으로 해석할지'가 일치해야 한다. 문제는 인코딩을 명시하는 방법이다.
 *   - (옛) FileReader: 인코딩을 인자로 못 받고 '플랫폼 기본 charset'을 쓴다(Windows 한국어=MS949 등).
 *     그래서 UTF-8로 저장된 파일을 기본이 다른 환경에서 FileReader로 읽으면 깨질 수 있다.
 *   - InputStreamReader: 생성자에 charset을 '명시'할 수 있다. 파일 인코딩에 맞춰 정확히 해석.
 * 이를 보이려고 EUC-KR로 저장된 한글 파일을 만든 뒤, 잘못된 인코딩(UTF-8)으로 읽으면 깨지고,
 * 맞는 인코딩(EUC-KR)을 명시하면 정상임을 InputStreamReader로 비교한다(인코딩 명시의 위력).
 *
 * 예상 결과:
 *   - EUC-KR 파일을 InputStreamReader(UTF-8)로 읽기 -> 한글 깨짐(인코딩 불일치)
 *   - 같은 파일을 InputStreamReader(EUC-KR)로 읽기   -> 한글 정상(인코딩 일치)
 * -> 핵심은 "Reader라고 다 되는 게 아니라 인코딩이 맞아야 한다". InputStreamReader는 인코딩을
 *    명시할 수 있어 안전하다. FileReader는 기본 charset에 의존해 환경에 따라 깨질 수 있어 권장되지 않는다.
 *    (Java 11+의 FileReader는 charset 인자를 받는 생성자가 추가됐지만, 인코딩을 항상 명시하는 습관이 중요.)
 */
public class Example2_FileReaderVsInputStreamReader {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] FileReader(기본 charset) vs InputStreamReader(인코딩 명시)");
        System.out.println();

        Charset eucKr = Charset.forName("EUC-KR");
        Path file = Files.createTempFile("euckr", ".txt");
        Files.write(file, "한글테스트".getBytes(eucKr)); // EUC-KR로 저장
        System.out.println("파일을 EUC-KR 인코딩으로 저장: \"한글테스트\"");
        System.out.println();

        // 잘못된 인코딩(UTF-8)으로 읽기 -> 깨짐
        System.out.print("InputStreamReader(UTF-8)로 읽기 : \"");
        try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
            printAll(r);
        }
        System.out.println("\"  <- 깨짐 (파일은 EUC-KR인데 UTF-8로 해석)");

        // 맞는 인코딩(EUC-KR)을 명시해서 읽기 -> 정상
        System.out.print("InputStreamReader(EUC-KR)로 읽기: \"");
        try (Reader r = new InputStreamReader(Files.newInputStream(file), eucKr)) {
            printAll(r);
        }
        System.out.println("\"  <- 정상 (인코딩을 맞게 명시)");

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> Reader라고 무조건 되는 게 아니라 '인코딩이 맞아야' 한다. InputStreamReader는");
        System.out.println("   charset을 명시할 수 있어 안전. FileReader는 플랫폼 기본 charset에 의존해");
        System.out.println("   환경에 따라 깨질 수 있다(인코딩은 항상 명시하는 습관이 중요).");
    }

    private static void printAll(Reader r) throws Exception {
        int c;
        while ((c = r.read()) != -1) System.out.print((char) c);
    }
}
