package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 1.1 절차지향 → 객체지향 — "왜 OOP가 등장했나"를 실행 결과로 직접 확인한다.
 *
 * 두 시나리오를 비교:
 *   (A) CarData + CarProceduralOps  — 데이터와 함수가 분리된 절차지향 스타일
 *   (B) Car                          — 상태+행동이 캡슐화된 객체지향 스타일
 *
 * 같은 "버그 시나리오"(brake를 너무 많이 호출)를 양쪽에 똑같이 적용해서
 * 절차지향에서는 막을 수 없는 문제가 객체지향에서는 구조적으로 막힌다는 걸 보여준다.
 */
public class ProceduralVsOopDemo {

    public static void main(String[] args) {
        System.out.println("=== (A) 절차지향 스타일 ===");
        proceduralScenario();

        System.out.println();
        System.out.println("=== (B) 객체지향 스타일 ===");
        oopScenario();
    }

    private static void proceduralScenario() {
        CarData car = new CarData("Procedural-Car", 0);

        CarProceduralOps.accelerate(car, 50);
        System.out.println(CarProceduralOps.describe(car));

        // 버그 시나리오: 가속한 양보다 더 많이 brake
        CarProceduralOps.brake(car, 80);
        System.out.println(CarProceduralOps.describe(car));
        System.out.println("→ speed = " + car.speed + " (음수가 그대로 노출됨, 규칙을 강제할 곳이 없음)");

        // 캡슐화 부재: 필드가 public이라 함수를 거치지 않고도 직접 조작 가능
        car.speed = 9999;
        System.out.println("→ 외부에서 car.speed = 9999 직접 대입 가능: " + car.speed);
    }

    private static void oopScenario() {
        Car car = new Car("OOP-Car");

        car.accelerate(50);
        System.out.println(car.describe());

        // 같은 버그 시나리오를 적용해도...
        car.brake(80);
        System.out.println(car.describe());
        System.out.println("→ speed = " + car.getSpeed() + " (brake() 내부 규칙 덕분에 0에서 멈춤)");

        // car.speed = 9999;  // 컴파일 에러: speed는 private — 캡슐화로 원천 차단
        System.out.println("→ car.speed 직접 접근은 컴파일 단계에서 막힌다 (private 필드)");
    }
}
