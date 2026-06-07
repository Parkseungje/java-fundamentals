package com.study.part02_jvm.s05_method_execution;

/**
 * 예시 1 / 3 — 오버라이딩: "어디에"는 런타임에 결정된다 (실제 객체의 코드 실행).
 *
 * 이 예시가 답하려는 질문: 부모 타입(Animal) 변수에 자식(Dog) 객체를 담고 sound()를 호출하면,
 * 컴파일러가 본 타입(Animal)의 코드가 실행되나, 아니면 실제 객체(Dog)의 코드가 실행되나?
 *
 * 왜 이 시나리오인가: 메서드 호출은 두 단계로 나뉜다.
 *   1) "무엇을 호출하나" — 컴파일러가 시그니처(sound())를 상수 풀에 기록 (Class Metadata 기반)
 *   2) "실제 코드는 어디" — JVM이 런타임에 '실제 객체의 타입'을 보고 그 코드를 찾아 실행
 * 이 둘이 분리되어 있어서, 같은 호출문 a.sound()라도 a가 실제로 가리키는 객체가 Dog면 Dog.sound()가
 * 실행된다. 이것이 오버라이딩(다형성)의 원리다.
 *
 * 예상 결과:
 *   - Animal a = new Dog(); a.sound() -> "멍멍" (Animal.sound가 아니라 실제 객체 Dog.sound 실행)
 * -> "무엇을(시그니처)"은 컴파일 시점에 정해지지만 "어디에(실제 코드)"는 런타임에 실제 객체로 정해진다.
 *    (바이트코드에서는 invokevirtual 명령이 이 런타임 결정을 담당 -> Example3에서 확인)
 */
public class Example1_OverridingRuntime {

    public static void main(String[] args) {
        System.out.println("[예시 1] 오버라이딩: '어디에'는 런타임에 실제 객체로 결정");
        System.out.println();

        Animal asAnimal = new Dog(); // 컴파일 타임 타입 Animal, 런타임 타입 Dog
        System.out.println("Animal a = new Dog(); a.sound() = " + asAnimal.sound()
                + "  <- Animal.sound가 아니라 실제 객체 Dog.sound 실행");

        Animal pureAnimal = new Animal();
        System.out.println("Animal b = new Animal(); b.sound() = " + pureAnimal.sound());

        System.out.println();
        System.out.println("=> 같은 호출문 a.sound()라도 실제 객체에 따라 실행 코드가 갈린다.");
        System.out.println("   '무엇을(시그니처)'은 컴파일 시점, '어디에(실제 코드)'는 런타임에 결정 = 오버라이딩.");
    }
}
