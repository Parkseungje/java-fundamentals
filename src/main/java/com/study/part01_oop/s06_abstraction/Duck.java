package com.study.part01_oop.s06_abstraction;

/**
 * [모델] Flyable과 Swimmable을 '동시에' 구현하는 오리.
 *
 * 클래스는 하나만 상속할 수 있지만(extends), 인터페이스는 여러 개를 구현할 수 있다(implements A, B).
 * 그래서 오리는 "날 수 있고(Flyable) + 헤엄칠 수 있는(Swimmable)" 능력 조합을 갖는다.
 *
 * 주목: describeFlight()를 오버라이드하지 않았다. Flyable의 default 구현을 그대로 사용한다
 * -> Example3에서 "default 메서드 덕분에 구현체가 새 메서드를 강제로 구현하지 않아도 됨"을 보여준다.
 */
public class Duck implements Flyable, Swimmable {

    private final String name;

    public Duck(String name) {
        this.name = name;
    }

    @Override
    public void fly() {
        System.out.println(name + ": 푸드덕 난다");
    }

    @Override
    public void swim() {
        System.out.println(name + ": 첨벙첨벙 헤엄친다");
    }

    // describeFlight()는 일부러 오버라이드하지 않음 -> Flyable의 default 구현 사용
}
