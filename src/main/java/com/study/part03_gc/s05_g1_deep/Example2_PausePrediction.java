package com.study.part03_gc.s05_g1_deep;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 예시 2 / 3 — 정지시간 예측 모델: "G1은 'M밀리초 안에 끝내라'는 목표에 맞춰 GC를 조절한다."
 *
 * 이 예시가 답하려는 질문: MaxGCPauseMillis(정지시간 목표)를 바꾸면 G1의 GC 동작이 실제로 달라지는가?
 *
 * 왜 이 시나리오인가: G1의 핵심은 '정지시간 예측 모델(Pause Prediction Model)'이다. "한 번의 GC를
 * 목표 시간(기본 200ms) 안에 끝내겠다"는 목표를 두고, 한 번에 회수할 리전 수를 조절한다. 목표를
 * 작게 잡으면(예: 20ms) 한 번에 적은 리전만 회수해 Pause는 짧아지지만 GC 횟수가 늘어나는 경향이
 * 있고, 크게 잡으면 반대가 된다. 같은 워크로드를 목표값만 바꿔 실행해 GC 횟수/총 시간을 비교한다.
 *
 * 예상 결과:
 *   - 현재 MaxGCPauseMillis 값과, 워크로드 후 GC 횟수/총 시간이 출력된다.
 *   - -XX:MaxGCPauseMillis=20 으로 실행하면(목표를 짧게) GC 횟수가 늘고 평균 Pause가 짧아지는 경향.
 *   - -XX:MaxGCPauseMillis=500 으로 실행하면(목표를 길게) GC 횟수가 줄고 한 번에 더 많이 회수하는 경향.
 * -> G1은 "정지시간을 예측·통제"한다는 것이 핵심. 목표값은 '약속'이 아니라 '노력 목표'이며,
 *    그 목표에 맞춰 한 번에 처리하는 양을 조절한다. (수치는 실행마다 다름 — 방향성에 주목)
 */
public class Example2_PausePrediction {

    static final List<byte[]> longLived = new ArrayList<>();

    public static void main(String[] args) {
        HotSpotDiagnosticMXBean diag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        String pauseTarget = diag.getVMOption("MaxGCPauseMillis").getValue();

        System.out.println("[예시 2] 정지시간 예측: MaxGCPauseMillis 목표에 맞춰 GC 조절");
        System.out.println("현재 MaxGCPauseMillis = " + pauseTarget + " ms");
        System.out.println();

        // 동일 워크로드
        for (int i = 0; i < 5000; i++) {
            List<byte[]> shortLived = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                shortLived.add(new byte[256 * 1024]);
            }
            if (i % 50 == 0) {
                longLived.add(new byte[256 * 1024]);
            }
        }

        long count = 0, time = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += gc.getCollectionCount();
            time += gc.getCollectionTime();
        }
        System.out.println("워크로드 후: 총 GC 횟수 = " + count + ", 총 GC 시간 = " + time + " ms");
        if (count > 0) {
            System.out.printf("평균 GC 시간 ≈ %.2f ms%n", (double) time / count);
        }

        System.out.println();
        System.out.println("=> 목표를 짧게(-XX:MaxGCPauseMillis=20) 주면 GC 횟수가 늘고 평균 Pause가 짧아지는 경향,");
        System.out.println("   길게(=500) 주면 반대. G1은 '정지시간을 예측·통제'하려 한 번에 회수할 양을 조절한다.");
        System.out.println("   docs의 명령으로 목표값을 바꿔가며 위 숫자를 비교해볼 것.");
    }
}
