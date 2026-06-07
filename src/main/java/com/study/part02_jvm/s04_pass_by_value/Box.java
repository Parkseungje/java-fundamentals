package com.study.part02_jvm.s04_pass_by_value;

/**
 * [모델] Pass by value 실험용 객체. 값 하나(value)를 담는 단순한 상자.
 *
 * 이 객체를 메서드에 넘길 때 "무엇이 복사되는가"를 관찰하는 데 쓴다.
 * 자바에서 객체를 메서드에 넘기면 '객체 자체'가 아니라 '객체를 가리키는 주소값(참조)'이 복사된다.
 * 그래서:
 *   - 복사된 주소로 같은 객체의 필드를 바꾸면(b.value = ...) 원본에 반영된다(같은 객체를 가리키므로).
 *   - 복사된 주소 변수에 새 객체를 대입하면(b = new Box(...)) 그 메서드 안의 지역 변수만 다른 객체를
 *     가리킬 뿐, 호출자의 변수는 여전히 원래 객체를 가리킨다.
 * 이 차이를 Example2에서 직접 확인한다.
 */
public class Box {

    int value;

    public Box(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Box(value=" + value + ")";
    }
}
