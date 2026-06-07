package com.study.part02_jvm.s01_variable_types;

/**
 * 예시 3 / 3 — 클래스 변수(static Variable): "Method Area에 단 1개, 모든 객체가 공유한다."
 *
 * 이 예시가 답하려는 질문: static 변수 totalCreated는 왜 "모든 객체에 공유"되는가? 객체 없이도
 * 접근할 수 있는가?
 *
 * 왜 이 시나리오인가: Counter 객체를 만들기 전/후로 Counter.totalCreated 값을 관찰한다. 또
 * 서로 다른 객체 c1, c2에서 같은 totalCreated 값이 보이는지 확인한다. 만약 "클래스 변수는
 * Method Area에 단 1개만 존재한다"는 설명이 맞다면:
 *   - 객체를 만들 때마다(생성자에서 totalCreated++) 값이 누적되고,
 *   - c1에서 보든 c2에서 보든 Counter.totalCreated로 보든 모두 같은 값이어야 한다.
 *
 * 예상 결과:
 *   - 객체 생성 전 Counter.totalCreated = 0 (객체 없이도 접근 가능 — 클래스에 속하므로)
 *   - 3개 생성 후 = 3
 *   - c1이 보는 값 == c2가 보는 값 == Counter.totalCreated == 3 (모두 같은 한 변수)
 * -> 클래스 변수는 Method Area에 클래스당 1개만 있어 모든 객체가 같은 것을 본다.
 *    이것이 Example2(인스턴스 변수, 객체마다 따로)와의 결정적 차이다.
 *    (싱글톤·전역 카운터에 쓰이지만, 멀티스레드에서 공유되므로 동시성 주의 — PART 7과 연결)
 */
public class Example3_StaticVariable {

    public static void main(String[] args) {
        System.out.println("[예시 3] 클래스 변수(static): Method Area에 1개 -> 모든 객체가 공유");
        System.out.println();

        // 객체를 하나도 안 만들었는데도 접근 가능 — 클래스 변수는 객체가 아니라 클래스에 속하므로.
        System.out.println("객체 생성 전 Counter.totalCreated = " + Counter.totalCreated + " (객체 없이 접근)");

        Counter c1 = new Counter();
        Counter c2 = new Counter();
        Counter c3 = new Counter();

        System.out.println("Counter 3개 생성 후:");
        System.out.println("  Counter.totalCreated = " + Counter.totalCreated);
        System.out.println("  c1이 보는 totalCreated = " + c1.totalCreated);
        System.out.println("  c2가 보는 totalCreated = " + c2.totalCreated);
        System.out.println("  -> 셋 다 같은 값(3). 객체가 달라도 같은 변수 하나를 본다.");

        System.out.println();
        System.out.println("=> 클래스 변수는 Method Area에 클래스당 단 1개. 그래서 모든 객체가 공유한다.");
        System.out.println("   Example2의 인스턴스 변수(객체마다 따로)와 정반대.");
    }
}
