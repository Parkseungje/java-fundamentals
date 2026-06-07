package com.study.part01_oop.s06_abstraction;

/**
 * [모델] 추상클래스 예시. "도형은 넓이를 가진다. 단 넓이를 구하는 방법은 도형마다 다르다."
 *
 * 추상클래스의 특징을 모두 담고 있다.
 *   1. 추상 메서드(area)를 하나라도 가지면 클래스는 abstract여야 한다.
 *   2. 그러면서도 '구현된 메서드'(describe)를 함께 가질 수 있다 -> 공통 구현 재사용.
 *   3. 공통 상태(name 필드)와 생성자를 가질 수 있다.
 *   4. 단, 직접 인스턴스화할 수 없다(new Shape(...) 불가). 자식을 통해서만 만들어진다.
 *
 * 의도는 "is-a"의 강한 관계다: Circle is a Shape, Rectangle is a Shape.
 * 즉 같은 종류(도형)이면서 공통 데이터/구현을 공유하는 계층을 만들 때 추상클래스를 쓴다.
 */
public abstract class Shape {

    // 공통 상태: 모든 도형이 공유하는 필드. 인터페이스에는 이런 인스턴스 필드를 둘 수 없다.
    protected final String name;

    // 생성자: 추상클래스도 생성자를 가질 수 있다. 자식 생성 시 super(name)으로 호출된다.
    protected Shape(String name) {
        this.name = name;
    }

    // 추상 메서드: 구현이 없다. "넓이는 도형마다 다르게 계산된다"는 약속만 한다.
    public abstract double area();

    // 구현된 메서드: 자식들이 공통으로 물려받아 쓰는 코드. 내부에서 추상 메서드 area()를 호출한다
    // (실제 실행은 자식의 area() — 동적 바인딩, 1.5와 연결).
    public String describe() {
        return name + "의 넓이 = " + area();
    }
}
