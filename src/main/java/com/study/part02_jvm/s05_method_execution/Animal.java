package com.study.part02_jvm.s05_method_execution;

/**
 * [모델] 메서드 실행 메커니즘 관찰용 부모 클래스.
 *
 * sound()는 자식(Dog)이 오버라이딩한다. 부모 타입 변수로 sound()를 호출했을 때,
 * "무엇을 호출할지(시그니처: Animal.sound)"는 컴파일 시점에 정해지지만, "실제 어느 코드를
 * 실행할지(실제 객체의 sound)"는 런타임에 정해진다. 이 분리가 오버라이딩(다형성)을 가능케 한다.
 */
public class Animal {
    public String sound() {
        return "동물 소리";
    }
}
