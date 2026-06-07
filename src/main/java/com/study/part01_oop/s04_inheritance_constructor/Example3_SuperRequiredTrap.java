package com.study.part01_oop.s04_inheritance_constructor;

/**
 * 예시 3 / 3 — 함정: "부모에 매개변수 생성자만 있으면, 자식은 super(...)를 명시해야 한다."
 *
 * 이 예시가 답하려는 질문: Example2에서는 super() 자동 호출 덕에 신경 쓸 게 없어 보였다.
 * 그런데 항상 자동으로 잘 되는가? 부모에 '매개변수 없는 기본 생성자'가 없으면 어떤 일이 벌어지나?
 *
 * 왜 이 시나리오인가: Account는 일부러 매개변수 생성자 Account(String)만 갖고, 기본 생성자
 * Account()는 없다(직접 생성자를 정의하면 자동 기본 생성자가 사라지기 때문). 이 상태에서
 * 자식 SavingsAccount가 super(owner)를 적었을 때는 정상 동작하고, 만약 안 적으면(아래 주석
 * 설명 참고) 컴파일 자체가 실패한다는 점을 보여준다.
 *
 * 예상 결과:
 *   - super(owner)를 제대로 호출하는 SavingsAccount는 정상 생성되어 describe()까지 동작한다.
 *   - super(owner)를 생략한 가상의 자식 클래스(BrokenSavingsAccount, 아래 주석)는
 *     "there is no default constructor available in Account" 컴파일 에러가 난다.
 * -> 핵심: super() 자동 호출은 '부모에 기본 생성자가 있을 때만' 통한다. 부모가 매개변수
 *    생성자만 가지면, 자식이 직접 super(...)로 어떤 부모 생성자를 부를지 지정해줘야 한다.
 *    (Example2처럼 항상 자동으로 되는 게 아니라는 점이 Example2와의 결정적 차이다.)
 */
public class Example3_SuperRequiredTrap {

    public static void main(String[] args) {
        System.out.println("[예시 3] 부모에 매개변수 생성자만 있을 때의 super 호출 함정");
        System.out.println();

        // 해결 버전: SavingsAccount는 super(owner)를 첫 줄에 명시했기에 정상 동작한다.
        System.out.println("정상 케이스(super(owner)를 명시한 SavingsAccount) 생성:");
        SavingsAccount sa = new SavingsAccount("홍길동", 0.03);
        System.out.println();
        sa.describe();

        System.out.println();
        System.out.println("=> super(owner)를 명시했기 때문에 컴파일·실행 모두 성공.");
        System.out.println();
        System.out.println("[함정 설명] 만약 자식이 super(owner)를 생략했다면:");
        System.out.println("  - 컴파일러가 super() 를 자동으로 넣으려 한다.");
        System.out.println("  - 그러나 Account에는 매개변수 없는 생성자 Account()가 없다.");
        System.out.println("  - 결과: 'there is no default constructor available in Account' 컴파일 에러.");
    }

    /*
     * 아래 클래스의 주석을 풀면 컴파일 에러가 난다(의도된 함정 재현):
     *
     *   class BrokenSavingsAccount extends Account {
     *       BrokenSavingsAccount(String owner) {
     *           // super(owner)를 호출하지 않음
     *           // -> 컴파일러가 super()를 자동 삽입하려 하지만 Account()가 없어 컴파일 실패:
     *           //    "there is no default constructor available in Account"
     *           this.owner = owner;
     *       }
     *   }
     */
}
