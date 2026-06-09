package com.study.part05_generics_functional.s01_generics_pecs;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 3 / 3 — PECS 원칙: "Producer-Extends, Consumer-Super."
 *
 * 이 예시가 답하려는 질문: 한 메서드가 한 컬렉션에서 꺼내(produce) 다른 컬렉션에 넣을(consume) 때,
 * 와일드카드를 어떻게 써야 가장 유연하면서 안전한가?
 *
 * 왜 이 시나리오인가: copy(src, dst)처럼 "src에서 꺼내 dst에 넣는" 메서드를 생각하자.
 *   - src는 원소를 '생산(produce)'한다(꺼내 읽기) -> ? extends T (상한)
 *   - dst는 원소를 '소비(consume)'한다(넣기)        -> ? super T (하한)
 * 이것이 PECS: Producer-Extends, Consumer-Super. 이렇게 선언하면 src로 List<Apple>을, dst로
 * List<Fruit>나 List<Object>를 받을 수 있어 가장 유연하다. 실제 자바 표준 라이브러리
 * (Collections.copy 등)도 이 패턴을 쓴다.
 *
 * 예상 결과:
 *   - copy(List<Apple>, List<Fruit>) 가 정상 동작해 Apple들이 Fruit 리스트로 복사됨.
 *   - copy(List<Apple>, List<Object>) 도 동작(Object는 Fruit의 상위라 super 조건 만족).
 * -> PECS는 "꺼내면(Produce) extends, 넣으면(Consume) super, 둘 다면 T 자체"라는 한 줄 규칙이다.
 *    불공변(예시1)의 엄격함을 와일드카드(예시2)로 풀되, 안전성을 유지하는 최적 지점이 PECS다.
 */
public class Example3_PECS {

    // PECS: src는 producer(꺼냄) -> extends, dst는 consumer(넣음) -> super
    static <T> void copy(List<? extends T> src, List<? super T> dst) {
        for (T item : src) {   // src에서 T로 꺼내 읽기(Producer-Extends)
            dst.add(item);     // dst에 T 넣기(Consumer-Super)
        }
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] PECS: Producer-Extends, Consumer-Super");
        System.out.println();

        List<Apple> apples = new ArrayList<>(List.of(new Apple(), new Apple()));

        // dst로 List<Fruit> (Fruit는 Apple의 상위 -> super 조건 OK)
        List<Fruit> fruits = new ArrayList<>();
        copy(apples, fruits); // src=List<Apple>(? extends Fruit), dst=List<Fruit>(? super Fruit)
        System.out.println("copy(List<Apple>, List<Fruit>) -> fruits = " + fruits);

        // dst로 List<Object> (Object도 Fruit의 상위 -> super 조건 OK, 더 유연)
        List<Object> objects = new ArrayList<>();
        copy(apples, objects);
        System.out.println("copy(List<Apple>, List<Object>) -> objects = " + objects);

        System.out.println();
        System.out.println("=> src는 꺼내므로 ? extends(Producer), dst는 넣으므로 ? super(Consumer).");
        System.out.println("   PECS 한 줄 규칙: 꺼내면 extends, 넣으면 super, 둘 다면 T 자체.");
        System.out.println("   불공변의 엄격함을 와일드카드로 풀되 안전성을 유지하는 최적점이다.");
    }
}
