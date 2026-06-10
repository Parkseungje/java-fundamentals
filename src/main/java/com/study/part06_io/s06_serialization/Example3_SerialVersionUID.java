package com.study.part06_io.s06_serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

/**
 * 예시 3 / 3 — serialVersionUID: "버전 식별자. 명시 안 하면 클래스 변경 시 깨진다."
 *
 * 이 예시가 답하려는 질문: serialVersionUID는 무엇이고, 왜 운영에서 반드시 명시해야 하나?
 *
 * 왜 이 시나리오인가: 직렬화 바이트에는 '클래스 이름 + serialVersionUID(버전)'가 함께 기록된다.
 * 역직렬화할 때 JVM은 "바이트에 적힌 UID == 지금 클래스의 UID"인지 확인하고, 다르면
 * InvalidClassException을 던진다("호환 안 되는 버전이야!"). 그런데 UID를 명시하지 않으면 컴파일러가
 * 클래스 구조로부터 UID를 '자동 계산'하는데, 필드를 추가하는 등 클래스를 조금만 바꿔도 이 값이 바뀐다.
 * 그래서 예전에 저장해둔 바이트를 새 버전 클래스로 읽으면 UID가 안 맞아 깨진다.
 *   - ObjectStreamClass.lookup(클래스).getSerialVersionUID()로 각 클래스의 UID를 직접 출력해 비교한다.
 *   - 명시한 WithUid는 항상 100, 명시 안 한 WithoutUid는 구조에서 계산된 큰 숫자(클래스 변경 시 바뀜).
 *   - WithUid로 직렬화/역직렬화 라운드트립이 정상 동작함도 확인.
 *
 * 예상 결과:
 *   - WithUid의 UID = 100 (명시값, 고정)
 *   - WithoutUid의 UID = 자동 계산된 큰 숫자(예: 어떤 long 값) — 클래스 구조가 바뀌면 이 값이 변한다
 *   - WithUid 라운드트립 성공
 * -> serialVersionUID는 직렬화 호환성의 '버전 도장'이다. 명시하지 않으면 클래스를 무심코 바꿨을 때
 *    자동 UID가 변해 기존 데이터 역직렬화가 InvalidClassException으로 깨진다. 그래서 운영에서 반드시 명시.
 *
 * [실제 InvalidClassException 재현법] 한 번 실행으론 못 보여준다(클래스 재컴파일이 필요):
 *   1) WithoutUid로 객체를 직렬화해 파일에 저장한다.
 *   2) WithoutUid에 필드를 하나 추가하고 '다시 컴파일'한다(UID 자동 계산값이 바뀜).
 *   3) 1)의 파일을 역직렬화하면 -> InvalidClassException: local class incompatible (UID 불일치).
 *   WithUid처럼 UID를 명시했다면 같은 변경에도 UID가 100으로 유지돼 (호환 변경 시) 읽힌다.
 */
public class Example3_SerialVersionUID {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] serialVersionUID: 직렬화 버전 식별자");
        System.out.println();

        // 각 클래스의 serialVersionUID 확인
        long withUid = ObjectStreamClass.lookup(WithUid.class).getSerialVersionUID();
        long withoutUid = ObjectStreamClass.lookup(WithoutUid.class).getSerialVersionUID();

        System.out.println("WithUid    (명시 100L)    UID = " + withUid + "  <- 명시값. 클래스 바뀌어도 고정");
        System.out.println("WithoutUid (명시 안 함)   UID = " + withoutUid
                + "  <- 구조에서 자동 계산된 값. 필드 추가 등 변경 시 이 값이 '바뀐다'");

        System.out.println();

        // WithUid 라운드트립 (정상 동작 확인)
        WithUid original = new WithUid();
        original.a = 7;
        original.b = "hello";

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            bytes = bos.toByteArray();
        }
        WithUid restored;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            restored = (WithUid) ois.readObject();
        }
        System.out.println("WithUid 라운드트립: a=" + restored.a + ", b=" + restored.b + " (정상 복원, UID 일치)");

        System.out.println();
        System.out.println("=> serialVersionUID는 직렬화 호환성의 '버전 도장'이다. 역직렬화 시 바이트의 UID와");
        System.out.println("   클래스의 UID가 다르면 InvalidClassException. 명시 안 하면 자동 계산값이 클래스 변경마다");
        System.out.println("   달라져 기존 데이터를 못 읽는 사고가 난다 -> 운영에선 반드시 명시(재현법은 주석 참고).");
    }
}
