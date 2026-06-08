package com.study.part04_collections.s08_hash;

import java.util.Objects;

/**
 * [모델] '좋은' hashCode를 가진 key. id 값에 따라 hashCode가 고르게 흩어진다.
 *
 * 좋은 해시 함수의 조건: 빠르고, 결정적(같은 입력은 같은 값)이고, 값이 골고루 분포한다.
 * id마다 다른 hashCode가 나오므로 HashMap에서 서로 다른 버킷에 분산 저장된다 -> 충돌이 적어
 * get/put이 평균 O(1)에 가깝다. (-> Example2에서 KeyBad와 성능 대비)
 */
public class KeyGood {
    final int id;

    public KeyGood(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // id에 따라 고르게 분산된 해시값
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyGood k)) return false;
        return id == k.id;
    }
}
