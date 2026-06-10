package com.study.part06_io.s06_serialization;

import java.io.Serializable;

/**
 * [모델] 직렬화 실습 대상. Serializable을 구현해 '직렬화 가능'함을 표시한다.
 *
 * Serializable은 메서드가 하나도 없는 '마커(marker) 인터페이스'다 — "이 클래스는 직렬화해도 된다"는
 * 표시만 한다. 이 표시가 없는 클래스를 writeObject하면 NotSerializableException이 난다.
 *
 * 필드 종류별 직렬화 동작(-> Example1, Example2에서 확인):
 *   - name, age (일반 필드): 직렬화 대상. 바이트로 저장되고 복원된다.
 *   - password (transient): 직렬화 '제외'. 복원 시 기본값(null)이 된다. 비밀번호/토큰 등 민감정보·
 *     임시 캐시는 transient로 둬야 바이트 스트림에 평문으로 새어 나가지 않는다(보안).
 *   - instanceCount (static): 직렬화 안 됨. static은 '객체'가 아니라 '클래스'에 속하므로 객체 바이트에
 *     포함되지 않는다(복원 시 그 시점의 클래스 값이 보일 뿐).
 *   - serialVersionUID: 직렬화 '버전 식별자'(-> Example3). 운영에서는 반드시 명시 권장.
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 1L; // 버전 식별자(명시 권장)

    String name;
    transient String password;   // 직렬화 제외(보안)
    static int instanceCount = 0; // static -> 직렬화 안 됨
    int age;

    public Person(String name, String password, int age) {
        this.name = name;
        this.password = password;
        this.age = age;
        instanceCount++;
        // 이 출력으로 "역직렬화 시 생성자가 호출되는지" 확인한다(2.6: 역직렬화는 생성자를 안 거친다).
        System.out.println("  >> Person 생성자 실행 (name=" + name + ")");
    }

    @Override
    public String toString() {
        return "Person{name=" + name + ", password=" + password + ", age=" + age + "}";
    }
}
