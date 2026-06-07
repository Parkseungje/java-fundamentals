package com.study.part01_oop.s04_inheritance_constructor;

/**
 * 예시 1 / 3 — "extends로 무엇을 물려받는가?"
 *
 * 이 예시가 답하려는 질문: 자식 클래스(Dog)가 부모(Animal)를 extends하면, 부모에 정의된
 * 필드(name)와 메서드(eat)를 자식 객체에서도 그대로 쓸 수 있는가? 자식만의 행동(bark)은
 * 어떻게 더해지는가?
 *
 * 왜 이 시나리오인가: Dog 객체 하나를 만들어 (1) 부모에게서 물려받은 eat()와 (2) 자식이
 * 새로 추가한 bark()를 둘 다 호출한다. 만약 상속이 "부모 것을 물려받는다"는 설명대로라면,
 * Dog 클래스 안에 eat()를 한 줄도 안 적었는데도 dog.eat()이 정상 동작해야 한다.
 *
 * 예상 결과:
 *   - dog.eat()   -> "초코가 먹는다"  (Animal에서 물려받은 메서드, Dog엔 정의 안 했는데도 호출됨)
 *   - dog.bark()  -> "초코: 멍멍!"    (Dog가 새로 추가한 메서드)
 * -> 상속 = 부모의 필드/메서드를 물려받고(eat) + 자식 고유의 것을 더한다(bark).
 *    name 필드도 Animal 것을 물려받아 양쪽 메서드가 공유한다.
 */
public class Example1_InheritFieldsAndMethods {

    public static void main(String[] args) {
        System.out.println("[예시 1] extends로 부모의 필드(name)/메서드(eat)를 물려받고, 자식 행동(bark)을 추가");
        System.out.println();

        Dog dog = new Dog("초코"); // 생성 과정의 출력은 Example2에서 자세히 다룬다
        System.out.println();

        // (1) 부모 Animal에서 물려받은 메서드 — Dog 클래스에는 eat()를 정의하지 않았다.
        System.out.print("물려받은 eat() 호출 -> ");
        dog.eat();

        // (2) 자식 Dog가 새로 추가한 메서드.
        System.out.print("자식이 추가한 bark() 호출 -> ");
        dog.bark();

        System.out.println();
        System.out.println("=> Dog에 eat()를 안 적었는데도 호출된다 = 부모 것을 물려받았다는 증거.");
        System.out.println("   상속은 '물려받기(eat) + 더하기(bark)'다.");
    }
}
