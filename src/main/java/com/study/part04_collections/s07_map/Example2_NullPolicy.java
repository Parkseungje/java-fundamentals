package com.study.part04_collections.s07_map;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 예시 2 / 3 — null 정책: "Map마다 null key/value 허용 여부가 다르다."
 *
 * 이 예시가 답하려는 질문: Map에 null key나 null value를 넣을 수 있나? 구현체마다 다른가?
 *
 * 왜 이 시나리오인가: Map 구현체는 null 허용 정책이 제각각이라 헷갈리기 쉽다.
 *   - HashMap: null key '1개' 허용(해시 0번 버킷에 특별 저장), null value도 허용.
 *   - TreeMap: key는 정렬을 위해 서로 '비교'해야 하는데 null은 비교 불가 -> null key 불가(NPE).
 *     단 value는 정렬과 무관하므로 null 허용.
 *   - ConcurrentHashMap: null key, null value '모두 불가'(NPE). 멀티스레드에서 "값이 null인지
 *     키가 없는 건지"를 구분할 수 없어 모호성이 생기기 때문(get이 null을 반환하면 둘 다 가능해짐).
 * 각 Map에 null key/value를 넣어보며 차이를 확인한다.
 *
 * 예상 결과:
 *   - HashMap: put(null, 1) OK, put("k", null) OK
 *   - TreeMap: put(null, 1) -> NullPointerException, put("k", null) OK
 *   - ConcurrentHashMap: put(null, 1) -> NPE, put("k", null) -> NPE
 * -> "HashMap은 null에 관대, TreeMap은 key만 금지, ConcurrentHashMap은 둘 다 금지"로 기억.
 */
public class Example2_NullPolicy {

    public static void main(String[] args) {
        System.out.println("[예시 2] Map별 null key/value 허용 정책");
        System.out.println();

        // HashMap: null key 1개 + null value 허용
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put(null, 1);
        hashMap.put("k", null);
        System.out.println("HashMap: put(null,1) OK, put(\"k\",null) OK -> " + hashMap);

        System.out.println();

        // TreeMap: null key 불가(비교 불가), value는 OK
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("k", null); // value null은 OK
        System.out.println("TreeMap: put(\"k\",null) OK -> " + treeMap);
        try {
            treeMap.put(null, 1);
        } catch (NullPointerException e) {
            System.out.println("TreeMap: put(null,1) -> NullPointerException (key는 정렬 비교 때문에 null 불가)");
        }

        System.out.println();

        // ConcurrentHashMap: null key, null value 모두 불가
        Map<String, Integer> chm = new ConcurrentHashMap<>();
        try {
            chm.put(null, 1);
        } catch (NullPointerException e) {
            System.out.println("ConcurrentHashMap: put(null,1)  -> NullPointerException");
        }
        try {
            chm.put("k", null);
        } catch (NullPointerException e) {
            System.out.println("ConcurrentHashMap: put(\"k\",null) -> NullPointerException (둘 다 금지)");
        }

        System.out.println();
        System.out.println("=> HashMap(null 관대) / TreeMap(key만 금지) / ConcurrentHashMap(둘 다 금지).");
        System.out.println("   ConcurrentHashMap이 null을 막는 건 'get==null'의 모호성(없음 vs null값) 때문.");
    }
}
