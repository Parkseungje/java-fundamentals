package com.study.part06_io.s05_buffered_data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 예시 3 / 3 — 보조 스트림 Data: "기본 타입을 타입별로 저장/읽기 (문자열 파싱 불필요)."
 *
 * 이 예시가 답하려는 질문: int/double/String 같은 값을 파일에 저장하고 다시 그 타입으로 읽으려면?
 * 문자열로 바꿔 저장하고 파싱해야 하나?
 *
 * 왜 이 시나리오인가: 숫자나 여러 타입을 파일에 저장할 때, 문자열(CSV 등)로 바꿔 저장하면 읽을 때
 * 다시 쪼개고(parse) 타입 변환(Integer.parseInt 등)을 해야 해서 번거롭고 오류가 나기 쉽다.
 * DataOutputStream/DataInputStream(보조 스트림)은 기본 타입을 '타입별 메서드'로 그대로 저장·복원한다.
 *   - writeInt/writeDouble/writeBoolean/writeUTF(문자열) 로 쓰고,
 *   - readInt/readDouble/readBoolean/readUTF 로 '쓴 순서 그대로' 읽는다.
 * 그래서 파싱이 필요 없다. 단 '쓴 순서와 같은 순서/같은 타입'으로 읽어야 한다(순서가 어긋나면 깨진다).
 *
 * 예상 결과:
 *   - writeInt(42), writeDouble(3.14), writeUTF("홍길동") 으로 저장
 *   - readInt -> 42, readDouble -> 3.14, readUTF -> "홍길동" (타입 그대로 복원, 파싱 없음)
 * -> Data 스트림은 기본 타입을 타입 안전하게 저장/복원한다. CSV 파싱 같은 수작업이 필요 없다.
 *    (단 순서 의존: 쓴 순서대로 읽어야 한다.)
 */
public class Example3_DataStream {

    public static void main(String[] args) throws Exception {
        System.out.println("[예시 3] Data 보조 스트림: 기본 타입을 타입별로 저장/읽기");
        System.out.println();

        Path file = Files.createTempFile("data", ".bin");

        // 쓰기: 타입별 메서드로 그대로 저장
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file.toFile()))) {
            out.writeInt(42);
            out.writeDouble(3.14);
            out.writeBoolean(true);
            out.writeUTF("홍길동"); // 문자열(길이+UTF-8)
        }
        System.out.println("저장: writeInt(42), writeDouble(3.14), writeBoolean(true), writeUTF(\"홍길동\")");

        // 읽기: '쓴 순서 그대로' 타입별 메서드로 복원 (파싱 불필요)
        try (DataInputStream in = new DataInputStream(new FileInputStream(file.toFile()))) {
            int i = in.readInt();
            double d = in.readDouble();
            boolean b = in.readBoolean();
            String s = in.readUTF();
            System.out.println("읽기: readInt=" + i + ", readDouble=" + d + ", readBoolean=" + b + ", readUTF=\"" + s + "\"");
        }

        Files.deleteIfExists(file);

        System.out.println();
        System.out.println("=> Data 스트림은 int/double/String 등을 타입별 메서드로 그대로 저장·복원한다.");
        System.out.println("   CSV처럼 문자열로 바꿔 저장 후 파싱할 필요가 없다. 단 '쓴 순서대로' 읽어야 한다.");
    }
}
