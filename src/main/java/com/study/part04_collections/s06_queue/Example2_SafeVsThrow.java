package com.study.part04_collections.s06_queue;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * 예시 2 / 3 — 안전 메서드 vs 강제 메서드: "빈 큐에서 실패할 때 false/null이냐, 예외냐."
 *
 * 이 예시가 답하려는 질문: Queue에는 비슷한 일을 하는 메서드가 두 벌(offer/peek/poll vs
 * add/element/remove) 있다. 무슨 차이인가?
 *
 * 왜 이 시나리오인가: Queue 연산은 실패할 수 있다(빈 큐에서 꺼내기, 가득 찬 큐에 넣기 등). 자바는
 * 실패 시 동작이 다른 두 벌의 메서드를 제공한다.
 *   - 안전 버전: 실패해도 예외를 안 던지고 false(삽입 실패) 또는 null(조회/제거 실패)을 반환.
 *   - 강제 버전: 실패하면 예외(NoSuchElementException 등)를 던진다.
 * 빈 큐에서 두 벌을 호출해 차이를 본다. 또한 Queue는 'null을 원소로 넣을 수 없다'는 점도 확인한다
 * (null을 반환값으로 쓰는 안전 메서드와 구분하기 위해 — null이 들어가면 "비었음"과 헷갈리니까).
 *
 *   | 동작 | 안전(실패 시 false/null) | 강제(실패 시 예외) |
 *   | 삽입 | offer()  | add()     |
 *   | 조회 | peek()   | element() |
 *   | 제거 | poll()   | remove()  |
 *
 * 예상 결과:
 *   - 빈 큐: poll()=null, peek()=null (안전, 예외 없음)
 *   - 빈 큐: remove(), element() -> NoSuchElementException (강제)
 *   - offer(null) -> NullPointerException (Queue는 null 원소 금지)
 * -> 흐름 제어가 필요하면 안전 버전(null 체크), 비었으면 안 되는 상황이면 강제 버전(예외로 빠르게 발견).
 */
public class Example2_SafeVsThrow {

    public static void main(String[] args) {
        System.out.println("[예시 2] 안전(offer/peek/poll) vs 강제(add/element/remove)");
        System.out.println();

        Queue<String> queue = new ArrayDeque<>();

        // 빈 큐에서 안전 메서드: 예외 없이 null 반환
        System.out.println("[빈 큐 + 안전 메서드]");
        System.out.println("  poll() = " + queue.poll() + "  <- null (예외 아님)");
        System.out.println("  peek() = " + queue.peek() + "  <- null (예외 아님)");

        System.out.println();

        // 빈 큐에서 강제 메서드: 예외 발생
        System.out.println("[빈 큐 + 강제 메서드]");
        try {
            queue.remove();
        } catch (Exception e) {
            System.out.println("  remove()  -> " + e.getClass().getSimpleName() + " (예외!)");
        }
        try {
            queue.element();
        } catch (Exception e) {
            System.out.println("  element() -> " + e.getClass().getSimpleName() + " (예외!)");
        }

        System.out.println();

        // null 삽입 불가
        System.out.println("[null 원소 삽입 시도]");
        try {
            queue.offer(null);
        } catch (NullPointerException e) {
            System.out.println("  offer(null) -> NullPointerException (Queue는 null 원소 금지)");
            System.out.println("  이유: poll/peek가 '비었음'을 null로 표현하므로, null 원소를 허용하면 구분 불가.");
        }

        System.out.println();
        System.out.println("=> 실패를 false/null로 받고 싶으면 안전 버전, 예외로 즉시 알고 싶으면 강제 버전.");
        System.out.println("   실무에선 흐름 제어가 쉬운 안전 버전(poll/peek/offer)을 더 자주 쓴다.");
    }
}
