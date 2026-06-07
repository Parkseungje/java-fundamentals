package com.study.part01_oop.s02_class_and_object;

/**
 * [모델] 클래스 = 청사진(붕어빵 틀), 객체 = 그 틀로 찍어낸 실물(붕어빵).
 *
 * 이 BankAccount 클래스는 "계좌란 무엇인가"를 정의하는 설계도일 뿐, 그 자체로는
 * 어떤 실제 계좌도 아니다. new BankAccount(...)로 찍어내야 비로소 "홍길동의 계좌",
 * "김철수의 계좌" 같은 개별 실물(객체/인스턴스)이 된다.
 *
 * 클래스가 가져야 할 두 가지를 모두 담고 있다.
 *   1. 상태(필드): owner, balance — "이 계좌가 지금 어떤 상황인가"를 나타내는 데이터
 *   2. 행동(메서드): deposit, withdraw — "이 계좌로 무엇을 할 수 있는가"
 *
 * 핵심: 행동(deposit/withdraw)은 항상 "자기 자신의" 상태(this.balance)에 작용한다.
 * 그래서 같은 deposit(1000) 호출이라도 어느 객체에 대고 호출하느냐에 따라 바뀌는 잔액이 다르다.
 * (-> Example2에서 직접 확인)
 */
public class BankAccount {

    // 상태 1: 계좌 주인. 생성 시 정해지고 바뀌지 않으므로 final.
    private final String owner;

    // 상태 2: 현재 잔액. deposit/withdraw 행동에 의해서만 변한다.
    private int balance;

    public BankAccount(String owner, int initialBalance) {
        this.owner = owner;
        this.balance = initialBalance;
    }

    // 행동: 입금. this.balance(자기 자신의 잔액)를 늘린다.
    public void deposit(int amount) {
        this.balance += amount;
    }

    // 행동: 출금. 잔액이 부족하면 거절한다(상태에 기반한 판단).
    public void withdraw(int amount) {
        if (amount > this.balance) {
            System.out.println("  [" + owner + "] 잔액 부족으로 출금 거절 (요청 " + amount + ", 잔액 " + balance + ")");
            return;
        }
        this.balance -= amount;
    }

    public int getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }
}
