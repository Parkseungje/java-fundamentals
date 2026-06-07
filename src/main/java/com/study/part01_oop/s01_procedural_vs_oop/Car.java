package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 1.1 객체지향 해결 — 상태(speed)와 행동(accelerate/brake)을 하나의 객체로 묶는다.
 *
 * CarProceduralOps와 비교할 핵심 차이:
 *   1. speed는 private — 외부에서 직접 대입 불가, 오직 이 클래스의 메서드를 통해서만 변경된다.
 *      → "데이터가 어디서 어떻게 바뀌는가"를 추적할 범위가 이 클래스 안으로 캡슐화된다.
 *   2. "0 미만으로 내려가지 않는다"는 규칙이 brake() 안에 단 한 번 구현되어 있다.
 *      → 호출하는 쪽에서 매번 검증을 반복할 필요가 없다.
 */
public class Car {

    private final String name;
    private int speed;

    public Car(String name) {
        this.name = name;
        this.speed = 0;
    }

    public void accelerate(int amount) {
        this.speed += amount;
    }

    public void brake(int amount) {
        this.speed = Math.max(0, this.speed - amount);
    }

    public String describe() {
        return name + "는 현재 시속 " + speed + "km로 달리는 중";
    }

    public int getSpeed() {
        return speed;
    }
}
