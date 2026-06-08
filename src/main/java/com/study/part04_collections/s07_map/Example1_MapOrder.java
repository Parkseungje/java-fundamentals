package com.study.part04_collections.s07_map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 예시 1 / 3 — Map의 순서: "HashMap=순서X, LinkedHashMap=삽입순, TreeMap=키 정렬."
 *
 * 이 예시가 답하려는 질문: 세 Map은 모두 key-value를 저장하지만, key를 순회하는 순서는 어떻게 다른가?
 *
 * 왜 이 시나리오인가: Set 3형제(4.5)와 정확히 대응된다(Map은 내부적으로 그 Set/해시 구조를 쓴다).
 *   - HashMap: 해시 테이블 -> 순서 보장 X.
 *   - LinkedHashMap: 해시 테이블 + 연결 리스트 -> 삽입 순서 유지.
 *   - TreeMap: Red-Black Tree -> key가 항상 정렬된 순서(put/get/remove 모두 O(log n)).
 * 같은 순서로 put한 뒤 순회 결과를 비교한다. 또 TreeMap에 숫자/대문자/소문자/한글 key를 넣어
 * 정렬 기준(유니코드 순: 숫자 < 대문자 < 소문자 < 한글)을 확인한다.
 *
 * 예상 결과(put: banana, apple, cherry):
 *   - HashMap      : 순서 예측 불가
 *   - LinkedHashMap: banana, apple, cherry (삽입 순서)
 *   - TreeMap      : apple, banana, cherry (key 정렬)
 *   - TreeMap에 "2","B","a","가" -> 2, B, a, 가 순서(유니코드 정렬)
 * -> Map도 "순서가 필요한가, 정렬이 필요한가"가 선택 기준. 순서 불필요 -> HashMap(가장 빠름).
 */
public class Example1_MapOrder {

    public static void main(String[] args) {
        System.out.println("[예시 1] Map의 순서: HashMap / LinkedHashMap / TreeMap");
        System.out.println();

        Map<String, Integer> hashMap = new HashMap<>();
        Map<String, Integer> linkedHashMap = new LinkedHashMap<>();
        Map<String, Integer> treeMap = new TreeMap<>();
        String[] keys = {"banana", "apple", "cherry"};
        for (int i = 0; i < keys.length; i++) {
            hashMap.put(keys[i], i);
            linkedHashMap.put(keys[i], i);
            treeMap.put(keys[i], i);
        }

        System.out.println("put 순서        : banana, apple, cherry");
        System.out.println("HashMap         : " + hashMap.keySet() + "  <- 순서 보장 X");
        System.out.println("LinkedHashMap   : " + linkedHashMap.keySet() + "  <- 삽입 순서");
        System.out.println("TreeMap         : " + treeMap.keySet() + "  <- key 정렬");

        System.out.println();

        // TreeMap 정렬 기준: 숫자 < 대문자 < 소문자 < 한글 (유니코드 순)
        Map<String, Integer> mixed = new TreeMap<>();
        for (String k : new String[]{"a", "2", "가", "B"}) mixed.put(k, 0);
        System.out.println("TreeMap 정렬 기준(숫자<대문자<소문자<한글): " + mixed.keySet());

        System.out.println();
        System.out.println("=> HashMap은 순서 X(가장 빠름), LinkedHashMap은 삽입순, TreeMap은 key 정렬.");
        System.out.println("   Set 3형제와 같은 구조다(Map이 내부에서 그 구조를 쓴다).");
    }
}
