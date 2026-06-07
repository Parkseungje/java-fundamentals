package com.study.part01_oop.s08_solid;

/**
 * 예시 3 / 5 — LSP (Liskov Substitution Principle, 리스코프 치환 원칙)
 *
 * 원칙: "자식은 부모를 완벽히 대체할 수 있어야 한다." 부모 타입을 기대하는 코드에 자식을 넣어도
 * 프로그램이 깨지지 않아야 한다.
 *
 * 이 예시가 답하려는 질문: "펭귄은 새다"라는 분류가 맞다고 Penguin extends Bird로 만들면 무슨
 * 문제가 생기나? 부모(Bird)가 fly()를 갖는데 펭귄은 날지 못한다면?
 *
 * 왜 이 시나리오인가: Bad에서 Bird는 fly()를 갖고, Penguin은 날 수 없어 fly()를 예외로 막는다.
 * 그러면 "모든 Bird는 날 수 있다"고 가정하고 작성된 코드(letItFly)가 Penguin을 받는 순간
 * 런타임에 깨진다 = 자식이 부모를 대체하지 못함 = LSP 위반. Good은 "나는 능력"을 별도
 * 인터페이스(FlyingBird)로 분리해, 날 수 있는 새만 그 능력을 갖게 한다. 그러면 날 수 있는 것만
 * 모아 날리므로 깨질 일이 없다.
 *
 * 예상 결과:
 *   - Bad: letItFly(penguin) 호출 시 UnsupportedOperationException (부모 자리에 자식을 넣었더니 폭발)
 *   - Good: FlyingBird 타입으로만 날리므로 Penguin은 애초에 그 자리에 들어갈 수 없다(컴파일 단계 안전)
 * -> LSP가 깨지면 다형성(1.5)도 깨진다: 부모 타입으로 묶어 일관되게 다루는 것이 불가능해지기 때문.
 */
public class Example3_LSP {

    // ===== Bad: 모든 새가 난다고 가정 -> 못 나는 펭귄이 그 약속을 깬다 =====
    static class Bird {
        protected final String name;
        Bird(String name) { this.name = name; }
        void fly() {
            System.out.println("  " + name + ": 날아오른다");
        }
    }

    static class Sparrow extends Bird {
        Sparrow(String name) { super(name); }
        // 참새는 잘 난다 — Bird의 fly()를 그대로 사용
    }

    static class Penguin extends Bird {
        Penguin(String name) { super(name); }
        @Override
        void fly() {
            // 펭귄은 날 수 없다 -> 부모의 약속(fly 가능)을 어기고 예외를 던진다 = LSP 위반의 신호
            throw new UnsupportedOperationException(name + "은 날 수 없습니다");
        }
    }

    // "모든 Bird는 날 수 있다"고 가정하고 작성된 코드. 부모 타입만 알면 된다고 믿는다.
    static void letItFly(Bird bird) {
        bird.fly();
    }

    // ===== Good: '나는 능력'을 별도 인터페이스로 분리. 못 나는 새는 이 능력을 갖지 않는다 =====
    static class BirdBase {
        protected final String name;
        BirdBase(String name) { this.name = name; }
        void eat() { System.out.println("  " + name + ": 먹는다"); }
    }

    interface FlyingBird {
        void fly();
    }

    static class SparrowGood extends BirdBase implements FlyingBird {
        SparrowGood(String name) { super(name); }
        public void fly() { System.out.println("  " + name + ": 날아오른다"); }
    }

    // 펭귄은 BirdBase이긴 하지만 FlyingBird는 구현하지 않는다 -> 날리는 코드에 끼어들 수 없다.
    static class PenguinGood extends BirdBase {
        PenguinGood(String name) { super(name); }
    }

    static void letItFly(FlyingBird bird) {
        bird.fly();
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] LSP — 펭귄이 Bird를 대체 못 함(Bad) vs 능력 분리(Good)");
        System.out.println();

        System.out.println("(Bad) 모든 Bird가 난다고 가정한 letItFly에 자식들을 넣어보기:");
        letItFly(new Sparrow("참새"));
        try {
            letItFly(new Penguin("펭귄")); // 부모(Bird) 자리에 자식(Penguin)을 넣었더니...
        } catch (UnsupportedOperationException e) {
            System.out.println("  펭귄에서 폭발! -> " + e.getMessage());
            System.out.println("  -> 자식이 부모를 대체하지 못함 = LSP 위반 (다형성이 깨진다)");
        }

        System.out.println();

        System.out.println("(Good) '나는 능력(FlyingBird)'을 가진 새만 날린다:");
        letItFly(new SparrowGood("참새"));
        // letItFly(new PenguinGood("펭귄"));  // <- 컴파일 에러: PenguinGood은 FlyingBird가 아님
        System.out.println("  PenguinGood은 FlyingBird가 아니라 letItFly에 넣는 것 자체가 컴파일 단계에서 막힘");

        System.out.println();
        System.out.println("=> Bad는 런타임에 폭발하지만, Good은 '날 수 있는 것만' 타입으로 보장해");
        System.out.println("   부모-자식 치환이 항상 안전하다. LSP를 지키면 다형성이 신뢰할 수 있게 된다.");
    }
}
