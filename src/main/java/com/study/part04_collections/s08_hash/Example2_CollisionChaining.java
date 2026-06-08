package com.study.part04_collections.s08_hash;

import java.util.HashMap;
import java.util.Map;

/**
 * 예시 2 / 3 — 해시 충돌과 체이닝: "나쁜 hashCode는 한 버킷에 몰려 O(n)으로 퇴화한다."
 *
 * 이 예시가 답하려는 질문: 해시 충돌이 잦으면 HashMap이 왜 느려지나? hashCode가 성능에 미치는 영향은?
 *
 * 왜 이 시나리오인가: HashMap은 hashCode로 버킷(저장 위치)을 정한다. hashCode가 고르게 분산되면
 * (KeyGood) 원소들이 여러 버킷에 흩어져 평균 O(1)이다. 하지만 hashCode가 모두 같으면(KeyBad,
 * 항상 1 반환) 모든 원소가 '같은 버킷 하나'로 몰린다(충돌). 그 버킷 안에서는 연결 리스트로 쌓여
 * (체이닝), get할 때 리스트를 처음부터 훑어야 하므로 O(n)으로 퇴화한다. 같은 개수의 KeyGood/KeyBad를
 * 넣고 get 시간을 비교해, 충돌이 성능을 어떻게 무너뜨리는지 본다.
 *
 * 예상 결과:
 *   - KeyGood(고른 분산): get 빠름(O(1) — 여러 버킷에 분산)
 *   - KeyBad(전부 충돌): get 매우 느림(O(n) — 한 버킷의 긴 체인을 매번 탐색)
 * -> 충돌은 비둘기집 원리상 완전히 피할 수 없지만, 잦으면 O(1)의 장점이 사라진다. 그래서 좋은
 *    hashCode(고른 분산)가 중요하다. Java 8은 한 버킷이 너무 길어지면(8 초과) Red-Black Tree로
 *    바꿔(treeify) 최악을 O(n)에서 O(log n)으로 완화하지만(key가 Comparable일 때), 애초에 충돌이
 *    적은 게 최선이다.
 */
public class Example2_CollisionChaining {

    public static void main(String[] args) {
        System.out.println("[예시 2] 해시 충돌과 체이닝: 나쁜 hashCode -> O(n) 퇴화");
        System.out.println();

        int n = 30_000;

        // KeyGood: hashCode가 고르게 분산 -> 여러 버킷에 흩어짐
        Map<KeyGood, Integer> goodMap = new HashMap<>();
        for (int i = 0; i < n; i++) goodMap.put(new KeyGood(i), i);

        // KeyBad: hashCode가 모두 1 -> 전부 같은 버킷으로 충돌(긴 체인)
        Map<KeyBad, Integer> badMap = new HashMap<>();
        for (int i = 0; i < n; i++) badMap.put(new KeyBad(i), i);

        int lookups = 30_000;

        long t1 = System.nanoTime();
        int sumG = 0;
        for (int i = 0; i < lookups; i++) {
            Integer v = goodMap.get(new KeyGood(i % n));
            if (v != null) sumG += 1;
        }
        long timeGood = (System.nanoTime() - t1) / 1_000_000;

        long t2 = System.nanoTime();
        int sumB = 0;
        for (int i = 0; i < lookups; i++) {
            Integer v = badMap.get(new KeyBad(i % n)); // 같은 버킷의 긴 체인을 매번 탐색
            if (v != null) sumB += 1;
        }
        long timeBad = (System.nanoTime() - t2) / 1_000_000;

        System.out.println(n + "개 저장, get " + lookups + "회:");
        System.out.println("  KeyGood (고른 분산) : " + timeGood + " ms (여러 버킷에 분산, O(1) 평균)");
        System.out.println("  KeyBad  (전부 충돌) : " + timeBad + " ms (한 버킷 긴 체인 탐색, O(n) 퇴화)");
        System.out.println("  (조회 성공 검증 " + sumG + " / " + sumB + " — 둘 다 동일해야 정상)");

        System.out.println();
        System.out.println("=> hashCode가 모두 같으면 한 버킷에 몰려 체이닝되고 O(n)으로 느려진다.");
        System.out.println("   좋은 hashCode(고른 분산)가 HashMap 성능의 핵심. (Java 8 treeify는 완화책일 뿐)");
    }
}
