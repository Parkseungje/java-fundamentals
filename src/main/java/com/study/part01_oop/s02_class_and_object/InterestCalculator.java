package com.study.part01_oop.s02_class_and_object;

/**
 * [모델] 상태가 없는 클래스 — 행동(메서드)만 있고 필드(상태)가 없다.
 *
 * BankAccount와 정반대다. BankAccount는 "각 객체마다 다른 잔액"이라는 상태를 기억해야 했지만,
 * 이자 계산은 "입력을 받아 결과를 돌려주는" 순수한 행동일 뿐 기억할 상태가 없다.
 * 같은 입력에는 항상 같은 출력 -> 객체마다 다르게 기억할 것이 없다.
 *
 * 이 클래스로 자기 점검 질문에 답한다: "상태 없이 행동만 있는 클래스는 의미가 있을까?"
 *   - 의미는 있다. 계산기·유틸리티처럼 "상태가 필요 없는 행동의 묶음"은 흔하고 유용하다.
 *   - 다만 상태가 없으므로 인스턴스를 여러 개 만들 이유가 없다. 모든 인스턴스가 완전히 동일하다.
 *     (-> Example3에서 인스턴스 2개가 사실상 구별 불가능함을 확인)
 *   - 그래서 이런 클래스는 보통 static 메서드로 두거나(아래 calculateStatic), 인스턴스를
 *     하나만 공유(싱글톤)한다. "인스턴스를 굳이 만들 필요가 있는가"가 상태 유무의 판별 기준이 된다.
 */
public class InterestCalculator {

    // 필드(상태)가 하나도 없다는 점에 주목. 이 클래스는 아무것도 "기억"하지 않는다.

    // 인스턴스 메서드지만 인스턴스 상태(this.xxx)를 전혀 사용하지 않는다.
    // 오직 인자로 받은 값만으로 결과를 계산한다 -> 어느 인스턴스로 호출하든 결과가 같다.
    public int calculate(int principal, double rate) {
        return (int) (principal * rate);
    }

    // 위 메서드는 사실 상태가 없으므로 static으로 두는 편이 자연스럽다.
    // (객체를 만들지 않고 InterestCalculator.calculateStatic(...)으로 바로 호출)
    public static int calculateStatic(int principal, double rate) {
        return (int) (principal * rate);
    }
}
