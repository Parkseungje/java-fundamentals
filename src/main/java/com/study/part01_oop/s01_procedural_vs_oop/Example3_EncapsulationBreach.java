package com.study.part01_oop.s01_procedural_vs_oop;

/**
 * 예시 3 / 3 — "함수/메서드를 아예 우회해서 데이터를 직접 건드릴 수 있는가?"
 *
 * <p><b>이 예시가 답하려는 질문</b>: 예시 2에서는 그래도 "함수를 통해" 데이터를 바꿨다.
 * 그런데 함수 자체를 무시하고 필드에 직접 값을 꽂아 넣으면? 절차지향과 객체지향은 이 "우회 시도"를
 * 각각 어떻게 다루는가?
 *
 * <p><b>왜 이 시나리오인가</b>: 예시 1·2는 "정상 통로(함수)를 거친 변경"을 다뤘다.
 * 이 예시는 한 단계 더 나아가 <b>통로를 무시한 직접 접근</b>을 시도한다. 이것이 캡슐화의 진짜
 * 시험대다. 규칙을 함수에 잘 넣어두더라도, 누군가 함수를 건너뛰고 필드를 직접 바꿀 수 있다면
 * 그 규칙은 무의미해지기 때문이다.
 *
 * <p><b>예상 결과</b>:
 * <ul>
 *   <li>절차지향: {@code car.speed = 9999}가 <b>런타임에 조용히 성공</b>한다. CarData의 필드가
 *       public이라 컴파일러도, 데이터 자신도 이를 막을 수단이 없다. 말도 안 되는 값이 그대로 들어간다.</li>
 *   <li>객체지향: {@code car.speed = 9999}는 <b>애초에 컴파일이 되지 않는다</b>(private 필드).
 *       그래서 아래 해당 줄은 주석 처리해 두었다 — 주석을 풀면 IDE/컴파일러가 즉시 에러를 낸다.
 *       이 "컴파일 자체가 안 됨"이야말로 자바가 <i>언어 차원에서</i> 캡슐화를 강제한다는 증거다.</li>
 * </ul>
 * → 예시 2와의 차이: 예시 2는 "함수를 거치되 규칙이 흩어진" 문제였고, 예시 3은 "함수를 아예 안 거치는"
 * 더 근본적인 위협이다. 객체지향은 후자를 <b>컴파일 단계에서</b> 원천 차단한다.
 */
public class Example3_EncapsulationBreach {

    public static void main(String[] args) {
        System.out.println("[예시 3] 함수/메서드를 우회한 직접 필드 접근 시도");
        System.out.println();

        // --- (A) 절차지향: public 필드라 함수를 거치지 않고 직접 대입 가능 ---
        CarData pCar = new CarData("절차지향-Car", 30);
        System.out.println("(A) 변경 전 절차지향 speed = " + pCar.speed);
        pCar.speed = 9999; // CarProceduralOps의 어떤 함수도 거치지 않은 직접 조작 — 막을 수 없다
        System.out.println("(A) car.speed = 9999 직접 대입 후 speed = " + pCar.speed
                + "  ← ❗ 말도 안 되는 값이 그대로 들어감(컴파일·실행 모두 통과)");

        System.out.println();

        // --- (B) 객체지향: private 필드라 직접 대입 자체가 컴파일 에러 ---
        Car oCar = new Car("객체지향-Car");
        oCar.accelerate(30);
        System.out.println("(B) 객체지향 speed = " + oCar.getSpeed() + " (정상 통로 accelerate로만 변경됨)");

        // 아래 줄의 주석을 풀면 컴파일 에러가 난다:
        //   java: speed has private access in com.study.part01_oop.s01_procedural_vs_oop.Car
        // oCar.speed = 9999;   // ← 컴파일 불가. 캡슐화를 "언어가" 강제한다는 증거.
        System.out.println("(B) oCar.speed = 9999 는 주석 처리됨 — 풀면 '컴파일 자체'가 실패한다.");
        System.out.println("    외부는 getSpeed()로 '읽기'만 가능하고 '쓰기'는 정상 메서드로만 가능.");

        System.out.println();
        System.out.println("=> 절차지향: 규칙을 함수에 넣어둬도 필드 직접 접근으로 무력화 가능(런타임에 조용히 성공).");
        System.out.println("   객체지향: 우회 시도 자체가 컴파일 단계에서 막힘 → '흉내'가 아니라 '언어의 보장'.");
    }
}
