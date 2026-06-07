package com.study.part02_jvm.s07_bytecode_constant_pool;

/**
 * 예시 1 / 3 — 객체 생성의 바이트코드: "new Point(3,4) 한 줄이 new/dup/invokespecial 3개로 쪼개진다."
 *
 * 이 예시가 답하려는 질문: 소스의 new Point(3,4) 한 줄은 JVM 바이트코드에서 단일 명령일까,
 * 아니면 여러 명령으로 나뉠까? 나뉜다면 왜 그렇게 나뉘나?
 *
 * 왜 이 시나리오인가: createPoint()는 new Point(3,4)로 객체를 만들어 반환한다. 이 한 줄을
 * 컴파일하면 javac는 보통 세 명령으로 쪼갠다.
 *   - new        : Heap에 Point용 메모리를 확보하고, '아직 초기화 안 된' 객체 참조를 스택에 올림
 *   - dup        : 그 객체 참조를 복제(생성자 호출이 참조를 소비하는데, 호출 후에도 그 객체를
 *                  돌려주거나 변수에 담아야 하므로 미리 복제해 둔다)
 *   - invokespecial : 복제된 참조 하나를 소비하며 생성자 <init>를 호출(초기화)
 * 즉 "메모리 확보(new)"와 "생성자 호출(invokespecial)"이 별개 명령이라는 점이 핵심이다
 * (2.6에서 본 'new의 단계'가 바이트코드로도 분리되어 나타난다).
 *
 * 예상 결과(실행): 거리 = 5.0 (3,4,5 직각삼각형)
 * 예상 결과(javap -c createPoint): new -> dup -> invokespecial #... 순서가 보인다.
 * -> docs의 javap 명령으로 직접 확인할 것.
 */
public class Example1_NewDupInvoke {

    static Point createPoint() {
        Point p = new Point(3, 4); // new + dup + invokespecial 로 컴파일됨
        return p;
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] 객체 생성 바이트코드: new / dup / invokespecial");
        System.out.println();

        Point p = createPoint();
        System.out.println("createPoint() 결과 distanceFromOrigin = " + p.distanceFromOrigin());

        System.out.println();
        System.out.println("=> new Point(3,4) 한 줄은 바이트코드에서 new/dup/invokespecial 세 명령으로 나뉜다.");
        System.out.println("   '메모리 확보(new)'와 '생성자 호출(invokespecial)'이 별개 명령. docs javap로 확인.");
    }
}
