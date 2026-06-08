package com.study.part03_gc.s01_gc_basics;

/**
 * [모델] 순환 참조 실험용 노드. 서로를 가리킬 수 있는 ref 필드를 가진다.
 *
 * Example2에서 두 Node가 서로를 참조(A.ref=B, B.ref=A)하게 만들어 '순환 참조'를 구성한다.
 * 참조 카운팅 방식이라면 외부 참조를 끊어도 A,B의 카운터가 서로 때문에 1로 남아 회수되지 않지만,
 * JVM은 도달 가능성(reachability) 기반이라 루트에서 끊긴 순환 묶음을 통째로 회수한다.
 * 그 차이를 관찰하는 데 쓰인다.
 */
public class Node {

    final String name;
    Node ref; // 다른 노드를 가리킬 수 있음 (순환 참조 구성용)

    public Node(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Node(" + name + ")";
    }
}
