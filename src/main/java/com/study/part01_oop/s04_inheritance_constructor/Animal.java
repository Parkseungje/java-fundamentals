package com.study.part01_oop.s04_inheritance_constructor;

/**
 * [모델] 상속 계층의 부모(슈퍼클래스).
 *
 * 자식(Dog)이 물려받을 공통 상태(name)와 공통 행동(eat)을 정의한다.
 * 생성자에 출력문을 넣어둔 이유: 자식 객체를 만들 때 부모 생성자가 '언제' 실행되는지를
 * 콘솔 순서로 눈으로 확인하기 위함이다 (-> Example2의 생성자 체이닝).
 */
public class Animal {

    // 자식에게도 물려줄 공통 상태. protected라 자식 클래스에서 직접 접근할 수 있다.
    protected String name;

    public Animal(String name) {
        this.name = name;
        // 이 줄이 Dog 생성자보다 '먼저' 찍히면, 부모 생성자가 먼저 실행된다는 증거가 된다.
        System.out.println("  [Animal 생성자] name=" + name + " 초기화");
    }

    // 자식이 그대로 물려받아 쓸 수 있는 공통 행동.
    public void eat() {
        System.out.println(name + "가 먹는다");
    }
}
