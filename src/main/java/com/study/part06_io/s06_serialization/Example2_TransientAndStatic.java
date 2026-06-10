package com.study.part06_io.s06_serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 예시 2 / 3 — transient와 static: "직렬화에서 제외되는 필드들."
 *
 * 이 예시가 답하려는 질문: 모든 필드가 직렬화되나? 비밀번호 같은 민감정보는 어떻게 빼나?
 *
 * 왜 이 시나리오인가: 두 종류의 필드는 직렬화에서 제외된다.
 *   - transient 필드(password): 의도적으로 직렬화하지 않는다. 그래서 복원하면 기본값(null)이 된다.
 *     비밀번호/토큰/세션 같은 민감정보나 임시 캐시는 transient로 둬야 한다 — 안 그러면 객체 바이트
 *     스트림에 평문으로 그대로 들어가 파일/네트워크로 새어 나갈 위험이 있다(보안).
 *   - static 필드(instanceCount): 객체가 아니라 '클래스'에 속하므로 객체 바이트에 포함되지 않는다.
 *     이를 증명하려고, 직렬화한 뒤 static 값을 바꾸고 역직렬화한다. 복원본이 보는 static 값이
 *     '직렬화 당시 값'이 아니라 '현재 클래스 값'이면, static이 객체와 함께 저장되지 않았다는 뜻이다.
 *
 * 예상 결과:
 *   - 복원본의 password = null (transient라 직렬화 안 됨)
 *   - 복원본의 name/age = 원본과 동일(일반 필드는 직렬화됨)
 *   - 직렬화 후 instanceCount를 999로 바꾸고 역직렬화 -> 복원본이 보는 static 값 = 999(현재 값)
 *     -> static은 객체 바이트에 저장되지 않았다는 증거
 * -> 직렬화 대상은 '인스턴스의 일반 필드'뿐이다. transient(의도적 제외)와 static(클래스 소속)은 빠진다.
 *    민감정보는 반드시 transient로 보호한다.
 */
public class Example2_TransientAndStatic {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 2] transient(보안 제외) + static(직렬화 안 됨)");
        System.out.println();

        Person original = new Person("홍길동", "secret123", 30);
        System.out.println("원본 = " + original + ", static instanceCount = " + Person.instanceCount);

        // 직렬화
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            bytes = bos.toByteArray();
        }

        // static을 직렬화 '이후'에 바꿔본다 -> 객체 바이트엔 영향 없어야 함
        Person.instanceCount = 999;

        // 역직렬화
        Person restored;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            restored = (Person) ois.readObject();
        }

        System.out.println();
        System.out.println("복원본 = " + restored);
        System.out.println("  password = " + restored.password + "  <- null! (transient라 직렬화 제외, 보안)");
        System.out.println("  name/age = " + restored.name + "/" + restored.age + "  <- 일반 필드는 정상 복원");
        System.out.println("  복원본이 보는 static instanceCount = " + Person.instanceCount
                + "  <- 직렬화 당시(1)가 아니라 현재 값(999) = static은 객체에 저장 안 됨");

        System.out.println();
        System.out.println("=> 직렬화 대상은 인스턴스의 일반 필드뿐. transient(의도적 제외)와 static(클래스 소속)은 빠진다.");
        System.out.println("   비밀번호/토큰 등 민감정보는 반드시 transient로 둬야 바이트 스트림 평문 노출을 막는다.");
    }
}
