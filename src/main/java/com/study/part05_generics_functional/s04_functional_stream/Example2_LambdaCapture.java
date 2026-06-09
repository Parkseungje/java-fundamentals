package com.study.part05_generics_functional.s04_functional_stream;

import java.util.function.Supplier;

/**
 * 예시 2 / 4 — 람다의 지역변수 캡처: "캡처한 지역변수는 '사실상 final'이어야 한다."
 *
 * 이 예시가 답하려는 질문: 람다 안에서 바깥 메서드의 지역변수를 쓸 수 있나? 제약은 무엇인가?
 *
 * 왜 이 시나리오인가: 람다는 바깥(감싸는 메서드)의 지역변수를 '캡처'해 사용할 수 있다. 단 그 변수는
 * '사실상 final(effectively final)'이어야 한다 — 즉 한 번 값이 정해진 뒤 바뀌지 않아야 한다. 이유는,
 * 람다가 그 변수의 '값을 복사'해 들고 있기 때문이다(지역변수는 스택에 있고 메서드가 끝나면 사라지는데,
 * 람다는 더 오래 살 수 있으므로 값을 복사해 가둔다). 만약 변수가 바뀔 수 있으면 "어느 시점의 값"인지
 * 모호해져 자바가 아예 금지한다. 캡처 변수를 수정하려 하면 컴파일 에러가 난다(주석으로 확인).
 *
 * 예상 결과:
 *   - 캡처한 message를 람다가 정상 사용(값을 복사해 가둠).
 *   - 캡처한 변수를 람다 밖/안에서 재할당하려 하면 컴파일 에러(주석 처리).
 * -> 람다 캡처 변수는 사실상 final. (1.7 익명 클래스의 캡처 제약과 동일한 규칙 — 익명 클래스가
 *    람다의 전신이므로 같은 제약을 공유한다.)
 */
public class Example2_LambdaCapture {

    public static void main(String[] args) {
        System.out.println("[예시 2] 람다의 지역변수 캡처 (사실상 final)");
        System.out.println();

        String message = "안녕";          // 한 번 정해진 뒤 안 바뀜 -> 사실상 final
        int times = 3;

        // 람다가 바깥 지역변수(message, times)를 캡처해 사용
        Supplier<String> repeater = () -> message.repeat(times);
        System.out.println("캡처한 변수 사용: " + repeater.get());

        // message = "다른값";  // <- 캡처된 변수를 이후에 재할당하면 컴파일 에러:
        //                          variable used in lambda should be final or effectively final
        System.out.println("message를 이후에 재할당하면 컴파일 에러(주석) -> 사실상 final이어야 함");

        System.out.println();
        System.out.println("=> 람다는 바깥 지역변수의 '값을 복사해' 가두므로, 그 변수는 사실상 final이어야 한다.");
        System.out.println("   (지역변수는 메서드 종료 시 사라지지만 람다는 더 오래 살 수 있어 값을 복사한다.)");
        System.out.println("   익명 클래스(1.7)의 캡처 제약과 동일 — 익명 클래스가 람다의 전신이기 때문.");
    }
}
