package com.study.part04_collections.s06_queue;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 예시 3 / 3 — ArrayDeque는 Deque: "Queue(FIFO)로도, Stack(LIFO)으로도 쓸 수 있다."
 *
 * 이 예시가 답하려는 질문: ArrayDeque는 왜 권장되나? 하나의 자료구조로 큐와 스택을 모두 표현할 수
 * 있나?
 *
 * 왜 이 시나리오인가: ArrayDeque는 'Deque(Double Ended Queue, 양쪽 끝 큐)'라서 앞/뒤 양쪽에서 넣고
 * 뺄 수 있다. 그래서 사용 메서드만 바꾸면 두 가지로 쓸 수 있다.
 *   - Queue처럼(FIFO): 뒤로 넣고(offerLast) 앞에서 뺀다(pollFirst) -> 넣은 순서대로 나옴.
 *   - Stack처럼(LIFO): 앞으로 넣고(push) 앞에서 뺀다(pop) -> 마지막에 넣은 게 먼저 나옴.
 * 같은 ArrayDeque를 두 방식으로 써서 결과(순서)가 어떻게 갈리는지 본다.
 *
 * 권장 이유: 큐가 필요할 때 LinkedList보다 ArrayDeque가 빠르고(배열 기반, 캐시 효율 좋음),
 * 스택이 필요할 때도 구식 java.util.Stack(Vector 기반, 동기화로 느리고 설계가 낡음) 대신 ArrayDeque를
 * 쓰는 것이 표준 권장이다.
 *
 * 예상 결과:
 *   - Queue 방식(FIFO): A, B, C 넣고 빼면 -> A, B, C (먼저 넣은 게 먼저)
 *   - Stack 방식(LIFO): A, B, C 넣고 빼면 -> C, B, A (나중에 넣은 게 먼저)
 * -> 하나의 ArrayDeque로 큐/스택을 모두 표현. FIFO가 필요하면 Queue 방식, LIFO면 Stack 방식.
 *    실무에서 큐도 스택도 ArrayDeque가 기본 권장(LinkedList/Stack보다 나음).
 */
public class Example3_DequeAsQueueAndStack {

    public static void main(String[] args) {
        System.out.println("[예시 3] ArrayDeque로 Queue(FIFO)와 Stack(LIFO) 둘 다 표현");
        System.out.println();

        // Queue 방식(FIFO): 뒤로 넣고 앞에서 뺀다
        Deque<String> asQueue = new ArrayDeque<>();
        asQueue.offerLast("A");
        asQueue.offerLast("B");
        asQueue.offerLast("C");
        System.out.print("[Queue 방식 FIFO] offerLast A,B,C -> pollFirst: ");
        while (!asQueue.isEmpty()) {
            System.out.print(asQueue.pollFirst() + (asQueue.isEmpty() ? "\n" : ", "));
        }

        // Stack 방식(LIFO): 앞으로 넣고(push) 앞에서 뺀다(pop)
        Deque<String> asStack = new ArrayDeque<>();
        asStack.push("A");
        asStack.push("B");
        asStack.push("C");
        System.out.print("[Stack 방식 LIFO] push A,B,C -> pop:        ");
        while (!asStack.isEmpty()) {
            System.out.print(asStack.pop() + (asStack.isEmpty() ? "\n" : ", "));
        }

        System.out.println();
        System.out.println("=> 같은 ArrayDeque인데 메서드만 바꾸면 FIFO(A,B,C) / LIFO(C,B,A)로 동작한다.");
        System.out.println("   큐도 스택도 ArrayDeque가 기본 권장(LinkedList·구식 Stack보다 빠르고 깔끔).");
    }
}
