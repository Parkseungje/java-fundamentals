package com.study.part03_gc.s03_algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * [모델] GC 알고리즘을 시뮬레이션하기 위한 '가짜 객체'.
 *
 * 실제 JVM의 GC 알고리즘(Mark-Sweep, Mark-Compact, 참조 카운팅)은 내부에 숨겨져 있어 자바 코드로
 * 직접 들여다볼 수 없다. 그래서 이 클래스로 '미니 힙' 위의 객체를 흉내 내어, 각 알고리즘이 어떻게
 * 동작하는지(마킹/스윕/압축/카운팅)를 우리가 직접 구현해 눈으로 확인한다.
 *
 * 필드 의미:
 *   - name   : 식별용 이름
 *   - marked : Mark 단계에서 '도달 가능'으로 표시됐는지 (Mark-Sweep/Compact용)
 *   - refs   : 이 객체가 가리키는 다른 객체들 (도달 가능성 추적 + 순환 참조 구성용)
 *   - refCount : 참조 카운팅 시뮬레이션용 카운터 (나를 가리키는 참조 수)
 */
public class MiniObject {

    final String name;
    boolean marked = false;
    final List<MiniObject> refs = new ArrayList<>();
    int refCount = 0;

    public MiniObject(String name) {
        this.name = name;
    }

    // 이 객체가 target을 가리키게 한다 -> 참조 카운팅 시뮬레이션을 위해 target의 refCount도 올린다.
    public void pointTo(MiniObject target) {
        refs.add(target);
        target.refCount++;
    }

    @Override
    public String toString() {
        return name;
    }
}
