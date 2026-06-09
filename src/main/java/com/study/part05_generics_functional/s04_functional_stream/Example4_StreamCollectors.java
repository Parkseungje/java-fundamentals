package com.study.part05_generics_functional.s04_functional_stream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 예시 4 / 4 — 스트림 가공·수집 + 한 번만 사용 가능: "filter/map/collect/groupingBy, 그리고 재사용 금지."
 *
 * 이 예시가 답하려는 질문: 스트림으로 데이터를 거르고/변환하고/모으는 전형적 흐름은? 스트림은
 * 여러 번 쓸 수 있나?
 *
 * 왜 이 시나리오인가: 스트림의 실전 사용은 보통 "거르고(filter) -> 변환하고(map) -> 정렬(sorted) ->
 * 모은다(collect)"의 선언적 파이프라인이다. 또 collect(Collectors.groupingBy)로 기준별로 묶을 수 있다
 * (예: 길이별 그룹). 마지막으로, 스트림은 '일회용'이다 — 최종 연산을 한 번 수행하면 그 스트림 객체는
 * 닫혀서 다시 쓸 수 없다(IllegalStateException). 매번 새로 stream()을 호출해야 한다.
 *
 * 예상 결과:
 *   - filter(길이>3) -> map(대문자) -> sorted -> collect(List): [APPLE, BANANA, CHERRY]
 *   - groupingBy(길이): {5=[apple], 6=[banana, cherry], 4=[kiwi...]} 형태로 길이별 묶음
 *   - 같은 스트림 변수에 최종 연산을 두 번 호출 -> IllegalStateException
 * -> 스트림은 선언적 파이프라인(가공의 의도가 코드로 읽힘)이며 collect/groupingBy로 다양하게 모은다.
 *    단 한 번 소비하면 닫히므로 재사용 불가 — 다시 쓰려면 stream()을 새로 호출한다.
 */
public class Example4_StreamCollectors {

    public static void main(String[] args) {
        System.out.println("[예시 4] 스트림 가공/수집(collect/groupingBy) + 한 번만 사용 가능");
        System.out.println();

        List<String> words = List.of("apple", "banana", "cherry", "kiwi", "fig", "date");

        // filter -> map -> sorted -> collect 파이프라인
        List<String> result = words.stream()
                .filter(s -> s.length() > 3)     // 길이 3 초과만
                .map(String::toUpperCase)        // 대문자로 변환
                .sorted()                        // 정렬
                .collect(Collectors.toList());   // 리스트로 수집
        System.out.println("filter(>3) -> map(대문자) -> sorted -> collect: " + result);

        // groupingBy: 길이별로 묶기
        Map<Integer, List<String>> byLength = words.stream()
                .collect(Collectors.groupingBy(String::length));
        System.out.println("groupingBy(길이): " + byLength);

        System.out.println();

        // 스트림은 일회용: 같은 스트림에 최종 연산 두 번 -> IllegalStateException
        System.out.println("[스트림 재사용 시도]");
        Stream<String> once = words.stream();
        long first = once.count();              // 첫 최종 연산 -> 스트림 닫힘
        System.out.println("  첫 count() = " + first);
        try {
            long second = once.count();         // 닫힌 스트림 재사용 -> 예외
            System.out.println("  둘째 count() = " + second);
        } catch (IllegalStateException e) {
            System.out.println("  둘째 count() -> IllegalStateException: stream has already been operated upon or closed");
            System.out.println("  -> 스트림은 일회용. 다시 쓰려면 words.stream()을 새로 호출해야 한다.");
        }

        System.out.println();
        System.out.println("=> 스트림은 filter/map/sorted/collect로 데이터를 선언적으로 가공·수집한다.");
        System.out.println("   groupingBy로 기준별 묶기도 가능. 단 '한 번만 사용 가능'(최종 연산 후 닫힘).");
    }
}
