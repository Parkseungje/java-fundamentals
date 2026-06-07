package com.study.part02_jvm.s01_variable_types;

/**
 * [모델] 변수 3종류 중 '인스턴스 변수'와 '클래스 변수(static)'의 저장 위치 차이를 보여주는 카운터.
 *
 * 같은 클래스 안에 두 종류의 필드를 나란히 두어, 저장 영역의 차이가 '동작'으로 어떻게 드러나는지
 * 비교한다.
 *   - totalCreated (클래스 변수, static): Method Area에 단 1개만 존재 -> 모든 Counter 객체가 공유.
 *     그래서 어느 객체에서 바꾸든 모두에게 보인다. (-> Example3)
 *   - instanceCount (인스턴스 변수): 객체가 new로 만들어질 때 Heap에 객체와 함께 생긴다 ->
 *     객체마다 별도 공간. 그래서 객체끼리 값이 독립적이다. (-> Example2)
 */
public class Counter {

    // 클래스 변수(static): 클래스 로딩 시 Method Area에 1개 생성, JVM 종료까지 유지.
    // "지금까지 만들어진 Counter 객체 수"처럼 모든 인스턴스가 공유해야 하는 값에 적합.
    static int totalCreated = 0;

    // 인스턴스 변수: new로 객체를 만들 때마다 Heap에 그 객체 전용으로 생성, GC가 회수할 때 소멸.
    int instanceCount = 0;

    public Counter() {
        // 객체가 하나 생길 때마다 '공유' 카운터를 올린다 -> 모든 객체가 같은 변수를 본다는 증거(Example3).
        totalCreated++;
    }

    // 이 객체만의 카운트를 올린다 -> 객체마다 독립적으로 증가(Example2).
    public void countUp() {
        instanceCount++;
    }
}
