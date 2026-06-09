package com.study.part05_generics_functional.s01_generics_pecs;

/**
 * [모델] 와일드카드(? extends / ? super) 실습용 과일 계층의 부모.
 *
 * Fruit <- Apple, Banana 의 상속 관계를 만들어, 제네릭의 공변/불공변과 상한/하한 와일드카드를
 * 관찰한다. 예를 들어 "Fruit를 꺼내 읽기"는 ? extends Fruit, "Fruit를 넣기"는 ? super Fruit로 본다.
 */
public class Fruit {
    final String name;

    public Fruit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
