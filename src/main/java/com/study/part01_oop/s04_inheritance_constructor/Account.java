package com.study.part01_oop.s04_inheritance_constructor;

/**
 * [모델] 생성자 함정을 보여주기 위한 부모 클래스.
 *
 * 핵심 포인트: 이 클래스에는 '매개변수 있는 생성자'만 있고, '매개변수 없는 기본 생성자'가 없다.
 *
 * 자바는 클래스에 생성자를 하나도 안 적으면 기본 생성자 Account()를 자동으로 넣어준다.
 * 하지만 이렇게 매개변수 생성자를 직접 하나라도 정의하면, 자동 기본 생성자는 만들어지지 않는다.
 * 그 결과 Account()는 존재하지 않게 되고, 자식이 super()를 자동 호출하려 할 때 부를 대상이 없어
 * 컴파일 에러가 난다 (-> Example3에서 확인).
 */
public class Account {

    protected String owner;

    // 매개변수 있는 생성자만 존재. 기본 생성자 Account()는 자동 생성되지 않는다.
    public Account(String owner) {
        this.owner = owner;
        System.out.println("  [Account 생성자] owner=" + owner);
    }
}
