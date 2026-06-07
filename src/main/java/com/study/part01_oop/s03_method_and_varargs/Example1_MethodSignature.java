package com.study.part01_oop.s03_method_and_varargs;

/**
 * 예시 1 / 3 — "메서드 시그니처는 어떤 요소로 이루어지는가?"
 *
 * 이 예시가 답하려는 질문: 메서드는 [접근제어자] [반환타입] 이름(매개변수)로 구성된다고 한다.
 * 각 요소가 실제로 어떤 역할을 하는지, 반환값이 있는 메서드와 없는(void) 메서드, 매개변수가
 * 있는 메서드와 없는 메서드가 각각 어떻게 호출되고 어떤 결과를 주는지 직접 본다.
 *
 * 왜 이 시나리오인가: SimpleLogger의 세 가지 서로 다른 시그니처(format/printLine/answer)를
 * 한 번씩 호출해, "반환타입이 있으면 그 값을 받아서 쓸 수 있고, void면 받을 값이 없다",
 * "매개변수 개수만큼 인자를 넘겨야 한다"는 시그니처의 기본 규칙을 결과로 확인한다.
 *
 * 예상 결과:
 *   - format(반환 String): 반환값을 변수에 담아 출력 -> "[INFO] 안녕하세요"
 *   - printLine(void): 반환값이 없으므로 변수에 담을 수 없고, 호출 자체가 출력 동작
 *   - answer(매개변수 0개, 반환 int): 인자 없이 호출, 42를 돌려받음
 */
public class Example1_MethodSignature {

    public static void main(String[] args) {
        System.out.println("[예시 1] 메서드 시그니처 구성요소: 접근제어자 / 반환타입 / 이름 / 매개변수 / return");
        System.out.println();

        SimpleLogger logger = new SimpleLogger();

        // 시그니처 A: 반환타입 String -> 돌려받은 값을 변수 line에 담아 재사용할 수 있다.
        String line = logger.format("INFO", "안녕하세요");
        System.out.println("format 반환값을 변수에 담음 -> " + line);

        // 시그니처 B: 반환타입 void -> 돌려받을 값이 없다. 호출 자체가 '출력'이라는 동작.
        // String x = logger.printLine("x");  // <- void는 값을 못 받으므로 이렇게 쓰면 컴파일 에러
        System.out.print("printLine 호출(반환값 없음) -> ");
        logger.printLine("이 줄은 printLine이 직접 출력");

        // 시그니처 C: 매개변수 0개, 반환 int -> 인자 없이 호출하고 정수를 돌려받는다.
        int a = logger.answer();
        System.out.println("answer() 매개변수 없이 호출 -> 반환 " + a);

        System.out.println();
        System.out.println("=> 반환타입이 있으면 return 값을 받아서 쓸 수 있고, void면 받을 값이 없다.");
        System.out.println("   매개변수 개수/타입은 '호출 시 무엇을 넘겨야 하는지'를 강제한다.");
    }
}
