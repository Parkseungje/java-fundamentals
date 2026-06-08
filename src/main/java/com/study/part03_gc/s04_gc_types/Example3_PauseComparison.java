package com.study.part03_gc.s04_gc_types;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 3 / 3 — STW(Pause) 비교: "G1과 ZGC는 정지 시간이 다르다 (ZGC는 초저지연)."
 *
 * 이 예시가 답하려는 질문: GC 종류에 따라 STW(애플리케이션 정지) 시간이 정말 달라지는가?
 * 특히 ZGC는 왜 '초저지연'이라 불리나?
 *
 * 왜 이 시나리오인가: 큰 힙을 채우는 워크로드를 돌리며 -Xlog:gc로 각 GC의 Pause 시간을 본다.
 *   - G1: 정지시간 예측 모델로 보통 수~수십 ms 수준의 Pause (대부분의 서버에 적합).
 *   - ZGC: 대부분의 작업을 애플리케이션과 '동시에(concurrent)' 수행해, STW Pause가 보통 1ms 미만.
 *     힙이 커져도 Pause가 거의 늘지 않는 것이 특징(초저지연 요구에 적합).
 * 같은 코드를 -XX:+UseG1GC 와 -XX:+UseZGC 로 각각 -Xlog:gc 와 함께 실행해 Pause 줄을 비교한다.
 *
 * 예상 결과(-Xlog:gc 로 실행):
 *   - G1:  "Pause Young ... 수 ms" 류가 보인다.
 *   - ZGC: "Pause Mark Start ... 0.0xx ms"처럼 Pause가 매우 짧고, 대부분 작업은 Concurrent 단계로 표시.
 * -> "더 좋은 GC"가 아니라 "다른 절충"이다: ZGC는 STW를 극단적으로 줄이는 대신 동시 작업의
 *    CPU/메모리 오버헤드를 감수한다. 응답 지연이 치명적인 서비스에 ZGC가 적합하다.
 */
public class Example3_PauseComparison {

    static final List<byte[]> retained = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("[예시 3] G1 vs ZGC STW 비교 (-Xlog:gc 와 함께 실행해 Pause를 비교)");
        System.out.println();

        // 힙을 어느 정도 채우면서 GC를 자극한다. 일부는 retained로 살려 Old/동시수집을 유발.
        for (int i = 0; i < 6000; i++) {
            List<byte[]> shortLived = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                shortLived.add(new byte[256 * 1024]);
            }
            if (i % 20 == 0) {
                retained.add(new byte[256 * 1024]);
            }
            if (i % 2000 == 0) {
                System.out.println("  진행 " + i + "/6000");
            }
        }

        System.out.println();
        System.out.println("완료. -Xlog:gc 로 실행했다면 GC별 Pause 줄을 비교해볼 것:");
        System.out.println("  - G1:  'Pause Young ... 수 ms'");
        System.out.println("  - ZGC: 'Pause ...' 가 보통 1ms 미만, 대부분 Concurrent 단계로 처리");
        System.out.println("=> ZGC는 STW를 극단적으로 줄이는 절충(동시 수집 오버헤드 감수) = 초저지연.");
    }
}
