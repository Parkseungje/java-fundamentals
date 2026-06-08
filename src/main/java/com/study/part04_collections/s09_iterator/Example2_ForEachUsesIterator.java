package com.study.part04_collections.s09_iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 예시 2 / 3 — for-each는 Iterator다 + 순회 중 수정의 함정(ConcurrentModificationException).
 *
 * 이 예시가 답하려는 질문: for-each 문법은 내부적으로 무엇을 쓰나? 순회 중에 컬렉션을 수정하면 왜
 * 예외가 나나?
 *
 * 왜 이 시나리오인가: for-each(`for (T x : collection)`)는 사실 컴파일 시 Iterator를 쓰는 코드로
 * 바뀐다(문법 설탕). 그래서 명시적 Iterator 순회와 결과가 같다 — 이를 나란히 보여준다. 그런데
 * for-each(=Iterator)로 순회하는 도중에 컬렉션 자체를 직접 수정(collection.remove(...))하면, Iterator가
 * "순회 중에 구조가 바뀌었다"를 감지해 ConcurrentModificationException(CME)을 던진다. 이는 잘못된
 * 순회로 인한 버그를 빨리 잡으라는 fail-fast 동작이다. 이 함정을 직접 재현한다.
 *
 * 예상 결과:
 *   - for-each와 명시적 Iterator 순회 결과가 동일하다.
 *   - for-each 도중 list.remove(...)를 호출하면 ConcurrentModificationException 발생.
 * -> for-each = Iterator. 그래서 순회 중에 컬렉션을 '직접' 수정하면 안 된다(안전한 삭제는 예시3).
 */
public class Example2_ForEachUsesIterator {

    public static void main(String[] args) {
        System.out.println("[예시 2] for-each는 Iterator + 순회 중 수정 시 CME");
        System.out.println();

        List<String> list = new ArrayList<>(List.of("a", "b", "c", "d"));

        // (1) for-each와 명시적 Iterator는 동등하다
        System.out.print("for-each       : ");
        for (String s : list) System.out.print(s + " ");
        System.out.println();

        System.out.print("명시적 Iterator: ");
        Iterator<String> it = list.iterator();
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();
        System.out.println("-> 둘은 같은 동작(for-each가 내부적으로 iterator()를 쓴다)");

        System.out.println();

        // (2) 순회 중 컬렉션 직접 수정 -> ConcurrentModificationException
        System.out.println("for-each 도중 list.remove(\"b\") 시도:");
        try {
            for (String s : list) {
                if (s.equals("b")) {
                    list.remove(s); // 순회 중 컬렉션 구조 변경 -> Iterator가 감지
                }
            }
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("  ConcurrentModificationException 발생! (fail-fast)");
            System.out.println("  이유: for-each(Iterator) 순회 중 컬렉션이 직접 수정되어 일관성이 깨짐을 감지");
        }
        // [유명한 함정] 만약 원소가 3개 [a,b,c]이고 '끝에서 두 번째'인 b를 지우면 CME가 '안' 난다.
        // remove 후 size가 2가 되고 cursor도 2라, 루프가 next()를 다시 부르기 전에 끝나버려
        // 감지 코드(checkForComodification)가 실행되지 않기 때문. 그래서 4개 [a,b,c,d]로 두어
        // 삭제 후에도 next()가 한 번 더 불리게 만들어 CME를 확실히 재현했다.

        System.out.println();
        System.out.println("=> for-each는 Iterator를 쓰는 문법 설탕이다. 그래서 순회 중 컬렉션을 직접");
        System.out.println("   수정하면 CME가 난다. 안전하게 지우려면 Iterator.remove()나 removeIf()를 쓴다(예시3).");
    }
}
