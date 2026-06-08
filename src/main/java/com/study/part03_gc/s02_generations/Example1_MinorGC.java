package com.study.part03_gc.s02_generations;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 1 / 3 — 단명 객체와 Minor GC: "대부분의 객체는 Young에서 태어나 Young에서 죽는다."
 *
 * 이 예시가 답하려는 질문: 금방 죽는 객체를 대량으로 만들면 어떤 GC가 일어나는가? Young 영역만
 * 청소하는 Minor GC가 자주 발생하는가, 아니면 전체를 멈추는 Full GC가 자주 발생하는가?
 *
 * 왜 이 시나리오인가: 1MB 배열을 잠깐 담았다 버리는 일을 반복한다. 객체가 거의 즉시 garbage가
 * 되므로, "약한 세대 가설(대부분 금방 죽음)"에 딱 맞는 상황이다. 이때 GC는 주로 Young 영역만
 * 빠르게 청소하는 Minor GC(로그상 "Pause Young")를 반복해야 하고, Old로 넘어가는(promotion) 객체가
 * 거의 없어 Full GC는 잘 안 일어나야 한다.
 *
 * 예상 결과(-Xlog:gc로 실행 시):
 *   - "Pause Young" 로그가 여러 번 반복해서 찍힌다.
 *   - GC 후 사용량이 매번 작게 떨어진다(예: 600M->10M) -> 단명 객체가 Young에서 대량 회수됨.
 * -> 객체의 일생은 보통 'Eden에서 생성 -> 곧 죽음 -> Minor GC로 회수'다. 이것이 Young 영역을
 *    자주, 그리고 싸게 청소하면 되는 이유다. (오래 사는 객체의 promotion은 Example2에서)
 */
public class Example1_MinorGC {

    public static void main(String[] args) {
        System.out.println("[예시 1] 단명 객체 -> Minor GC(Pause Young) 빈발 (-Xlog:gc 로 실행)");
        System.out.println();

        int rounds = 3000;
        for (int i = 0; i < rounds; i++) {
            // 1MB 배열 10개를 잠깐 담았다가 다음 반복에서 버린다 -> 거의 즉시 garbage.
            List<byte[]> shortLived = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                shortLived.add(new byte[1024 * 1024]);
            }
            if (i % 1000 == 0) {
                System.out.println("  진행 " + i + "/" + rounds);
            }
        }

        System.out.println();
        System.out.println("완료. -Xlog:gc 로 실행했다면 'Pause Young'이 여러 번 찍혔을 것이다.");
        System.out.println("=> 단명 객체는 Young(Eden)에서 나고 Minor GC로 회수된다(Full GC는 거의 없음).");
    }
}
