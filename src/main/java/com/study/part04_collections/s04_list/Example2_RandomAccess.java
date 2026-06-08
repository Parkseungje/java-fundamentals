package com.study.part04_collections.s04_list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 예시 2 / 3 — 임의 접근(get): "ArrayList는 인덱스로 O(1), LinkedList는 순차 탐색 O(n)."
 *
 * 이 예시가 답하려는 질문: get(index)로 특정 위치 원소를 꺼낼 때 ArrayList와 LinkedList의 속도가
 * 왜 다른가?
 *
 * 왜 이 시나리오인가: ArrayList는 내부가 배열이라 "i번째 원소의 주소 = 시작주소 + i*크기"로 즉시
 * 계산해 접근한다(O(1)). LinkedList는 각 노드가 다음/이전 노드만 가리키는 이중 연결 리스트라,
 * i번째에 가려면 처음(또는 끝)부터 i번 따라가야 한다(O(n)). 큰 리스트에서 임의 인덱스 get을 많이
 * 반복하면 이 차이가 크게 벌어진다. 같은 크기·같은 횟수로 두 리스트의 get 시간을 측정해 비교한다.
 *
 * 예상 결과:
 *   - ArrayList의 임의 get은 매우 빠르다(O(1)).
 *   - LinkedList의 임의 get은 훨씬 느리다(O(n), 매번 노드를 따라감).
 * -> "인덱스로 자주 조회"하는 작업에는 ArrayList가 압도적으로 유리하다. LinkedList의 강점은 임의
 *    접근이 아니라 (위치를 이미 알 때의) 삽입/삭제다 -> 예시3에서 다룬다.
 */
public class Example2_RandomAccess {

    public static void main(String[] args) {
        System.out.println("[예시 2] 임의 접근 get(): ArrayList O(1) vs LinkedList O(n)");
        System.out.println();

        int size = 100_000;
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        int repeats = 50_000;
        // 임의 인덱스들을 반복 조회
        long t1 = System.nanoTime();
        long sumA = 0;
        for (int i = 0; i < repeats; i++) {
            sumA += arrayList.get((i * 31) % size); // 흩어진 인덱스 접근
        }
        long timeA = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        long sumL = 0;
        for (int i = 0; i < repeats; i++) {
            sumL += linkedList.get((i * 31) % size);
        }
        long timeL = (System.nanoTime() - t2) / 1_000_000;

        System.out.println("크기 " + size + " 리스트에서 임의 get " + repeats + "회:");
        System.out.println("  ArrayList : " + timeA + " ms (인덱스로 즉시 접근 O(1))");
        System.out.println("  LinkedList: " + timeL + " ms (매번 노드를 따라감 O(n))");
        System.out.println("  (합계 검증 sumA==sumL? " + (sumA == sumL) + " — 같은 값을 읽었다는 확인)");

        System.out.println();
        System.out.println("=> 인덱스로 자주 조회하는 작업은 ArrayList가 압도적으로 빠르다.");
        System.out.println("   LinkedList의 강점은 임의 접근이 아니라 '위치를 알 때의' 삽입/삭제(예시3).");
    }
}
