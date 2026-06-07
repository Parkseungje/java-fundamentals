package com.study.part01_oop.s06_abstraction;

/**
 * [모델] Shape를 상속한 직사각형. area()를 원과는 다르게 구현한다.
 * 같은 추상 메서드라도 자식마다 구현이 다르다는 점이 핵심.
 */
public class Rectangle extends Shape {

    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        super("직사각형");
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}
