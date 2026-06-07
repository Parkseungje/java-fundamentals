package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 예시 1 / 3 — "정상 범위 안에서는 두 방식이 다른가?"
 *
 * 이 예시가 답하려는 질문: 절차지향과 객체지향의 차이는 "평범하게 잘 쓸 때"부터 드러나는가?
 * 아니면 무언가 잘못될 때(규칙 위반·우회)에만 드러나는가?
 *
 * 왜 이 시나리오인가: 일부러 규칙을 어기지 않는 안전한 입력만 사용한다
 * (가속 60 -> 브레이크 20 -> 최종 40, 한 번도 0 밑으로 가지 않음). 이렇게 하면 두 방식의
 * "결과"가 같은지 비교할 수 있다.
 *
 * 예상 결과: 두 방식 모두 최종 speed = 40으로 동일하게 나올 것이다. 즉 정상 흐름에서는
 * 절차지향이든 객체지향이든 겉보기 결과에 차이가 없다.
 * -> 이로써 "두 패러다임의 차이는 정상 동작이 아니라, 예시 2·3에서 다룰 비정상 상황에서
 * 비로소 드러난다"는 점을 먼저 못 박아 둔다. (예시 2, 3과의 대비를 위한 기준선 역할)
 */
public class Example1_NormalFlow {

    public static void main(String[] args) {
        System.out.println("[예시 1] 정상 범위 시나리오: 가속 60 -> 브레이크 20 (한 번도 0 밑으로 가지 않음)");
        System.out.println();

        // (A) 절차지향: 데이터(CarData)를 함수(CarProceduralOps)에 넘겨서 다룬다
        CarData proceduralCar = new CarData("절차지향-Car", 0);
        CarProceduralOps.accelerate(proceduralCar, 60);
        CarProceduralOps.brake(proceduralCar, 20);
        System.out.println("(A) 절차지향: " + CarProceduralOps.describe(proceduralCar)
                + "  -> speed = " + proceduralCar.speed);

        // (B) 객체지향: 객체(Car)가 스스로의 상태를 자기 메서드로 다룬다
        Car oopCar = new Car("객체지향-Car");
        oopCar.accelerate(60);
        oopCar.brake(20);
        System.out.println("(B) 객체지향: " + oopCar.describe()
                + "  -> speed = " + oopCar.getSpeed());

        System.out.println();
        System.out.println("=> 두 결과가 동일하다(40). 정상 흐름만 보면 차이가 없다.");
        System.out.println("   진짜 차이는 예시 2(규칙 위반)와 예시 3(캡슐화 우회)에서 드러난다.");
    }
}
