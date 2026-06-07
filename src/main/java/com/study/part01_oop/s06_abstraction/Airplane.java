package com.study.part01_oop.s06_abstraction;

/**
 * [모델] Flyable만 구현하는 비행기.
 *
 * 오리와 종류는 전혀 다르지만(동물 vs 기계), '나는 능력'은 공유한다 -> 같은 Flyable 타입으로
 * 다룰 수 있다(can-do). 이것이 "is-a 계층(추상클래스)"으로는 묶기 어려운 관계를 인터페이스가
 * 자연스럽게 표현하는 지점이다(비행기를 동물 계층에 넣을 수는 없으니까).
 *
 * Duck과 달리 describeFlight()를 직접 오버라이드한다 -> default 메서드는 "원하면 재정의도 가능"함을
 * 보여준다(Example3에서 Duck의 기본 동작과 대비).
 */
public class Airplane implements Flyable {

    @Override
    public void fly() {
        System.out.println("비행기: 제트엔진으로 난다");
    }

    @Override
    public String describeFlight() {
        return "비행기는 제트엔진 추력으로 비행한다"; // default를 자기 방식으로 재정의
    }
}
