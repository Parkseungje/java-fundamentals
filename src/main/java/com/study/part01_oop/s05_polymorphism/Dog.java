package com.study.part01_oop.s05_polymorphism;

/**
 * [모델] Animal의 자식. makeSound()를 개답게 오버라이딩하고, 개만의 행동 fetch()를 추가한다.
 *
 * fetch()는 부모 Animal에는 없는 '자식 고유 메서드'다. 그래서 부모 타입(Animal) 변수로는
 * 호출할 수 없고, 실제 타입(Dog)을 확인/캐스팅해야 부를 수 있다 (-> Example2, Example3).
 */
public class Dog extends Animal {

    public Dog(String name) {
        super(name);
    }

    @Override
    public String makeSound() {
        return "멍멍";
    }

    // Dog만의 고유 행동. Animal 타입에는 존재하지 않는다.
    public String fetch() {
        return name + "가 공을 물어온다";
    }
}
