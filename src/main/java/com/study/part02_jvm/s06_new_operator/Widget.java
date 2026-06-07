package com.study.part02_jvm.s06_new_operator;

import java.io.Serializable;

/**
 * [모델] 객체 생성 방법별로 "생성자가 호출되는가"를 관찰하기 위한 클래스.
 *
 * 생성자에 출력문을 넣어 두었다. 그래서 어떤 방법으로 객체를 만들었을 때 이 출력이 찍히면
 * "생성자가 호출됐다"는 뜻이고, 안 찍히면 "생성자 없이 객체가 만들어졌다"는 뜻이다.
 *
 * Serializable: 역직렬화 실험용(직렬화 가능 표시 마커 인터페이스, PART 6에서 심화).
 * Cloneable: clone() 실험용(이 인터페이스가 있어야 Object.clone()이 동작).
 * serialVersionUID: 직렬화 버전 식별자(운영에서 명시 권장, PART 6).
 */
public class Widget implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    int id;

    public Widget(int id) {
        this.id = id;
        // 이 줄이 찍히면 '생성자가 호출됐다'는 증거다.
        System.out.println("    >> Widget 생성자 실행 (id=" + id + ")");
    }

    // clone()을 쓰려면 오버라이드 필요. super.clone()은 생성자를 거치지 않고 메모리를 복사한다.
    @Override
    public Widget clone() throws CloneNotSupportedException {
        return (Widget) super.clone();
    }

    @Override
    public String toString() {
        return "Widget(id=" + id + ")";
    }
}
