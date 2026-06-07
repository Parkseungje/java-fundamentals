package com.study.part01_oop.s05_polymorphism;

/**
 * 예시 3 / 3 — 자기 점검: "캐스팅 전에 instanceof 검사가 왜 필요한가?"
 *
 * 이 예시가 답하려는 질문: Example2에서는 instanceof로 확인하고 캐스팅했다. 그 확인을 건너뛰고
 * 바로 캐스팅하면 무슨 일이 벌어지나? 왜 굳이 instanceof를 먼저 해야 하나?
 *
 * 왜 이 시나리오인가: 실제로는 Cat인 객체를 Animal 타입 변수에 담아두고, 이를 Dog로 잘못
 * 캐스팅해 fetch()를 호출하려 시도한다. 컴파일러는 "Animal -> Dog 캐스팅"이 문법적으로
 * 가능하다고 보아 통과시키지만(둘 다 Animal 계열이므로), 실제 객체가 Dog가 아니므로
 * 런타임에 ClassCastException이 터진다. 이어서 instanceof로 먼저 검사하면 그 폭발을 어떻게
 * 안전하게 피하는지 보여준다.
 *
 * 예상 결과:
 *   - 검사 없이 (Dog) cat 캐스팅 -> 런타임에 ClassCastException 발생(잡아서 메시지 출력)
 *   - instanceof Dog 검사 -> false이므로 캐스팅을 시도조차 하지 않고 안전하게 건너뜀
 * -> 자기 점검 답: instanceof 검사는 "이 객체가 정말 그 타입인가"를 런타임에 미리 확인해서,
 *    잘못된 캐스팅으로 인한 ClassCastException(런타임 크래시)을 예방한다.
 *    캐스팅은 컴파일러가 막아주지 못하는(같은 계열이면 문법상 허용) 위험한 연산이기 때문이다.
 */
public class Example3_WrongCastWithoutCheck {

    public static void main(String[] args) {
        System.out.println("[예시 3] instanceof 없이 잘못 캐스팅하면? (Cat을 Dog로 착각)");
        System.out.println();

        // 실제 객체는 Cat인데, 부모 타입 Animal 변수에 담겨 있어 겉으론 구분이 안 된다.
        Animal a = new Cat("나비");

        // (1) 검사 없이 바로 Dog로 캐스팅 시도 — 컴파일은 통과하지만 런타임에 터진다.
        System.out.println("(1) 검사 없이 (Dog) a 캐스팅 시도:");
        try {
            Dog wrong = (Dog) a;          // 실제는 Cat -> 여기서 ClassCastException
            System.out.println("    " + wrong.fetch()); // 도달하지 못함
        } catch (ClassCastException e) {
            System.out.println("    ClassCastException 발생! -> " + e.getMessage());
        }

        System.out.println();

        // (2) instanceof로 먼저 확인 — 안전하게 폭발을 피한다.
        System.out.println("(2) instanceof Dog 로 먼저 확인:");
        if (a instanceof Dog d) {
            System.out.println("    " + d.fetch());
        } else {
            System.out.println("    a는 Dog가 아니므로 캐스팅을 시도하지 않고 안전하게 건너뜀");
        }

        System.out.println();
        System.out.println("=> 자기 점검 답: 캐스팅은 같은 계열이면 컴파일러가 막지 못한다(문법상 허용).");
        System.out.println("   그래서 instanceof로 '실제 그 타입이 맞는지' 런타임에 먼저 확인해야");
        System.out.println("   ClassCastException(런타임 크래시)을 예방할 수 있다.");
    }
}
