package com.study.part02_jvm.s06_new_operator;

/**
 * 예시 1 / 3 — new의 단계와 초기화 순서: "static(1번) -> 필드/블록 -> 생성자".
 *
 * 이 예시가 답하려는 질문: new LoadOrder()를 호출하면 내부적으로 어떤 순서로 초기화가 일어나는가?
 * 그리고 static 초기화는 객체를 만들 때마다 실행되는가, 아니면 한 번만 실행되는가?
 *
 * 왜 이 시나리오인가: LoadOrder를 두 번 생성한다. new가 "클래스 정보 찾기(필요 시 로딩+static
 * 초기화) -> Heap 메모리 확보 -> 멤버 초기화 -> 생성자"의 순서로 동작한다면:
 *   - static 초기화 블록 [1] 은 '첫 번째 new에서 클래스가 처음 로딩될 때 딱 1번'만 찍혀야 한다.
 *   - 인스턴스 필드/블록 초기화 [3] 과 생성자 [4] 는 'new 할 때마다'(즉 두 번) 찍혀야 한다.
 *   - 한 번의 new 안에서는 [3]들이 먼저, [4] 생성자가 마지막에 와야 한다.
 *
 * 예상 결과:
 *   첫 번째 new -> [1] static (1번) -> [3] 필드 -> [3] 블록 -> [4] 생성자
 *   두 번째 new -> ([1] 없음) -> [3] 필드 -> [3] 블록 -> [4] 생성자
 * -> static 초기화는 클래스당 1번, 인스턴스 초기화+생성자는 객체마다. 그리고 생성자는 항상 '마지막'.
 *    (2단계 Heap 메모리 확보는 출력으로는 안 보이지만, [3] 초기화가 가능하다는 것 자체가 메모리가
 *    이미 확보됐다는 의미다.)
 */
public class Example1_NewSteps {

    public static void main(String[] args) {
        System.out.println("[예시 1] new의 단계와 초기화 순서");
        System.out.println();

        System.out.println("첫 번째 new LoadOrder():");
        new LoadOrder();

        System.out.println();
        System.out.println("두 번째 new LoadOrder():");
        new LoadOrder();

        System.out.println();
        System.out.println("=> static 초기화[1]은 클래스 최초 로딩 시 1번만. 인스턴스 초기화[3]+생성자[4]는");
        System.out.println("   new 할 때마다. 한 번의 new 안에서는 필드/블록 초기화가 먼저, 생성자가 마지막.");
    }
}
