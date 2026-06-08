package com.study.part04_collections.s06_queue;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 예시 1 / 3 — Queue의 FIFO: "먼저 넣은 것이 먼저 나온다. peek는 보기만 하고 빼지 않는다."
 *
 * 이 예시가 답하려는 질문: Queue에 넣은 순서와 꺼내는 순서는 어떤 관계인가? peek와 poll은 어떻게 다른가?
 *
 * 왜 이 시나리오인가: Queue는 FIFO(First In First Out, 선입선출) 자료구조다. 줄 서기처럼 먼저 온
 * 사람이 먼저 처리된다. A, B, C 순서로 offer(넣기)한 뒤 poll(꺼내며 제거)하면 정확히 A, B, C 순서로
 * 나와야 한다. peek는 "맨 앞을 보기만 하고 제거하지 않는" 조회이므로, peek를 여러 번 해도 큐 크기가
 * 줄지 않아야 한다. 이 둘을 직접 확인한다. (사용처: BFS 탐색, 버퍼, 메시지 큐(MQ) 등 '들어온 순서대로
 * 처리'가 필요한 모든 곳)
 *
 * 예상 결과:
 *   - offer A,B,C 후 peek() -> A (제거 안 됨, size 그대로 3)
 *   - poll() 반복 -> A, B, C 순서로 나옴 (FIFO)
 * -> Queue는 선입선출. peek(보기)와 poll(꺼내기)을 구분해서 쓴다.
 */
public class Example1_Fifo {

    public static void main(String[] args) {
        System.out.println("[예시 1] Queue FIFO: 먼저 넣은 게 먼저 나온다 + peek vs poll");
        System.out.println();

        // Queue 구현체로 ArrayDeque 사용(권장, 예시3에서 이유 설명)
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("A");
        queue.offer("B");
        queue.offer("C");
        System.out.println("offer A, B, C 후 큐: " + queue + " (size=" + queue.size() + ")");

        // peek: 맨 앞을 보기만 함(제거 X)
        System.out.println();
        System.out.println("peek() = " + queue.peek() + "  <- 맨 앞 A를 보기만");
        System.out.println("peek() = " + queue.peek() + "  <- 다시 봐도 A (제거 안 됨)");
        System.out.println("peek 두 번 후 size = " + queue.size() + "  <- 그대로 3");

        // poll: 맨 앞을 꺼내며 제거
        System.out.println();
        System.out.print("poll() 반복 -> ");
        while (!queue.isEmpty()) {
            System.out.print(queue.poll() + (queue.isEmpty() ? "\n" : ", "));
        }
        System.out.println("모두 poll 후 size = " + queue.size());

        System.out.println();
        System.out.println("=> 넣은 순서(A,B,C)대로 나온다 = FIFO. peek는 보기만, poll은 꺼내며 제거.");
        System.out.println("   BFS·버퍼·메시지 큐처럼 '들어온 순서대로 처리'가 필요할 때 쓴다.");
    }
}
