package com.study.part02_jvm.s05_method_execution;

/**
 * [모델] '오버로딩'된 메서드 두 개. 이름은 같지만 매개변수 타입이 다르다.
 *
 * describe(Animal)와 describe(Dog) 중 어느 것이 호출될지는 '컴파일 시점'에 인자의 '정적 타입
 * (변수 선언 타입)'으로 결정된다. 이것이 오버라이딩(런타임 결정)과 정반대 지점이며,
 * "무엇을 호출할지"가 컴파일 시점에 고정된다는 것을 보여준다(-> Example2).
 */
public class SoundDescriber {

    // 오버로딩 1: 매개변수 타입이 Animal
    public static String describe(Animal a) {
        return "describe(Animal) 선택됨 -> a.sound() = " + a.sound();
    }

    // 오버로딩 2: 매개변수 타입이 Dog
    public static String describe(Dog d) {
        return "describe(Dog) 선택됨 -> d.sound() = " + d.sound();
    }
}
