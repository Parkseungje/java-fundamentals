package com.study.part06_io.s01_io_overview;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 2 / 3 — File → Files/Path 진화: "boolean(이유 모름) vs 정확한 예외."
 *
 * 이 예시가 답하려는 질문: 옛 File API와 NIO.2의 Files API는 실패를 어떻게 알려주는가?
 *
 * 왜 이 시나리오인가: 자바 1.0의 java.io.File은 설계가 낡았다. 대표적으로 File.delete()는 성공/실패를
 * boolean으로만 돌려준다 — false가 나오면 "실패했다"는 것만 알 뿐 '왜'(파일이 없어서? 권한이 없어서?
 * 잠겨서?)는 알 수 없다. NIO.2(Java 7+)의 Files.delete(Path)는 실패 시 '구체적인 예외'를 던진다
 * (없으면 NoSuchFileException 등). 그래서 원인을 정확히 알고 대처할 수 있다. 없는 파일을 두 방식으로
 * 삭제 시도해 차이를 본다. (Files API는 메서드가 모두 static이고 Path를 인자로 받는 점도 특징)
 *
 * 예상 결과:
 *   - new File("없는파일").delete() -> false (이유는 알 수 없음)
 *   - Files.delete(없는 Path)       -> NoSuchFileException (원인이 명확)
 * -> File은 실패를 boolean으로 뭉개고, Files는 정확한 예외로 알려준다. 그래서 신규 코드는 Files/Path를 쓴다.
 */
public class Example2_FileVsFiles {

    public static void main(String[] args) {
        System.out.println("[예시 2] File.delete()(boolean) vs Files.delete()(정확한 예외)");
        System.out.println();

        // 존재하지 않는 경로
        String missing = System.getProperty("java.io.tmpdir") + "/__nonexistent_io_demo__.txt";

        // 옛 방식: File.delete() -> boolean (실패해도 이유 모름)
        File file = new File(missing);
        boolean deleted = file.delete();
        System.out.println("[File ] new File(...).delete() = " + deleted + "  <- false지만 '왜' 실패했는지 모름");

        // 새 방식: Files.delete(Path) -> 실패 시 구체적 예외
        Path path = Path.of(missing);
        System.out.print("[Files] Files.delete(path)     -> ");
        try {
            Files.delete(path);
            System.out.println("삭제 성공(여기 도달 안 함)");
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + " (원인이 명확: 파일이 없음)");
        }

        System.out.println();
        System.out.println("=> File은 실패를 boolean으로 뭉개 '왜'를 못 알려준다. Files는 정확한 예외로 알려준다.");
        System.out.println("   Files API는 모두 static + Path 인자(NIO.2). 그래서 신규 코드는 Files/Path를 쓴다.");
    }
}
