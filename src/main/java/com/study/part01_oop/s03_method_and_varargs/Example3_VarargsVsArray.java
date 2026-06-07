package com.study.part01_oop.s03_method_and_varargs;

/**
 * 예시 3 / 3 — 자기 점검: "void log(String... args)와 void log(String[] args)의 차이는?"
 *
 * 이 예시가 답하려는 질문: 가변인자(String...)와 배열 매개변수(String[])는 메서드 내부에서는
 * 둘 다 String[]로 똑같이 다뤄진다. 그렇다면 차이는 어디에 있는가?
 *
 * 왜 이 시나리오인가: 같은 일을 하는 두 메서드 logVarargs(String...)와 logArray(String[])를
 * 만들어 두고, 호출하는 쪽에서 각각 어떻게 불러야 하는지 비교한다. 차이는 '호출 편의성'에서 난다.
 *   - 가변인자: 인자를 콤마로 나열(logVarargs("a","b")) 가능 + 배열 통째로 넘기기도 가능 + 0개도 가능
 *   - 배열 매개변수: 반드시 배열 객체를 만들어 넘겨야 함(logArray(new String[]{"a","b"}))
 *
 * 예상 결과: 두 방식 모두 같은 출력을 내지만, 호출 코드의 모양이 다르다.
 *   가변인자는 "a","b" 처럼 편하게 나열 가능 / 배열 매개변수는 new String[]{...}를 강제.
 *
 * 추가로 중요한 사실(주석으로만): logVarargs(String...)와 logArray(String[])를 '같은 이름'으로
 * 오버로딩할 수는 없다. 컴파일된 시그니처가 사실상 동일(String[])해서 충돌하기 때문이다.
 * 즉 둘은 호출 편의성만 다를 뿐, JVM 입장에서는 같은 '배열 받는 메서드'다.
 */
public class Example3_VarargsVsArray {

    // 가변인자 버전: 호출 측이 인자를 자유롭게 나열할 수 있다.
    static void logVarargs(String... args) {
        System.out.println("  logVarargs 받은 개수 = " + args.length + " -> " + String.join(", ", args));
    }

    // 배열 매개변수 버전: 호출 측이 반드시 배열 객체를 만들어 넘겨야 한다.
    static void logArray(String[] args) {
        System.out.println("  logArray 받은 개수 = " + args.length + " -> " + String.join(", ", args));
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] 가변인자(String...) vs 배열 매개변수(String[]) — 차이는 '호출 편의성'");
        System.out.println();

        // --- 가변인자: 세 가지 방식 모두 가능 ---
        System.out.println("(가변인자) 콤마로 나열 호출:");
        logVarargs("a", "b", "c");

        System.out.println("(가변인자) 인자 0개 호출도 가능:");
        logVarargs();

        System.out.println("(가변인자) 배열을 통째로 넘기는 것도 가능:");
        logVarargs(new String[]{"x", "y"});

        System.out.println();

        // --- 배열 매개변수: 반드시 배열 객체를 만들어야 함 ---
        System.out.println("(배열 매개변수) 반드시 new String[]{...} 형태로만 호출 가능:");
        logArray(new String[]{"a", "b", "c"});
        // logArray("a", "b", "c");  // <- 컴파일 에러: 배열 매개변수는 인자 나열을 못 받는다
        // logArray();               // <- 컴파일 에러: 인자 0개도 불가(배열 객체가 필요)

        System.out.println();
        System.out.println("=> 자기 점검 답: 둘 다 메서드 내부에서는 String[]로 같지만,");
        System.out.println("   - 가변인자는 '인자 나열/0개/배열' 모두 허용해 호출이 편하다.");
        System.out.println("   - 배열 매개변수는 반드시 배열 객체를 만들어 넘겨야 한다.");
        System.out.println("   (그리고 둘은 시그니처가 사실상 같아서 같은 이름으로 오버로딩 불가)");
    }
}
