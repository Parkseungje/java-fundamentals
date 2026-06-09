package com.study.part05_generics_functional.s04_functional_stream;

/**
 * [모델] 직접 만든 함수형 인터페이스.
 *
 * 함수형 인터페이스 = 추상 메서드가 '정확히 1개'인 인터페이스. 그래서 람다로 인스턴스화할 수 있다.
 * @FunctionalInterface를 붙이면 컴파일러가 "추상 메서드가 1개인지" 검증한다(2개면 컴파일 에러).
 * 필수는 아니지만 의도를 명확히 하고 실수를 막아준다.
 *
 * 추상 메서드가 1개뿐이므로, 람다 (a, b) -> a + b 가 곧 이 calculate의 구현이 된다(-> Example1).
 */
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b);
}
