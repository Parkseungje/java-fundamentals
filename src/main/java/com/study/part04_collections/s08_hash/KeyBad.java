package com.study.part04_collections.s08_hash;

/**
 * [모델] '나쁜' hashCode를 가진 key. id가 달라도 hashCode가 항상 같다(전부 충돌).
 *
 * hashCode()가 상수(1)를 반환하므로, 서로 다른 모든 key가 HashMap의 '같은 버킷'으로 몰린다
 * (해시 충돌 최악의 경우). 그러면 한 버킷에 모든 원소가 연결 리스트로 쌓여(체이닝), get/put이
 * 그 리스트를 처음부터 훑어야 해서 O(n)으로 퇴화한다.
 *
 * 주의: 이 클래스는 일부러 Comparable을 구현하지 않았다. Java 8의 HashMap은 한 버킷이 8개를 넘고
 * 전체 용량이 64 이상이면 그 버킷을 Red-Black Tree로 바꿔(treeify) O(log n)으로 개선하는데,
 * 트리로 정렬하려면 key가 Comparable이어야 한다. Comparable이 없으면 트리 변환의 이점이 제한되어
 * 연결 리스트 탐색에 더 가깝게 동작한다 -> 충돌의 O(n) 퇴화를 더 뚜렷하게 관찰할 수 있다.
 * (equals는 id 기준이라 값이 다르면 다른 key로 정확히 구분된다.)
 */
public class KeyBad {
    final int id;

    public KeyBad(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return 1; // 모든 key가 같은 해시값 -> 전부 같은 버킷으로 충돌
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyBad k)) return false;
        return id == k.id;
    }
}
