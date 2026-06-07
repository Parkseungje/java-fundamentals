package com.study.part01_oop.s03_method_and_varargs;

/**
 * 예시 2 / 3 — "가변인자(varargs)는 어떻게 동작하는가?"
 *
 * 이 예시가 답하려는 질문: String... 으로 선언한 가변인자는 정말로 0개부터 여러 개까지 자유롭게
 * 받을 수 있는가? 메서드 내부에서는 그것을 어떻게 다루는가(배열인가)?
 *
 * 왜 이 시나리오인가: 같은 logAll 메서드를 인자 개수만 바꿔(0개 / 1개 / 3개) 세 번 호출한다.
 * 만약 가변인자가 "개수 무관"이라는 설명이 맞다면 세 호출 모두 컴파일·실행되어야 하고,
 * 메서드 내부의 messages.length가 각각 0, 1, 3으로 찍혀야 한다(= 내부에서 배열로 다뤄진다는 증거).
 *
 * 예상 결과:
 *   - logAll("[A]")            -> 받은 메시지 개수 = 0
 *   - logAll("[B]", "하나")     -> 받은 메시지 개수 = 1
 *   - logAll("[C]", "x","y","z")-> 받은 메시지 개수 = 3
 * -> "가변인자는 호출 시 개수가 자유롭고, 내부에서는 배열(length/인덱스 접근)로 취급된다"를 확인.
 * 또한 가변인자는 매개변수 목록의 '마지막'에만 올 수 있다는 규칙도 짚는다(prefix가 앞, messages가 뒤).
 */
public class Example2_Varargs {

    public static void main(String[] args) {
        System.out.println("[예시 2] 가변인자: 0개~여러 개 자유 호출 + 내부에서는 배열로 취급");
        System.out.println();

        SimpleLogger logger = new SimpleLogger();

        // 같은 메서드를 인자 개수만 바꿔 호출 — 모두 정상 동작해야 한다.
        System.out.println("(1) 메시지 0개 호출:");
        logger.logAll("[A]");

        System.out.println("(2) 메시지 1개 호출:");
        logger.logAll("[B]", "하나");

        System.out.println("(3) 메시지 3개 호출:");
        logger.logAll("[C]", "x", "y", "z");

        System.out.println();
        System.out.println("=> 호출 시 개수가 0개든 여러 개든 모두 허용되고,");
        System.out.println("   내부의 messages.length가 0/1/3으로 찍힌다 -> 내부에서는 배열로 다뤄진다는 증거.");
        System.out.println("   (가변인자는 매개변수 목록의 마지막에만 올 수 있다: logAll(String prefix, String... messages))");
    }
}
