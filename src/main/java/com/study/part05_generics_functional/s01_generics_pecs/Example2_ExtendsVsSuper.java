package com.study.part05_generics_functional.s01_generics_pecs;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 2 / 3 — 상한(? extends) vs 하한(? super): "읽기는 extends, 쓰기는 super."
 *
 * 이 예시가 답하려는 질문: 와일드카드 ? extends T 와 ? super T 는 각각 무엇을 할 수 있고 무엇을 막나?
 *
 * 왜 이 시나리오인가: 불공변(예시1) 때문에 "Fruit 또는 그 하위 타입의 리스트"를 유연하게 받으려면
 * 와일드카드가 필요하다.
 *   - ? extends Fruit (상한, Producer): "Fruit이거나 그 하위 타입의 리스트". 원소를 꺼내면 적어도
 *     Fruit임이 보장되므로 '읽기(꺼내기)'는 안전. 하지만 정확한 타입을 모르므로(Apple 리스트일 수도,
 *     Banana 리스트일 수도) '쓰기(넣기)'는 막힌다(null만 가능).
 *   - ? super Fruit (하한, Consumer): "Fruit이거나 그 상위 타입의 리스트". Fruit(과 그 하위 Apple/
 *     Banana)를 '넣기'는 안전(어떤 상위 타입 리스트든 Fruit는 담을 수 있으므로). 하지만 꺼내면
 *     타입이 Object로만 나온다(상위 타입이 무엇인지 모르므로).
 *
 * 예상 결과:
 *   - ? extends Fruit 리스트: 꺼내서 Fruit로 읽기 OK / add는 컴파일 에러(주석)
 *   - ? super Fruit 리스트: Apple/Banana add OK / 꺼내면 Object로만
 * -> "꺼내 읽을 거면 extends, 넣을 거면 super"가 다음 예시(PECS)의 핵심 규칙으로 이어진다.
 */
public class Example2_ExtendsVsSuper {

    public static void main(String[] args) {
        System.out.println("[예시 2] ? extends(읽기) vs ? super(쓰기)");
        System.out.println();

        // ? extends Fruit : Fruit이거나 하위 타입의 리스트 -> 읽기 안전, 쓰기 불가
        List<Apple> apples = new ArrayList<>(List.of(new Apple(), new Apple()));
        List<? extends Fruit> producers = apples; // Apple 리스트를 받을 수 있다(유연!)
        System.out.println("[? extends Fruit] (Producer, 읽기 전용)");
        Fruit first = producers.get(0); // 꺼내면 최소 Fruit임이 보장 -> 읽기 OK
        System.out.println("  꺼내 읽기: get(0) = " + first + " (Fruit로 안전하게 읽음)");
        // producers.add(new Apple()); // <- 컴파일 에러: 정확한 타입을 몰라 넣을 수 없음
        System.out.println("  add(...)는 컴파일 에러(주석) -> 정확한 원소 타입을 몰라 넣기 불가");

        System.out.println();

        // ? super Fruit : Fruit이거나 상위 타입의 리스트 -> 쓰기 안전, 읽기는 Object
        List<Fruit> fruits = new ArrayList<>();
        List<? super Fruit> consumers = fruits; // Fruit(또는 상위) 리스트
        System.out.println("[? super Fruit] (Consumer, 쓰기 가능)");
        consumers.add(new Apple());  // Fruit의 하위 Apple 넣기 OK
        consumers.add(new Banana()); // Banana 넣기 OK
        System.out.println("  add(Apple), add(Banana) OK -> 넣기 안전");
        Object obj = consumers.get(0); // 꺼내면 Object로만(상위 타입이 무엇인지 몰라서)
        System.out.println("  꺼내면 Object로만: get(0) = " + obj + " (정확한 타입 보장 안 됨)");

        System.out.println();
        System.out.println("=> ? extends = 꺼내 읽기 안전(넣기 불가), ? super = 넣기 안전(꺼내면 Object).");
        System.out.println("   이 비대칭이 'PECS' 규칙으로 정리된다(예시3).");
    }
}
