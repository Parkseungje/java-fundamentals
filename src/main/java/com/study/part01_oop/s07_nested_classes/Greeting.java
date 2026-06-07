package com.study.part01_oop.s07_nested_classes;

/**
 * [모델] 익명 클래스 / 람다 비교용 인터페이스.
 *
 * 추상 메서드가 1개뿐인 인터페이스(함수형 인터페이스)다. 이런 인터페이스는
 *   - 익명 클래스로 즉석 구현할 수도 있고(전통 방식)
 *   - 람다로 더 짧게 표현할 수도 있다(Java 8+, 익명 클래스의 축약형)
 * 이 둘을 Example3에서 나란히 비교한다. (함수형 인터페이스/람다 자체는 PART 5에서 심화)
 */
public interface Greeting {
    String greet(String name);
}
