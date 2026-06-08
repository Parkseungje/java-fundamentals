package com.study.part03_gc.s04_gc_types;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * 예시 2 / 3 — GC별 수집 통계 비교: "같은 일을 시켜도 GC마다 횟수·총 시간이 다르다."
 *
 * 이 예시가 답하려는 질문: GC 종류를 바꾸면 같은 워크로드에서 수집 횟수와 총 수집 시간이 달라지는가?
 *
 * 왜 이 시나리오인가: 동일한 메모리 할당 작업(단명 객체 대량 + 일부 장수 객체)을 수행한 뒤,
 * MXBean으로 각 컬렉터의 누적 '수집 횟수(getCollectionCount)'와 '총 수집 시간(getCollectionTime, ms)'을
 * 출력한다. 이 프로그램을 -XX 플래그만 바꿔(Serial/Parallel/G1) 여러 번 실행하면, 같은 코드인데도
 * GC마다 통계가 다르게 나온다. 이는 GC가 처리량/지연을 다른 방식으로 절충하기 때문이다.
 *   - Serial: 싱글 스레드라 보통 횟수당 시간이 길고 총 시간이 크다.
 *   - Parallel: 멀티 스레드로 처리량(throughput) 우선.
 *   - G1: Region 기반으로 정지시간을 예측·통제(대부분의 서버 기본).
 *
 * 예상 결과:
 *   - 한 번 실행하면 현재 GC의 (Young/Old) 컬렉터별 count와 time(ms)이 출력된다.
 *   - 다른 GC로 실행하면 그 숫자가 달라진다 -> docs에서 세 GC의 결과를 비교한다.
 * -> "어떤 GC가 무조건 빠르다"가 아니라, 처리량 vs 지연의 절충이 다르다. 워크로드/요구사항에 맞춰 고른다.
 */
public class Example2_GCStatsByCollector {

    static final List<byte[]> longLived = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("[예시 2] GC별 수집 통계 (같은 워크로드, GC만 바꿔 비교)");
        System.out.println();

        long start = System.currentTimeMillis();

        // 동일 워크로드: 단명 객체 대량 + 가끔 장수 객체
        for (int i = 0; i < 4000; i++) {
            List<byte[]> shortLived = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                shortLived.add(new byte[256 * 1024]); // 256KB
            }
            if (i % 50 == 0) {
                longLived.add(new byte[256 * 1024]); // 가끔 장수 객체 (Old로 승격)
            }
        }

        long elapsed = System.currentTimeMillis() - start;

        // 컬렉터별 누적 통계 출력
        System.out.println("워크로드 수행 시간 = " + elapsed + " ms");
        System.out.println("컬렉터별 누적 통계:");
        long totalCount = 0, totalTime = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();
            long time = gc.getCollectionTime();
            totalCount += count;
            totalTime += time;
            System.out.printf("  - %-26s count=%d, time=%dms%n", gc.getName(), count, time);
        }
        System.out.println("  => 총 GC 횟수=" + totalCount + ", 총 GC 시간=" + totalTime + "ms");

        System.out.println();
        System.out.println("=> 같은 코드라도 GC 종류에 따라 횟수/시간이 다르다(처리량 vs 지연 절충).");
        System.out.println("   docs의 명령으로 Serial/Parallel/G1 결과를 비교해볼 것.");
    }
}
