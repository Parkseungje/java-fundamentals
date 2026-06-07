package com.study.part02_jvm.s02_runtime_data_areas;

/**
 * 예시 3 / 3 — Method Area: "클래스 정보는 클래스당 1번만 로딩되어 모든 객체가 공유한다."
 *
 * 이 예시가 답하려는 질문: Robot 객체를 여러 개 만들면 '클래스 정보(메타데이터)'도 객체 수만큼
 * 복제되는가? 아니면 클래스 정보는 한 번만 로딩되고 객체들은 그것을 공유하는가?
 *
 * 왜 이 시나리오인가: Robot 객체 두 개(r1, r2)를 만든다. 객체 자체는 Heap에 따로 있으므로
 * r1 != r2 다(서로 다른 객체). 하지만 getClass()가 돌려주는 '클래스 정보 객체(Class)'는 어떨까?
 * 만약 "클래스 정보가 Method Area에 1개만 로딩된다(Class Metadata Zone)"는 설명이 맞다면,
 * r1.getClass() == r2.getClass() 가 참이어야 한다(둘이 같은 Class 객체를 가리킴).
 *
 * 예상 결과:
 *   - r1 == r2            -> false (객체는 Heap에 각각 따로 존재)
 *   - r1.getClass() == r2.getClass() -> true (클래스 정보는 Method Area에 단 1개)
 * -> 클래스의 '설계 정보'(필드 선언, 메서드 시그니처 등)는 객체마다 복제되지 않는다. Method Area에
 *    한 번만 로딩되고 모든 인스턴스가 그것을 공유한다. 그래서 객체 100만 개를 만들어도 클래스
 *    메타데이터는 1벌뿐이다(메모리 효율). 이것이 Heap(객체별)과 Method Area(클래스당 1개)의 차이다.
 */
public class Example3_MethodAreaClassInfo {

    public static void main(String[] args) {
        System.out.println("[예시 3] Method Area: 클래스 정보는 1번 로딩되어 모든 객체가 공유");
        System.out.println();

        Robot r1 = new Robot("로봇A");
        Robot r2 = new Robot("로봇B");

        // 객체 자체는 Heap에 따로 존재 -> 서로 다른 객체.
        System.out.println("r1 == r2 ? " + (r1 == r2) + "  <- 객체는 Heap에 각각 따로(다른 객체)");

        // 클래스 정보(Class 객체)는 Method Area에 1개 -> 같은 것을 가리킴.
        Class<?> class1 = r1.getClass();
        Class<?> class2 = r2.getClass();
        System.out.println("r1.getClass() == r2.getClass() ? " + (class1 == class2)
                + "  <- 클래스 정보는 Method Area에 단 1개(공유)");
        System.out.println("로딩된 클래스: " + class1.getName());

        System.out.println();
        System.out.println("=> 객체는 객체마다 따로(Heap)지만, 클래스의 설계 정보는 한 번만 로딩되어");
        System.out.println("   모두가 공유한다(Method Area). 객체를 아무리 만들어도 클래스 메타데이터는 1벌.");
    }
}
