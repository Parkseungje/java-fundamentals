package com.study.part01_oop;

/**
 * 자기 점검: "캐스팅 전에 instanceof 검사가 왜 필요한가?"
 * → animals[1]은 컴파일 타임 타입이 Animal이라 fetch()를 직접 호출할 수 없다.
 *   Dog의 기능을 쓰려면 instanceof로 실제 타입을 확인 후 캐스팅(또는 패턴 매칭)해야
 *   ClassCastException 없이 안전하게 접근할 수 있다.
 */
public class PolymorphismDemo {

    public static void main(String[] args) {
        Animal[] animals = { new Cat("Nabi"), new Dog("Choco") };

        for (Animal a : animals) {
            // 동적 바인딩: 컴파일 타임 타입은 Animal이지만 실행되는 건 실제 객체의 메서드
            System.out.println(a.getName() + " says " + a.makeSound());

            if (a instanceof Dog d) {
                System.out.println(d.fetch());
            }
        }
    }
}
