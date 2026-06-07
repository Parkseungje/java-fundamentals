package com.study.part01_oop.s08_solid;

/**
 * 예시 1 / 5 — SRP (Single Responsibility Principle, 단일 책임 원칙)
 *
 * 원칙: "클래스가 바뀌어야 하는 이유는 단 하나여야 한다." 즉 한 클래스는 하나의 책임만 진다.
 *
 * 이 예시가 답하려는 질문: 한 클래스가 여러 책임(데이터 보관 + 저장 + 이메일 발송)을 떠안으면
 * 무엇이 문제이고, 책임을 쪼개면 무엇이 좋아지는가?
 *
 * 왜 이 시나리오인가: UserGod(Bad)은 사용자 데이터도 갖고, DB 저장도 하고, 이메일도 보낸다.
 * 이러면 "DB 방식이 바뀌어도" "이메일 양식이 바뀌어도" "검증 규칙이 바뀌어도" 전부 이 한 클래스를
 * 수정해야 한다 = 변경 이유가 셋. 반면 Good은 User(데이터) / UserRepository(저장) /
 * EmailService(발송)로 책임을 분리해, 각자 변경 이유가 하나뿐이다.
 *
 * 예상 결과: 두 방식 모두 "가입 + 환영메일" 동작은 동일하게 수행한다. 차이는 '결과'가 아니라
 * '변경에 대한 구조적 견고함'이다 — 책임이 분리된 Good은 한 부분을 고쳐도 다른 부분에 영향이 없다.
 */
public class Example1_SRP {

    // ===== Bad: 한 클래스가 데이터 + 저장 + 이메일 발송을 모두 떠안음 (변경 이유가 셋) =====
    static class UserGod {
        private final String email;

        UserGod(String email) {
            this.email = email;
        }

        // 책임 1: 검증
        boolean isValid() {
            return email != null && email.contains("@");
        }

        // 책임 2: 저장 (DB 방식이 바뀌면 여기를 고쳐야 함)
        void saveToDatabase() {
            System.out.println("  [Bad] DB에 저장: " + email);
        }

        // 책임 3: 이메일 발송 (메일 양식/서버가 바뀌면 여기를 고쳐야 함)
        void sendWelcomeEmail() {
            System.out.println("  [Bad] 환영 메일 발송 -> " + email);
        }
    }

    // ===== Good: 책임별로 분리 — 각 클래스의 변경 이유가 하나뿐 =====

    // 책임: 사용자 데이터(상태)만 보관
    static class User {
        private final String email;
        User(String email) { this.email = email; }
        String getEmail() { return email; }
        boolean isValid() { return email != null && email.contains("@"); }
    }

    // 책임: 저장만 담당 (DB 방식 변경 시 여기만 수정)
    static class UserRepository {
        void save(User user) {
            System.out.println("  [Good] UserRepository가 저장: " + user.getEmail());
        }
    }

    // 책임: 이메일 발송만 담당 (메일 관련 변경 시 여기만 수정)
    static class EmailService {
        void sendWelcome(User user) {
            System.out.println("  [Good] EmailService가 환영 메일 발송 -> " + user.getEmail());
        }
    }

    public static void main(String[] args) {
        System.out.println("[예시 1] SRP — 한 클래스에 책임 몰아넣기(Bad) vs 책임 분리(Good)");
        System.out.println();

        System.out.println("(Bad) UserGod 하나가 검증/저장/메일을 모두 처리:");
        UserGod god = new UserGod("hong@test.com");
        if (god.isValid()) {
            god.saveToDatabase();
            god.sendWelcomeEmail();
        }
        System.out.println("  -> 변경 이유가 셋(검증/DB/메일). 메일만 바뀌어도 이 클래스를 건드려야 함.");

        System.out.println();

        System.out.println("(Good) 책임을 세 클래스로 분리해 협력:");
        User user = new User("hong@test.com");
        UserRepository repository = new UserRepository();
        EmailService emailService = new EmailService();
        if (user.isValid()) {
            repository.save(user);
            emailService.sendWelcome(user);
        }
        System.out.println("  -> 각 클래스의 변경 이유가 하나. 메일 양식이 바뀌어도 EmailService만 수정.");

        System.out.println();
        System.out.println("=> 동작 결과는 같지만, Good은 '변경의 파급'이 한 클래스에 갇혀 유지보수가 쉽다.");
    }
}
