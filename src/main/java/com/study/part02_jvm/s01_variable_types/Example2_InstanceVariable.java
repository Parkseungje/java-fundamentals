package com.study.part02_jvm.s01_variable_types;

/**
 * 예시 2 / 3 — 인스턴스 변수(Instance Variable): "Heap에 객체와 함께 저장되고, 객체마다 따로다."
 *
 * 이 예시가 답하려는 질문: 두 객체가 같은 클래스(Counter)에서 만들어졌을 때, 인스턴스 변수
 * instanceCount는 두 객체가 공유하는가, 각자 따로 갖는가?
 *
 * 왜 이 시나리오인가: Counter 객체 두 개(c1, c2)를 만들고 c1만 여러 번 countUp()한다. 만약
 * "인스턴스 변수는 new로 만들 때 Heap에 객체별로 따로 생긴다"는 설명이 맞다면, c1의 카운트만
 * 올라가고 c2의 카운트는 그대로여야 한다(서로 독립).
 *
 * 예상 결과: c1.instanceCount = 3, c2.instanceCount = 0
 * -> 인스턴스 변수는 각 객체가 Heap에 자기 전용 공간을 갖기 때문에 객체끼리 값이 섞이지 않는다.
 *    (1.2에서 본 '객체별 독립 상태'를, 여기서는 "Heap에 객체와 함께 저장된다"는 저장 위치 관점으로 본다)
 *    다음 예시(클래스 변수)와 정반대 결과가 나온다는 점이 핵심 대비다.
 */
public class Example2_InstanceVariable {

    public static void main(String[] args) {
        System.out.println("[예시 2] 인스턴스 변수: 객체마다 Heap에 따로 저장 -> 값이 독립적");
        System.out.println();

        Counter c1 = new Counter();
        Counter c2 = new Counter();

        // c1만 3번 증가시킨다.
        c1.countUp();
        c1.countUp();
        c1.countUp();

        System.out.println("c1.instanceCount = " + c1.instanceCount + "  <- 3번 올림");
        System.out.println("c2.instanceCount = " + c2.instanceCount + "  <- 건드리지 않음(독립)");

        System.out.println();
        System.out.println("=> c1을 올려도 c2는 그대로. 인스턴스 변수는 객체마다 Heap에 별도로 존재한다.");
        System.out.println("   (다음 Example3의 static 변수와 정반대 결과 — 비교해볼 것)");
    }
}
