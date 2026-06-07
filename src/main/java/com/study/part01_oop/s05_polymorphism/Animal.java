package com.study.part01_oop.s05_polymorphism;

/**
 * [모델] 다형성 계층의 부모. 추상 메서드 makeSound()를 둔다.
 *
 * abstract로 둔 이유: "모든 동물은 소리를 내지만, 그 소리는 동물마다 다르다"를 표현하기 위함.
 * 부모는 "소리를 낸다"는 약속(메서드 시그니처)만 정하고, 실제 구현은 자식이 각자 한다(오버라이딩).
 * 이렇게 하면 부모 타입 변수 하나로 여러 자식을 가리켜도, 호출 시 '실제 객체'의 구현이 실행된다
 * (동적 바인딩 -> Example1에서 확인).
 */
public abstract class Animal {

    protected final String name;

    protected Animal(String name) {
        this.name = name;
    }

    // 자식마다 다르게 구현할 행동. 부모는 "있다"는 약속만 한다.
    public abstract String makeSound();

    public String getName() {
        return name;
    }
}
