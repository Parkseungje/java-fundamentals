package com.study.part05_generics_functional.s04_functional_stream;

import java.util.List;
import java.util.stream.Stream;

/**
 * 예시 3 / 4 — 스트림의 중간/최종 연산과 지연 평가: "최종 연산이 없으면 아무것도 실행되지 않는다."
 *
 * 이 예시가 답하려는 질문: 스트림의 filter/map 같은 연산은 호출하는 즉시 실행되나? '지연 평가'란 무엇인가?
 *
 * 왜 이 시나리오인가: 스트림 연산은 두 종류다.
 *   - 중간 연산(intermediate): Stream을 반환. filter, map, sorted, distinct, limit, skip 등.
 *     이들은 '지연(lazy)' — 호출만 해두고 실제로는 실행하지 않는다(설계도만 쌓는다).
 *   - 최종 연산(terminal): 값이나 void 반환. forEach, collect, count, reduce, anyMatch 등.
 *     이것이 호출되는 순간 비로소 쌓아둔 중간 연산들이 한꺼번에 실행된다.
 * peek(원소가 지나갈 때 들여다보는 중간 연산)로 "언제 실제로 실행되는지"를 출력으로 관찰한다.
 *   (A) 중간 연산만 호출하고 최종 연산이 없으면 -> peek가 한 번도 안 찍힌다(실행 안 됨).
 *   (B) 최종 연산(count)을 붙이면 -> 그제서야 peek가 찍힌다(실행됨).
 *
 * 예상 결과:
 *   - (A) 중간 연산만: 아무 출력 없음(지연 평가 — 실행 안 됨)
 *   - (B) 최종 연산 추가: peek 출력이 나타남(실행됨), count 결과 반환
 * -> 스트림은 게으르다(lazy). 중간 연산은 '무엇을 할지' 쌓기만 하고, 최종 연산이 방아쇠가 되어
 *    한꺼번에 실행된다. 그래서 중간 연산만 적어두면 아무 일도 일어나지 않는다.
 */
public class Example3_StreamLazy {

    public static void main(String[] args) {
        System.out.println("[예시 3] 스트림 중간/최종 연산과 지연 평가");
        System.out.println();

        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        // (A) 중간 연산만 호출 (최종 연산 없음) -> 실행되지 않는다
        System.out.println("(A) 중간 연산(filter/map/peek)만 호출, 최종 연산 없음:");
        Stream<Integer> lazy = numbers.stream()
                .peek(n -> System.out.println("    peek(filter전): " + n))   // 중간 연산(지연)
                .filter(n -> n % 2 == 0)
                .map(n -> n * 10);
        System.out.println("    -> peek 출력이 하나도 없음 (지연 평가: 최종 연산이 없어 실행 안 됨)");

        System.out.println();

        // (B) 같은 파이프라인에 최종 연산(count)을 붙이면 그제서야 실행
        System.out.println("(B) 최종 연산(count) 추가 -> 그제서야 실행:");
        long count = numbers.stream()
                .peek(n -> System.out.println("    peek: " + n + " 통과"))    // 이제 실행됨
                .filter(n -> n % 2 == 0)
                .count();
        System.out.println("    count(짝수 개수) = " + count);

        System.out.println();
        System.out.println("=> 중간 연산(filter/map/peek)은 지연(lazy) — 쌓기만 한다. 최종 연산(count/collect 등)이");
        System.out.println("   방아쇠가 되어 비로소 실행된다. 최종 연산이 없으면 아무 일도 일어나지 않는다.");
    }
}
