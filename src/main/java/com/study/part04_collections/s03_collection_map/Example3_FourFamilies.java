package com.study.part04_collections.s03_collection_map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 예시 3 / 3 — 네 갈래의 성격: "같은 데이터를 넣어도 List/Set/Queue/Map은 다르게 동작한다."
 *
 * 이 예시가 답하려는 질문: List, Set, Queue, Map은 각각 어떤 성격(순서·중복·꺼내는 방식)을 갖나?
 *
 * 왜 이 시나리오인가: 똑같은 입력 [A, B, A, C]를 네 자료구조에 넣고, 결과가 어떻게 달라지는지
 * 한 화면에서 비교한다.
 *   - List : 순서 유지 + 중복 허용 -> 넣은 그대로 [A, B, A, C]
 *   - Set  : 중복 불허 -> 같은 값은 한 번만 (여기선 LinkedHashSet로 순서까지 유지해 [A, B, C])
 *   - Queue: FIFO(먼저 넣은 게 먼저 나옴) -> poll 순서가 A, B, A, C
 *   - Map  : key-value 저장, 같은 key를 다시 put하면 값이 덮어써짐(중복 key 불가)
 * 이렇게 "무엇을 위한 자료구조인가"가 결과로 드러난다.
 *
 * 예상 결과:
 *   - List: [A, B, A, C] (순서·중복 그대로)
 *   - Set : [A, B, C] (중복 A 하나 제거)
 *   - Queue: poll 하면 A -> B -> A -> C 순서로 나옴(FIFO)
 *   - Map : {A=2, B=1, C=1} 같은 형태(같은 key는 값만 갱신) — 여기선 '단어 빈도 세기'로 활용
 * -> 자료구조 선택은 "순서가 필요한가? 중복을 막아야 하나? 먼저 온 것부터 처리하나? key로 찾나?"에
 *    대한 답이다. 컬렉션 지도를 알면 상황에 맞는 도구를 고를 수 있다.
 */
public class Example3_FourFamilies {

    public static void main(String[] args) {
        System.out.println("[예시 3] 같은 데이터 [A, B, A, C]를 네 자료구조에 넣어 비교");
        System.out.println();

        String[] data = {"A", "B", "A", "C"};

        // List: 순서 유지 + 중복 허용
        List<String> list = new ArrayList<>();
        for (String s : data) list.add(s);
        System.out.println("List  (순서O 중복O) : " + list);

        // Set: 중복 불허 (LinkedHashSet은 삽입 순서도 유지)
        Set<String> set = new LinkedHashSet<>();
        for (String s : data) set.add(s);
        System.out.println("Set   (중복X)       : " + set + "  <- 중복 A 하나 제거됨");

        // Queue: FIFO — 넣은 순서대로 꺼낸다
        Queue<String> queue = new ArrayDeque<>();
        for (String s : data) queue.offer(s);
        System.out.print("Queue (FIFO)        : poll 순서 -> ");
        while (!queue.isEmpty()) {
            System.out.print(queue.poll() + (queue.isEmpty() ? "\n" : ", "));
        }

        // Map: key-value, 같은 key는 값 갱신 -> '단어 빈도 세기'에 활용
        Map<String, Integer> map = new HashMap<>();
        for (String s : data) {
            map.put(s, map.getOrDefault(s, 0) + 1); // 같은 key면 카운트 증가
        }
        System.out.println("Map   (key-value)   : " + map + "  <- 같은 key는 값만 갱신(빈도 세기)");

        System.out.println();
        System.out.println("=> 같은 입력도 자료구조에 따라 결과가 다르다.");
        System.out.println("   순서? 중복 차단? FIFO? key로 조회? -> 질문에 맞는 컬렉션을 고른다.");
    }
}
