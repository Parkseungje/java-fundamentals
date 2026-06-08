package com.study.part04_collections.s03_collection_map;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;

/**
 * 예시 2 / 3 — Collection 계층: "List/Set/Queue는 Collection을 상속하지만, Map은 별개다."
 *
 * 이 예시가 답하려는 질문: 자바 컬렉션들은 어떤 상속 구조인가? Map도 Collection인가?
 *
 * 왜 이 시나리오인가: List, Set, Queue는 모두 Collection 인터페이스를 상속한다. 그래서 add(),
 * size(), iterator() 같은 공통 메서드를 가지며, 'Collection 타입'으로 묶어서 똑같이 다룰 수 있다
 * (다형성). 반면 Map은 'key-value 쌍'을 다루므로 단일 원소를 다루는 Collection과 구조가 달라
 * Collection을 상속하지 않는다(별개 계층). 이를 instanceof로 확인하고, Collection 타입 하나로
 * List/Set/Queue를 공통 처리하는 메서드를 만들어 다형성을 보여준다.
 *
 * 계층 요약:
 *   Collection ─┬─ List  (ArrayList, LinkedList ...)
 *               ├─ Set   (HashSet, TreeSet ...)
 *               └─ Queue (ArrayDeque, LinkedList ...)
 *   Map (Collection 아님) ─ HashMap, TreeMap ...
 *
 * 예상 결과:
 *   - ArrayList / HashSet / ArrayDeque 는 instanceof Collection -> true
 *   - HashMap 은 instanceof Collection -> false (별개 계층)
 *   - Collection 타입 매개변수 하나로 List/Set/Queue를 모두 처리 가능(공통 메서드).
 * -> List/Set/Queue는 'Collection'이라는 공통 추상으로 묶이고, Map은 별도다. 이 구조 덕에
 *    "어떤 컬렉션이든 받는" 코드를 Collection 타입으로 작성할 수 있다(다형성, 1.5와 연결).
 */
public class Example2_CollectionHierarchy {

    public static void main(String[] args) {
        System.out.println("[예시 2] Collection 계층: List/Set/Queue는 Collection, Map은 별개");
        System.out.println();

        Collection<String> list = new ArrayList<>();
        Collection<String> set = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> map = new HashMap<>();

        System.out.println("ArrayList  instanceof Collection ? " + (list instanceof Collection));
        System.out.println("HashSet    instanceof Collection ? " + (set instanceof Collection));
        System.out.println("ArrayDeque instanceof Collection ? " + (queue instanceof Collection));
        System.out.println("HashMap    instanceof Collection ? " + (map instanceof Collection)
                + "  <- Map은 Collection이 아님(key-value라 별개 계층)");

        System.out.println();

        // 다형성: Collection 타입 하나로 List/Set/Queue를 공통 처리
        System.out.println("Collection 타입 하나로 공통 처리(add/size):");
        System.out.println("  ArrayList  -> " + fillAndCount(new ArrayList<>()));
        System.out.println("  HashSet    -> " + fillAndCount(new HashSet<>()));
        System.out.println("  ArrayDeque -> " + fillAndCount(new ArrayDeque<>()));

        System.out.println();
        System.out.println("=> List/Set/Queue는 Collection 공통 추상으로 묶여 같은 코드로 다룰 수 있다.");
        System.out.println("   Map은 key-value 구조라 Collection 계층 밖의 별도 인터페이스다.");
    }

    // 어떤 Collection이든 받아서 공통 메서드(add, size)로 처리 -> 다형성
    private static int fillAndCount(Collection<String> c) {
        c.add("A");
        c.add("B");
        c.add("A"); // Set이면 중복 무시되어 size가 다르게 나올 것(예시3에서 자세히)
        return c.size();
    }
}
