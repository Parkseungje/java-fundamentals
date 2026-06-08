package com.study.part04_collections.s04_list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 예시 3 / 3 — 삽입/삭제의 진짜 이유: "'위치 찾기'와 '실제 조작'을 나눠서 봐야 한다."
 *
 * 이 예시가 답하려는 질문: "LinkedList는 삽입/삭제가 O(1)이라 빠르다"는 말은 항상 맞는가?
 *
 * 왜 이 시나리오인가: 흔한 오해를 바로잡는다. 삽입/삭제 비용은 두 단계로 나뉜다.
 *   - 위치 찾기:   ArrayList O(1)(인덱스 계산)  /  LinkedList O(n)(노드 따라가기)
 *   - 실제 삽입/삭제: ArrayList O(n)(원소들을 한 칸씩 밀어 복사) / LinkedList O(1)(참조 몇 개만 변경)
 * 그래서 결과는 "어디에, 어떻게" 삽입하느냐에 따라 갈린다.
 *   (A) 맨 앞에 반복 삽입: 위치 찾기는 둘 다 즉시(앞이니까). 실제 조작에서 ArrayList는 매번 전체를
 *       뒤로 밀어 복사(O(n)), LinkedList는 head 참조만 변경(O(1)). -> LinkedList 압승.
 *   (B) 인덱스로 중간 삽입: ArrayList는 위치 즉시+복사(O(n)), LinkedList는 위치 찾기 O(n)+조작 O(1).
 *       -> 둘 다 O(n)이라 LinkedList의 'O(1) 삽입' 장점이 탐색 비용에 묻힌다.
 * 이 예시는 (A) 맨 앞 삽입으로 LinkedList의 진짜 강점을 보여준다.
 *
 * 예상 결과:
 *   - 맨 앞(addFirst/add(0,..)) 반복 삽입: LinkedList가 ArrayList보다 훨씬 빠르다.
 * -> "LinkedList 삽입이 O(1)"은 '위치를 이미 알 때(맨 앞/뒤, 또는 Iterator 위치)'에만 참이다.
 *    인덱스로 접근해야 하면 탐색 O(n)이 붙어 장점이 사라진다. 무조건 LinkedList가 빠른 게 아니다.
 */
public class Example3_InsertDelete {

    public static void main(String[] args) {
        System.out.println("[예시 3] 삽입/삭제: '위치 찾기' vs '실제 조작'을 나눠서 보기");
        System.out.println();

        int n = 100_000;

        // 맨 앞에 n번 삽입 — 위치 찾기는 즉시(앞), 차이는 '실제 조작'에서 난다
        long t1 = System.currentTimeMillis();
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(0, i); // 맨 앞 삽입 -> 기존 원소 전체를 한 칸씩 뒤로 복사(O(n))
        }
        long timeA = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        LinkedList<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            linkedList.addFirst(i); // 맨 앞 삽입 -> head 참조만 변경(O(1))
        }
        long timeL = System.currentTimeMillis() - t2;

        System.out.println("맨 앞에 " + n + "번 삽입:");
        System.out.println("  ArrayList  add(0, x) : " + timeA + " ms (매번 전체를 뒤로 복사 O(n))");
        System.out.println("  LinkedList addFirst  : " + timeL + " ms (head 참조만 변경 O(1))");

        System.out.println();
        System.out.println("[핵심 정리 — 삽입/삭제 비용 = 위치 찾기 + 실제 조작]");
        System.out.println("            | 위치 찾기      | 실제 삽입/삭제");
        System.out.println("  ArrayList | O(1) 인덱스    | O(n) 메모리 복사");
        System.out.println("  LinkedList| O(n) 순차 탐색 | O(1) 참조 변경");
        System.out.println();
        System.out.println("=> 'LinkedList 삽입은 O(1)'은 위치를 이미 알 때(맨 앞/뒤·Iterator)만 참이다.");
        System.out.println("   인덱스로 중간에 넣으려면 탐색 O(n)이 붙어 장점이 사라진다. 무조건 빠른 게 아니다.");
        System.out.println("   실무에서는 대부분 ArrayList가 기본(임의 접근 빠름 + 캐시 효율 좋음).");
    }
}
