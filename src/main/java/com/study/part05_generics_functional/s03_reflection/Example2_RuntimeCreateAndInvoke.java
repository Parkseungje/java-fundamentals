package com.study.part05_generics_functional.s03_reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 예시 2 / 3 — 런타임 생성과 호출: "문자열 이름만으로 객체를 만들고 메서드를 부른다."
 *
 * 이 예시가 답하려는 질문: 컴파일 시점에 Person이라는 타입을 직접 쓰지 않고도(이름 문자열만으로)
 * 객체를 만들고 메서드를 호출할 수 있나?
 *
 * 왜 이 시나리오인가: 코드 어디에도 'new Person(...)'이나 'person.add(...)' 같은 '직접 참조'가 없다.
 * 오직 문자열("...Person", "add")로 Class를 얻고, 생성자를 찾아 newInstance로 객체를 만들고,
 * 메서드를 찾아 invoke로 호출한다. 이렇게 하면 "어떤 클래스/메서드를 쓸지"를 런타임에 결정할 수
 * 있다. 이것이 Spring이 설정만 보고 객체를 만들고(DI), 프레임워크가 임의의 메서드를 호출하는 원리다.
 *
 * 예상 결과:
 *   - getDeclaredConstructor(String, int).newInstance("홍길동", 30) -> Person{name=홍길동, age=30}
 *   - getMethod("add", int, int).invoke(obj, 1, 2) -> 3
 * -> new 키워드 없이(2.6의 'Reflection 생성') 객체를 만들고, 메서드 이름 문자열로 호출했다.
 *    컴파일 시점에 타입을 몰라도 동작하므로 프레임워크가 사용자 클래스를 다룰 수 있다.
 */
public class Example2_RuntimeCreateAndInvoke {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] 런타임 인스턴스 생성 + 메서드 invoke (직접 참조 없이)");
        System.out.println();

        // 1) 문자열 이름으로 Class 얻기
        Class<?> clazz = Class.forName("com.study.part05_generics_functional.s03_reflection.Person");

        // 2) 생성자를 찾아 객체 생성 (new 키워드 없이!)
        Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, int.class);
        Object obj = ctor.newInstance("홍길동", 30);
        System.out.println("newInstance(\"홍길동\", 30) -> " + obj);

        // 3) 메서드를 이름으로 찾아 호출
        Method addMethod = clazz.getMethod("add", int.class, int.class);
        Object result = addMethod.invoke(obj, 1, 2);
        System.out.println("getMethod(\"add\",...).invoke(obj, 1, 2) -> " + result);

        System.out.println();
        System.out.println("=> 코드에 'new Person'도 '.add'도 없이, 문자열 이름만으로 객체 생성·메서드 호출.");
        System.out.println("   '어떤 클래스/메서드를 쓸지'를 런타임에 결정 -> Spring DI/프레임워크의 핵심 원리.");
    }
}
