package com.study.part03_gc.s03_algorithms;

import java.util.List;

/**
 * 예시 3 / 3 — Reference Counting vs Reachability: "같은 순환 참조를 두 알고리즘으로 판정해본다."
 *
 * 이 예시가 답하려는 질문: 참조 카운팅과 도달 가능성은 순환 참조(A⇄B)를 각각 어떻게 처리하는가?
 * 왜 JVM이 참조 카운팅을 버리고 도달 가능성을 택했는가?
 *
 * 왜 이 시나리오인가: 3.1에서 'JVM은 순환 참조도 회수한다'를 실제 JVM으로 봤다. 여기서는 두
 * 알고리즘을 '직접 구현'해 같은 순환 참조에 적용함으로써, 왜 결과가 갈리는지 메커니즘으로 보인다.
 *   - 구성: A와 B가 서로를 가리킨다(A.refCount=1, B.refCount=1). 루트는 둘 중 누구도 가리키지 않는다.
 *   - 참조 카운팅 판정: refCount가 0인 것만 garbage로 본다 -> A,B 모두 1이라 '살아있다'고 오판(누수).
 *   - 도달 가능성 판정: 루트에서 mark를 시작 -> A,B에 도달 못 함 -> 둘 다 garbage로 정확히 판정.
 *
 * 예상 결과:
 *   - 참조 카운팅: A,B 모두 "회수 안 됨"(refCount=1) -> 메모리 누수
 *   - 도달 가능성: A,B 모두 "회수됨"(unmarked) -> 정확
 * -> 참조 카운팅은 순환 참조를 못 푼다. 도달 가능성은 '루트에서 닿는가'만 보므로 순환이어도
 *    루트에서 끊겼으면 회수한다. 이것이 JVM이 도달 가능성을 택한 이유다(3.1의 결론을 메커니즘으로 재확인).
 */
public class Example3_RefCountVsReachability {

    public static void main(String[] args) {
        System.out.println("[예시 3] 참조 카운팅 vs 도달 가능성 (같은 순환 참조 A⇄B에 적용)");
        System.out.println();

        // 루트(아무도 A,B를 가리키지 않게 둔다)
        MiniObject root = new MiniObject("ROOT");

        // 순환 참조 구성: A⇄B (서로의 refCount가 1이 됨)
        MiniObject a = new MiniObject("A");
        MiniObject b = new MiniObject("B");
        a.pointTo(b); // b.refCount = 1
        b.pointTo(a); // a.refCount = 1

        System.out.println("구성: A⇄B, 루트는 A/B를 가리키지 않음");
        System.out.println("  A.refCount=" + a.refCount + ", B.refCount=" + b.refCount);
        System.out.println();

        // --- 알고리즘 1: 참조 카운팅 판정 (refCount==0 만 회수) ---
        System.out.println("[참조 카운팅] refCount==0 인 것만 garbage로 본다:");
        System.out.println("  A 회수? " + (a.refCount == 0) + " / B 회수? " + (b.refCount == 0)
                + "  <- 둘 다 1이라 회수 안 됨 = 순환 참조 누수!");
        System.out.println();

        // --- 알고리즘 2: 도달 가능성 판정 (루트에서 mark) ---
        mark(root); // 루트에서 도달 가능한 것만 marked. A,B는 루트에서 못 감.
        System.out.println("[도달 가능성] 루트에서 mark -> 안 찍힌 것은 garbage:");
        System.out.println("  A 회수? " + (!a.marked) + " / B 회수? " + (!b.marked)
                + "  <- 둘 다 루트에서 도달 불가라 정확히 회수됨");

        System.out.println();
        System.out.println("=> 참조 카운팅은 순환 참조를 못 푼다(카운트가 0이 안 됨). 도달 가능성은");
        System.out.println("   '루트에서 닿는가'만 보므로 순환이어도 끊겼으면 회수한다 -> JVM이 택한 방식(3.1).");
    }

    private static void mark(MiniObject obj) {
        if (obj == null || obj.marked) return;
        obj.marked = true;
        for (MiniObject child : obj.refs) mark(child);
    }
}
