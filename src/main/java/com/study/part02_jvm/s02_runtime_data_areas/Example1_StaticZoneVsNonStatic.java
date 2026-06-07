package com.study.part02_jvm.s02_runtime_data_areas;

/**
 * 예시 1 / 3 — Static Zone vs Non-Static Zone: "static 메서드는 인스턴스 메서드를 직접 못 부른다."
 *
 * 이 예시가 답하려는 질문: main() 같은 static 메서드는 객체 없이 실행된다. 그런데 왜 static
 * 메서드 안에서 인스턴스 메서드(greet)를 그냥 부르지 못하고 객체를 먼저 만들어야 하는가?
 *
 * 왜 이 시나리오인가: Method Area는 메서드 바이트코드를 'Static Zone'(static 메서드)과
 * 'Non-Static Zone'(인스턴스 메서드)으로 나눠 갖는다. static 메서드는 객체 없이 호출되므로
 * '어떤 객체(this)'라는 정보가 없다. 반면 인스턴스 메서드는 반드시 '특정 객체의' 행동이라 this가
 * 필요하다. 그래서 static -> 인스턴스 '직접' 호출은 불가능하고, 객체(Heap)를 거쳐야 한다.
 *   - Robot.factoryInfo()    : static -> static, 객체 없이 바로 호출 (정상)
 *   - Robot.staticCallsInstance() : static 안에서 인스턴스 메서드를 부르려고 객체를 먼저 생성
 *
 * 예상 결과:
 *   - factoryInfo()는 객체 없이 호출되어 생산 수를 출력
 *   - staticCallsInstance()는 내부에서 객체를 만들어 greet() 호출 성공
 *   - (직접 greet() 호출은 Robot.java에 컴파일 에러로 주석 처리됨)
 * -> main()이 static인 이유도 같은 맥락이다: JVM은 프로그램 시작 시 '아직 아무 객체도 없으므로',
 *    객체 없이 호출 가능한 static 메서드여야 진입점이 될 수 있다. main이 인스턴스 메서드였다면
 *    "그 객체를 누가 먼저 만들지?"라는 모순이 생긴다.
 */
public class Example1_StaticZoneVsNonStatic {

    // main 자체가 static 메서드(Static Zone)다. 객체 없이 JVM이 직접 호출한다.
    public static void main(String[] args) {
        System.out.println("[예시 1] Static Zone vs Non-Static Zone");
        System.out.println();

        // static -> static: 객체 없이 바로 호출 가능.
        System.out.println("(1) static 메서드 factoryInfo()를 객체 없이 호출:");
        Robot.factoryInfo();

        System.out.println();

        // static -> 인스턴스: 직접은 불가. 객체를 만들어 그 객체를 통해 호출해야 한다.
        System.out.println("(2) static 메서드 안에서 인스턴스 메서드를 부르려면 객체가 필요:");
        Robot.staticCallsInstance();

        System.out.println();
        System.out.println("=> static 메서드는 this(어떤 객체)가 없어서 인스턴스 메서드를 직접 못 부른다.");
        System.out.println("   객체(Heap)를 거쳐야만 인스턴스 메서드에 접근 가능.");
        System.out.println("   main()이 static인 이유도 동일: 시작 시점엔 객체가 없으므로 객체 없이");
        System.out.println("   호출 가능한 static이어야 진입점이 될 수 있다.");
    }
}
