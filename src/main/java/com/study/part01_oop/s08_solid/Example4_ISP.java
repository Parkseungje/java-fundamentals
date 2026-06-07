package com.study.part01_oop.s08_solid;

/**
 * 예시 4 / 5 — ISP (Interface Segregation Principle, 인터페이스 분리 원칙)
 *
 * 원칙: "클라이언트는 자신이 사용하지 않는 메서드에 의존하면 안 된다." 뚱뚱한(fat) 인터페이스
 * 하나보다, 역할별로 잘게 쪼갠 인터페이스 여러 개가 낫다.
 *
 * 이 예시가 답하려는 질문: work()와 eat()를 한 인터페이스(Worker)에 묶으면, 먹지 않는 로봇
 * 노동자는 어떻게 되나? 인터페이스를 쪼개면 무엇이 좋아지나?
 *
 * 왜 이 시나리오인가: Bad의 Worker는 work()와 eat()를 함께 요구한다. 사람은 둘 다 자연스럽지만,
 * 로봇은 '먹기'가 의미 없다. 그런데도 Worker를 구현하려면 eat()를 억지로 구현(빈 메서드/예외)해야
 * 한다 = 쓰지 않는 메서드에 의존 = ISP 위반. Good은 Workable / Eatable로 분리해, 각 타입이
 * 자신에게 필요한 능력만 구현하게 한다.
 *
 * 예상 결과:
 *   - Bad: RobotWorker가 eat()를 억지로 구현(예외)하고, 이를 모르고 호출하면 런타임에 터짐
 *   - Good: 로봇은 Workable만 구현 -> eat()가 아예 없으니 잘못 호출할 일조차 없다
 * -> 인터페이스를 작게 쪼개면 "필요 없는 의존"이 사라져, 구현체가 깔끔하고 오용 위험이 준다.
 */
public class Example4_ISP {

    // ===== Bad: 하나의 뚱뚱한 인터페이스가 work + eat을 강요 =====
    interface Worker {
        void work();
        void eat();
    }

    static class HumanWorker implements Worker {
        public void work() { System.out.println("  사람: 일한다"); }
        public void eat() { System.out.println("  사람: 점심을 먹는다"); }
    }

    static class RobotWorker implements Worker {
        public void work() { System.out.println("  로봇: 일한다"); }
        // 로봇은 먹지 않는데도 eat()을 억지로 구현해야 한다 = 쓰지 않는 메서드에 강제 의존
        public void eat() {
            throw new UnsupportedOperationException("로봇은 먹지 않습니다");
        }
    }

    // ===== Good: 역할별로 인터페이스를 분리 =====
    interface Workable { void work(); }
    interface Eatable { void eat(); }

    // 사람은 두 능력을 모두 가짐 -> 둘 다 구현
    static class HumanGood implements Workable, Eatable {
        public void work() { System.out.println("  사람: 일한다"); }
        public void eat() { System.out.println("  사람: 점심을 먹는다"); }
    }

    // 로봇은 일만 함 -> Workable만 구현. eat()는 아예 존재하지 않으니 오용 불가.
    static class RobotGood implements Workable {
        public void work() { System.out.println("  로봇: 일한다"); }
    }

    public static void main(String[] args) {
        System.out.println("[예시 4] ISP — 뚱뚱한 인터페이스(Bad) vs 역할별 분리(Good)");
        System.out.println();

        System.out.println("(Bad) Worker가 work+eat을 강요 -> 로봇이 eat을 억지로 구현:");
        Worker robot = new RobotWorker();
        robot.work();
        try {
            robot.eat(); // 의미 없는 메서드인데 인터페이스가 강요해서 존재 -> 호출하면 폭발
        } catch (UnsupportedOperationException e) {
            System.out.println("  로봇 eat() 호출 -> " + e.getMessage());
            System.out.println("  -> 쓰지 않는 메서드에 의존하게 됨 = ISP 위반");
        }

        System.out.println();

        System.out.println("(Good) Workable / Eatable 분리:");
        Workable robotGood = new RobotGood();
        robotGood.work();
        // robotGood.eat();  // <- 컴파일 에러: RobotGood에는 eat()이 아예 없다(애초에 오용 불가)
        System.out.println("  RobotGood에는 eat()이 없어 잘못 호출하는 것 자체가 불가능");
        HumanGood human = new HumanGood();
        human.work();
        human.eat();

        System.out.println();
        System.out.println("=> 인터페이스를 작게 쪼개면 구현체가 '필요한 능력만' 갖게 되어");
        System.out.println("   불필요한 의존과 오용 위험이 사라진다.");
    }
}
