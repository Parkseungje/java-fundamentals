package com.study.part04_collections.s02_stringbuilder;

/**
 * 예시 2 / 3 — StringBuilder의 가변성: "append는 새 객체를 만들지 않고 같은 객체를 수정한다."
 *
 * 이 예시가 답하려는 질문: StringBuilder의 append()는 String의 concat()처럼 새 객체를 반환하는가,
 * 아니면 원본(같은 객체)을 직접 바꾸는가?
 *
 * 왜 이 시나리오인가: String은 불변이라 모든 수정이 새 객체였다(4.1, 예시1). StringBuilder는 정반대로
 * '가변(mutable)'이다. append()는 내부 char 배열에 글자를 덧붙이고 '자기 자신(this)'을 반환한다.
 * 그래서 append를 호출해도 새 객체가 안 생기고, 반환값은 호출한 그 StringBuilder와 == 로 같다.
 * 이 점을 == 비교로 직접 확인한다. (append가 this를 반환하기 때문에 .append().append()... 체이닝이 가능)
 *
 * 예상 결과:
 *   - sb.append("b")의 반환값 == sb  -> true (같은 객체)
 *   - 여러 번 append 후 sb 자체의 내용이 누적되어 바뀌어 있다(새 변수 없이도).
 *   - append 체이닝(sb.append(..).append(..))이 동작한다.
 * -> StringBuilder는 가변이라 '제자리 수정'이 가능하고, 그래서 루프 조립이 효율적이다(예시1).
 *    String(불변, 수정=새 객체)과 정반대 성질임을 == 로 확인.
 */
public class Example2_MutableBuilder {

    public static void main(String[] args) {
        System.out.println("[예시 2] StringBuilder는 가변: append가 같은 객체를 수정");
        System.out.println();

        StringBuilder sb = new StringBuilder("a");
        System.out.println("초기 sb = \"" + sb + "\"");

        // append의 반환값이 sb 자신과 같은 객체인지 확인
        StringBuilder returned = sb.append("b");
        System.out.println("sb.append(\"b\") 후 sb = \"" + sb + "\"  <- sb 자체가 바뀜(새 변수 없이)");
        System.out.println("반환값 == sb ? " + (returned == sb) + "  <- 같은 객체(append는 this를 반환)");

        System.out.println();

        // append는 this를 반환하므로 체이닝 가능
        sb.append("c").append("d").append("e");
        System.out.println("체이닝 append 후 sb = \"" + sb + "\"  (sb.append().append()... 가능)");

        System.out.println();
        System.out.println("=> StringBuilder는 가변이라 append가 새 객체 없이 '같은 객체'를 제자리 수정한다.");
        System.out.println("   String(불변, 수정=새 객체)과 정반대. 그래서 반복 조립에 효율적(예시1).");
    }
}
