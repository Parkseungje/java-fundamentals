package com.study.part06_io.s05_buffered_data;

/**
 * 예시 1 / 3 — try-with-resources: "자동 close + 여러 자원은 선언 역순으로 닫힌다."
 *
 * 이 예시가 답하려는 질문: 스트림 같은 자원을 안전하게 닫으려면? try-with-resources는 무엇을 자동화하나?
 *
 * 왜 이 시나리오인가: 스트림/소켓 등은 OS 자원을 쓰므로 사용 후 반드시 close해야 한다. 옛날에는
 * try 안에서 close를 호출했는데, try 도중 예외가 나면 close 줄에 도달하지 못해 자원이 새는 문제가
 * 있었다. 그래서 finally에서 close하는 장황한 코드(+ close 자체의 예외 처리)가 필수였다.
 * try-with-resources(Java 7+)는 `try (자원 선언) { ... }` 형태로, 블록을 벗어날 때(정상이든 예외든)
 * 자동으로 close를 호출해준다. 자원이 여러 개면 '선언의 역순'으로 닫는다(나중에 연 것을 먼저 닫음 —
 * 의존 관계상 안전). 단 그 자원은 AutoCloseable을 구현해야 한다. AutoCloseable을 구현한 가짜 자원으로
 * close 호출과 순서, 그리고 예외 시에도 닫히는지 관찰한다.
 *
 * 예상 결과:
 *   - try-with-resources 블록을 벗어나면 자원이 자동 close 된다.
 *   - 두 자원을 A, B 순서로 열면 close는 B -> A 역순.
 *   - try 안에서 예외가 나도 close는 호출된다(자원 누수 없음).
 * -> try-with-resources는 "쓰고 나면 자동으로, 예외가 나도, 역순으로 닫아준다". 옛 finally 보일러플레이트를 없앤다.
 */
public class Example1_TryWithResources {

    // AutoCloseable을 구현해야 try-with-resources에 쓸 수 있다.
    static class Resource implements AutoCloseable {
        private final String name;
        Resource(String name) {
            this.name = name;
            System.out.println("  open  " + name);
        }
        void use() {
            System.out.println("  use   " + name);
        }
        @Override
        public void close() {
            System.out.println("  close " + name + "  (자동 호출)");
        }
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] try-with-resources: 자동 close + 역순 close");
        System.out.println();

        // (1) 정상 흐름: 여러 자원 선언 -> 블록 끝에서 역순으로 자동 close
        System.out.println("(1) 정상 흐름 (A, B 순서로 열기):");
        try (Resource a = new Resource("A");
             Resource b = new Resource("B")) {
            a.use();
            b.use();
        } // 여기서 자동 close: 선언 역순 B -> A
        System.out.println("  -> close 순서가 B, A (선언 역순)인 것에 주목");

        System.out.println();

        // (2) 예외가 나도 close는 호출된다
        System.out.println("(2) try 안에서 예외 발생 시에도 close 되는가:");
        try (Resource c = new Resource("C")) {
            c.use();
            throw new RuntimeException("작업 중 예외!");
        } catch (RuntimeException e) {
            System.out.println("  catch: " + e.getMessage());
        }
        System.out.println("  -> 예외가 났는데도 C가 close 됐다 (자원 누수 없음)");

        System.out.println();
        System.out.println("=> try-with-resources는 블록을 벗어날 때(정상/예외 무관) 자동 close, 여러 개면 역순.");
        System.out.println("   옛날엔 finally에서 일일이 close(+예외 처리)해야 했던 보일러플레이트를 없앤다.");
    }
}
