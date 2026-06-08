package com.study.part04_collections.s09_iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 예시 1 / 3 — Iterator 패턴: "내부 구조를 몰라도 같은 코드로 순회한다."
 *
 * 이 예시가 답하려는 질문: ArrayList(인덱스 기반), HashSet(인덱스 없음), LinkedList(연결 리스트)는
 * 내부 구조가 다 다른데, 어떻게 '같은 코드'로 순회할 수 있나?
 *
 * 왜 이 시나리오인가: Iterator가 없던 시절에는 컬렉션마다 순회 방식이 달랐다. ArrayList는 인덱스로
 * (for i; get(i)), HashSet은 인덱스가 없어 다른 방식으로... 즉 순회하려면 '내부 구조를 알아야' 했고
 * 캡슐화가 깨지고 코드가 중복됐다. Iterator 패턴은 모든 Collection이 iterator()를 제공하게 해서,
 * hasNext()/next()만으로 내부 구조와 무관하게 동일하게 순회할 수 있게 했다. 같은 순회 메서드
 * countAll(Collection)을 ArrayList/HashSet/LinkedList에 그대로 적용해 이를 확인한다.
 *
 * 예상 결과:
 *   - 같은 countAll 코드가 세 컬렉션 모두에서 동작해 원소를 빠짐없이 순회한다.
 * -> Iterator는 "내부 구조를 숨기고 순회 방법만 통일"한 추상화다. 그래서 컬렉션 종류가 바뀌어도
 *    순회 코드는 그대로 둘 수 있다(다형성, 1.5/4.3과 연결).
 */
public class Example1_UniformIteration {

    public static void main(String[] args) {
        System.out.println("[예시 1] Iterator 패턴: 내부 구조 무관하게 동일 코드로 순회");
        System.out.println();

        Collection<String> arrayList = new ArrayList<>(java.util.List.of("a", "b", "c"));
        Collection<String> hashSet = new HashSet<>(java.util.List.of("x", "y", "z"));
        Collection<String> linkedList = new LinkedList<>(java.util.List.of("1", "2", "3"));

        // 같은 메서드가 내부 구조가 다른 세 컬렉션 모두에 동작한다.
        System.out.println("ArrayList  원소 수 = " + countAll(arrayList));
        System.out.println("HashSet    원소 수 = " + countAll(hashSet));
        System.out.println("LinkedList 원소 수 = " + countAll(linkedList));

        System.out.println();
        System.out.println("=> 내부 구조(배열/해시/연결 리스트)가 달라도 iterator() + hasNext()/next()로");
        System.out.println("   똑같이 순회한다. Iterator는 '순회 방법을 통일한' 추상화다.");
    }

    // Collection이면 무엇이든 받아 Iterator로 순회 — 내부 구조를 전혀 몰라도 됨
    private static int countAll(Collection<String> collection) {
        int count = 0;
        Iterator<String> it = collection.iterator(); // 모든 Collection이 제공
        while (it.hasNext()) {
            String value = it.next();
            count++;
        }
        return count;
    }
}
