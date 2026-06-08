package com.study.part04_collections.s01_string_pool;

/**
 * 예시 2 / 3 — 불변성(Immutability): "String을 '수정'하는 메서드는 원본을 바꾸지 않고 새 객체를 만든다."
 *
 * 이 예시가 답하려는 질문: toUpperCase()나 concat() 같은 메서드를 호출하면 원본 문자열이 바뀌는가,
 * 아니면 새 문자열이 만들어지는가?
 *
 * 왜 이 시나리오인가: String은 한 번 만들어지면 내부 내용을 절대 바꿀 수 없는 '불변(immutable)'
 * 객체다. 그래서 toUpperCase/concat/replace 같은 "수정처럼 보이는" 메서드는 사실 원본을 그대로 두고
 * '새 String을 만들어 반환'한다. 이를 확인하려고, 메서드 호출 후 원본이 그대로인지, 반환값이 원본과
 * 다른 객체인지를 == 와 출력으로 검사한다.
 *
 * 예상 결과:
 *   - s.toUpperCase() 호출 후에도 s는 "hello" 그대로, 반환값은 "HELLO"(새 객체)
 *   - s.concat(" world") 후에도 s는 "hello" 그대로
 *   - 즉 String은 "수정"이 불가능하고, 모든 변경 메서드는 새 객체를 만든다.
 * -> 이 불변성 때문에 루프에서 String을 += 로 이어붙이면 매번 새 객체가 생겨 비효율적이다.
 *    (그 문제를 푸는 StringBuilder는 4.2에서 다룬다.)
 */
public class Example2_Immutability {

    public static void main(String[] args) {
        System.out.println("[예시 2] String 불변성: 수정 메서드는 새 객체를 반환, 원본은 그대로");
        System.out.println();

        String s = "hello";
        System.out.println("원본 s = \"" + s + "\"");

        // toUpperCase: 원본을 바꾸지 않고 새 문자열 반환
        String upper = s.toUpperCase();
        System.out.println("s.toUpperCase() = \"" + upper + "\"");
        System.out.println("호출 후 s = \"" + s + "\"  <- 그대로 (원본 불변)");
        System.out.println("s == upper ? " + (s == upper) + "  <- 다른 객체(새로 만들어짐)");

        System.out.println();

        // concat: 역시 새 문자열 반환
        String joined = s.concat(" world");
        System.out.println("s.concat(\" world\") = \"" + joined + "\"");
        System.out.println("호출 후 s = \"" + s + "\"  <- 여전히 그대로");

        System.out.println();
        System.out.println("=> String은 '수정'이 불가능하다. 변경 메서드는 모두 새 객체를 만든다.");
        System.out.println("   그래서 루프에서 += 로 이어붙이면 객체가 폭증한다 -> StringBuilder 필요(4.2).");
    }
}
