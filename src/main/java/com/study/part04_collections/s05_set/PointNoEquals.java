package com.study.part04_collections.s05_set;

/**
 * [모델] equals/hashCode를 오버라이드하지 '않은' 좌표.
 *
 * 이 클래스는 equals/hashCode를 재정의하지 않았으므로 Object의 기본 동작을 쓴다.
 * 기본 equals는 '주소 비교'(==와 동일), 기본 hashCode는 '객체마다 다른 값'이다.
 * 그래서 값이 같은(1,2) 두 인스턴스라도 HashSet은 '서로 다른 객체'로 보고 둘 다 담는다.
 * (-> Example2에서 PointWithEquals와 대비)
 */
public class PointNoEquals {
    final int x;
    final int y;

    public PointNoEquals(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
