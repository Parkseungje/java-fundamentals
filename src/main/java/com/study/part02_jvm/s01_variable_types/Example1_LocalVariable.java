package com.study.part02_jvm.s01_variable_types;

/**
 * 예시 1 / 3 — 지역 변수(Local Variable): "Stack에 저장되고, 메서드 호출마다 새로 태어났다 사라진다."
 *
 * 이 예시가 답하려는 질문: 메서드 안에서 선언한 지역 변수는 메서드 호출이 끝나도 값을 기억하는가?
 * 아니면 호출할 때마다 처음부터 다시 생기는가?
 *
 * 왜 이 시나리오인가: countLocal()은 지역 변수 x를 0으로 새로 만들고 1 증가시킨 뒤 출력한다.
 * 이 메서드를 연속 3번 호출한다. 만약 "지역 변수는 호출마다 Stack에 새로 생성되고 종료 시
 * 사라진다"는 설명이 맞다면, 세 번 모두 x는 0에서 시작해 결과가 항상 1이어야 한다(누적되지 않음).
 * 만약 어딘가에 값이 남아 누적된다면 1, 2, 3이 나올 것이다.
 *
 * 예상 결과: 1, 1, 1 (매 호출마다 x가 새로 생겼다 사라지므로 절대 누적되지 않는다)
 * -> 지역 변수는 그 변수를 선언한 '메서드 호출(스택 프레임)' 동안만 살아 있다. 호출이 끝나면
 *    스택 프레임이 통째로 제거되며 지역 변수도 함께 사라진다. (스택 프레임 동작은 2.3에서 심화)
 *    참고: 메서드 매개변수도 지역 변수에 속한다(호출 시 생기고 종료 시 사라진다).
 */
public class Example1_LocalVariable {

    // step은 매개변수 = 지역 변수의 일종. x도 지역 변수. 둘 다 이 호출 동안만 Stack에 존재.
    static void countLocal(int step) {
        int x = 0;     // 매 호출마다 새로 생성(초기화)
        x += step;
        System.out.println("  countLocal 안의 x = " + x + " (호출이 끝나면 사라짐)");
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] 지역 변수: 메서드 호출마다 새로 생성되고 종료 시 사라진다(Stack)");
        System.out.println();

        System.out.println("countLocal(1) 을 연속 3번 호출:");
        countLocal(1);
        countLocal(1);
        countLocal(1);

        System.out.println();
        System.out.println("=> 세 번 모두 x=1. 이전 호출의 값이 남지 않는다 = 호출마다 새로 생겼다 사라진다는 증거.");
        System.out.println("   지역 변수의 수명 = 그 변수를 담은 메서드 호출(스택 프레임)의 수명.");
    }
}
