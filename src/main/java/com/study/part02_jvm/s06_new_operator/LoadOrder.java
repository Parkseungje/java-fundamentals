package com.study.part02_jvm.s06_new_operator;

/**
 * [모델] new가 객체를 만드는 단계와 '초기화 순서'를 출력으로 관찰하기 위한 클래스.
 *
 * new 연산자가 하는 일(개념적 4단계):
 *   1. Class Metadata Zone에서 클래스 정보 찾기 (아직 로딩 안 됐으면 로딩 -> static 초기화 1번 실행)
 *   2. Heap에 객체 메모리 확보 (직접 출력으로는 안 보임 — 묵시적 단계)
 *   3. 멤버변수(인스턴스 필드/초기화 블록) 초기화 — 소스에 적힌 순서대로
 *   4. 생성자 본문 실행
 *
 * 이 클래스는 각 단계에 출력문을 심어, new를 호출할 때 어떤 순서로 실행되는지 보여준다.
 * 특히 static 초기화(1단계)는 '클래스 최초 사용 시 딱 1번'만 일어나고, 3~4단계는 'new 할 때마다'
 * 반복된다는 점을 두 번 생성해서 확인한다(-> Example1).
 */
public class LoadOrder {

    // 1단계 관련: 클래스가 처음 로딩될 때 단 한 번 실행된다.
    static {
        System.out.println("  [1] static 초기화 블록 (클래스 최초 로딩 시 1번만)");
    }

    // 3단계: 인스턴스 필드 초기화. new 할 때마다 실행된다.
    int field = logStep("  [3] 인스턴스 필드 초기화 (new 할 때마다)");

    // 3단계: 인스턴스 초기화 블록. 필드 초기화와 함께 '소스에 적힌 순서대로' 실행된다.
    {
        System.out.println("  [3] 인스턴스 초기화 블록 (new 할 때마다)");
    }

    // 4단계: 생성자 본문. 3단계가 모두 끝난 뒤 마지막에 실행된다.
    public LoadOrder() {
        System.out.println("  [4] 생성자 본문 (마지막)");
    }

    private static int logStep(String message) {
        System.out.println(message);
        return 0;
    }
}
