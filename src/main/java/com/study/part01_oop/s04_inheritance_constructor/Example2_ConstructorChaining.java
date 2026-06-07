package com.study.part01_oop.s04_inheritance_constructor;

/**
 * 예시 2 / 3 — "자식을 생성하면 부모 생성자는 언제 실행되는가?"
 *
 * 이 예시가 답하려는 질문: new Dog(...) 한 번 호출로 객체가 만들어질 때, 부모(Animal)의
 * 생성자와 자식(Dog)의 생성자는 어떤 순서로 실행되는가? 자식 생성자가 부모 생성자를
 * 자동으로 부르는가?
 *
 * 왜 이 시나리오인가: Animal 생성자와 Dog 생성자에 각각 출력문을 심어 두었다. new Dog()를
 * 한 번 호출하고 콘솔에 찍히는 '순서'를 보면, 누가 먼저 실행되는지 객관적으로 알 수 있다.
 * "자식 생성자는 가장 먼저 부모 생성자(super())를 호출한다"는 설명이 맞다면,
 * Animal 생성자 출력이 Dog 생성자 출력보다 먼저 나와야 한다.
 *
 * 예상 결과(출력 순서):
 *   1) [Animal 생성자] name=초코 초기화      <- 부모가 먼저
 *   2) [Dog 생성자] 초코 준비 완료           <- 그다음 자식
 * -> 자식 생성자 본문(System.out 등)이 실행되기 '전에' 부모 생성자가 먼저 끝난다.
 *    즉 객체는 '부모 부분 -> 자식 부분' 순서로 초기화된다(생성자 체이닝).
 *    Dog 생성자에 super(name)을 적었기 때문이며, 안 적어도 컴파일러가 super()를 첫 줄에
 *    자동 삽입한다(단 부모에 기본 생성자가 있을 때만 — 그 함정은 Example3).
 */
public class Example2_ConstructorChaining {

    public static void main(String[] args) {
        System.out.println("[예시 2] new Dog(\"초코\") 한 번 호출 -> 생성자 실행 순서 관찰");
        System.out.println();

        System.out.println("객체 생성 시작:");
        Dog dog = new Dog("초코");
        System.out.println("객체 생성 끝");

        System.out.println();
        // 사용 코드를 한 번 호출해 객체가 정상적으로 완성됐는지 확인
        dog.bark();

        System.out.println();
        System.out.println("=> 출력 순서가 [Animal 생성자] -> [Dog 생성자] 였다면,");
        System.out.println("   부모 생성자가 자식 생성자보다 먼저 실행된다(생성자 체이닝)는 뜻이다.");
        System.out.println("   객체는 '부모 부분 먼저, 자식 부분 나중'으로 초기화된다.");
    }
}
