package com.study.part04_collections.s05_set;

import java.util.Objects;

/**
 * [모델] equals/hashCode를 오버라이드'한' 좌표.
 *
 * "x와 y가 같으면 같은 점"이라고 값 기준으로 동등성을 재정의했다.
 * HashSet은 중복을 검사할 때 (1) hashCode로 버킷을 찾고 (2) 그 버킷 안에서 equals로 같은지 본다.
 * 그래서 이 클래스는 값이 같은(1,2) 두 인스턴스를 '같은 것'으로 판정 -> HashSet에 하나만 남는다.
 *
 * 규칙: equals를 재정의하면 hashCode도 반드시 함께 재정의해야 한다(같은 객체는 같은 hashCode여야
 * 버킷을 제대로 찾기 때문). 이 규약은 4.8에서 더 깊이 다룬다.
 */
public class PointWithEquals {
    final int x;
    final int y;

    public PointWithEquals(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointWithEquals p)) return false;
        return x == p.x && y == p.y; // 값(x,y)이 같으면 같은 점
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y); // 같은 값이면 같은 hashCode
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
