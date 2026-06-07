package com.study.part01_oop.s06_abstraction;

/**
 * 예시 3 / 3 — "Java 8 default 메서드는 어떤 문제를 푸는가?"
 *
 * 이 예시가 답하려는 질문: 인터페이스에 메서드를 새로 추가하면 원래는 '모든 구현체'가 그것을
 * 구현해야 해서 기존 코드가 전부 깨진다. default 메서드는 이 문제를 어떻게 해결하나?
 *
 * 왜 이 시나리오인가: Flyable에 describeFlight()가 '나중에' default 메서드로 추가되었다고 가정한다.
 *   - Duck: 이 메서드를 오버라이드하지 않았다 -> 그래도 컴파일/실행 OK. default 구현을 물려받는다.
 *   - Airplane: 필요해서 자기 방식으로 오버라이드했다 -> 자기 구현이 실행된다.
 * 만약 describeFlight()가 default가 아닌 일반 추상 메서드였다면, Duck은 그것을 구현하지 않아
 * 컴파일이 깨졌을 것이다. default가 본문을 제공하기 때문에 "선택적 구현"이 되어 하위 호환이 유지된다.
 *
 * 예상 결과:
 *   - duck.describeFlight()     -> "기본 비행 설명: 무언가가 날고 있다" (Flyable의 default 사용)
 *   - airplane.describeFlight() -> "비행기는 제트엔진 추력으로 비행한다" (오버라이드한 자기 구현)
 * -> default 메서드 = 인터페이스에 구현(본문)을 담아, 새 메서드를 추가해도 기존 구현체를 깨지 않는 장치.
 *    이로 인해 추상클래스와 인터페이스의 경계가 흐려졌다(인터페이스도 이제 '구현'을 가질 수 있으므로).
 */
public class Example3_DefaultMethod {

    public static void main(String[] args) {
        System.out.println("[예시 3] default 메서드: 인터페이스에 기능을 추가해도 기존 구현체가 안 깨진다");
        System.out.println();

        Flyable duck = new Duck("도널드");
        Flyable airplane = new Airplane();

        // Duck은 describeFlight()를 오버라이드하지 않았지만, default 구현 덕분에 정상 호출된다.
        System.out.println("오리(오버라이드 안 함, default 사용):");
        System.out.println("  " + duck.describeFlight());

        // Airplane은 직접 오버라이드 -> 자기 구현이 실행된다.
        System.out.println("비행기(default를 오버라이드):");
        System.out.println("  " + airplane.describeFlight());

        System.out.println();
        System.out.println("=> describeFlight()가 만약 일반 추상 메서드였다면 Duck은 그것을 구현하지 않아");
        System.out.println("   컴파일이 깨졌을 것이다. default가 본문을 주므로 '선택적 구현'이 되어 하위 호환 유지.");
        System.out.println("   -> 인터페이스도 구현(본문)을 가질 수 있게 되어 추상클래스와의 경계가 흐려졌다.");
    }
}
