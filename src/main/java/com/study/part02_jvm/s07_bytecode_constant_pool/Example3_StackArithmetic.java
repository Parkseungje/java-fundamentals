package com.study.part02_jvm.s07_bytecode_constant_pool;

/**
 * 예시 3 / 3 — 스택 기반 명령: "JVM 바이트코드는 피연산자 스택으로 계산한다."
 *
 * 이 예시가 답하려는 질문: a + b 같은 산술 한 줄은 바이트코드에서 어떻게 표현되나? CPU 레지스터처럼
 * 동작하나, 아니면 다른 방식인가?
 *
 * 왜 이 시나리오인가: JVM은 '스택 머신(stack machine)'이다. 계산할 값들을 피연산자 스택에 올린 뒤
 * (push), 연산 명령이 스택에서 값을 꺼내(pop) 계산하고 결과를 다시 스택에 올린다. add(a,b)를
 * 컴파일하면 대략 이렇게 된다:
 *   iload_0   // 첫 번째 인자 a를 스택에 push
 *   iload_1   // 두 번째 인자 b를 스택에 push
 *   iadd      // 스택에서 두 값을 pop -> 더함 -> 결과를 push
 *   ireturn   // 스택 맨 위 값을 반환
 * 이 방식은 특정 CPU의 레지스터 구조에 의존하지 않아, 어떤 플랫폼에서도 동일하게 해석된다
 * (= "Write Once, Run Anywhere"의 바이트코드 레벨 근거).
 *
 * 예상 결과(실행): add(3,4)=7, compute()=14
 * 예상 결과(javap -c add): iload_0 / iload_1 / iadd / ireturn 이 보인다.
 * -> docs의 javap -c 명령으로 스택 연산을 직접 확인할 것.
 */
public class Example3_StackArithmetic {

    // 두 정수를 더한다. 바이트코드로는 iload/iload/iadd/ireturn.
    static int add(int a, int b) {
        return a + b;
    }

    // 지역 변수를 쓰는 약간 더 복잡한 계산. istore(저장)/iload(로드)/imul(곱)까지 관찰 가능.
    static int compute() {
        int a = 3;
        int b = 4;
        int c = a + b;
        return c * 2;
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] 스택 기반 산술 명령 (iload / iadd / ...)");
        System.out.println();

        System.out.println("add(3, 4) = " + add(3, 4));
        System.out.println("compute() = " + compute());

        System.out.println();
        System.out.println("=> a + b 는 '값을 스택에 올리고(iload) 연산이 꺼내 계산(iadd)'하는 스택 머신 방식.");
        System.out.println("   특정 CPU 레지스터에 의존하지 않아 플랫폼 독립적. docs javap -c로 확인.");
    }
}
