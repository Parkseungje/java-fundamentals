package com.study.part01_oop.s06_abstraction;

/**
 * 예시 2 / 3 — "인터페이스는 왜 다중 구현이 되고, 그것으로 무엇을 표현하는가?"
 *
 * 이 예시가 답하려는 질문: 클래스는 하나만 상속할 수 있는데(1.4 다이아몬드 문제), 인터페이스는
 * 여러 개를 동시에 구현할 수 있다. 이걸로 무엇을 표현하나? "종류"가 아니라 "능력의 조합"이다.
 *
 * 왜 이 시나리오인가: Duck은 Flyable과 Swimmable을 동시에 구현한다 -> "날 수도 헤엄칠 수도"
 * 있는 객체. 반면 Airplane은 Flyable만 구현한다(종류는 오리와 전혀 다른 기계지만 '나는 능력'은 공유).
 * 같은 Flyable 타입 변수로 오리와 비행기를 함께 다뤄, "종류가 달라도 능력이 같으면 한 타입으로
 * 묶인다"는 can-do 사고를 확인한다.
 *
 * 예상 결과:
 *   - duck은 fly()와 swim() 둘 다 가능 (두 능력 조합)
 *   - airplane은 fly()만 가능 (swim()은 없음)
 *   - Flyable[]에 duck과 airplane을 함께 담아 fly() 호출 가능 (종류 무관, 능력 기준)
 * -> 추상클래스가 "is-a 한 줄기 계층"이라면, 인터페이스는 "여러 능력을 자유롭게 조합"하는 도구다.
 *    그리고 이 다중 구현이 클래스 다중 상속(금지됨)의 보완책이다.
 */
public class Example2_InterfaceMultiImpl {

    public static void main(String[] args) {
        System.out.println("[예시 2] 인터페이스 다중 구현 = 능력(can-do)의 조합");
        System.out.println();

        Duck duck = new Duck("도널드");
        Airplane airplane = new Airplane();

        // 오리는 두 능력을 모두 가진다.
        System.out.println("오리(Flyable + Swimmable):");
        duck.fly();
        duck.swim();

        System.out.println();

        // 비행기는 나는 능력만. (airplane.swim()은 컴파일 에러 — Swimmable을 구현하지 않았으므로)
        System.out.println("비행기(Flyable만):");
        airplane.fly();

        System.out.println();

        // 종류(오리/비행기)가 달라도 '나는 능력'이 있으면 같은 Flyable 타입으로 묶을 수 있다.
        System.out.println("Flyable 타입 하나로 종류가 다른 둘을 함께 다루기:");
        Flyable[] flyers = { duck, airplane };
        for (Flyable f : flyers) {
            f.fly();
        }

        System.out.println();
        System.out.println("=> 인터페이스는 '종류(is-a)'가 아니라 '능력(can-do)'으로 묶는다.");
        System.out.println("   한 클래스가 여러 인터페이스를 구현해 능력을 조합할 수 있고,");
        System.out.println("   이 다중 구현이 클래스 다중 상속(다이아몬드 문제로 금지)의 보완책이다.");
    }
}
