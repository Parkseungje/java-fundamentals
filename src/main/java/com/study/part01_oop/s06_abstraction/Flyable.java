package com.study.part01_oop.s06_abstraction;

/**
 * [모델] "날 수 있다"는 능력(can-do)을 나타내는 인터페이스.
 *
 * 인터페이스의 의도는 "is-a"(같은 종류)가 아니라 "can-do"(어떤 능력이 있다)다.
 * 비행기도 날고 오리도 난다 — 둘은 전혀 다른 종류지만 '나는 능력'은 공유한다.
 * 이런 "종류는 다르지만 능력은 같은" 경우를 인터페이스로 묶는다.
 *
 * Java 8 default 메서드(describeFlight)도 함께 둔다 -> Example3에서 "인터페이스에 기능을
 * 나중에 추가해도 기존 구현체가 안 깨지는" 하위 호환을 보여주는 데 쓰인다.
 */
public interface Flyable {

    // 추상 메서드: 구현체가 반드시 구현해야 하는 능력. (인터페이스의 메서드는 기본 public abstract)
    void fly();

    // Java 8 default 메서드: 인터페이스가 '구현(본문)'을 직접 제공한다.
    // 이 메서드는 "나중에 Flyable에 추가되었다"고 가정하자. default가 있으므로 기존 구현체들이
    // 이 메서드를 오버라이드하지 않아도 컴파일이 깨지지 않는다(기본 동작을 물려받음).
    default String describeFlight() {
        return "기본 비행 설명: 무언가가 날고 있다";
    }
}
