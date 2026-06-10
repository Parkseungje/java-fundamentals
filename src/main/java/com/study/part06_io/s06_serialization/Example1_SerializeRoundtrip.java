package com.study.part06_io.s06_serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 예시 1 / 3 — 직렬화 라운드트립: "객체 → 바이트 → 객체. 단 복원 시 생성자는 안 거친다."
 *
 * 이 예시가 답하려는 질문: 객체를 어떻게 바이트로 바꿔 저장/전송하고, 다시 객체로 복원하나?
 * 복원된 객체는 생성자를 거치나?
 *
 * 왜 이 시나리오인가: '직렬화(serialization)'는 객체를 바이트 스트림으로 변환하는 것이다(파일 저장·
 * 네트워크 전송용). ObjectOutputStream.writeObject로 직렬화하고, ObjectInputStream.readObject로
 * 역직렬화(복원)한다. 여기선 파일 대신 메모리(ByteArrayOutputStream)로 라운드트립해 결과를 비교한다.
 * 핵심 관찰: 원본을 만들 때만 생성자가 실행되고, 역직렬화로 복원할 때는 '생성자가 호출되지 않는다'
 * (2.6에서 본 것 — 역직렬화는 생성자를 건너뛰고 바이트로부터 객체를 복원한다).
 *
 * 예상 결과:
 *   - 원본 생성 시 "Person 생성자 실행"이 1번 출력.
 *   - 역직렬화로 복원할 때는 생성자 출력이 '없다'(생성자 안 거침).
 *   - 복원본은 원본과 다른 객체지만(restored != original) name/age 값은 같다.
 * -> 직렬화는 객체의 상태(필드 값)를 바이트로 저장했다가 복원하는 것. 복원은 생성자를 거치지 않으므로,
 *    생성자에만 있던 검증·초기화는 적용되지 않는다(주의점 — 2.6과 연결).
 */
public class Example1_SerializeRoundtrip {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 1] 직렬화 라운드트립 (객체 -> 바이트 -> 객체)");
        System.out.println();

        System.out.println("원본 생성:");
        Person original = new Person("홍길동", "secret123", 30);
        System.out.println("원본 = " + original);

        // 직렬화: 객체 -> 바이트
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            bytes = bos.toByteArray();
        }
        System.out.println();
        System.out.println("직렬화 완료: 객체가 " + bytes.length + " 바이트로 변환됨");

        // 역직렬화: 바이트 -> 객체 (생성자 출력이 없어야 정상)
        System.out.println();
        System.out.println("역직렬화 (생성자 출력이 안 나와야 정상):");
        Person restored;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            restored = (Person) ois.readObject();
        }
        System.out.println("복원본 = " + restored);
        System.out.println("원본과 다른 객체인가? " + (restored != original) + " / name·age 값은 같은가? "
                + (restored.name.equals(original.name) && restored.age == original.age));

        System.out.println();
        System.out.println("=> 직렬화는 객체 상태를 바이트로 저장/복원한다. 복원 시 생성자는 호출되지 않는다(2.6).");
        System.out.println("   그래서 생성자에만 있던 검증/초기화는 복원 객체에 적용되지 않을 수 있다(주의).");
    }
}
