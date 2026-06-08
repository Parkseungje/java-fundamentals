package com.study.part03_gc.s01_gc_basics;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 3 / 3 — STW와 GC 로그: "GC는 실제로 일어나고, 그동안 애플리케이션이 멈춘다(Stop The World)."
 *
 * 이 예시가 답하려는 질문: GC는 정말로 주기적으로 일어나는가? 그 순간 무슨 일이 로그에 찍히는가?
 *
 * 왜 이 시나리오인가: 짧은 수명의 객체(1MB 배열)를 대량으로 만들었다가 곧바로 버린다. 이렇게 하면
 * Heap이 빠르게 차고 GC가 반복적으로 트리거된다. GC 로그 옵션(-Xlog:gc)을 켜고 실행하면, GC가
 * 일어날 때마다 "Pause ... " 같은 줄이 찍힌다. 그 Pause가 바로 STW(Stop The World) 구간 —
 * GC가 도는 동안 애플리케이션 스레드가 멈춘 시간이다.
 *   - 객체를 List에 잠깐 담았다가 비우는 것을 반복 -> 대부분 객체가 금방 죽는다(약한 세대 가설의 상황).
 *
 * 예상 결과:
 *   - 그냥 실행하면 콘솔엔 진행 메시지만. GC 로그는 -Xlog:gc 옵션을 줘야 보인다.
 *   - -Xlog:gc 로 실행하면 "GC(0) Pause Young ... 10M->1M ... 2.3ms" 같은 줄이 여러 번 찍힌다.
 *     -> GC가 실제로 여러 번 일어났고, 각 Pause(ms)가 그때의 STW 시간이다.
 * -> GC는 추상 개념이 아니라 실측 가능한 이벤트다. 'GC 튜닝'은 보통 이 Pause(STW) 시간을 줄이는 것.
 *    docs의 -Xlog:gc 명령으로 직접 로그를 관찰할 것.
 */
public class Example3_StopTheWorldLog {

    public static void main(String[] args) {
        System.out.println("[예시 3] STW와 GC 로그 (-Xlog:gc 로 실행해야 GC 로그가 보임)");
        System.out.println();

        int rounds = 2000;
        for (int i = 0; i < rounds; i++) {
            // 매 라운드 1MB짜리 배열들을 잠깐 담았다 버린다 -> 금방 죽는 객체 대량 생성.
            List<byte[]> shortLived = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                shortLived.add(new byte[1024 * 1024]); // 1MB
            }
            // shortLived는 다음 반복에서 새 List로 교체되며 이전 것은 garbage가 된다.
            if (i % 500 == 0) {
                System.out.println("  진행 " + i + "/" + rounds + " (객체 대량 생성·폐기 중)");
            }
        }

        System.out.println();
        System.out.println("완료. -Xlog:gc 로 실행했다면 위/아래에 GC Pause 로그가 여러 줄 찍혔을 것이다.");
        System.out.println("=> 각 'Pause Young/Full ... Xms'가 그 GC의 STW(애플리케이션 정지) 시간이다.");
    }
}
