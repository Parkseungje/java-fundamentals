package com.study.part02_jvm.s03_stack_area;

/**
 * 예시 2 / 3 — StackOverflowError: "Stack은 유한하다. 프레임을 끝없이 쌓으면 넘친다."
 *
 * 이 예시가 답하려는 질문: 메서드 호출마다 프레임이 쌓인다면, 종료되지 않는 재귀처럼 프레임을
 * 계속 쌓기만 하면 어떻게 되는가? Stack에는 한계가 있는가?
 *
 * 왜 이 시나리오인가: 종료 조건(base case) 없이 자기 자신을 계속 부르는 재귀 메서드를 만든다.
 * 호출할 때마다 프레임이 하나씩 Stack에 쌓이는데, 빠져나가지(pop) 않으므로 무한정 쌓이기만 한다.
 * Stack 공간은 유한하므로, 한계를 넘는 순간 StackOverflowError가 발생해야 한다. 그때까지 도달한
 * 재귀 깊이를 세어 출력한다 = "프레임 하나가 Stack 공간을 차지한다"는 사실의 증거.
 *
 * 예상 결과:
 *   - StackOverflowError 발생(에러를 잡아 메시지 출력)
 *   - 도달한 깊이는 수천~수만 단위(JVM/스택 크기/프레임 크기에 따라 다름)
 * -> Stack은 무한이 아니다. 종료 조건 없는 재귀(혹은 너무 깊은 재귀)는 프레임을 계속 쌓아
 *    결국 StackOverflowError를 낸다. 정상 재귀라면 base case에서 더 이상 호출하지 않으므로
 *    프레임이 pop되며 줄어든다(Example1의 정상 종료와 대비).
 * 참고: -Xss 옵션으로 스택 크기를 바꾸면 도달 깊이가 달라진다 (예: java -Xss256k ...).
 */
public class Example2_StackOverflow {

    static int depth = 0; // 도달한 재귀 깊이를 기록(static이라 에러 후에도 값 확인 가능)

    // 종료 조건이 없는 재귀 -> 프레임이 계속 쌓이기만 한다.
    static void recurseForever() {
        depth++;
        recurseForever(); // 자기 자신 호출 -> 새 프레임 push, 영원히 pop되지 않음
    }

    public static void main(String[] args) {
        System.out.println("[예시 2] StackOverflowError: 프레임을 끝없이 쌓으면 Stack이 넘친다");
        System.out.println();

        try {
            recurseForever();
        } catch (StackOverflowError e) {
            // 에러가 아니라 Error지만, 학습 목적상 잡아서 도달 깊이를 확인한다.
            System.out.println("StackOverflowError 발생!");
            System.out.println("터지기 직전까지 도달한 재귀 깊이 = " + depth);
        }

        System.out.println();
        System.out.println("=> 메서드 호출마다 프레임이 쌓이는데, 종료 조건이 없으면 pop되지 않고");
        System.out.println("   계속 쌓여 유한한 Stack을 넘긴다 -> StackOverflowError.");
        System.out.println("   (java -Xss256k 처럼 스택 크기를 줄이면 도달 깊이가 더 작아진다)");
    }
}
