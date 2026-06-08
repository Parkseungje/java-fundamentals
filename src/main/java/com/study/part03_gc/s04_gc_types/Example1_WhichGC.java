package com.study.part03_gc.s04_gc_types;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

/**
 * 예시 1 / 3 — 현재 GC 확인: "지금 어떤 GC가 동작 중인가? 플래그로 바꾸면 이름이 달라진다."
 *
 * 이 예시가 답하려는 질문: 내 JVM이 지금 어떤 가비지 컬렉터를 쓰는지 코드로 알 수 있는가?
 * -XX 플래그로 GC를 바꾸면 그 변화가 보이는가?
 *
 * 왜 이 시나리오인가: GarbageCollectorMXBean은 현재 등록된 GC들의 이름을 알려준다. GC 종류마다
 * 컬렉터 이름이 다르기 때문에, 이 이름만 봐도 어떤 GC가 동작 중인지 알 수 있다.
 *   - 기본(Java 9+): G1 -> "G1 Young Generation", "G1 Old Generation"
 *   - -XX:+UseSerialGC -> "Copy"(Young), "MarkSweepCompact"(Old)
 *   - -XX:+UseParallelGC -> "PS Scavenge"(Young), "PS MarkSweep"(Old)
 *   - -XX:+UseZGC -> "ZGC ..." 류
 * 즉 같은 코드를 다른 플래그로 실행하면 출력되는 컬렉터 이름이 바뀐다 = GC가 교체됐다는 증거.
 *
 * 예상 결과:
 *   - 플래그 없이 실행: G1 컬렉터 이름들이 출력(Java 9+ 기본).
 *   - -XX:+UseSerialGC 등으로 실행: 해당 GC의 컬렉터 이름으로 바뀜.
 * -> GC는 '교체 가능한 부품'이다. 이름으로 종류를 식별할 수 있고, 플래그로 선택할 수 있다.
 *    터미널에서 java -XX:+PrintCommandLineFlags -version 으로도 현재 GC 플래그를 확인할 수 있다.
 */
public class Example1_WhichGC {

    public static void main(String[] args) {
        System.out.println("[예시 1] 현재 활성 GC 확인 (MXBean의 컬렉터 이름)");
        System.out.println();

        System.out.println("등록된 GarbageCollector:");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("  - " + gc.getName());
        }

        System.out.println();
        System.out.println("=> 컬렉터 이름으로 현재 GC 종류를 알 수 있다.");
        System.out.println("   같은 코드를 -XX:+UseSerialGC / -XX:+UseParallelGC / -XX:+UseZGC 로 실행하면");
        System.out.println("   이름이 바뀐다(GC는 교체 가능한 부품). java -XX:+PrintCommandLineFlags -version 도 참고.");
    }
}
