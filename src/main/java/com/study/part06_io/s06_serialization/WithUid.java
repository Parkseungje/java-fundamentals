package com.study.part06_io.s06_serialization;

import java.io.Serializable;

/**
 * [모델] serialVersionUID를 '명시한' 클래스.
 *
 * UID를 100L로 고정했다. 클래스에 필드를 추가하거나 바꿔도 이 값은 100으로 유지된다. 그래서 예전
 * 바이트를 새 클래스로 역직렬화해도 UID가 맞아 (호환 가능한 변경이라면) 복원된다. 운영 시스템에서
 * 직렬화 대상 클래스에 serialVersionUID를 반드시 명시하는 이유다(자동 계산값에 의존하면 무심코
 * 클래스를 바꿨을 때 기존 데이터를 못 읽는 사고가 난다).
 */
public class WithUid implements Serializable {
    private static final long serialVersionUID = 100L; // 명시 -> 고정

    int a;
    String b;
}
