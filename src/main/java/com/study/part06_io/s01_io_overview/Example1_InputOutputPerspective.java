package com.study.part06_io.s01_io_overview;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 1 / 3 — I/O의 기준점은 JVM: "외부 → JVM = Input, JVM → 외부 = Output."
 *
 * 이 예시가 답하려는 질문: Input과 Output은 무엇을 기준으로 나뉘는가? 콘솔 출력은 Input인가 Output인가?
 *
 * 왜 이 시나리오인가: I/O에서 헷갈리기 쉬운 게 "어느 방향이 Input/Output이냐"다. 기준은 항상 'JVM(내 프로그램)'이다.
 *   - 외부(파일/네트워크/키보드) -> JVM 으로 들어오면 Input (예: 파일 읽기, DB 조회, 키보드 입력)
 *   - JVM -> 외부(파일/화면/네트워크) 로 나가면 Output (예: 파일 쓰기, 콘솔 출력 System.out)
 * 그래서 System.out.println은 "JVM -> 화면"이므로 Output이다(헷갈리지 말 것). 파일에 글을 쓰고
 * (Output) 다시 읽어와(Input) 이 관점을 코드로 확인한다. (여기선 간결한 NIO.2 Files API 사용)
 *
 * 예상 결과:
 *   - Files.writeString(파일 쓰기) = Output (JVM -> 파일)
 *   - Files.readString(파일 읽기) = Input (파일 -> JVM)
 *   - System.out.println = Output (JVM -> 화면)
 * -> Input/Output은 'JVM 기준'으로 들어오느냐 나가느냐로 나뉜다. 콘솔 출력도 Output이다.
 */
public class Example1_InputOutputPerspective {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] I/O 기준점은 JVM: 외부->JVM=Input, JVM->외부=Output");
        System.out.println();

        // 임시 파일 생성(학습용, 끝나면 삭제)
        Path file = Files.createTempFile("io-demo", ".txt");

        // Output: JVM -> 파일 (글을 '내보낸다')
        String content = "안녕하세요 I/O";
        Files.writeString(file, content);
        System.out.println("[Output] Files.writeString : JVM -> 파일 ('" + content + "' 내보냄)");

        // Input: 파일 -> JVM (글을 '받아온다')
        String read = Files.readString(file);
        System.out.println("[Input ] Files.readString  : 파일 -> JVM (받아온 값 = '" + read + "')");

        // System.out도 Output: JVM -> 화면
        System.out.println("[Output] System.out.println: JVM -> 화면 (지금 이 출력이 Output)");

        Files.deleteIfExists(file); // 정리

        System.out.println();
        System.out.println("=> Input/Output은 'JVM 기준'으로 들어오느냐(Input) 나가느냐(Output)로 나뉜다.");
        System.out.println("   파일 읽기=Input, 파일 쓰기/콘솔 출력=Output. (콘솔 출력이 Output인 점 주의)");
    }
}
