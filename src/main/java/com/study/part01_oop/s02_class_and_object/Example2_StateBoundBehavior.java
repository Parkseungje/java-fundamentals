package com.study.part01_oop.s02_class_and_object;

/**
 * 예시 2 / 3 — "객체의 행동은 '자기 자신의' 상태에 작용하는가?"
 *
 * 이 예시가 답하려는 질문: 예시 1은 "상태가 객체마다 독립"임을 봤다. 그렇다면 행동(메서드)은
 * 어떤가? deposit(2000)이라는 똑같은 호출이라도, 어느 객체에 대고 호출하느냐에 따라
 * 영향을 받는 잔액이 달라지는가?
 *
 * 왜 이 시나리오인가: 두 계좌에 "같은 코드"인 withdraw(3000)을 각각 호출한다. 단, 두 계좌의
 * 초기 잔액을 다르게(홍길동 5000 / 김철수 2000) 둔다. withdraw 안에는 "잔액보다 많이 출금
 * 불가" 규칙이 있으므로, 같은 3000 출금이라도 한쪽은 성공하고 한쪽은 거절될 것이다.
 *
 * 예상 결과:
 *   홍길동(5000): 3000 출금 성공 -> 2000
 *   김철수(2000): 3000 출금은 잔액 부족 -> 거절, 2000 그대로
 * -> 같은 메서드(같은 코드)지만 결과가 다른 이유는, 메서드가 항상 "호출 대상 객체의 상태(this)"를
 * 기준으로 판단하기 때문이다. 이것이 "상태와 행동이 한 객체로 묶여 있다"는 말의 핵심이다.
 * 행동은 떠다니는 함수가 아니라 "특정 객체에 소속된" 동작이다. (예시 1의 '독립된 상태' +
 * 이 예시의 '상태에 묶인 행동' = 객체지향의 기본 단위)
 */
public class Example2_StateBoundBehavior {

    public static void main(String[] args) {
        System.out.println("[예시 2] 같은 메서드 호출 withdraw(3000)이라도 대상 객체의 상태에 따라 결과가 다르다");
        System.out.println();

        BankAccount hong = new BankAccount("홍길동", 5000);
        BankAccount kim = new BankAccount("김철수", 2000);

        System.out.println("처음: 홍길동=" + hong.getBalance() + ", 김철수=" + kim.getBalance());
        System.out.println();

        System.out.println("두 계좌에 똑같이 withdraw(3000) 호출:");
        hong.withdraw(3000); // 5000 >= 3000 이므로 성공
        kim.withdraw(3000);  // 2000 < 3000 이므로 거절 (위 메서드가 콘솔에 거절 메시지 출력)

        System.out.println();
        System.out.println("결과: 홍길동=" + hong.getBalance() + " (출금 성공), 김철수=" + kim.getBalance() + " (거절되어 그대로)");

        System.out.println();
        System.out.println("=> 코드는 같아도 메서드는 '자기 객체의 상태(this.balance)'를 기준으로 동작한다.");
        System.out.println("   행동은 떠다니는 함수가 아니라 '특정 객체에 묶인' 동작이다.");
    }
}
