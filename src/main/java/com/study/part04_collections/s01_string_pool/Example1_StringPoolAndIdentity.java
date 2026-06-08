package com.study.part04_collections.s01_string_pool;

/**
 * 예시 1 / 3 — String Pool과 동일성: "리터럴은 풀에서 재사용되고, new String은 강제로 새 객체다."
 *
 * 이 예시가 답하려는 질문: 같은 문자열 "abc"를 여러 번 써도 같은 객체인가? new String("abc")는?
 * == 와 equals()는 각각 무엇을 비교하나? (자기 점검 질문에 직접 답한다)
 *
 * 왜 이 시나리오인가: 문자열 리터럴("abc")은 'String Constant Pool'이라는 특별한 영역에 저장되고,
 * 같은 값의 리터럴은 새로 만들지 않고 풀의 것을 '재사용'한다. 그래서 a와 b는 같은 객체(== true).
 * 반면 new String("abc")는 풀을 무시하고 Heap에 '무조건 새 객체'를 만든다(== false). 단 값은 같으니
 * equals()는 true. intern()은 "이 문자열을 풀에 등록(이미 있으면 풀의 것 반환)"이라 풀 객체와 ==된다.
 *   - a = "abc", b = "abc"          : 둘 다 풀의 같은 객체
 *   - c = new String("abc")         : Heap의 새 객체
 *   - d = c.intern()                : 풀의 객체(= a와 같은 것)
 *
 * 예상 결과:
 *   - a == b        -> true  (리터럴 재사용, 같은 풀 객체)
 *   - a == c        -> false (c는 Heap의 새 객체)
 *   - a.equals(c)   -> true  (값은 같음)
 *   - a == d        -> true  (intern이 풀의 객체를 돌려줌)
 * -> ==는 '같은 객체(주소)인가', equals()는 '값이 같은가'를 비교한다. 문자열 비교는 거의 항상 equals().
 */
public class Example1_StringPoolAndIdentity {

    public static void main(String[] args) {
        System.out.println("[예시 1] String Pool과 == vs equals()");
        System.out.println();

        String a = "abc";                 // 리터럴 -> String Constant Pool
        String b = "abc";                 // 같은 값 리터럴 -> 풀의 것 재사용
        String c = new String("abc");     // new -> Heap에 강제로 새 객체
        String d = c.intern();            // 풀에 등록(이미 있으니 풀의 것 반환)

        System.out.println("a = \"abc\", b = \"abc\", c = new String(\"abc\"), d = c.intern()");
        System.out.println();
        System.out.println("a == b      -> " + (a == b) + "   (리터럴 재사용 -> 같은 풀 객체)");
        System.out.println("a == c      -> " + (a == c) + "  (c는 Heap의 새 객체라 다름)");
        System.out.println("a.equals(c) -> " + a.equals(c) + "   (값은 같음)");
        System.out.println("a == d      -> " + (a == d) + "   (intern이 풀의 객체를 돌려줌)");

        System.out.println();
        System.out.println("=> ==는 '같은 객체(주소)인가', equals()는 '값이 같은가'를 비교한다.");
        System.out.println("   자기 점검 답: a==b(new 비교)는 false, a.equals(b)는 true. 문자열 비교는 equals()로!");
    }
}
