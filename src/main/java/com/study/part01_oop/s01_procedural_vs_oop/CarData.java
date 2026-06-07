package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 1.1 절차지향 스타일 — C의 구조체를 흉내낸 "데이터 보관 전용" 클래스.
 *
 * 이 클래스는 상태(필드)만 가지고 행동(메서드)이 없다.
 * "자동차가 가속한다"는 동작은 이 클래스 밖, CarProceduralOps의 static 메서드에 있다.
 * → 데이터와 그 데이터를 다루는 함수가 분리되어 있는 절차지향 구조를 그대로 재현.
 */
public class CarData {
    public String name;
    public int speed;

    public CarData(String name, int speed) {
        this.name = name;
        this.speed = speed;
    }
}
