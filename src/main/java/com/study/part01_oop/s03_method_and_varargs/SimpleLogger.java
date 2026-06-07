package com.study.part01_oop.s03_method_and_varargs;

/**
 * [모델] 메서드 시그니처와 가변인자(varargs)를 보여주기 위한 간단한 로거.
 *
 * 메서드 시그니처의 일반 형태:
 *   [접근제어자] [반환타입] 메서드명(매개변수목록) { ... return ...; }
 *   예: public    String   format    (String tag, String msg)
 *
 * 이 클래스에는 시그니처의 여러 변형(반환값 유무, 매개변수 개수)과 가변인자 메서드를
 * 함께 모아두어, Example1~3이 각자 다른 관점에서 가져다 쓰도록 했다.
 */
public class SimpleLogger {

    // 시그니처 예시 A: 반환타입 있음(String), 매개변수 2개.
    // return 문으로 호출자에게 값을 돌려준다.
    public String format(String tag, String message) {
        return "[" + tag + "] " + message;
    }

    // 시그니처 예시 B: 반환타입 void(돌려줄 값 없음), 매개변수 1개.
    // void 메서드는 return 값이 없다(흐름 종료용 return; 은 가능).
    public void printLine(String message) {
        System.out.println(message);
    }

    // 시그니처 예시 C: 매개변수 0개, 반환타입 int.
    // 매개변수가 없어도 메서드는 성립한다.
    public int answer() {
        return 42;
    }

    // 가변인자(varargs) 메서드: String... 으로 "개수가 정해지지 않은" 인자를 받는다.
    // 호출 측은 log(), log("a"), log("a","b","c") 처럼 0개부터 여러 개까지 자유롭게 넘길 수 있다.
    // 메서드 내부에서 messages는 사실상 String[] 배열로 취급된다(length, 인덱스 접근 가능).
    // 규칙: 가변인자는 매개변수 목록의 '마지막'에만 올 수 있다. 그래서 prefix가 앞, messages가 뒤다.
    public void logAll(String prefix, String... messages) {
        System.out.println(prefix + " 받은 메시지 개수 = " + messages.length
                + " (내부에서는 배열로 다뤄짐)");
        for (int i = 0; i < messages.length; i++) {
            System.out.println("  " + i + ": " + messages[i]);
        }
    }
}
