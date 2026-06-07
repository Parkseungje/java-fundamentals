package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 예시 2 / 3 — "규칙을 어기는 입력이 들어오면? 그리고 그 규칙이 여러 함수에 흩어지면?"
 *
 * <p><b>이 예시가 답하려는 질문</b>: "속도는 0 미만이 될 수 없다"는 규칙(invariant)을
 * 코드에서 어떻게 강제하는가? 절차지향에서 이 규칙을 한 함수에는 넣고 다른 함수에는 깜빡하면
 * 어떤 일이 벌어지는가? 객체지향은 왜 그런 사고를 구조적으로 막는가?
 *
 * <p><b>왜 이 시나리오인가</b>: 두 단계로 나눠 일부러 규칙을 시험한다.
 * <ol>
 *   <li>1단계 — 가속 50 → 브레이크 80: 가속한 양보다 더 크게 감속해서 0 밑(음수) 경계를 넘겨본다.
 *       절차지향의 {@code brake}는 클램프가 있으므로 여기서는 둘 다 0으로 잘 막힌다.</li>
 *   <li>2단계 — applyPenalty 30: 같은 speed를 깎지만, 절차지향의 {@code applyPenalty}는
 *       클램프를 "깜빡한" 함수다. 같은 규칙이 함수마다 따로 관리되기 때문에 생기는 구멍을 노린다.</li>
 * </ol>
 *
 * <p><b>예상 결과</b>:
 * <ul>
 *   <li>1단계: 절차지향·객체지향 모두 speed = 0 (절차지향도 brake에는 클램프가 있으므로).</li>
 *   <li>2단계: <b>절차지향은 speed = -30</b> (applyPenalty가 규칙을 안 지킴) /
 *       <b>객체지향은 speed = 0</b> (모든 변경이 단일 통로 changeSpeedBy를 거쳐 규칙이 자동 적용).</li>
 * </ul>
 * → 핵심 교훈: 절차지향에서 규칙은 "함수마다 알아서 지켜야 하는 약속"이라 한 곳만 빠뜨려도 샌다.
 * 객체지향은 규칙을 한 곳(private 통로)에 모아 두므로, 새 동작을 추가해도 규칙이 따라온다.
 */
public class Example2_ScatteredRuleViolation {

    public static void main(String[] args) {
        System.out.println("[예시 2] 규칙 위반 + 규칙이 함수마다 흩어진 상황");
        System.out.println();

        // ===== 1단계: 가속(50)보다 큰 브레이크(80) — 음수 경계를 넘겨본다 =====
        System.out.println("--- 1단계: 가속 50 → 브레이크 80 (둘 다 brake에는 클램프가 있음) ---");

        CarData pCar = new CarData("절차지향-Car", 0);
        CarProceduralOps.accelerate(pCar, 50);
        CarProceduralOps.brake(pCar, 80); // brake 내부 Math.max(0, ...)로 음수 방지
        System.out.println("(A) 절차지향 brake 후 speed = " + pCar.speed + " (클램프 작동, 0)");

        Car oCar = new Car("객체지향-Car");
        oCar.accelerate(50);
        oCar.brake(80);
        System.out.println("(B) 객체지향 brake 후 speed = " + oCar.getSpeed() + " (클램프 작동, 0)");

        System.out.println();

        // ===== 2단계: 같은 규칙을 깜빡한 다른 함수(applyPenalty)를 호출 =====
        System.out.println("--- 2단계: applyPenalty(30) 호출 ---");
        System.out.println("    절차지향 applyPenalty는 클램프를 깜빡한 함수 / 객체지향은 같은 단일 통로를 거침");

        CarProceduralOps.applyPenalty(pCar, 30); // 현재 0에서 30을 더 깎음 → 클램프 없음
        System.out.println("(A) 절차지향 applyPenalty 후 speed = " + pCar.speed
                + "  ← ❗ 음수! 규칙이 흩어져서 한 함수에서 샜다");

        oCar.applyPenalty(30); // 현재 0에서 30을 더 깎지만 changeSpeedBy가 다시 0으로 막음
        System.out.println("(B) 객체지향 applyPenalty 후 speed = " + oCar.getSpeed()
                + "  ← 규칙이 단일 통로에 모여 있어 그대로 0 유지");

        System.out.println();
        System.out.println("=> 절차지향: brake는 막았지만 applyPenalty에서 규칙이 새어 음수(-30)가 됨.");
        System.out.println("   객체지향: 어떤 동작을 거치든 changeSpeedBy 한 곳에서 규칙을 강제 → 항상 0 이상.");
    }
}
