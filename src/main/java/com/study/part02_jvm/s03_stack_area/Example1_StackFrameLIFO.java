package com.study.part02_jvm.s03_stack_area;

/**
 * 예시 1 / 3 — 스택 프레임의 LIFO: "메서드 호출마다 프레임이 쌓이고, 나중에 불린 것이 먼저 끝난다."
 *
 * 이 예시가 답하려는 질문: 메서드가 다른 메서드를 호출하며 중첩되면, 그 호출들은 어떤 순서로
 * 시작되고 어떤 순서로 끝나는가?
 *
 * 왜 이 시나리오인가: methodA가 methodB를, methodB가 methodC를 호출하도록 3단 중첩을 만들고,
 * 각 메서드의 '진입'과 '종료'에 로그를 찍는다. 메서드를 호출하면 Stack 맨 위에 그 메서드의
 * 프레임(지역변수 + 매개변수)이 쌓이고(push), 메서드가 끝나면 맨 위 프레임이 제거된다(pop).
 * Stack은 LIFO(Last In First Out)이므로, 가장 나중에 쌓인 methodC가 가장 먼저 끝나야 한다.
 *
 * 예상 결과(출력 순서):
 *   A 진입 -> B 진입 -> C 진입 -> C 종료 -> B 종료 -> A 종료
 * 즉 진입은 A,B,C 순서지만 종료는 C,B,A 역순. 안쪽(나중 호출)이 먼저 끝나고 바깥이 나중에 끝난다.
 * 각 메서드의 지역변수(depthMarker)는 자기 프레임 안에만 있어 서로 간섭하지 않는다.
 * -> 이 push/pop의 역순 구조가 스택 프레임의 LIFO 동작이다. (메서드 호출의 "되돌아오는 길"이
 *    정확히 역순인 이유)
 */
public class Example1_StackFrameLIFO {

    static void methodA() {
        String depthMarker = "A의 지역변수"; // 이 변수는 A의 프레임에만 존재
        System.out.println("A 진입  (" + depthMarker + ")");
        methodB();                          // B 프레임이 A 위에 쌓인다(push)
        System.out.println("A 종료");        // B가 모두 끝나고 A 프레임으로 돌아온 뒤 실행
    }

    static void methodB() {
        String depthMarker = "B의 지역변수";
        System.out.println("  B 진입  (" + depthMarker + ")");
        methodC();                          // C 프레임이 B 위에 쌓인다(push)
        System.out.println("  B 종료");
    }

    static void methodC() {
        String depthMarker = "C의 지역변수";
        System.out.println("    C 진입  (" + depthMarker + ")");
        // 더 호출하지 않음 -> C가 끝나면 C 프레임이 먼저 제거된다(pop)
        System.out.println("    C 종료");
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] 스택 프레임 LIFO: 나중에 호출된 메서드가 먼저 끝난다");
        System.out.println();

        methodA();

        System.out.println();
        System.out.println("=> 진입 순서 A,B,C / 종료 순서 C,B,A (역순). 이것이 Stack의 LIFO다.");
        System.out.println("   호출하면 프레임 push, 끝나면 맨 위 프레임 pop. 각 프레임의 지역변수는 독립.");
    }
}
