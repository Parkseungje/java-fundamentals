package com.study.part02_jvm.s02_runtime_data_areas;

/**
 * 예시 2 / 3 — Heap: "new로 만든 객체는 Heap에 쌓이고, 참조가 끊기면 GC 대상이 된다."
 *
 * 이 예시가 답하려는 질문: new로 객체를 많이 만들면 정말 Heap 사용량이 늘어나는가? 그리고
 * 그 객체들을 더 이상 참조하지 않으면 회수(GC)될 수 있는가?
 *
 * 왜 이 시나리오인가: Runtime으로 '사용 중인 힙 메모리'를 측정한 뒤, Robot 객체 100만 개를
 * 배열에 담아 Heap을 채우고 다시 측정한다. 만약 "객체는 Heap에 저장된다"는 설명이 맞다면 사용량이
 * 눈에 띄게 증가해야 한다. 그다음 배열 참조를 끊고(null) GC를 요청하면, 회수되어 사용량이
 * 다시 줄어드는 경향을 볼 수 있다(= Heap이 GC 대상 영역이라는 증거).
 *
 * 예상 결과:
 *   - 할당 후 used 메모리가 할당 전보다 크게 증가(수십 MB 단위)
 *   - 참조를 끊고 System.gc() 후 used 메모리가 다시 감소하는 경향
 * 주의: System.gc()는 '요청'일 뿐 강제가 아니며, 정확한 수치는 실행/JVM마다 다르다. 핵심은
 *      '절대값'이 아니라 '늘었다가 줄어드는 방향성'이다. (GC 메커니즘 자체는 PART 3에서 심화)
 */
public class Example2_HeapGrowth {

    private static long usedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        long usedBytes = rt.totalMemory() - rt.freeMemory();
        return usedBytes / (1024 * 1024);
    }

    public static void main(String[] args) {
        System.out.println("[예시 2] Heap: 객체 할당 시 사용량 증가, 참조 해제 + GC 시 감소");
        System.out.println();

        System.out.println("할당 전 사용 힙 = " + usedMemoryMB() + " MB");

        // Robot 객체 100만 개를 Heap에 생성해 배열(역시 Heap)에 담는다.
        int n = 1_000_000;
        Robot[] robots = new Robot[n];
        for (int i = 0; i < n; i++) {
            robots[i] = new Robot("R" + i);
        }
        System.out.println("객체 " + n + "개 할당 후 사용 힙 = " + usedMemoryMB() + " MB  <- 늘어남(Heap에 쌓임)");

        // 참조를 끊으면 이 객체들은 더 이상 도달 불가 -> GC 회수 대상이 된다.
        robots = null;
        System.gc(); // GC 요청(강제 아님). 잠시 대기로 GC가 동작할 여지를 준다.
        try { Thread.sleep(200); } catch (InterruptedException ignored) { }

        System.out.println("참조 해제 + System.gc() 후 사용 힙 = " + usedMemoryMB() + " MB  <- 줄어드는 경향(GC 회수)");

        System.out.println();
        System.out.println("=> new 객체는 Heap에 저장되어 사용량을 늘리고, 참조가 끊기면 GC 대상이 된다.");
        System.out.println("   (수치는 실행마다 다를 수 있음 — '늘었다 줄어드는 방향'에 주목. GC는 PART 3)");
    }
}
