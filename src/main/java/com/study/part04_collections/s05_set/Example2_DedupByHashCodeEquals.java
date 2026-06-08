package com.study.part04_collections.s05_set;

import java.util.HashSet;
import java.util.Set;

/**
 * 예시 2 / 3 — HashSet의 중복 판정: "hashCode + equals로 같은지 본다."
 *
 * 이 예시가 답하려는 질문: HashSet은 "같은 원소인지"를 무엇으로 판단하나? 사용자 정의 객체를 넣으면?
 *
 * 왜 이 시나리오인가: HashSet은 add할 때 (1) 원소의 hashCode로 버킷(저장 위치)을 찾고, (2) 그 버킷에
 * 이미 있는 원소와 equals로 비교해 같으면 '중복'으로 보고 안 넣는다. 따라서 사용자 정의 객체가
 * equals/hashCode를 재정의하지 않으면(기본=주소 비교), 값이 같아도 '다른 객체'로 취급되어 중복이
 * 걸리지 않는다. 같은 값(1,2)인 두 인스턴스를 각각 HashSet에 넣어 size를 비교한다.
 *   - PointNoEquals(재정의 X): 값이 같아도 다른 객체로 봄 -> 둘 다 들어가 size=2
 *   - PointWithEquals(재정의 O): 값이 같으면 같은 것으로 봄 -> 하나만 남아 size=1
 *
 * 예상 결과:
 *   - PointNoEquals  HashSet size = 2 (중복 제거 실패 — 의도와 다름!)
 *   - PointWithEquals HashSet size = 1 (값 기준 중복 제거 성공)
 * -> HashSet의 "중복 제거"는 equals/hashCode에 의존한다. 사용자 정의 객체를 Set/Map에 쓰려면
 *    반드시 equals/hashCode를 (둘 다) 재정의해야 한다. (자세한 규약은 4.8)
 */
public class Example2_DedupByHashCodeEquals {

    public static void main(String[] args) {
        System.out.println("[예시 2] HashSet 중복 판정 = hashCode + equals");
        System.out.println();

        // (A) equals/hashCode 재정의 X -> 값이 같아도 다른 객체로 취급
        Set<PointNoEquals> noEq = new HashSet<>();
        noEq.add(new PointNoEquals(1, 2));
        noEq.add(new PointNoEquals(1, 2)); // 값은 같지만 다른 객체
        System.out.println("PointNoEquals (재정의 X):");
        System.out.println("  (1,2) 두 번 add 후 size = " + noEq.size() + "  <- 2 (중복 제거 실패!)");
        System.out.println("  내용: " + noEq);

        System.out.println();

        // (B) equals/hashCode 재정의 O -> 값이 같으면 같은 것으로 취급
        Set<PointWithEquals> withEq = new HashSet<>();
        withEq.add(new PointWithEquals(1, 2));
        withEq.add(new PointWithEquals(1, 2)); // 값이 같으니 같은 것으로 판정
        System.out.println("PointWithEquals (재정의 O):");
        System.out.println("  (1,2) 두 번 add 후 size = " + withEq.size() + "  <- 1 (값 기준 중복 제거 성공)");
        System.out.println("  내용: " + withEq);

        System.out.println();
        System.out.println("=> HashSet의 중복 제거는 hashCode(버킷 찾기) + equals(같은지 확인)에 의존한다.");
        System.out.println("   사용자 정의 객체를 Set/Map에 쓰려면 equals/hashCode를 둘 다 재정의해야 한다(4.8).");
    }
}
