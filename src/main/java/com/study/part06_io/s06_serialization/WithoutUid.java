package com.study.part06_io.s06_serialization;

import java.io.Serializable;

/**
 * [모델] serialVersionUID를 '명시하지 않은' 클래스.
 *
 * UID를 안 적으면 컴파일러가 클래스 구조(필드·메서드 시그니처 등)를 기반으로 UID를 '자동 계산'한다.
 * 문제: 필드를 하나 추가하는 등 클래스를 조금만 바꿔도 이 계산값이 '달라진다'. 그러면 예전 구조로
 * 직렬화해 둔 바이트를 새 클래스로 역직렬화할 때 UID가 안 맞아 InvalidClassException이 난다.
 * (-> Example3에서 자동 계산 UID 값을 직접 출력해 확인)
 */
public class WithoutUid implements Serializable {
    int a;
    String b;
}
