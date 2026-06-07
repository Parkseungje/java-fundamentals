package com.study.part01_oop.s08_solid;

/**
 * 예시 5 / 5 — DIP (Dependency Inversion Principle, 의존 역전 원칙)
 *
 * 원칙: "고수준 모듈은 저수준 모듈의 '구체 구현'에 의존하면 안 되고, 둘 다 '추상화'에 의존해야
 * 한다." 정책(상위)이 세부 기술(하위)에 직접 묶이지 않게 한다.
 *
 * 이 예시가 답하려는 질문: 주문 서비스(고수준)가 EmailSender(저수준 구체 클래스)를 직접 new로
 * 만들어 쓰면 무엇이 문제인가? 추상화(MessageSender)에 의존하고 외부에서 주입하면 무엇이 좋아지나?
 *
 * 왜 이 시나리오인가: Bad의 OrderServiceBad는 내부에서 new EmailSenderBad()로 구체 클래스를
 * 직접 만든다. 알림 수단을 SMS로 바꾸려면 OrderServiceBad 코드를 '수정'해야 한다(강결합).
 * Good의 OrderServiceGood는 MessageSender 인터페이스에만 의존하고, 어떤 구현을 쓸지는 생성자로
 * '주입'받는다. 그래서 같은 서비스 코드로 이메일/ SMS를 자유롭게 갈아끼울 수 있다.
 *
 * 예상 결과:
 *   - Bad: 항상 이메일로만 발송. 수단 변경 = 서비스 코드 수정 필요.
 *   - Good: 같은 OrderServiceGood에 EmailSender를 넣으면 이메일, SmsSender를 넣으면 SMS로 발송.
 *     서비스 코드는 한 줄도 바뀌지 않는다.
 * -> 이 "추상화에 의존 + 외부 주입" 구조가 곧 DI(Dependency Injection)이며, PART 8의 Spring IoC가
 *    이 주입을 컨테이너가 대신 해주는 것이다. DIP는 그 사상적 토대다.
 */
public class Example5_DIP {

    // ===== Bad: 고수준(주문 서비스)이 저수준 구체 클래스에 직접 의존 =====
    static class EmailSenderBad {
        void sendEmail(String msg) { System.out.println("  [Email] " + msg); }
    }

    static class OrderServiceBad {
        // 구체 클래스를 직접 생성 = 강결합. SMS로 바꾸려면 이 줄과 사용부를 모두 고쳐야 함.
        private final EmailSenderBad sender = new EmailSenderBad();

        void completeOrder() {
            sender.sendEmail("주문이 완료되었습니다");
        }
    }

    // ===== Good: 고수준/저수준 모두 추상화(MessageSender)에 의존 + 외부 주입 =====
    interface MessageSender {
        void send(String msg);
    }

    static class EmailSender implements MessageSender {
        public void send(String msg) { System.out.println("  [Email] " + msg); }
    }

    static class SmsSender implements MessageSender {
        public void send(String msg) { System.out.println("  [SMS] " + msg); }
    }

    static class OrderServiceGood {
        // 구체가 아니라 추상화(MessageSender)에만 의존. 무엇이 주입될지는 모른다(알 필요도 없다).
        private final MessageSender sender;

        // 생성자 주입: 어떤 구현을 쓸지는 '바깥'이 결정해서 넣어준다(= 제어의 역전, PART 8).
        OrderServiceGood(MessageSender sender) {
            this.sender = sender;
        }

        void completeOrder() {
            sender.send("주문이 완료되었습니다");
        }
    }

    public static void main(String[] args) {
        System.out.println("[예시 5] DIP — 구체에 직접 의존(Bad) vs 추상화 의존 + 주입(Good)");
        System.out.println();

        System.out.println("(Bad) OrderServiceBad가 EmailSenderBad를 직접 new:");
        new OrderServiceBad().completeOrder();
        System.out.println("  -> 항상 이메일. SMS로 바꾸려면 OrderServiceBad 코드를 수정해야 함(강결합).");

        System.out.println();

        System.out.println("(Good) OrderServiceGood는 MessageSender만 의존, 구현을 주입받음:");
        // 같은 서비스 클래스에 다른 구현을 넣기만 하면 동작이 바뀐다.
        OrderServiceGood byEmail = new OrderServiceGood(new EmailSender());
        OrderServiceGood bySms = new OrderServiceGood(new SmsSender());
        byEmail.completeOrder();
        bySms.completeOrder();
        System.out.println("  -> 서비스 코드 수정 0. 주입하는 구현만 바꿔 이메일/SMS 전환.");

        System.out.println();
        System.out.println("=> '추상화에 의존 + 외부 주입'이 DI다. PART 8 Spring IoC는 이 주입을");
        System.out.println("   컨테이너가 대신 해주는 것. DIP는 Spring DI와 정확히 같은 원리다.");
    }
}
