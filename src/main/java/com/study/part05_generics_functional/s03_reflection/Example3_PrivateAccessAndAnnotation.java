package com.study.part05_generics_functional.s03_reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 예시 3 / 3 — private 접근과 어노테이션: "Reflection은 캡슐화를 우회하고 메타데이터를 읽는다."
 *
 * 이 예시가 답하려는 질문: Reflection으로 private 필드/메서드에 접근할 수 있나? 어노테이션은 어떻게
 * 읽나? 이게 왜 프레임워크의 핵심이고, 단점은 무엇인가?
 *
 * 왜 이 시나리오인가: Person의 name/age는 private이고 secret()도 private이라 일반 코드로는 외부에서
 * 접근 불가다(캡슐화). 하지만 Reflection은 setAccessible(true)로 이 접근 제어를 '우회'한다. 또
 * 클래스에 붙은 @Info 어노테이션을 런타임에 읽는다. 바로 이 두 능력(private 접근 + 어노테이션 읽기)이
 * 프레임워크의 동작 원리다:
 *   - JPA/Jackson: private 필드에 직접 값을 넣고 뺀다(getter/setter 없어도) -> setAccessible.
 *   - Spring/JPA/Jackson: @Component/@Entity/@JsonProperty 같은 어노테이션을 읽어 동작 결정.
 * 단점도 분명하다: (1) 캡슐화를 깨고, (2) 컴파일 타임 타입 안전성을 잃고(문자열 오타가 런타임 오류),
 * (3) 일반 호출보다 느리다.
 *
 * 예상 결과:
 *   - private 필드 name을 읽고/바꾸기 가능(setAccessible 후).
 *   - private 메서드 secret() 호출 가능.
 *   - @Info("...") 어노테이션 값을 읽음.
 * -> Reflection의 "캡슐화 우회 + 메타데이터 읽기"가 Spring/JPA/Jackson의 마법을 가능케 한다.
 *    강력하지만 단점(성능·안전성·캡슐화)이 있어, 일반 코드에서 남용하면 안 된다.
 */
public class Example3_PrivateAccessAndAnnotation {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] private 접근(캡슐화 우회) + 어노테이션 읽기");
        System.out.println();

        Person person = new Person("홍길동", 30);
        Class<?> clazz = person.getClass();

        // 1) private 필드 접근 (setAccessible로 우회)
        Field nameField = clazz.getDeclaredField("name");
        nameField.setAccessible(true); // private 접근 제어 우회 (사용자 클래스라 --add-opens 불필요)
        System.out.println("private 필드 name 읽기 : " + nameField.get(person));
        nameField.set(person, "김변경"); // private인데도 값 변경
        System.out.println("private 필드 name 변경 후: " + person);

        // 2) private 메서드 호출 (setAccessible로 우회)
        Method secret = clazz.getDeclaredMethod("secret");
        secret.setAccessible(true);
        System.out.println("private 메서드 secret() : " + secret.invoke(person));

        // 3) 어노테이션 읽기 (RUNTIME 유지라 가능)
        Info info = clazz.getAnnotation(Info.class);
        System.out.println("@Info 어노테이션 값     : \"" + info.value() + "\"");

        System.out.println();
        System.out.println("=> Reflection은 private 접근을 우회하고(setAccessible) 어노테이션을 읽는다.");
        System.out.println("   이 능력이 JPA(private 필드 매핑)·Jackson(JSON 매핑)·Spring(@어노테이션 처리)의 기반.");
        System.out.println("   단 단점: 캡슐화 위반 / 컴파일 타임 안전성 상실(문자열 오타=런타임 오류) / 성능 저하.");
    }
}
