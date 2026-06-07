package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * [절차지향 모델] C의 구조체(struct)를 흉내 낸 "데이터 보관 전용" 클래스.
 *
 * <p>이 클래스의 핵심 특징은 두 가지다.
 * <ol>
 *   <li><b>행동(메서드)이 전혀 없다.</b> "자동차가 가속한다/감속한다"는 동작은 이 클래스 안이 아니라
 *       바깥의 {@link CarProceduralOps}에 static 함수로 분리되어 있다. 즉 데이터와 그 데이터를
 *       다루는 함수가 물리적으로 떨어져 있다 — 이것이 절차지향의 본질적 구조다.</li>
 *   <li><b>모든 필드가 {@code public}이다.</b> 이는 우연이 아니라 의도된 설계다. C 구조체의 멤버는
 *       기본적으로 외부에서 자유롭게 접근·수정 가능하므로, 그 상황을 그대로 재현하기 위해 public으로 둔다.
 *       이 때문에 "규칙을 거치지 않고 데이터를 직접 망가뜨릴 수 있는" 문제가 발생한다
 *       (→ {@link Example3_EncapsulationBreach}에서 직접 확인).</li>
 * </ol>
 *
 * <p>비교 대상: 같은 자동차 개념을 객체지향으로 모델링한 {@link Car}.
 * Car는 필드를 private으로 감추고 행동을 자기 안에 갖는다는 점에서 정반대다.
 */
public class CarData {

    /** 자동차 이름. public이므로 외부에서 직접 읽고 쓸 수 있다(절차지향 재현 목적). */
    public String name;

    /**
     * 현재 속도. public이라 외부에서 {@code car.speed = -100} 같은 잘못된 값도 막을 수 없다.
     * "속도는 0 미만이 될 수 없다"는 규칙을 이 데이터 자체는 강제하지 못한다는 점이 핵심.
     */
    public int speed;

    public CarData(String name, int speed) {
        this.name = name;
        this.speed = speed;
    }
}
