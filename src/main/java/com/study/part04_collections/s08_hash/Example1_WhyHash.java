package com.study.part04_collections.s08_hash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 예시 1 / 3 — 왜 해시인가: "순차 검색 O(n) vs 해시 O(1)."
 *
 * 이 예시가 답하려는 질문: 데이터가 많을 때 "이 값이 들어있나?"를 확인하는 속도가 List와 HashSet은
 * 왜 그렇게 다른가?
 *
 * 왜 이 시나리오인가: List.contains(x)는 원소를 처음부터 하나씩 비교한다(순차 검색 O(n)). 데이터가
 * N개면 한 번 찾는 데 최대 N번 비교한다. 반면 HashSet.contains(x)는 x의 hashCode로 '저장 위치를
 * 즉시 계산'해 거기만 확인한다(평균 O(1)). 그래서 "위치를 계산으로 바로 찾는다"는 해시의 핵심
 * 아이디어가 큰 데이터에서 압도적 차이를 만든다. 같은 데이터·같은 횟수로 contains를 측정해 비교한다.
 *
 * 예상 결과:
 *   - List.contains 반복: 매우 느림(O(n) × 횟수)
 *   - HashSet.contains 반복: 매우 빠름(O(1) × 횟수)
 * -> "데이터가 많으면 순차 검색은 너무 느리다 -> 위치를 수학적으로 바로 계산하자"가 해시의 출발점.
 *    이 아이디어가 HashSet/HashMap의 빠른 조회를 만든다.
 */
public class Example1_WhyHash {

    public static void main(String[] args) {
        System.out.println("[예시 1] 순차 검색 O(n) vs 해시 O(1)");
        System.out.println();

        int size = 100_000;
        List<Integer> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
            set.add(i);
        }

        int lookups = 50_000;
        // 존재하지 않는 값을 찾으면 List는 끝까지 다 봐야 함(최악 O(n)) — 차이를 극적으로 보여줌
        long t1 = System.currentTimeMillis();
        int foundL = 0;
        for (int i = 0; i < lookups; i++) {
            if (list.contains(size + i)) foundL++; // 없는 값 -> 전체 순회
        }
        long timeList = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        int foundS = 0;
        for (int i = 0; i < lookups; i++) {
            if (set.contains(size + i)) foundS++; // 위치 계산 후 그 버킷만 확인
        }
        long timeSet = System.currentTimeMillis() - t2;

        System.out.println("크기 " + size + ", contains " + lookups + "회:");
        System.out.println("  List.contains   : " + timeList + " ms (순차 검색 O(n))");
        System.out.println("  HashSet.contains: " + timeSet + " ms (해시로 위치 즉시 계산 O(1))");
        System.out.println("  (찾은 개수 검증 " + foundL + " / " + foundS + " — 둘 다 0이어야 정상)");

        System.out.println();
        System.out.println("=> 순차 검색은 데이터가 많을수록 급격히 느려진다. 해시는 위치를 바로 계산해");
        System.out.println("   평균 O(1)로 찾는다. 이것이 HashSet/HashMap이 빠른 이유다.");
    }
}
