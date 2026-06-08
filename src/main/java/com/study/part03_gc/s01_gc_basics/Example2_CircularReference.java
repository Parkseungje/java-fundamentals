package com.study.part03_gc.s01_gc_basics;

import java.lang.ref.WeakReference;

/**
 * 예시 2 / 3 — 참조 카운팅의 한계: "순환 참조도 JVM은 회수한다 (= 참조 카운팅이 아니라는 증거)."
 *
 * 이 예시가 답하려는 질문: 두 객체가 서로를 가리키는 순환 참조(A⇄B)에서, 외부 참조를 모두 끊으면
 * 이 둘은 회수될까? 참조 카운팅 방식이라면 어떻게 될까?
 *
 * 왜 이 시나리오인가: A와 B를 만들어 서로를 가리키게 한다(A.ref=B, B.ref=A). 그리고 둘을 들여다볼
 * WeakReference를 둔다. 그다음 외부 strong 참조(a, b)를 모두 null로 끊는다.
 *   - 만약 GC가 '참조 카운팅'이라면: A는 B가 가리키므로 카운트 1, B는 A가 가리키므로 카운트 1.
 *     외부를 끊어도 서로 때문에 카운트가 0이 되지 않아 '영원히 회수 안 됨' = 메모리 누수.
 *   - 하지만 JVM은 '도달 가능성'이라면: 루트에서 A,B 묶음 전체로 가는 길이 끊겼으므로(서로만 참조)
 *     둘 다 garbage로 판정되어 회수된다.
 *
 * 예상 결과:
 *   - 외부 참조 끊고 gc 후 weakA.get()==null, weakB.get()==null (둘 다 회수됨)
 * -> 순환 참조인데도 둘 다 회수됐다 = JVM은 참조 카운팅이 아니라 도달 가능성으로 판단한다는 증거.
 *    참조 카운팅의 치명적 단점(순환 참조 누수)을 JVM이 어떻게 피하는지 보여준다.
 */
public class Example2_CircularReference {

    public static void main(String[] args) {
        System.out.println("[예시 2] 순환 참조(A⇄B)도 JVM은 회수한다 = 참조 카운팅이 아님");
        System.out.println();

        Node a = new Node("A");
        Node b = new Node("B");
        // 순환 참조 구성: 서로를 가리킨다.
        a.ref = b;
        b.ref = a;

        WeakReference<Node> weakA = new WeakReference<>(a);
        WeakReference<Node> weakB = new WeakReference<>(b);

        System.out.println("순환 참조 구성: A.ref=" + a.ref + ", B.ref=" + b.ref);

        // 외부 strong 참조를 모두 끊는다. 이제 A,B는 '서로만' 가리킬 뿐 루트에서 도달 불가.
        a = null;
        b = null;

        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) { }

        System.out.println("외부 참조 끊고 gc 후:");
        System.out.println("  weakA.get() = " + weakA.get() + "  <- null이면 A 회수됨");
        System.out.println("  weakB.get() = " + weakB.get() + "  <- null이면 B 회수됨");

        System.out.println();
        System.out.println("=> 서로를 가리키는 순환 참조인데도 둘 다 회수됐다.");
        System.out.println("   참조 카운팅이라면 카운트가 1로 남아 누수됐을 것 -> JVM은 도달 가능성 기반.");
    }
}
