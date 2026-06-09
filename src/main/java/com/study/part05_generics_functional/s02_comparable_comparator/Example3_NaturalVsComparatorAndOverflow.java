package com.study.part05_generics_functional.s02_comparable_comparator;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * 예시 3 / 3 — 자연 순서 vs Comparator(우선) + 'a - b' 오버플로 함정.
 *
 * 이 예시가 답하려는 질문: TreeSet에 Comparator를 주면 클래스의 자연 순서는 어떻게 되나?
 * 그리고 비교를 a - b 로 구현하면 왜 위험한가?
 *
 * 왜 이 시나리오인가: 두 가지를 본다.
 *   (1) 우선순위: Member는 자연 순서가 나이순이지만, TreeSet 생성 시 이름순 Comparator를 주면
 *       '주입한 Comparator가 우선'한다(자연 순서를 덮어씀). 4.5/4.7에서 본 "Comparator로 순서 바꾸기"의 원리.
 *   (2) 오버플로 함정: 정수 비교를 (a, b) -> a - b 로 짜면 빼기 결과가 int 범위를 넘칠 때
 *       (예: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE) 오버플로로 '부호가 뒤집혀' 잘못된 비교가 된다.
 *       Integer.compare(a, b)는 빼지 않고 비교만 하므로 안전하다.
 *
 * 예상 결과:
 *   - 자연 순서 TreeSet: 나이순 / 이름순 Comparator TreeSet: 이름순 (Comparator가 우선)
 *   - 잘못된 비교(a - b): MAX vs MIN에서 음수가 나와야 하는데 양수(오버플로) -> 잘못된 결과
 *   - 올바른 비교(Integer.compare): 정상
 * -> Comparator는 자연 순서를 덮어쓴다. 그리고 정수 비교는 '빼기' 대신 Integer.compare(또는
 *    Comparator.comparingInt)를 써야 오버플로 버그를 피한다.
 */
public class Example3_NaturalVsComparatorAndOverflow {

    public static void main(String[] args) {
        System.out.println("[예시 3] 자연 순서 vs Comparator(우선) + a-b 오버플로 함정");
        System.out.println();

        // (1) Comparator가 자연 순서를 덮어쓴다
        var data = java.util.List.of(new Member("철수", 30), new Member("영희", 25), new Member("민수", 28));

        TreeSet<Member> natural = new TreeSet<>(data);                       // 자연 순서(나이)
        TreeSet<Member> byName = new TreeSet<>(Comparator.comparing(m -> m.name)); // 이름순 주입
        byName.addAll(data);

        System.out.println("[우선순위] 같은 데이터, TreeSet 기준만 다르게");
        System.out.println("  자연 순서(나이)        : " + natural);
        System.out.println("  이름순 Comparator 주입 : " + byName + "  <- Comparator가 자연 순서를 덮어씀");

        System.out.println();

        // (2) a - b 오버플로 함정
        System.out.println("[오버플로 함정] MAX와 MIN을 비교");
        int big = Integer.MAX_VALUE;
        int small = Integer.MIN_VALUE;

        // 잘못된 방식: 빼기 -> 오버플로로 부호가 뒤집힘
        Comparator<Integer> bad = (x, y) -> x - y;
        // 올바른 방식: Integer.compare -> 빼지 않고 비교만
        Comparator<Integer> good = Integer::compare;

        System.out.println("  big > small 이므로 비교 결과는 '양수'가 나와야 정상");
        System.out.println("  (x - y) 방식      : " + bad.compare(big, small) + "  <- 음수! 오버플로로 부호 뒤집힘(버그)");
        System.out.println("  Integer.compare   : " + good.compare(big, small) + "  <- 양수(정상)");

        System.out.println();
        System.out.println("=> Comparator를 주면 자연 순서를 덮어쓴다. 그리고 정수 비교는 'a - b'(오버플로 위험)");
        System.out.println("   대신 Integer.compare 또는 Comparator.comparingInt를 써야 안전하다.");
    }
}
