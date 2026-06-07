package com.study.part01_oop.s05_polymorphism;

/**
 * [모델] Animal의 또 다른 자식. makeSound()를 고양이답게 오버라이딩하고, 고양이만의 행동
 * scratch()를 추가한다.
 *
 * Dog와 Cat이 같은 부모(Animal)를 다르게 구현하기 때문에, 부모 타입으로 묶어 다뤄도
 * 각자 다른 소리가 난다(동적 바인딩). Example3에서는 "Cat을 Dog로 잘못 캐스팅"하는 실수를
 * 일부러 일으켜 ClassCastException을 재현하는 데 쓰인다.
 */
public class Cat extends Animal {

    public Cat(String name) {
        super(name);
    }

    @Override
    public String makeSound() {
        return "야옹";
    }

    // Cat만의 고유 행동.
    public String scratch() {
        return name + "가 발톱을 세운다";
    }
}
