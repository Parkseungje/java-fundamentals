package com.study.part02_jvm.s04_pass_by_value;

/**
 * 예시 1 / 3 — 원시 타입(primitive): "값 자체가 복사된다. 메서드 안의 변경은 원본과 무관."
 *
 * 이 예시가 답하려는 질문: int 같은 원시 타입을 메서드에 넘기고, 메서드 안에서 그 매개변수를
 * 바꾸면 호출한 쪽의 원래 변수도 바뀌는가?
 *
 * 왜 이 시나리오인가: 자바는 항상 'pass by value(값에 의한 전달)'다. 원시 타입의 경우 '값 자체'가
 * 복사되어 메서드의 스택 프레임에 별도로 들어간다. 그래서 메서드 안에서 매개변수 x를 999로 바꿔도,
 * 그것은 '복사본'을 바꾼 것일 뿐 호출자의 변수 a와는 다른 메모리다. 만약 원본이 바뀐다면
 * '값 복사'라는 설명이 틀린 것이 된다.
 *
 * 예상 결과:
 *   - modify 안에서 x = 999 로 바꿔도, main의 a 는 그대로 10
 * -> 원시 타입은 값 자체가 복사되므로, 메서드 안의 변경이 호출자에게 절대 영향을 주지 않는다.
 *    (다음 Example2의 객체 타입과 비교 — 객체는 '주소값'이 복사되어 미묘하게 다르게 동작한다)
 */
public class Example1_PrimitiveByValue {

    // 매개변수 x는 호출 시 '값이 복사되어' 이 메서드의 스택 프레임에 새로 생긴 지역 변수다.
    static void modify(int x) {
        x = 999; // 복사본만 바뀐다. 호출자의 변수와는 다른 메모리.
        System.out.println("  modify 안에서 x = " + x);
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] 원시 타입: 값 자체가 복사된다");
        System.out.println();

        int a = 10;
        System.out.println("호출 전 a = " + a);
        modify(a);
        System.out.println("호출 후 a = " + a + "  <- 여전히 10 (복사본만 바뀌었음)");

        System.out.println();
        System.out.println("=> 원시 타입은 값이 복사되어 전달된다. 메서드 안의 변경은 원본과 무관.");
    }
}
