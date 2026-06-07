package com.study.part01_oop.s06_abstraction;

/**
 * [모델] Shape를 상속한 원. 추상 메서드 area()를 자기 방식으로 구현한다.
 * describe()는 Shape에서 물려받아 그대로 쓴다(공통 구현 재사용).
 */
public class Circle extends Shape {

    private final double radius;

    public Circle(double radius) {
        super("원");
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}
