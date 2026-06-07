package com.study.part02_jvm.s06_new_operator;

/**
 * 예시 2 / 3 — Reflection으로 new 없이 객체 생성: "new 키워드는 없지만 생성자는 호출된다."
 *
 * 이 예시가 답하려는 질문: new 키워드를 직접 쓰지 않고도 객체를 만들 수 있는가? 그렇게 만든
 * 객체도 생성자를 거치는가?
 *
 * 왜 이 시나리오인가: Reflection은 런타임에 클래스 정보(Class)를 얻어, 그 클래스의 생성자를 찾아
 * 호출함으로써 객체를 만든다. new 키워드는 코드에 없지만, 내부적으로는 생성자를 호출하는 것이라
 * Widget 생성자의 출력문이 찍혀야 한다. 즉 "new 없이 객체 생성"의 한 방법이지만, 생성자는 거친다.
 *
 * 예상 결과:
 *   - clazz.getDeclaredConstructor(int.class).newInstance(42) 호출 시 Widget 생성자 출력문이 찍힌다.
 *   - 만들어진 객체는 정상적인 Widget(id=42).
 * -> Reflection 생성도 결국 생성자를 호출한다. (다음 Example3의 clone/역직렬화는 생성자를 '건너뛴다'는
 *    점과 대비된다.) Reflection은 Spring/JPA/Jackson 등이 객체를 만들 때 쓰는 핵심 기법이다(PART 5).
 */
public class Example2_ReflectionNew {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] Reflection으로 객체 생성 (new 키워드 없이, 단 생성자는 호출됨)");
        System.out.println();

        // 1) 클래스 정보(Class) 얻기 — 문자열 이름으로도 가능
        Class<?> clazz = Class.forName("com.study.part02_jvm.s06_new_operator.Widget");
        System.out.println("Class 정보 획득: " + clazz.getName());

        // 2) 생성자를 찾아 호출 -> 객체 생성. new는 안 썼지만 생성자 출력문이 찍힐 것이다.
        System.out.println("newInstance(42) 호출:");
        Object obj = clazz.getDeclaredConstructor(int.class).newInstance(42);

        System.out.println("생성된 객체: " + obj);

        System.out.println();
        System.out.println("=> new 키워드는 없지만 생성자 출력문이 찍혔다 = Reflection 생성도 생성자를 호출한다.");
        System.out.println("   (Spring DI, JPA 엔티티, Jackson JSON 매핑이 모두 이 방식으로 객체를 만든다 — PART 5)");
    }
}
