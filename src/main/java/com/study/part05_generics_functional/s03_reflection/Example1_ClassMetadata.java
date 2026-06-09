package com.study.part05_generics_functional.s03_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 예시 1 / 3 — Class 객체와 메타데이터: "모든 객체는 자기 설계 정보(Class)를 들고 있다."
 *
 * 이 예시가 답하려는 질문: Reflection의 출발점인 Class 객체는 어떻게 얻나? 그것으로 무엇을 알 수 있나?
 *
 * 왜 이 시나리오인가: 자바의 모든 클래스 정보는 Method Area에 Class 객체로 단 하나 로딩된다(2.2).
 * 이 Class 객체를 얻는 방법은 세 가지인데, 모두 '같은 객체'를 가리킨다(== 확인). Class 객체를 통해
 * 필드/메서드/생성자 목록 같은 '메타데이터'를 런타임에 조회할 수 있다 — 이것이 Reflection의 기본이다.
 *   - Person.class           : 컴파일 시점에 타입을 알 때
 *   - person.getClass()      : 인스턴스로부터
 *   - Class.forName("...")   : 문자열 이름으로(컴파일 시점에 타입을 몰라도 됨!)
 *
 * 예상 결과:
 *   - 세 방법으로 얻은 Class가 모두 == 로 같다(Method Area에 1개).
 *   - getDeclaredFields/Methods/Constructors로 Person의 멤버 목록을 런타임에 조회.
 * -> Class 객체는 "클래스의 설계도를 런타임에 들여다보는 창"이다. 이름만으로도 얻을 수 있어,
 *    컴파일 시점에 어떤 클래스가 올지 몰라도 다룰 수 있다(프레임워크의 기반).
 */
public class Example1_ClassMetadata {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] Class 객체 얻기 + 메타데이터 조회");
        System.out.println();

        Person person = new Person("홍길동", 30);

        // Class 객체 얻는 3가지 방법
        Class<?> c1 = Person.class;
        Class<?> c2 = person.getClass();
        Class<?> c3 = Class.forName("com.study.part05_generics_functional.s03_reflection.Person");

        System.out.println("Person.class == person.getClass() ? " + (c1 == c2));
        System.out.println("person.getClass() == Class.forName(...) ? " + (c2 == c3)
                + "  <- 셋 다 같은 Class 객체(Method Area에 1개, 2.2)");

        System.out.println();

        // 메타데이터 조회 (선언된 멤버 = getDeclaredXxx)
        System.out.println("필드 목록   : " + Arrays.stream(c1.getDeclaredFields())
                .map(Field::getName).collect(Collectors.joining(", ")));
        System.out.println("메서드 목록 : " + Arrays.stream(c1.getDeclaredMethods())
                .map(Method::getName).collect(Collectors.joining(", ")));
        System.out.println("생성자 개수 : " + c1.getDeclaredConstructors().length);
        for (Constructor<?> ctor : c1.getDeclaredConstructors()) {
            System.out.println("  생성자 파라미터 타입 = " + Arrays.toString(ctor.getParameterTypes()));
        }

        System.out.println();
        System.out.println("=> Class 객체는 '클래스 설계도를 런타임에 들여다보는 창'이다. 이름(문자열)만으로도");
        System.out.println("   얻을 수 있어, 컴파일 시점에 타입을 몰라도 다룰 수 있다(프레임워크의 기반).");
    }
}
