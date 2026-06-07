package com.study.part01_oop.s06_abstraction;

/**
 * [모델] "헤엄칠 수 있다"는 능력(can-do)을 나타내는 인터페이스.
 *
 * Flyable과는 독립적인 별개의 능력이다. 한 클래스가 Flyable과 Swimmable을 '동시에' 구현하면
 * "날 수도 있고 헤엄칠 수도 있는" 객체가 된다(-> Duck). 이것이 인터페이스 다중 구현으로
 * 능력을 조합하는 방식이며, 클래스 다중 상속(다이아몬드 문제로 금지됨, 1.4)의 보완책이다.
 */
public interface Swimmable {
    void swim();
}
