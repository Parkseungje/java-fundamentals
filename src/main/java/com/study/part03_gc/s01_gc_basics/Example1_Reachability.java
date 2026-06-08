package com.study.part03_gc.s01_gc_basics;

import java.lang.ref.WeakReference;

/**
 * 예시 1 / 3 — Garbage와 도달 가능성(Reachability): "루트에서 도달 가능하면 살고, 끊기면 회수된다."
 *
 * 이 예시가 답하려는 질문: GC는 무엇을 '쓰레기(garbage)'로 보고 회수하는가? "참조되는지"를 어떻게
 * 판단하는가?
 *
 * 왜 이 시나리오인가: 객체 하나를 만들어 strong(강한) 참조 변수로 잡아두고, 그 객체를 가리키는
 * WeakReference(약한 참조)도 따로 둔다. WeakReference.get()은 "그 객체가 아직 살아있으면 객체를,
 * 회수됐으면 null을 돌려준다" — 즉 객체 생존 여부를 들여다보는 창이다.
 *   1) strong 참조가 살아있는 동안: GC를 해도 객체는 살아있어야 한다(루트에서 도달 가능).
 *   2) strong 참조를 null로 끊은 뒤: 더 이상 어디서도 도달 불가 -> GC가 회수해야 한다.
 * 만약 "GC는 도달 가능성으로 판단한다"는 설명이 맞다면, 1)에서는 get()이 객체를, 2)에서는 null을
 * 돌려줘야 한다.
 *
 * 예상 결과:
 *   - strong 참조 살아있을 때 + gc: weakRef.get() != null (살아있음)
 *   - strong 참조 끊고 + gc:        weakRef.get() == null (회수됨)
 * -> GC는 '참조 카운트'가 아니라 '루트(스택 지역변수/ static 등)에서 도달 가능한가'로 생존을 판단한다.
 *    도달 불가능해진 객체가 garbage이고, 그것을 Heap에서 자동 회수하는 것이 GC다.
 * 참고: System.gc()는 GC '요청'이라 보장은 아니지만, HotSpot 기본 설정에서는 대개 수행된다.
 */
public class Example1_Reachability {

    public static void main(String[] args) {
        System.out.println("[예시 1] 도달 가능성: 루트에서 닿으면 생존, 끊기면 회수");
        System.out.println();

        // strong 참조: 이 변수가 루트(main의 스택 지역변수)에서 객체를 붙잡는다.
        Object strong = new Object();
        // weak 참조: 객체 생존 여부를 들여다보는 창. strong 참조와 달리 GC를 막지 못한다.
        WeakReference<Object> weakRef = new WeakReference<>(strong);

        // 1) strong 참조가 살아있는 동안 GC -> 도달 가능하므로 회수되면 안 된다.
        System.gc();
        sleep();
        System.out.println("strong 참조 살아있을 때 weakRef.get() = " + weakRef.get()
                + "  <- null 아님(루트에서 도달 가능 -> 생존)");

        // 2) strong 참조를 끊는다 -> 이제 이 객체는 어디서도 도달 불가.
        strong = null;
        System.gc();
        sleep();
        System.out.println("strong 참조 끊은 뒤 weakRef.get() = " + weakRef.get()
                + "  <- null(도달 불가 -> garbage로 회수됨)");

        System.out.println();
        System.out.println("=> GC는 '루트에서 도달 가능한가'로 생존을 판단한다(Reachability Analysis).");
        System.out.println("   도달 불가능해진 객체가 garbage이고 자동 회수된다.");
    }

    private static void sleep() {
        try { Thread.sleep(100); } catch (InterruptedException ignored) { }
    }
}
