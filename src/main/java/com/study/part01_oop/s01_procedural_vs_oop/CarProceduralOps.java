package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 1.1 절차지향 스타일 — "자동차를 가속시키는 함수"가 데이터(CarData) 밖에 따로 존재한다.
 *
 * 로우레벨의 불편함을 코드로 보여주는 지점:
 *   - CarData.speed를 누가, 어디서, 어떻게 바꾸는지 추적하려면
 *     CarData를 인자로 받는 모든 static 메서드를 찾아다녀야 한다.
 *   - CarData에는 "0 이하로 내려가면 안 된다" 같은 규칙을 강제할 방법이 없다.
 *     (필드가 public이라 누구나 직접 대입 가능 → 캡슐화 부재)
 */
public class CarProceduralOps {

    public static void accelerate(CarData car, int amount) {
        car.speed += amount;
    }

    public static void brake(CarData car, int amount) {
        car.speed -= amount;
        // 절차지향 버전의 함정: 규칙(0 미만 금지)을 깜빡하면 그대로 음수가 된다.
        // 이 메서드를 호출하는 곳마다 검증을 반복해야 하고, 빠뜨리면 버그가 된다.
    }

    public static String describe(CarData car) {
        return car.name + "는 현재 시속 " + car.speed + "km로 달리는 중";
    }
}
