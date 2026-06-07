package com.study.part01_oop.s04_inheritance_constructor;

/**
 * [모델] Animal을 상속한 자식(서브클래스).
 *
 * extends Animal로 부모의 name 필드와 eat() 메서드를 그대로 물려받는다.
 * 여기에 자식만의 행동(bark)을 추가한다 -> "상속 = 부모 것을 물려받고 + 내 것을 더한다".
 */
public class Dog extends Animal {

    public Dog(String name) {
        // super(name)을 명시적으로 호출한다.
        // (만약 생략하면 컴파일러가 super()를 자동 삽입하려 하지만, Animal에는 매개변수 없는
        //  생성자가 없으므로 컴파일 에러가 난다 -> 이 함정은 Example3에서 별도로 다룬다.)
        super(name);
        System.out.println("  [Dog 생성자] " + name + " 준비 완료");
    }

    // 자식만의 새 행동. 부모(Animal)에는 없다.
    public void bark() {
        System.out.println(name + ": 멍멍!");
    }
}
