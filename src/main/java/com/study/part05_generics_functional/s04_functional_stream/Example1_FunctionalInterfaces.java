package com.study.part05_generics_functional.s04_functional_stream;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 예시 1 / 4 — 함수형 인터페이스 + 람다: "추상 메서드 1개짜리는 람다로 인스턴스화한다."
 *
 * 이 예시가 답하려는 질문: 함수형 인터페이스란 무엇이고, 람다는 그것과 어떤 관계인가? 자바 표준
 * 함수형 인터페이스 4종은 각각 무엇인가?
 *
 * 왜 이 시나리오인가: 추상 메서드가 1개인 인터페이스(함수형 인터페이스)는 익명 클래스로 구현할 수도
 * 있고(1.7), 람다로 더 짧게 쓸 수도 있다. 먼저 직접 만든 Calculator를 익명 클래스 vs 람다로 만들어
 * 둘이 같은 일을 함을 보인다. 그다음 자바가 미리 만들어 둔 표준 함수형 인터페이스 4종을 사용한다.
 *   - Function<T,R>  : R apply(T)      — 입력을 받아 '변환'해 출력
 *   - Predicate<T>   : boolean test(T) — 입력이 '조건'을 만족하는지
 *   - Consumer<T>    : void accept(T)  — 입력을 받아 '소비'(출력 등), 반환 없음
 *   - Supplier<T>    : T get()         — 입력 없이 값을 '공급'
 *
 * 예상 결과:
 *   - 익명 클래스 Calculator와 람다 Calculator가 같은 결과(5)를 낸다.
 *   - Function/Predicate/Consumer/Supplier가 각각 변환/조건/소비/공급으로 동작.
 * -> 함수형 인터페이스 + 람다 = "동작(함수)을 값처럼 변수에 담고 전달"하는 수단. 표준 4종은
 *    자바 곳곳(특히 스트림)에서 쓰인다(-> Example3,4).
 */
public class Example1_FunctionalInterfaces {

    public static void main(String[] args) {
        System.out.println("[예시 1] 함수형 인터페이스 + 람다 (익명 클래스 vs 람다, 표준 4종)");
        System.out.println();

        // 익명 클래스로 Calculator 구현
        Calculator byAnonymous = new Calculator() {
            @Override
            public int calculate(int a, int b) {
                return a + b;
            }
        };
        // 람다로 Calculator 구현 (추상 메서드 1개라 가능)
        Calculator byLambda = (a, b) -> a + b;

        System.out.println("익명 클래스 calculate(2,3) = " + byAnonymous.calculate(2, 3));
        System.out.println("람다       calculate(2,3) = " + byLambda.calculate(2, 3) + "  <- 같은 동작");

        System.out.println();
        System.out.println("[자바 표준 함수형 인터페이스 4종]");

        // Function<T,R>: 변환 (입력 -> 출력)
        Function<String, Integer> length = s -> s.length();
        System.out.println("Function (변환) : \"hello\".length() = " + length.apply("hello"));

        // Predicate<T>: 조건 (입력 -> boolean)
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println("Predicate (조건): 4는 짝수? " + isEven.test(4) + ", 7은 짝수? " + isEven.test(7));

        // Consumer<T>: 소비 (입력 -> void)
        Consumer<String> printer = s -> System.out.println("Consumer (소비) : 출력 -> " + s);
        printer.accept("소비됨");

        // Supplier<T>: 공급 (입력 없이 -> 값)
        Supplier<String> greeting = () -> "Supplier (공급): 안녕하세요";
        System.out.println(greeting.get());

        System.out.println();
        System.out.println("=> 추상 메서드 1개짜리(함수형 인터페이스)는 람다로 인스턴스화한다(익명 클래스의 축약).");
        System.out.println("   표준 4종(변환/조건/소비/공급)은 스트림 등 자바 전반에서 '동작을 값처럼' 전달하는 데 쓰인다.");
    }
}
