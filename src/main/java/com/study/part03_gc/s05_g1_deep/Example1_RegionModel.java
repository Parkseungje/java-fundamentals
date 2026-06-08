package com.study.part03_gc.s05_g1_deep;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;

/**
 * 예시 1 / 3 — G1 Region 모델: "G1은 힙을 동일 크기 리전으로 쪼개 관리한다."
 *
 * 이 예시가 답하려는 질문: G1이 말하는 '리전(Region)'은 실제로 얼마 크기이고, 정지시간 목표는
 * 몇 ms로 잡혀 있나? 이걸 코드로 확인할 수 있나?
 *
 * 왜 이 시나리오인가: 기존 GC는 힙을 [Eden | Survivor | Old]처럼 '크기·위치가 고정된 큰 덩어리'로
 * 나눴다. 큰 힙에서는 이 덩어리를 통째로 회수해야 해서 STW가 폭발했다. G1은 대신 힙을 '동일 크기의
 * 작은 리전(1~32MB)' 수백~수천 개로 쪼개고, 각 리전을 그때그때 Eden/Survivor/Old로 '동적'으로
 * 쓴다. 그래서 전체가 아니라 '리전 단위로' 부분 회수가 가능해진다.
 *   - G1HeapRegionSize: 리전 하나의 크기 (힙 크기에 따라 JVM이 자동 결정, -XX로 강제도 가능)
 *   - MaxGCPauseMillis: 정지시간 목표 (기본 200ms) — "이 시간 안에 끝내도록 노력"하는 목표값
 * 이 값들을 HotSpotDiagnosticMXBean으로 읽어, 리전 모델이 실제 설정으로 존재함을 확인한다.
 *
 * 예상 결과:
 *   - G1HeapRegionSize가 1~32MB 범위의 값으로 출력(예: 4MB).
 *   - MaxGCPauseMillis = 200 (기본 정지시간 목표).
 *   - 힙 크기 / 리전 크기 = 대략적인 리전 개수.
 * -> 힙이 '동일 크기 리전들의 집합'으로 관리된다는 것을 수치로 확인. (-Xms/-Xmx, -XX:G1HeapRegionSize로
 *    바꿔 실행하면 값이 달라진다.)
 */
public class Example1_RegionModel {

    public static void main(String[] args) {
        System.out.println("[예시 1] G1 Region 모델: 리전 크기 / 정지시간 목표 확인");
        System.out.println();

        HotSpotDiagnosticMXBean diag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);

        boolean useG1 = Boolean.parseBoolean(diag.getVMOption("UseG1GC").getValue());
        long regionSize = Long.parseLong(diag.getVMOption("G1HeapRegionSize").getValue());
        long maxHeap = Long.parseLong(diag.getVMOption("MaxHeapSize").getValue());
        String pauseTarget = diag.getVMOption("MaxGCPauseMillis").getValue();

        System.out.println("UseG1GC          = " + useG1 + (useG1 ? "" : "  (G1이 아니면 리전 개념이 다름)"));
        System.out.println("G1HeapRegionSize = " + (regionSize / (1024 * 1024)) + " MB  (리전 하나의 크기, 1~32MB)");
        System.out.println("MaxHeapSize      = " + (maxHeap / (1024 * 1024)) + " MB  (전체 힙)");
        System.out.println("MaxGCPauseMillis = " + pauseTarget + " ms  (정지시간 목표, 기본 200)");
        if (regionSize > 0) {
            System.out.println("=> 대략 리전 개수 ≈ " + (maxHeap / regionSize) + " 개 (힙 / 리전 크기)");
        }

        System.out.println();
        System.out.println("=> G1은 힙을 '동일 크기 리전' 여러 개로 쪼개고, 각 리전을 Eden/Survivor/Old로");
        System.out.println("   동적으로 쓴다. 기존의 [Eden|Survivor|Old] 고정 덩어리와 달리 부분 회수가 가능.");
        System.out.println("   (-Xmx512m, -XX:G1HeapRegionSize=1m 등으로 실행하면 값이 달라진다)");
    }
}
