package com.study.part02_jvm.s05_method_execution;

/**
 * 예시 2 / 3 — 오버로딩: "무엇을"은 컴파일 시점에 변수의 정적 타입으로 결정된다.
 *
 * 이 예시가 답하려는 질문: describe(Animal)과 describe(Dog) 중 어느 것이 호출될지는, 인자의
 * '실제 객체 타입'으로 정해지나, '변수 선언 타입(정적 타입)'으로 정해지나?
 *
 * 왜 이 시나리오인가: 오버로딩 선택은 '컴파일 시점'에 이뤄진다(= "무엇을 호출할지"). 컴파일러는
 * 인자의 '정적 타입(변수 선언 타입)'만 보고 어느 오버로드를 부를지 결정한다. 실제 객체가 Dog여도,
 * 변수 타입이 Animal이면 describe(Animal)이 선택된다. 이것이 오버라이딩(런타임 결정)과 정반대다.
 *   - Animal a = new Dog();  describe(a) -> 정적 타입 Animal -> describe(Animal) 선택
 *   - Dog d = new Dog();     describe(d) -> 정적 타입 Dog    -> describe(Dog) 선택
 * 단, 선택된 메서드 '안에서' a.sound()를 호출하면 그건 오버라이딩이라 런타임에 Dog.sound가 실행된다.
 *
 * 예상 결과:
 *   - describe(a) [a의 정적 타입 Animal] -> "describe(Animal) 선택됨 ... sound() = 멍멍"
 *       (오버로드는 Animal 버전 선택, 단 sound()는 런타임이라 멍멍)
 *   - describe(d) [d의 정적 타입 Dog]    -> "describe(Dog) 선택됨 ... sound() = 멍멍"
 * -> 오버로딩(무엇을) = 컴파일 시점 정적 타입으로 결정 / 오버라이딩(어디에) = 런타임 실제 객체로 결정.
 *    같은 Dog 객체인데 describe는 변수 타입에 따라 갈리고, sound는 항상 Dog 것 -> 두 메커니즘의 분리.
 */
public class Example2_OverloadingCompileTime {

    public static void main(String[] args) {
        System.out.println("[예시 2] 오버로딩: '무엇을'은 컴파일 시점 정적 타입으로 결정");
        System.out.println();

        Dog dog = new Dog();

        Animal a = dog; // 같은 객체지만 정적 타입은 Animal
        Dog d = dog;    // 같은 객체, 정적 타입은 Dog

        System.out.println("같은 Dog 객체를, 변수 타입만 다르게 해서 describe()에 넘긴다:");
        System.out.println("  describe(a) [정적 타입 Animal] -> " + SoundDescriber.describe(a));
        System.out.println("  describe(d) [정적 타입 Dog]    -> " + SoundDescriber.describe(d));

        System.out.println();
        System.out.println("=> 같은 객체인데 describe는 '변수 타입'에 따라 Animal/Dog 버전으로 갈린다(오버로딩=컴파일 시점).");
        System.out.println("   반면 그 안의 sound()는 항상 'Dog 것'으로 실행된다(오버라이딩=런타임).");
        System.out.println("   => '무엇을(오버로딩)'과 '어디에(오버라이딩)'가 분리되어 있다는 증거.");
    }
}
