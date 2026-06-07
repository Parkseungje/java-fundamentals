package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * [절차지향 모델] CarData를 "바깥에서" 조작하는 함수들의 모음.
 *
 * C에서 void accelerate(Car* car, int amount)처럼 데이터를 인자로 받아 처리하는 함수
 * 스타일을 자바의 static 메서드로 재현한 것이다. 모든 메서드가 첫 인자로 CarData car를
 * 받는 것에 주목하라 — 이것이 "데이터가 함수 바깥에 있고, 함수가 그 데이터를 넘겨받아
 * 다룬다"는 절차지향의 형태다.
 *
 * 이 클래스가 드러내려는 문제: "규칙이 흩어진다".
 * "속도는 0 미만이 될 수 없다"는 규칙(invariant)을 지키려면, speed를 깎는 모든 함수가
 * 각자 알아서 그 규칙을 검사해야 한다. 함수가 하나 늘어날 때마다 규칙 검사를 빠뜨릴
 * 위험도 함께 늘어난다. 아래 brake와 applyPenalty는 일부러 한쪽만 규칙을 지키고 다른
 * 한쪽은 지키지 않게 만들어, 이 "흩어짐"의 위험을 코드로 보여준다.
 */
public class CarProceduralOps {

    // 가속. speed를 늘리기만 하므로 0 미만 규칙과는 무관하다.
    public static void accelerate(CarData car, int amount) {
        car.speed += amount;
    }

    // 감속(규칙을 지키는 버전).
    // 여기서는 개발자가 "0 미만 금지" 규칙을 기억하고 Math.max(0, ...)로 막아 두었다.
    // 즉 이 함수 하나만 보면 안전해 보인다. 문제는 이 규칙이 이 함수 안에만 있다는 점이다 —
    // speed를 깎는 다른 함수가 같은 보호를 한다는 보장이 전혀 없다(applyPenalty 참고).
    public static void brake(CarData car, int amount) {
        car.speed = Math.max(0, car.speed - amount);
    }

    // 페널티 적용(규칙을 깜빡한 버전).
    // 나중에 추가된 함수라고 가정하자. 개발자가 brake에 클램프가 있다는 사실을 모르거나,
    // "여기도 막아야 한다"는 걸 깜빡하면 이렇게 규칙 없는 코드가 태어난다. 같은 speed 필드를
    // 깎지만 0 미만을 막지 않으므로, 이 함수를 거치면 speed가 음수가 될 수 있다.
    // 이것이 절차지향에서 규칙이 흩어졌을 때의 전형적 사고다: "한 곳은 막았는데 다른 곳에서 샌다."
    // -> Example2_ScatteredRuleViolation에서 brake와 applyPenalty의 결과 차이로 직접 확인한다.
    public static void applyPenalty(CarData car, int amount) {
        car.speed -= amount; // 의도적으로 Math.max 클램프를 누락 — 규칙이 흩어진 상황을 재현
    }

    // 상태를 사람이 읽을 문자열로. CarData 자신은 자기를 설명할 능력이 없으므로 바깥 함수가 대신 한다.
    public static String describe(CarData car) {
        return car.name + "는 현재 시속 " + car.speed + "km로 달리는 중";
    }
}
