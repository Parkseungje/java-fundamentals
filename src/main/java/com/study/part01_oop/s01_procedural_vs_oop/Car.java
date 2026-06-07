package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * [객체지향 모델] 상태(speed)와 행동(accelerate/brake/applyPenalty)을 하나의 객체로 묶은 자동차.
 *
 * <p>{@link CarData} + {@link CarProceduralOps} 조합과 정확히 대비된다.
 * <ul>
 *   <li><b>필드가 private이다.</b> 외부에서 {@code car.speed = -100} 같은 직접 조작이 불가능하다.
 *       speed를 바꾸는 유일한 통로는 이 클래스가 제공하는 메서드뿐이다 → "데이터가 어디서 바뀌는가?"의
 *       답이 항상 "이 클래스 안"으로 좁혀진다(캡슐화). (→ {@link Example3_EncapsulationBreach})</li>
 *   <li><b>규칙이 한 곳에 모여 있다.</b> "0 미만 금지" 규칙을 각 메서드에 흩뿌리지 않고,
 *       speed를 변경하는 모든 메서드가 {@link #changeSpeedBy(int)}라는 단일 통로를 거치게 했다.
 *       그래서 새 동작(applyPenalty 등)을 추가해도 규칙을 다시 구현하거나 깜빡할 일이 없다.
 *       이것이 절차지향의 "규칙 흩어짐" 문제({@link CarProceduralOps#applyPenalty})에 대한 해법이다.</li>
 * </ul>
 */
public class Car {

    private final String name;
    private int speed;

    public Car(String name) {
        this.name = name;
        this.speed = 0;
    }

    /** 가속: 내부적으로도 단일 통로를 거친다. */
    public void accelerate(int amount) {
        changeSpeedBy(amount);
    }

    /** 감속: 마찬가지로 단일 통로를 거치므로 0 미만 규칙이 자동 적용된다. */
    public void brake(int amount) {
        changeSpeedBy(-amount);
    }

    /**
     * 페널티 적용: {@link CarProceduralOps#applyPenalty}와 "이름·의도"는 같지만 결과가 다르다.
     * 절차지향 버전은 클램프를 깜빡해 음수가 났지만, 여기서는 speed를 바꾸는 모든 길이
     * {@link #changeSpeedBy(int)}를 통과하므로 개발자가 따로 신경 쓰지 않아도 규칙이 지켜진다.
     */
    public void applyPenalty(int amount) {
        changeSpeedBy(-amount);
    }

    /**
     * speed를 변경하는 <b>유일한 내부 통로</b>. "0 미만 금지" 규칙을 여기 단 한 번만 구현한다.
     * private이므로 외부는 이 메서드의 존재조차 알 필요가 없고, 내부의 모든 변경은 여기로 수렴한다.
     * → 규칙을 "한 곳에 모아 강제한다"는 캡슐화의 실체.
     */
    private void changeSpeedBy(int delta) {
        this.speed = Math.max(0, this.speed + delta);
    }

    public String describe() {
        return name + "는 현재 시속 " + speed + "km로 달리는 중";
    }

    /** 읽기 전용 접근자. 외부는 값을 "읽을" 수만 있고 "쓸" 수는 없다. */
    public int getSpeed() {
        return speed;
    }
}
