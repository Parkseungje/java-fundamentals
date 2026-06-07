package com.study.part01_oop.s02_class_and_object;

/**
 * 예시 3 / 3 — 자기 점검: "상태 없이 행동만 있는 클래스는 의미가 있을까?"
 *
 * 이 예시가 답하려는 질문: 예시 1·2의 BankAccount는 '상태(잔액)'가 있었기에 객체마다 다른
 * 의미를 가졌다. 그렇다면 상태가 전혀 없는 클래스(InterestCalculator)는? 그런 클래스도 쓸모가
 * 있나? 있다면 객체를 여러 개 만드는 게 의미가 있나?
 *
 * 왜 이 시나리오인가: 두 가지를 직접 확인한다.
 *   1. 상태 없는 클래스도 행동(계산)은 정상적으로 수행한다 -> 쓸모는 분명히 있다.
 *   2. 그런데 인스턴스를 2개 만들어 같은 입력을 주면 결과가 완전히 같다 -> 인스턴스를 구별할
 *      이유가 없다(상태가 없으니 기억하는 게 없어서 모든 인스턴스가 동일).
 *
 * 예상 결과:
 *   - calc1.calculate(10000, 0.03)와 calc2.calculate(10000, 0.03)가 둘 다 300으로 동일.
 *   - 즉 객체를 따로 만든 의미가 없다. 그래서 이런 클래스는 static 메서드로 두거나
 *     인스턴스 하나만 공유하는 게 자연스럽다(calculateStatic으로 객체 없이 호출).
 * -> 결론(자기 점검 답): 상태 없는 클래스도 "행동의 묶음"으로서 의미가 있다(유틸리티/계산기).
 * 다만 "상태가 없다 = 객체별로 다를 게 없다"이므로, 인스턴스를 여러 개 만들 이유가 없다는 점이
 * 상태 있는 클래스(BankAccount)와의 결정적 차이다.
 */
public class Example3_StatelessClass {

    public static void main(String[] args) {
        System.out.println("[예시 3] 상태 없는 클래스(InterestCalculator)는 의미가 있는가?");
        System.out.println();

        // 1. 행동은 정상 수행 — 쓸모는 있다.
        InterestCalculator calc1 = new InterestCalculator();
        InterestCalculator calc2 = new InterestCalculator();

        int r1 = calc1.calculate(10000, 0.03);
        int r2 = calc2.calculate(10000, 0.03);
        System.out.println("calc1.calculate(10000, 0.03) = " + r1);
        System.out.println("calc2.calculate(10000, 0.03) = " + r2);

        // 2. 인스턴스를 따로 만들었지만 같은 입력에 같은 결과 -> 구별할 이유가 없다.
        System.out.println("두 인스턴스의 결과가 같은가? " + (r1 == r2)
                + "  <- 상태가 없으니 어느 인스턴스로 호출하든 동일");

        System.out.println();

        // 3. 그래서 객체를 만들 필요 없이 static으로 호출하는 게 자연스럽다.
        int r3 = InterestCalculator.calculateStatic(10000, 0.03);
        System.out.println("객체 없이 static 호출 calculateStatic(10000, 0.03) = " + r3);

        System.out.println();
        System.out.println("=> 자기 점검 답: 상태 없는 클래스도 '행동의 묶음'으로 의미가 있다(유틸리티/계산기).");
        System.out.println("   단, 상태가 없으면 인스턴스를 여러 개 만들 이유가 없다 — 이것이");
        System.out.println("   상태 있는 BankAccount(객체마다 다른 잔액)와의 결정적 차이다.");
    }
}
