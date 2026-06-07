package com.study.part02_jvm.s07_bytecode_constant_pool;

/**
 * 예시 2 / 3 — 상수 풀과 심볼 참조: "바이트코드의 #숫자는 상수 풀 인덱스이고, 그것은 이름(심볼) 참조다."
 *
 * 이 예시가 답하려는 질문: 바이트코드에 자주 나오는 #7, #13 같은 숫자는 무엇인가? 컴파일 시점엔
 * 클래스/메서드/필드의 '실제 메모리 주소'를 알 수 없는데, JVM은 어떻게 호출 대상을 가리키는가?
 *
 * 왜 이 시나리오인가: 이 클래스는 문자열 상수("안녕하세요")와 메서드 호출(System.out.println,
 * String 연결)을 포함한다. 컴파일하면 javac는 이런 상수·참조들을 클래스 파일 내부의 '상수 풀
 * (Constant Pool)'에 저장하고, 바이트코드에서는 그 항목을 #인덱스로 가리킨다.
 *
 * 핵심 개념 — 심볼 참조(Symbolic Reference):
 *   컴파일 시점에는 PrintStream.println의 실제 메모리 주소를 알 수 없다(아직 로딩도 안 됨).
 *   그래서 "java/io/PrintStream.println:(String)V" 같은 '이름(심볼)'으로 상수 풀에 적어두고,
 *   런타임에 실제 위치로 해석(resolve)한다. 즉 #숫자 -> 상수 풀의 심볼 -> 런타임에 실제 주소.
 *   이 덕분에 바이트코드는 특정 머신의 주소에 묶이지 않아 '플랫폼 독립적'일 수 있다.
 *
 * 예상 결과(실행): "안녕하세요 JVM" 출력
 * 예상 결과(javap -v): 상수 풀 목록(#1, #2, ...)에 String "안녕하세요", Class·Methodref·Fieldref
 *   항목들이 보이고, 바이트코드의 ldc/invokevirtual 등이 그 #인덱스를 가리킨다.
 * -> docs의 javap -v 명령으로 상수 풀을 직접 확인할 것.
 */
public class Example2_ConstantPool {

    // 문자열 리터럴은 상수 풀에 String 상수로 저장된다.
    static final String GREETING = "안녕하세요";

    public static void main(String[] args) {
        System.out.println("[예시 2] 상수 풀과 심볼 참조 (#숫자)");
        System.out.println();

        String msg = GREETING + " JVM"; // 문자열 연결도 상수 풀의 참조들을 사용
        System.out.println(msg);

        System.out.println();
        System.out.println("=> 바이트코드의 #숫자는 상수 풀 인덱스다. 상수 풀에는 문자열·클래스·메서드·필드");
        System.out.println("   참조가 '이름(심볼)'으로 저장되고, 런타임에 실제 주소로 해석(resolve)된다.");
        System.out.println("   주소가 아니라 이름으로 참조하기에 바이트코드가 플랫폼 독립적이다. docs javap -v로 확인.");
    }
}
