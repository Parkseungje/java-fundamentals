package com.study.part01_oop;

/**
 * PART 1.5 다형성 실습용 — 부모 타입 변수가 자식 객체를 가리킬 때
 * 실제로 호출되는 메서드는 "런타임 타입"의 메서드임을 확인한다.
 */
public abstract class Animal {

    private final String name;

    protected Animal(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String makeSound();
}
