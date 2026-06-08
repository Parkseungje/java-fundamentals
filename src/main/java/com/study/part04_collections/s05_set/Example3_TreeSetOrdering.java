package com.study.part04_collections.s05_set;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * 예시 3 / 3 — TreeSet 정렬: "자동 정렬은 좋지만, '정렬 기준'이 반드시 있어야 한다."
 *
 * 이 예시가 답하려는 질문: TreeSet은 어떻게 자동 정렬하나? 정렬 순서를 바꾸려면? 정렬 기준이 없는
 * 객체를 넣으면 어떻게 되나?
 *
 * 왜 이 시나리오인가: TreeSet은 내부가 Red-Black Tree라 원소를 항상 '정렬된 상태'로 유지한다.
 * 정렬하려면 "무엇이 더 큰지" 비교 기준이 필요한데, 두 가지 방법이 있다.
 *   - 원소가 Comparable을 구현(자연 순서): Integer/String 등은 이미 구현되어 있어 바로 정렬됨.
 *   - 생성 시 Comparator를 넘김: 원하는 기준(역순 등)으로 정렬.
 * 만약 Comparable도 없고 Comparator도 안 주면, TreeSet은 비교를 못 해 add 시 ClassCastException이 난다.
 * 세 경우를 모두 보여준다. (Comparable/Comparator 개념은 PART 5.2에서 더 깊이)
 *
 * 예상 결과:
 *   - Integer TreeSet: 자동으로 오름차순 정렬 (Comparable 내장)
 *   - Comparator.reverseOrder()를 준 TreeSet: 내림차순
 *   - 비교 기준 없는 PointNoEquals를 TreeSet에 add -> ClassCastException
 * -> TreeSet의 자동 정렬은 "비교 기준(Comparable 또는 Comparator)이 있을 때"만 가능하다.
 */
public class Example3_TreeSetOrdering {

    public static void main(String[] args) {
        System.out.println("[예시 3] TreeSet 자동 정렬과 정렬 기준");
        System.out.println();

        // (1) Comparable 내장 타입(Integer): 자동 오름차순
        Set<Integer> asc = new TreeSet<>();
        asc.add(30); asc.add(10); asc.add(20); asc.add(10); // 중복 10 제거 + 정렬
        System.out.println("Integer TreeSet (자연 순서)        : " + asc + "  <- 오름차순 자동 정렬");

        // (2) Comparator로 정렬 기준 지정: 내림차순
        Set<Integer> desc = new TreeSet<>(Comparator.reverseOrder());
        desc.add(30); desc.add(10); desc.add(20);
        System.out.println("Integer TreeSet (Comparator 역순)  : " + desc + "  <- 내림차순");

        System.out.println();

        // (3) 비교 기준이 없는 객체: add 시 ClassCastException
        System.out.println("비교 기준 없는 객체(PointNoEquals)를 TreeSet에 add 시도:");
        try {
            Set<PointNoEquals> noOrder = new TreeSet<>();
            noOrder.add(new PointNoEquals(1, 2));
            noOrder.add(new PointNoEquals(3, 4)); // 두 번째 add에서 비교 시도 -> 예외
            System.out.println("  (여기 도달 못 함)");
        } catch (ClassCastException e) {
            System.out.println("  ClassCastException 발생! -> PointNoEquals는 Comparable이 아니라 비교 불가");
            System.out.println("  해결: PointNoEquals가 Comparable을 구현하거나, TreeSet 생성 시 Comparator를 넘긴다.");
        }

        System.out.println();
        System.out.println("=> TreeSet은 항상 정렬 상태를 유지하지만, 그러려면 '비교 기준'이 반드시 필요하다.");
        System.out.println("   Comparable(자연 순서) 또는 Comparator(직접 지정) 중 하나가 있어야 한다(PART 5.2).");
    }
}
