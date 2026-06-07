package com.study.part01_oop.s04_inheritance_constructor;

/**
 * [모델] Account를 상속한 자식. "함정의 해결 버전".
 *
 * 부모 Account에는 매개변수 없는 생성자가 없으므로, 자식은 반드시 명시적으로 super(owner)를
 * 호출해야 한다. 아래처럼 super(owner)를 첫 줄에 적어주면 정상 컴파일된다.
 *
 * 만약 super(owner)를 빼면:
 *   - 컴파일러가 자동으로 super()를 첫 줄에 넣으려 한다.
 *   - 그런데 Account()(매개변수 없는 생성자)가 존재하지 않는다.
 *   - 따라서 "there is no default constructor available in Account" 컴파일 에러가 난다.
 * (Example3에서 이 에러 상황을 주석으로 재현해 둔다.)
 */
public class SavingsAccount extends Account {

    private double interestRate;

    public SavingsAccount(String owner, double interestRate) {
        // 부모에 기본 생성자가 없으므로 이 명시적 super 호출이 '필수'다. 그리고 항상 첫 줄이어야 한다.
        super(owner);
        this.interestRate = interestRate;
        System.out.println("  [SavingsAccount 생성자] interestRate=" + interestRate);
    }

    public void describe() {
        System.out.println(owner + "의 저축계좌, 이율 " + interestRate);
    }
}
