package com.study.part01_oop.s08_solid;

/**
 * 예시 2 / 5 — OCP (Open-Closed Principle, 개방-폐쇄 원칙)
 *
 * 원칙: "확장에는 열려 있고, 수정에는 닫혀 있어야 한다." 새 기능을 추가할 때 기존 코드를
 * 고치지 않고, 새 코드를 더하는 것만으로 확장할 수 있어야 한다.
 *
 * 이 예시가 답하려는 질문: 등급별 할인 계산을 if-else 분기로 짜면 새 등급이 생길 때 무슨 일이
 * 벌어지나? 전략(인터페이스 다형성)으로 바꾸면 무엇이 달라지나?
 *
 * 왜 이 시나리오인가: DiscountServiceBad는 등급을 if-else로 분기한다. 새 등급(예: PLATINUM)이
 * 추가되면 이 메서드 자체를 '수정'해야 한다(= 수정에 열려 있음 = OCP 위반). 반면 Good은 할인
 * 정책을 DiscountPolicy 인터페이스로 추상화해서, 새 등급은 새 구현 클래스를 '추가'하기만 하면
 * 되고 기존 서비스 코드는 한 줄도 바뀌지 않는다.
 *
 * 예상 결과: 기존 등급(VIP/GOLD) 계산은 두 방식 모두 동일. 핵심은 'PLATINUM 추가' 시점이다 —
 * Bad는 분기 메서드를 고쳐야 하지만, Good은 PlatinumDiscount 클래스만 새로 만들면 끝난다.
 * (이 인터페이스+다형성 구조가 바로 1.1~1.6에서 본 전략 패턴이며, PART 8 Spring DI의 뿌리다.)
 */
public class Example2_OCP {

    // ===== Bad: if-else 타입 분기 — 새 등급마다 이 메서드를 '수정'해야 함 =====
    enum Grade { VIP, GOLD }

    static class DiscountServiceBad {
        int discountedPrice(Grade grade, int price) {
            if (grade == Grade.VIP) {
                return price - (price * 30 / 100);
            } else if (grade == Grade.GOLD) {
                return price - (price * 10 / 100);
            }
            // PLATINUM을 추가하려면? 여기에 else if를 또 끼워 넣어야 한다 = 기존 코드 수정 = OCP 위반
            return price;
        }
    }

    // ===== Good: 할인 정책을 인터페이스로 추상화 — 새 정책은 '추가'만 하면 됨 =====
    interface DiscountPolicy {
        int apply(int price);
    }

    static class VipDiscount implements DiscountPolicy {
        public int apply(int price) { return price - (price * 30 / 100); }
    }

    static class GoldDiscount implements DiscountPolicy {
        public int apply(int price) { return price - (price * 10 / 100); }
    }

    // 새 등급은 이렇게 '새 클래스 추가'만으로 확장된다. 아래 서비스 코드는 손대지 않는다.
    static class PlatinumDiscount implements DiscountPolicy {
        public int apply(int price) { return price - (price * 50 / 100); }
    }

    static class DiscountServiceGood {
        // 어떤 정책인지 모른 채, 추상화(DiscountPolicy)에만 의존한다.
        int discountedPrice(DiscountPolicy policy, int price) {
            return policy.apply(price);
        }
    }

    public static void main(String[] args) {
        System.out.println("[예시 2] OCP — if-else 분기(Bad) vs 전략 인터페이스(Good)");
        System.out.println();

        int price = 10000;

        System.out.println("(Bad) if-else 분기:");
        DiscountServiceBad bad = new DiscountServiceBad();
        System.out.println("  VIP  -> " + bad.discountedPrice(Grade.VIP, price));
        System.out.println("  GOLD -> " + bad.discountedPrice(Grade.GOLD, price));
        System.out.println("  -> PLATINUM을 넣으려면 discountedPrice 메서드를 '수정'해야 한다(OCP 위반).");

        System.out.println();

        System.out.println("(Good) 전략 인터페이스 + 다형성:");
        DiscountServiceGood good = new DiscountServiceGood();
        System.out.println("  VIP      -> " + good.discountedPrice(new VipDiscount(), price));
        System.out.println("  GOLD     -> " + good.discountedPrice(new GoldDiscount(), price));
        // 새 등급을 추가했지만 DiscountServiceGood 코드는 전혀 바뀌지 않았다.
        System.out.println("  PLATINUM -> " + good.discountedPrice(new PlatinumDiscount(), price)
                + "  (새 클래스만 추가, 서비스 코드 수정 0)");

        System.out.println();
        System.out.println("=> Good은 '확장에 열림(새 정책 추가) + 수정에 닫힘(서비스 불변)'을 만족한다.");
        System.out.println("   이 구조가 전략 패턴이고, PART 8 Spring DI로 이어진다.");
    }
}
