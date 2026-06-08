package com.study.part03_gc.s02_generations;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

/**
 * 예시 3 / 3 — 힙의 세대 구조를 코드로 보기: "Heap은 Eden/Survivor/Old로 나뉘어 있다."
 *
 * 이 예시가 답하려는 질문: "Young(Eden, Survivor)과 Old로 나뉜다"는 세대 구조는 정말 실재하는가?
 * 코드로 그 구획들을 직접 확인할 수 있는가?
 *
 * 왜 이 시나리오인가: JVM은 메모리 영역을 'Memory Pool'이라는 단위로 관리하고, MXBean으로 이를
 * 조회할 수 있다. HEAP 타입 풀들의 이름을 출력하면 "G1 Eden Space / G1 Survivor Space / G1 Old Gen"
 * 처럼 세대별 구획이 그대로 드러난다. 즉 세대 구조가 개념도가 아니라 실제 JVM 내부 구획임을 본다.
 * (이름 앞의 "G1"은 현재 GC가 G1이라는 뜻 — 3.4에서 다룬다. Parallel GC면 "Eden Space" 등으로 나온다.)
 *
 * 예상 결과:
 *   - HEAP 풀 목록에 Eden / Survivor / Old(Tenured) 에 해당하는 항목들이 출력된다.
 *   - 각 풀의 used/max 사용량도 함께 보인다.
 * -> "Young = Eden + Survivor, 그리고 Old"라는 세대 구조가 JVM의 실제 메모리 풀로 존재한다.
 *    (Non-Heap인 Metaspace 등은 세대 구조와 별개라 여기선 HEAP만 필터링한다.)
 */
public class Example3_HeapPools {

    public static void main(String[] args) {
        System.out.println("[예시 3] 힙의 세대 구조를 MXBean으로 직접 출력");
        System.out.println();

        System.out.println("HEAP 메모리 풀 목록 (세대별 구획):");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                long usedMB = pool.getUsage().getUsed() / (1024 * 1024);
                long maxBytes = pool.getUsage().getMax();
                String maxStr = (maxBytes < 0) ? "무제한" : (maxBytes / (1024 * 1024)) + "MB";
                System.out.printf("  - %-20s used=%dMB, max=%s%n", pool.getName(), usedMB, maxStr);
            }
        }

        System.out.println();
        System.out.println("=> Eden / Survivor / Old(Gen) 구획이 실제 JVM 메모리 풀로 존재한다.");
        System.out.println("   Young = Eden + Survivor(2개), 그리고 Old. 이름 앞 'G1'은 현재 GC가 G1이라는 뜻(3.4).");
    }
}
