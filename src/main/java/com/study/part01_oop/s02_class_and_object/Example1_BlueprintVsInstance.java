package com.study.part01_oop.s02_class_and_object;

/**
 * 예시 1 / 3 — "클래스(틀) 하나로 찍어낸 여러 객체는 서로 독립된 상태를 갖는가?"
 *
 * 이 예시가 답하려는 질문: 클래스는 청사진이고 객체는 그 청사진으로 찍어낸 실물이라고 한다.
 * 그렇다면 같은 클래스에서 만든 객체 여러 개는 상태를 공유할까, 아니면 각자 따로 가질까?
 *
 * 왜 이 시나리오인가: 같은 BankAccount 클래스로 계좌 2개(홍길동/김철수)를 만들고,
 * 한쪽에만 입금해본다. 만약 둘이 상태를 공유한다면 양쪽 잔액이 같이 움직일 것이고,
 * 독립적이라면 입금한 쪽만 변할 것이다.
 *
 * 예상 결과: 홍길동 계좌에만 입금했으므로 홍길동의 잔액만 늘고 김철수의 잔액은 그대로일 것이다.
 * -> "붕어빵 틀(클래스)은 하나지만 찍어낸 붕어빵(객체)은 각자 다른 속을 가진다"는 비유의 실체.
 * 각 객체는 자신만의 필드 공간을 힙(heap)에 따로 갖는다. (메모리 구조는 PART 2에서 심화)
 */
public class Example1_BlueprintVsInstance {

    public static void main(String[] args) {
        System.out.println("[예시 1] 클래스 하나(BankAccount) -> 객체 둘. 상태는 공유될까 독립일까?");
        System.out.println();

        // 같은 클래스(틀)로 서로 다른 실물 2개를 찍어낸다.
        BankAccount hong = new BankAccount("홍길동", 1000);
        BankAccount kim = new BankAccount("김철수", 1000);

        System.out.println("처음: 홍길동=" + hong.getBalance() + ", 김철수=" + kim.getBalance());

        // 홍길동 계좌에만 입금한다.
        hong.deposit(5000);
        System.out.println("홍길동에게만 5000 입금 후:");
        System.out.println("  홍길동=" + hong.getBalance() + "  <- 늘어남");
        System.out.println("  김철수=" + kim.getBalance() + "  <- 그대로 (영향 없음)");

        System.out.println();
        System.out.println("=> 같은 클래스로 만들었어도 각 객체는 자기만의 상태(잔액)를 따로 가진다.");
        System.out.println("   클래스는 '틀'일 뿐, 실제 데이터는 객체마다 독립적으로 존재한다.");
    }
}
