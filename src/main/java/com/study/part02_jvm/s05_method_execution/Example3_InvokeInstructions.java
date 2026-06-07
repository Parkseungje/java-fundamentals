package com.study.part02_jvm.s05_method_execution;

/**
 * 예시 3 / 3 — 호출 경로와 invoke 명령: "메서드 종류마다 다른 바이트코드 명령으로 호출된다."
 *
 * 이 예시가 답하려는 질문: 앞에서 본 '무엇을 vs 어디에'의 분리, 그리고 호출 경로(static/instance/
 * interface)는 바이트코드 레벨에서 어떻게 구분되는가?
 *
 * 왜 이 시나리오인가: 한 메서드 안에서 네 가지 종류의 호출을 일부러 모아 둔다.
 *   1) static 메서드 호출        -> invokestatic
 *   2) 생성자 호출(new)          -> invokespecial
 *   3) 인스턴스 메서드(오버라이드 가능) -> invokevirtual  (런타임에 실제 코드 결정 = "어디에")
 *   4) 인터페이스 메서드          -> invokeinterface
 * 컴파일하면 javac가 호출 종류에 따라 서로 다른 invoke 명령을 생성한다. 이 코드를 실행한 뒤
 * javap로 main의 바이트코드를 보면, 네 명령이 실제로 찍혀 있음을 확인할 수 있다(docs 참고).
 *
 * 예상 결과(실행):
 *   - 각 호출이 정상 동작해 결과 문자열들이 출력된다.
 * 예상 결과(javap):
 *   - main 바이트코드에 invokestatic / invokespecial / invokevirtual / invokeinterface 가 보인다.
 * -> "메서드 실행 메커니즘"은 추상적 개념이 아니라, 호출 종류별로 다른 JVM 명령으로 구현되어 있다.
 *    특히 invokevirtual이 오버라이딩(런타임 결정 = "어디에")을 담당한다는 점이 핵심.
 *    (invoke 명령들의 의미는 2.7 바이트코드에서 더 깊이 다룬다)
 */
public class Example3_InvokeInstructions {

    // 인터페이스 메서드 호출(invokeinterface)을 만들기 위한 인터페이스 + 구현
    interface Speaker {
        String speak();
    }

    static class Parrot implements Speaker {
        public String speak() {
            return "안녕(앵무새)";
        }
    }

    // static 메서드(invokestatic 대상)
    static String staticGreeting() {
        return "정적 인사";
    }

    public static void main(String[] args) {
        System.out.println("[예시 3] 호출 경로별 invoke 명령 (javap로 확인)");
        System.out.println();

        // 1) static 메서드 호출 -> invokestatic
        String s1 = staticGreeting();
        System.out.println("1) static 호출(invokestatic)     : " + s1);

        // 2) 생성자 호출 -> invokespecial
        Dog dog = new Dog();
        System.out.println("2) 생성자 호출(invokespecial)     : new Dog() 생성 완료");

        // 3) 인스턴스 메서드(오버라이드 가능) -> invokevirtual (런타임에 실제 코드 결정)
        Animal a = dog;
        System.out.println("3) 인스턴스 호출(invokevirtual)   : a.sound() = " + a.sound());

        // 4) 인터페이스 참조로 호출 -> invokeinterface
        Speaker speaker = new Parrot();
        System.out.println("4) 인터페이스 호출(invokeinterface): speaker.speak() = " + speaker.speak());

        System.out.println();
        System.out.println("=> 호출 종류마다 javac가 다른 invoke 명령을 생성한다.");
        System.out.println("   docs의 javap 명령으로 main 바이트코드에서 네 명령을 직접 확인할 것.");
    }
}
