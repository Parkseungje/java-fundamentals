package com.study.part05_generics_functional.s02_comparable_comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 예시 2 / 3 — Comparator: "외부에서 여러 정렬 기준을 자유롭게 정의한다."
 *
 * 이 예시가 답하려는 질문: 클래스의 자연 순서(나이)와 다른 기준으로, 또는 여러 기준으로 정렬하려면?
 * 클래스를 수정하지 않고 가능한가?
 *
 * 왜 이 시나리오인가: Comparable은 클래스당 '기본 기준 하나'만 정의한다. 하지만 현실에선 "나이순,
 * 이름순, 나이 역순, 이름 같으면 나이순..." 등 여러 기준이 필요하다. Comparator는 이를 '클래스
 * 외부에서' 정의한다(클래스 수정 권한이 없어도 OK). 람다와 Comparator의 콤비네이터(comparing,
 * reversed, thenComparing)로 다양한 기준을 만들어 list.sort에 주입한다.
 *
 * 예상 결과:
 *   - byAge: 나이 오름차순
 *   - byName: 이름 사전순
 *   - byAge.reversed(): 나이 내림차순
 *   - byName.thenComparing(byAge): 이름순, 이름 같으면 나이순(2차 기준)
 * -> Comparator는 외부에서 '무제한'의 정렬 기준을 만든다. 정렬 기준을 데이터(클래스)와 분리하므로
 *    유연하다. (Comparable=내부 1개 기준, Comparator=외부 N개 기준)
 */
public class Example2_Comparator {

    public static void main(String[] args) {
        System.out.println("[예시 2] Comparator: 외부에서 여러 기준 정렬");
        System.out.println();

        List<Member> base = List.of(
                new Member("철수", 30), new Member("영희", 25),
                new Member("영희", 20), new Member("민수", 25));

        // 정렬 기준들을 외부에서 정의(클래스 수정 없이)
        Comparator<Member> byAge = Comparator.comparingInt(m -> m.age);
        Comparator<Member> byName = Comparator.comparing(m -> m.name);

        System.out.println("원본            : " + base);
        System.out.println("byAge           : " + sorted(base, byAge));
        System.out.println("byName          : " + sorted(base, byName));
        System.out.println("byAge.reversed(): " + sorted(base, byAge.reversed()) + "  (나이 내림차순)");
        // 이름순, 이름이 같으면 나이순(2차 기준) — 콤비네이터로 다단계 정렬
        System.out.println("byName.then(age): " + sorted(base, byName.thenComparing(byAge))
                + "  (이름순, 같으면 나이순)");

        System.out.println();
        System.out.println("=> Comparator는 외부에서 무제한의 기준을 만든다(comparing/reversed/thenComparing).");
        System.out.println("   정렬 기준을 클래스와 분리 -> 클래스 수정 없이 다양하게 정렬 가능.");
    }

    // 원본을 건드리지 않고 정렬된 새 리스트 반환
    private static List<Member> sorted(List<Member> src, Comparator<Member> cmp) {
        List<Member> copy = new ArrayList<>(src);
        copy.sort(cmp);
        return copy;
    }
}
