package com.study.part02_jvm.s05_method_execution;

/**
 * [모델] Animal의 sound()를 오버라이딩한 자식.
 * 부모 타입(Animal) 변수에 담겨도 sound() 호출 시 이 Dog.sound()가 런타임에 선택된다.
 */
public class Dog extends Animal {
    @Override
    public String sound() {
        return "멍멍";
    }
}
