package com.study.part04_collections.s05_set;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * 예시 1 / 3 — 세 Set의 순서 차이: "HashSet=순서X, LinkedHashSet=삽입순, TreeSet=정렬순."
 *
 * 이 예시가 답하려는 질문: 세 가지 Set은 모두 중복을 제거하지만, 원소를 '순회하는 순서'는 어떻게 다른가?
 *
 * 왜 이 시나리오인가: 똑같은 입력(중복 포함)을 세 Set에 넣고 순회 결과를 비교한다.
 *   - HashSet: 내부가 해시 테이블이라 순서를 보장하지 않는다(삽입순도 정렬순도 아님).
 *   - LinkedHashSet: 해시 테이블 + 연결 리스트라 '삽입한 순서'를 유지한다.
 *   - TreeSet: 내부가 Red-Black Tree라 항상 '정렬된 순서'로 유지한다.
 * 셋 다 중복은 제거된다는 공통점과, 순서가 다르다는 차이점을 한 화면에서 본다.
 *
 * 예상 결과(입력: banana, apple, cherry, apple, date):
 *   - HashSet      : 순서 예측 불가 (삽입순도 정렬순도 아닐 수 있음), 중복 apple 제거
 *   - LinkedHashSet: banana, apple, cherry, date (삽입 순서 유지)
 *   - TreeSet      : apple, banana, cherry, date (사전순 정렬)
 * -> "중복 제거"는 공통, "순서"가 선택 기준이다. 순서가 필요 없으면 HashSet(가장 빠름),
 *    삽입순이 필요하면 LinkedHashSet, 정렬이 필요하면 TreeSet.
 */
public class Example1_SetOrder {

    public static void main(String[] args) {
        System.out.println("[예시 1] 세 Set의 순서 차이 (공통: 중복 제거)");
        System.out.println();

        String[] data = {"banana", "apple", "cherry", "apple", "date"};

        Set<String> hashSet = new HashSet<>();
        Set<String> linkedHashSet = new LinkedHashSet<>();
        Set<String> treeSet = new TreeSet<>();
        for (String s : data) {
            hashSet.add(s);
            linkedHashSet.add(s);
            treeSet.add(s);
        }

        System.out.println("입력 순서       : banana, apple, cherry, apple, date");
        System.out.println("HashSet         : " + hashSet + "  <- 순서 보장 X (해시 테이블)");
        System.out.println("LinkedHashSet   : " + linkedHashSet + "  <- 삽입 순서 유지");
        System.out.println("TreeSet         : " + treeSet + "  <- 정렬 순서(사전순)");

        System.out.println();
        System.out.println("=> 셋 다 중복(apple)은 제거. 차이는 순회 '순서'다.");
        System.out.println("   순서 불필요 -> HashSet(빠름) / 삽입순 -> LinkedHashSet / 정렬 -> TreeSet.");
    }
}
