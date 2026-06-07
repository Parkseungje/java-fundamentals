package com.study.part01_oop.s05_polymorphism;

/**
 * 예시 1 / 3 — "부모 타입 변수로 자식 객체를 가리키면, 어느 메서드가 실행되는가?"
 *
 * 이 예시가 답하려는 질문: Animal 타입 변수에 Dog/Cat 객체를 담고 makeSound()를 호출하면,
 * Animal의 것이 실행될까(변수 타입 기준) 아니면 실제 담긴 객체의 것이 실행될까(객체 기준)?
 *
 * 왜 이 시나리오인가: 같은 Animal[] 배열에 Dog와 Cat을 섞어 담고, 동일한 코드 a.makeSound()를
 * 반복 호출한다. 만약 "실제 객체의 메서드가 실행된다(동적 바인딩)"는 설명이 맞다면, 똑같은
 * a.makeSound() 호출인데도 Dog 자리에서는 "멍멍", Cat 자리에서는 "야옹"이 나와야 한다.
 *
 * 예상 결과:
 *   초코(Dog) -> 멍멍
 *   나비(Cat) -> 야옹
 * -> 변수의 타입(Animal)이 아니라 '실제 가리키는 객체'의 오버라이딩된 메서드가 실행된다.
 *    이것이 동적 바인딩(dynamic binding)이며, 다형성의 핵심이다. 호출하는 쪽 코드는
 *    "Animal"만 알면 되고, 구체 타입이 무엇인지 몰라도 각 객체가 알아서 자기 방식으로 동작한다.
 */
public class Example1_DynamicBinding {

    public static void main(String[] args) {
        System.out.println("[예시 1] 부모 타입(Animal)으로 묶어도, 실제 객체의 makeSound()가 실행된다");
        System.out.println();

        // 부모 타입 배열 하나에 서로 다른 자식 객체들을 담는다.
        Animal[] animals = { new Dog("초코"), new Cat("나비") };

        // 똑같은 코드 a.makeSound() 를 반복 — 그런데 결과는 객체마다 다르다.
        for (Animal a : animals) {
            System.out.println(a.getName() + " -> " + a.makeSound());
        }

        System.out.println();
        System.out.println("=> 같은 a.makeSound() 호출인데 멍멍/야옹으로 갈린다.");
        System.out.println("   변수 타입(Animal)이 아니라 '실제 객체'의 메서드가 실행된다 = 동적 바인딩.");
    }
}
