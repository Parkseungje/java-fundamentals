package com.study.part05_generics_functional.s03_reflection;

/**
 * [모델] Reflection 실습 대상 클래스.
 *
 * 다양한 멤버를 일부러 갖춰 두었다.
 *   - private 필드(name, age): Example3에서 setAccessible로 우회 접근.
 *   - 매개변수 있는 생성자: Example2에서 런타임 인스턴스 생성.
 *   - public 메서드(add): Example2에서 invoke로 호출.
 *   - private 메서드(secret): Example3에서 우회 호출.
 *   - 클래스에 붙은 @Info: Example3에서 어노테이션 읽기.
 */
@Info("사람을 표현하는 클래스")
public class Person {

    private String name;
    private int age;

    // 매개변수 있는 생성자(Reflection으로 호출)
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // public 메서드(invoke 대상)
    public int add(int a, int b) {
        return a + b;
    }

    // private 메서드(setAccessible로 우회 호출 대상)
    private String secret() {
        return name + "의 비밀: 나이는 " + age;
    }

    @Override
    public String toString() {
        return "Person{name=" + name + ", age=" + age + "}";
    }
}
