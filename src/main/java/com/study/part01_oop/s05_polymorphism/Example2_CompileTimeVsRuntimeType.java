package com.study.part01_oop.s05_polymorphism;

/**
 * 예시 2 / 3 — "컴파일 타임 타입과 런타임 타입은 다르다. 그래서 자식 고유 메서드는 어떻게 부르나?"
 *
 * 이 예시가 답하려는 질문: Animal a = new Dog(); 에서 변수 a의 '컴파일 타임 타입'은 Animal,
 * '런타임 타입'(실제 객체)은 Dog다. 이때 Dog에만 있는 fetch()를 호출하려면 어떻게 해야 하나?
 *
 * 왜 이 시나리오인가: a.makeSound()는 Animal에 선언돼 있으니 바로 호출되지만, a.fetch()는
 * Animal에 없으므로 컴파일러가 "Animal에는 fetch()가 없다"며 막는다(컴파일 타임 타입 기준 검사).
 * 실제로는 Dog 객체이니 fetch()가 분명히 존재한다 -> 이 간극을 instanceof + 캐스팅으로 메운다.
 * Java 16+의 instanceof 패턴 매칭(if (a instanceof Dog d))을 쓰면 검사와 캐스팅이 한 번에 된다.
 *
 * 예상 결과:
 *   - a.makeSound() : 바로 호출됨 -> "멍멍" (Animal에 선언된 메서드라 컴파일 OK + 동적 바인딩)
 *   - a.fetch()     : 그냥 호출하면 컴파일 에러(아래 주석). instanceof로 Dog 확인 후 호출하면 성공.
 * -> 변수의 타입(컴파일 타임)은 "무엇을 호출할 수 있는지"를 제한하고,
 *    실제 객체의 타입(런타임)은 "실제로 무엇이 실행되는지"를 결정한다. 둘은 별개다.
 */
public class Example2_CompileTimeVsRuntimeType {

    public static void main(String[] args) {
        System.out.println("[예시 2] 컴파일 타임 타입(Animal) vs 런타임 타입(Dog)");
        System.out.println();

        // 컴파일 타임 타입은 Animal, 런타임 타입(실제 객체)은 Dog.
        Animal a = new Dog("초코");

        // makeSound()는 Animal에 선언돼 있으므로 변수 타입이 Animal이어도 바로 호출 가능.
        System.out.println("a.makeSound() -> " + a.makeSound() + "  (Animal에 선언됨, 컴파일 OK)");

        // a.fetch();  // <- 컴파일 에러: cannot find symbol method fetch() in type Animal
        //                  실제로는 Dog지만, 컴파일러는 '변수 타입 Animal'만 보고 판단하기 때문.
        System.out.println("a.fetch() 는 그냥은 호출 불가 (Animal 타입에는 fetch()가 없으므로 컴파일 에러)");

        System.out.println();

        // 해결: instanceof로 실제 타입을 확인하고, 그 안에서 Dog로 다룬다.
        // Java 16+ 패턴 매칭: 조건이 참이면 변수 d가 이미 Dog로 캐스팅되어 바인딩된다.
        if (a instanceof Dog d) {
            System.out.println("instanceof Dog 확인 후 d.fetch() -> " + d.fetch());
        }

        System.out.println();
        System.out.println("=> 변수 타입(컴파일 타임)은 '호출 가능한 메서드 범위'를 제한하고,");
        System.out.println("   실제 객체 타입(런타임)은 '실제 실행될 구현'을 정한다. 자식 고유 메서드는");
        System.out.println("   instanceof로 런타임 타입을 확인한 뒤 캐스팅해서 호출한다.");
    }
}
