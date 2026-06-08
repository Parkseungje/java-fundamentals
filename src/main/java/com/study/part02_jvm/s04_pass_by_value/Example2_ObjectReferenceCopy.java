package com.study.part02_jvm.s04_pass_by_value;

/**
 * 예시 2 / 3 — 객체 타입: "주소값(참조)이 복사된다. 그래서 미묘하게 두 갈래로 갈린다."
 *
 * 이 예시가 답하려는 질문: 객체를 메서드에 넘기면 무엇이 복사되나? (a) 그 객체의 필드를 바꾸는
 * 것과 (b) 매개변수에 새 객체를 대입하는 것은 호출자에게 각각 어떻게 보이나?
 *
 * 왜 이 시나리오인가: 자바는 객체도 'pass by value'이지만, 복사되는 값이 '객체 자체'가 아니라
 * '객체를 가리키는 주소값(참조)'이다. 두 가지 경우를 나눠 본다.
 *   (a) mutateField(b): 복사된 주소가 가리키는 '같은 객체'의 필드를 바꾼다 -> 호출자도 같은 객체를
 *       가리키므로 변경이 보인다.
 *   (b) reassign(b): 매개변수 b에 new Box(30)을 대입한다 -> b는 메서드 스택 프레임의 지역 변수라
 *       '복사된 주소'일 뿐이다. 거기에 새 주소를 넣어도 호출자의 변수는 여전히 원래 객체를 가리킨다.
 *
 * 예상 결과:
 *   - mutateField 후: box.value = 20  (필드 변경은 반영됨 — 같은 객체)
 *   - reassign 후:    box.value = 20  (그대로! 재할당은 반영 안 됨 — 지역 변수만 새 객체 가리킴)
 * -> "객체를 넘기면 다 바뀐다"가 아니다. '같은 객체의 필드 변경'은 반영되지만, '매개변수 자체에 다른
 *    객체 대입'은 반영되지 않는다. 이 차이가 자바가 pass by value임을 보여주는 결정적 증거다.
 *    (만약 pass by reference였다면 reassign도 호출자에 반영됐을 것이다 -> Example3에서 확인)
 */
public class Example2_ObjectReferenceCopy {

    // 객체의 '주소값'을 눈으로 보기 위한 대용물. (실제 메모리 주소는 못 보지만, identityHashCode는
    // 객체마다 고유해서 "같은 객체인가/다른 객체인가"를 구분하는 데 쓸 수 있다. 시각화의 0x100/0x200 역할)
    static String addr(Box b) {
        return "0x" + Integer.toHexString(System.identityHashCode(b));
    }

    // (a) 복사된 주소가 가리키는 같은 객체의 필드를 변경 -> 호출자에 반영됨
    static void mutateField(Box b) {
        System.out.println("  mutateField 안: 받은 주소 b=" + addr(b) + " (호출자 box와 같은 주소 = 같은 객체)");
        b.value = 20;
        System.out.println("  mutateField 안: " + b + " (같은 객체의 필드를 바꿈)");
    }

    // (b) 매개변수(지역 변수)에 새 객체를 대입 -> 이 프레임의 지역 변수만 바뀜, 호출자 무관
    static void reassign(Box b) {
        System.out.println("  reassign 안: 받은 주소 b=" + addr(b) + " (대입 전, 호출자 box와 같은 주소)");
        b = new Box(30); // b는 '복사된 주소'. 새 주소를 넣어도 호출자의 변수와는 끊긴다.
        System.out.println("  reassign 안: 대입 후 b=" + addr(b) + " " + b + " (지역 변수 b만 새 객체를 가리킴)");
    }

    public static void main(String[] args) {
        System.out.println("[예시 2] 객체 타입: 주소값이 복사된다 (필드 변경 vs 재할당)");
        System.out.println();

        Box box = new Box(10);
        System.out.println("처음: " + box + " 주소 box=" + addr(box));

        System.out.println();
        System.out.println("(a) mutateField(box) 호출:");
        mutateField(box);
        System.out.println("호출 후 box = " + box + " 주소 box=" + addr(box) + "  <- 20으로 바뀜 (같은 객체의 필드라 반영됨)");

        System.out.println();
        System.out.println("(b) reassign(box) 호출:");
        reassign(box);
        System.out.println("호출 후 box = " + box + " 주소 box=" + addr(box) + "  <- 여전히 20, 주소도 그대로 (재할당은 호출자에 반영 안 됨)");

        System.out.println();
        System.out.println("=> 복사되는 건 '주소값'이다. (a)는 box와 b의 주소가 같아 필드 변경이 보이고,");
        System.out.println("   (b)는 b만 새 주소로 바뀔 뿐 호출자 box의 주소는 그대로 = 자바는 pass by value.");
    }
}
