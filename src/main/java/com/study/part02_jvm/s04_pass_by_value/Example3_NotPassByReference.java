package com.study.part02_jvm.s04_pass_by_value;

/**
 * 예시 3 / 3 — 자기 점검: "'주소값 복사'와 'pass by reference'는 무엇이 다른가?"
 *
 * 이 예시가 답하려는 질문: 자바가 객체의 '주소값'을 넘긴다면, C++의 pass by reference와 같은
 * 것 아닌가? 둘의 결정적 차이는 무엇인가?
 *
 * 왜 이 시나리오인가: 두 객체 참조를 메서드 안에서 'swap(교환)'해본다.
 *   - 만약 자바가 pass by reference라면(매개변수가 호출자 변수의 별칭이라면), 메서드 안에서
 *     x와 y를 바꾸면 호출자의 p와 q도 바뀌어야 한다.
 *   - 하지만 자바는 '주소값을 복사'해서 넘긴다. swap 안의 x, y는 '복사된 주소'를 담은 지역 변수일
 *     뿐이라, 그것들을 아무리 교환해도 호출자의 p, q에는 영향이 없다.
 * 또한 reassignArg(a, b)에서 a = b (메서드 안에서 매개변수끼리 대입)도 호출자에 영향이 없음을 본다
 * (자기 점검 질문: "arg2 = arg1이 호출자에 영향 못 주는 이유").
 *
 * 예상 결과:
 *   - swap(p, q) 후에도 p.value=1, q.value=2 (교환 안 됨 -> pass by reference가 아니라는 증거)
 *   - reassignArg(p, q) 후에도 p.value=1 (메서드 안 a=b는 지역 변수만 바뀜)
 * -> 스택 프레임으로 설명: 메서드를 호출하면 매개변수는 그 메서드의 스택 프레임에 '복사되어' 새로
 *    생긴 지역 변수다(주소값을 복사해 받음). 그 지역 변수에 무엇을 대입하든(=, swap) 프레임 안에서만
 *    유효하고, 호출자의 프레임에 있는 변수는 건드리지 못한다. 그래서 '주소값 복사'는 '필드 변경은
 *    공유하되, 변수 자체의 재지정은 공유하지 않는다'. pass by reference는 변수 자체가 공유되어
 *    재지정까지 반영된다는 점에서 다르다.
 */
public class Example3_NotPassByReference {

    // pass by reference였다면 호출자의 두 변수가 교환됐을 것. 하지만 자바에선 안 된다.
    static void swap(Box x, Box y) {
        Box temp = x;
        x = y;   // x, y는 '복사된 주소'를 담은 지역 변수 -> 교환해도 이 프레임 안에서만 유효
        y = temp;
        System.out.println("  swap 안: x=" + x + ", y=" + y + " (이 프레임 안에서는 교환됨)");
    }

    // 자기 점검: arg2 = arg1 (메서드 안에서 매개변수끼리 대입)도 호출자에 영향 없음
    static void reassignArg(Box a, Box b) {
        a = b; // a라는 '지역 변수'가 b가 가리키던 객체를 가리키게 될 뿐, 호출자의 변수와 무관
        System.out.println("  reassignArg 안: a=" + a + " (지역 변수 a만 바뀜)");
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] 주소값 복사 != pass by reference (swap이 안 되는 것으로 증명)");
        System.out.println();

        Box p = new Box(1);
        Box q = new Box(2);
        System.out.println("처음: p=" + p + ", q=" + q);

        System.out.println();
        System.out.println("swap(p, q) 호출:");
        swap(p, q);
        System.out.println("호출 후: p=" + p + ", q=" + q + "  <- 교환 안 됨! (pass by reference가 아니라는 증거)");

        System.out.println();
        System.out.println("reassignArg(p, q) 호출:");
        reassignArg(p, q);
        System.out.println("호출 후: p=" + p + "  <- 그대로 (메서드 안 a=b는 지역 변수만 바꿈)");

        System.out.println();
        System.out.println("=> 자바는 '주소값을 복사'해 넘긴다. 매개변수는 호출 메서드의 스택 프레임에");
        System.out.println("   복사되어 생긴 지역 변수라, 그것을 재지정/교환해도 호출자 변수는 그대로다.");
        System.out.println("   pass by reference는 변수 자체가 공유되어 swap도 반영된다는 점에서 다르다.");
    }
}
