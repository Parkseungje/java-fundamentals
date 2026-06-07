package com.study.part01_oop.s07_nested_classes;

/**
 * 예시 3 / 3 — "익명 클래스는 무엇이고, 왜 람다의 전신인가?"
 *
 * 이 예시가 답하려는 질문: 인터페이스(Greeting)를 구현하는 클래스를 따로 파일로 만들지 않고
 * '그 자리에서 즉석으로' 정의해 객체를 만들 수 있는가(익명 클래스)? 그리고 그것과 람다는
 * 어떤 관계인가?
 *
 * 왜 이 시나리오인가: 같은 Greeting 구현을 세 가지 방식으로 만들어 결과가 같은지 비교한다.
 *   1) 익명 클래스: new Greeting() { ... } — 이름 없는 클래스를 즉석 정의하며 동시에 인스턴스화
 *   2) 람다: name -> ... — 추상 메서드가 1개인 인터페이스를 짧게 표현(익명 클래스의 축약형)
 *   3) (참고) 익명 클래스 안에서 지역 변수 캡처
 * 만약 "람다는 익명 클래스의 축약"이라는 설명이 맞다면, 1)과 2)의 호출 결과가 완전히 같아야 한다.
 *
 * 예상 결과:
 *   - anonymous.greet("철수")와 lambda.greet("철수")가 동일한 문자열을 반환
 * -> 익명 클래스는 "한 번만 쓸 구현을 그 자리에서 정의"하는 도구이고, 함수형 인터페이스(추상
 *    메서드 1개)에 한해 람다로 더 간결히 쓸 수 있다. 그래서 익명 클래스를 '람다의 전신'이라 부른다.
 *    (람다/함수형 인터페이스의 본격 학습은 PART 5)
 */
public class Example3_AnonymousClass {

    public static void main(String[] args) {
        System.out.println("[예시 3] 익명 클래스 vs 람다 (람다 = 익명 클래스의 축약)");
        System.out.println();

        // 1) 익명 클래스: 이름 없는 클래스를 즉석에서 정의하면서 동시에 객체를 만든다.
        Greeting anonymous = new Greeting() {
            @Override
            public String greet(String name) {
                return "안녕하세요, " + name + "님 (익명 클래스)";
            }
        };

        // 2) 람다: 추상 메서드가 1개인 인터페이스라서 이렇게 짧게 쓸 수 있다(같은 일을 함).
        Greeting lambda = name -> "안녕하세요, " + name + "님 (람다)";

        System.out.println(anonymous.greet("철수"));
        System.out.println(lambda.greet("철수"));

        System.out.println();

        // 3) 익명 클래스는 바깥의 지역 변수를 '캡처'할 수 있다(사실상 final이어야 함 — PART 5에서 심화).
        String suffix = "!!";
        Greeting capturing = new Greeting() {
            @Override
            public String greet(String name) {
                return name + "에게 인사" + suffix; // 바깥 지역변수 suffix 캡처
            }
        };
        System.out.println("지역변수 캡처: " + capturing.greet("영희"));

        System.out.println();
        System.out.println("=> 익명 클래스는 '한 번 쓸 구현을 그 자리에서' 정의하는 도구다.");
        System.out.println("   추상 메서드가 1개인 인터페이스는 람다로 더 짧게 쓸 수 있고(결과 동일),");
        System.out.println("   그래서 익명 클래스를 '람다의 전신'이라 부른다. (람다 본편은 PART 5)");
    }
}
