package com.study.part05_generics_functional.s02_comparable_comparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * 예시 1 / 3 — Comparable: "클래스 자신이 정한 '자연 순서'를 sort/TreeSet이 자동으로 쓴다."
 *
 * 이 예시가 답하려는 질문: 객체는 < > 로 비교할 수 없는데(어떤 필드 기준인지 모호), 어떻게 정렬되나?
 * Comparable을 구현하면 무엇이 자동으로 동작하나?
 *
 * 왜 이 시나리오인가: Member는 Comparable<Member>를 구현해 compareTo를 나이 오름차순으로 정의했다
 * (자연 순서). 이렇게 해두면 Collections.sort(list)나 TreeSet이 '비교 기준을 따로 주지 않아도'
 * 이 compareTo를 자동으로 사용한다. compareTo의 반환값(음수/0/양수)이 "누가 앞인지"를 정한다는
 * 점도 직접 확인한다.
 *
 * 예상 결과:
 *   - compareTo: 25세.compareTo(30세) -> 음수(앞), 30.compareTo(25) -> 양수, 같으면 0
 *   - Collections.sort(list): 나이 오름차순으로 정렬됨(자연 순서 자동 사용)
 *   - TreeSet<Member>: 추가만 해도 나이 순으로 정렬 유지(자연 순서 자동 사용)
 * -> Comparable은 "이 클래스의 기본 정렬 기준"을 클래스 내부에 한 개 정의한다. 표준 정렬 도구들이
 *    그것을 자동으로 쓴다. (다른 기준이 필요하면 Comparator로 외부에서 준다 -> Example2)
 */
public class Example1_Comparable {

    public static void main(String[] args) {
        System.out.println("[예시 1] Comparable: 자연 순서(compareTo)를 sort/TreeSet이 자동 사용");
        System.out.println();

        Member a = new Member("철수", 25);
        Member b = new Member("영희", 30);

        // compareTo 반환값의 의미(음수/0/양수)
        System.out.println("철수(25).compareTo(영희(30)) = " + a.compareTo(b) + "  (음수 -> 철수가 앞)");
        System.out.println("영희(30).compareTo(철수(25)) = " + b.compareTo(a) + "  (양수 -> 영희가 뒤)");
        System.out.println("철수(25).compareTo(철수(25)) = " + a.compareTo(new Member("철수", 25)) + "  (0 -> 같음)");

        System.out.println();

        // Collections.sort는 compareTo(자연 순서)를 자동 사용
        List<Member> list = new ArrayList<>(List.of(
                new Member("영희", 30), new Member("철수", 25), new Member("민수", 28)));
        Collections.sort(list); // 비교 기준을 안 줘도 Member.compareTo(나이) 사용
        System.out.println("Collections.sort(list)      -> " + list + "  (나이 오름차순, 자연 순서)");

        // TreeSet도 자연 순서 자동 사용
        TreeSet<Member> set = new TreeSet<>(List.of(
                new Member("영희", 30), new Member("철수", 25), new Member("민수", 28)));
        System.out.println("TreeSet<Member> (자연 순서)  -> " + set + "  (나이 순 유지)");

        System.out.println();
        System.out.println("=> Comparable로 정의한 '자연 순서(나이)'를 sort/TreeSet이 별도 지정 없이 자동 사용.");
        System.out.println("   compareTo는 클래스의 '기본 정렬 기준' 하나를 클래스 내부에 둔다.");
    }
}
