package com.study.part04_collections.s09_iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 예시 3 / 3 — 순회 중 안전한 삭제: "Iterator.remove() 또는 removeIf()를 쓴다."
 *
 * 이 예시가 답하려는 질문: 순회하면서 조건에 맞는 원소를 지우고 싶을 때, CME(예시2) 없이 안전하게
 * 하려면 어떻게 하나?
 *
 * 왜 이 시나리오인가: 예시2에서 봤듯 for-each(Iterator) 도중 collection.remove()를 직접 호출하면
 * CME가 난다. 안전한 방법은 두 가지다.
 *   (A) Iterator.remove(): Iterator 자신이 제공하는 remove()는 "내가 방금 next()로 꺼낸 원소"를
 *       Iterator를 통해 제거하므로, Iterator가 그 변경을 인지해 CME가 안 난다.
 *   (B) removeIf(predicate): 조건을 주면 컬렉션이 알아서 안전하게 일괄 제거한다(가장 간결, 권장).
 * 짝수만 남기고 홀수를 제거하는 같은 작업을 두 방법으로 보여준다.
 *
 * 예상 결과:
 *   - Iterator.remove()로 홀수 제거 -> [2, 4, 6] (CME 없음)
 *   - removeIf로 홀수 제거 -> [2, 4, 6] (한 줄, CME 없음)
 * -> 순회 중 삭제는 '컬렉션을 직접' 건드리지 말고, Iterator.remove()나 removeIf()로 한다.
 *    현대 코드에서는 removeIf가 가장 간결하고 안전하다.
 */
public class Example3_SafeRemove {

    public static void main(String[] args) {
        System.out.println("[예시 3] 순회 중 안전한 삭제: Iterator.remove() / removeIf()");
        System.out.println();

        // (A) Iterator.remove() — Iterator를 통해 제거하므로 안전
        List<Integer> listA = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = listA.iterator();
        while (it.hasNext()) {
            int v = it.next();
            if (v % 2 == 1) {   // 홀수면
                it.remove();    // collection.remove()가 아니라 Iterator.remove() -> 안전
            }
        }
        System.out.println("(A) Iterator.remove()로 홀수 제거 -> " + listA + "  (CME 없음)");

        // (B) removeIf(predicate) — 가장 간결하고 안전(권장)
        List<Integer> listB = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        listB.removeIf(v -> v % 2 == 1); // 조건에 맞는 원소를 안전하게 일괄 제거
        System.out.println("(B) removeIf로 홀수 제거       -> " + listB + "  (한 줄, CME 없음)");

        System.out.println();
        System.out.println("=> 순회 중 삭제는 컬렉션을 직접(remove) 건드리지 말고,");
        System.out.println("   Iterator.remove() 또는 removeIf()를 쓴다. 현대 코드는 removeIf가 가장 간결.");
    }
}
