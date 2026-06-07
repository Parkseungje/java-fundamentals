package com.study.part01_oop.s06_abstraction;

/**
 * 예시 1 / 3 — "추상클래스는 무엇을 할 수 있고, 무엇을 막는가?"
 *
 * 이 예시가 답하려는 질문: 추상클래스는 (1) 추상 메서드와 구현 메서드를 함께 가질 수 있는가?
 * (2) 공통 상태/구현을 자식이 재사용하는가? (3) 직접 인스턴스화는 정말 막히는가?
 *
 * 왜 이 시나리오인가: Circle/Rectangle을 만들어 부모 Shape 타입으로 다룬다. 두 자식은 area()를
 * 각자 다르게 구현하지만, describe()는 Shape에서 물려받아 공통으로 쓴다. describe() 내부는
 * area()를 호출하는데, 실제로는 각 자식의 area()가 실행된다(동적 바인딩). 그리고 new Shape(...)는
 * 컴파일 자체가 안 된다는 점을 주석으로 확인한다.
 *
 * 예상 결과:
 *   - circle.describe() -> "원의 넓이 = 78.53..."     (Circle.area() 사용)
 *   - rect.describe()   -> "직사각형의 넓이 = 24.0"   (Rectangle.area() 사용)
 *   - new Shape("x")    -> 컴파일 에러 (추상클래스는 직접 인스턴스화 불가)
 * -> 추상클래스는 "공통(상태 name + 구현 describe)은 모아주고, 다른 부분(area)만 자식이 채우게"
 *    하는 도구다. 의도는 is-a 강한 관계(Circle is a Shape).
 */
public class Example1_AbstractClass {

    public static void main(String[] args) {
        System.out.println("[예시 1] 추상클래스: 추상 메서드 + 구현 메서드 혼합, 공통 재사용, 인스턴스화 불가");
        System.out.println();

        // 부모(Shape) 타입으로 자식들을 다룬다. (다형성 — 1.5)
        Shape circle = new Circle(5);
        Shape rect = new Rectangle(4, 6);

        // describe()는 Shape에서 물려받은 '구현 메서드'. 내부의 area()는 각 자식 것이 실행된다.
        System.out.println(circle.describe());
        System.out.println(rect.describe());

        // new Shape("도형");  // <- 컴파일 에러: Shape is abstract; cannot be instantiated
        System.out.println();
        System.out.println("new Shape(...) 는 주석 처리됨 — 추상클래스는 직접 인스턴스화할 수 없다.");

        System.out.println();
        System.out.println("=> 추상클래스는 공통 상태/구현(name, describe)은 모아주고,");
        System.out.println("   도형마다 다른 부분(area)만 자식이 채우게 한다. 의도는 is-a 강한 관계.");
    }
}
