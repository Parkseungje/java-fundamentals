package com.study.part02_jvm.s06_new_operator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 예시 3 / 3 — clone과 역직렬화: "생성자를 호출하지 않고 객체를 만든다."
 *
 * 이 예시가 답하려는 질문: new도 Reflection도 결국 생성자를 호출했다. 그런데 생성자를 '아예 거치지
 * 않고' 객체를 만드는 방법도 있는가?
 *
 * 왜 이 시나리오인가: clone()과 역직렬화는 둘 다 객체를 만들지만, 생성자를 호출하지 않는다.
 *   - clone(): super.clone()이 기존 객체의 메모리를 그대로 복사해 새 객체를 만든다(생성자 거치지 않음).
 *   - 역직렬화: 저장된 바이트 스트림으로부터 객체를 '복원'하는데, 이때도 생성자를 호출하지 않는다.
 * 그래서 Widget 생성자의 출력문이 이 경우엔 '찍히지 않아야' 한다(원본을 만들 때 1번 외에는).
 *
 * 예상 결과:
 *   - 원본 new Widget(7) 에서만 생성자 출력문이 1번 찍힌다.
 *   - clone()으로 복제, 역직렬화로 복원할 때는 생성자 출력문이 '찍히지 않는다'.
 *   - 그럼에도 복제본/복원본은 원본과 같은 id(7)를 가진 정상 객체다.
 * -> 객체를 만드는 방법은 여러 가지이고, 그중 clone/역직렬화는 생성자를 건너뛴다.
 *    (이 때문에 '생성자에서만 하던 검증/초기화'가 clone·역직렬화 객체엔 적용 안 될 수 있다는
 *    함정이 생긴다. 직렬화는 PART 6에서 심화.)
 */
public class Example3_CloneAndDeserialization {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] clone / 역직렬화: 생성자를 호출하지 않고 객체 생성");
        System.out.println();

        // 원본 생성 — 여기서만 생성자 출력문이 찍혀야 한다.
        System.out.println("원본 new Widget(7):");
        Widget original = new Widget(7);
        System.out.println("원본 = " + original);

        System.out.println();

        // (1) clone — 생성자 호출 없이 메모리 복사
        System.out.println("(1) original.clone() 호출 (생성자 출력문이 안 찍혀야 정상):");
        Widget cloned = original.clone();
        System.out.println("복제본 = " + cloned + ", 원본과 다른 객체? " + (cloned != original));

        System.out.println();

        // (2) 역직렬화 — 바이트 스트림으로 직렬화했다가 다시 객체로 복원(생성자 호출 없음)
        System.out.println("(2) 직렬화 -> 역직렬화 (생성자 출력문이 안 찍혀야 정상):");
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(original);
            bytes = bos.toByteArray();
        }
        Widget restored;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            restored = (Widget) ois.readObject();
        }
        System.out.println("복원본 = " + restored + ", 원본과 다른 객체? " + (restored != original));

        System.out.println();
        System.out.println("=> 원본 생성 때만 생성자가 1번 실행됐고, clone/역직렬화에서는 생성자가 호출되지 않았다.");
        System.out.println("   객체 생성 방법: new/Reflection(생성자 O) vs clone/역직렬화(생성자 X).");
        System.out.println("   => 생성자에서만 하던 검증이 clone·역직렬화 객체엔 적용 안 될 수 있다(함정).");
    }
}
