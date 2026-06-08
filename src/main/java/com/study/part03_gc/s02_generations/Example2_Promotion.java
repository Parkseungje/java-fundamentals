package com.study.part03_gc.s02_generations;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

/**
 * 예시 2 / 3 — Promotion: "오래 살아남는 객체는 Survivor를 거쳐 결국 Old로 승격된다."
 *
 * 이 예시가 답하려는 질문: 계속 참조되어 죽지 않는 객체는 어떻게 되는가? Young에 영원히 머무는가,
 * 아니면 Old 영역으로 옮겨지는가(promotion)?
 *
 * 왜 이 시나리오인가: 한 List(longLived)에 객체를 계속 쌓아 '죽지 않는' 객체를 만든다. 동시에
 * 단명 객체도 섞어 Minor GC를 유발한다. 객체의 일생은 'Eden 생성 -> Minor GC 생존 시 Survivor로
 * 복사 -> From/To를 오가며 N번 생존 -> Old로 promotion'이다. 그래서 longLived 객체가 쌓일수록
 * Old Gen 사용량이 늘어나야 한다. 이를 MXBean으로 'Old Gen used'를 시작과 끝에 출력해 비교한다.
 *
 * 예상 결과:
 *   - 시작 시 Old Gen used는 작다(거의 0~수MB).
 *   - longLived 객체를 많이 쌓은 뒤 Old Gen used가 눈에 띄게 증가한다(promotion 누적).
 *   - -Xlog:gc 로 실행하면 promotion이 누적되며 Old가 차 결국 더 큰 GC(Mixed/Full)가 보일 수 있다.
 * -> 안 죽는 객체는 Young을 거쳐 Old로 승격된다. Old가 가득 차면 Full GC(전체 STW)가 일어난다.
 *    (Example1의 단명 객체가 Young에서 끝나는 것과 대비)
 */
public class Example2_Promotion {

    // static에 매달아 둬서 GC가 회수하지 못하게 한다 -> '오래 사는 객체'.
    static final List<byte[]> longLived = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("[예시 2] Promotion: 오래 사는 객체는 Old로 승격 (-Xlog:gc 와 함께 보면 좋음)");
        System.out.println();

        System.out.println("시작 시 Old Gen used = " + oldGenUsedMB() + " MB");

        // 죽지 않는 객체를 꾸준히 쌓는다(promotion 유발) + 단명 객체로 Minor GC 유발.
        for (int i = 0; i < 200; i++) {
            longLived.add(new byte[1024 * 1024]); // 계속 보관 -> 살아남아 결국 Old로 승격
            // 단명 객체도 만들어 Minor GC를 자극
            for (int j = 0; j < 20; j++) {
                byte[] tmp = new byte[1024 * 1024]; // 즉시 버려짐
            }
        }

        System.gc();
        sleep();
        System.out.println("오래 사는 객체 200MB 보관 후 Old Gen used = " + oldGenUsedMB() + " MB  <- 증가(promotion 누적)");

        System.out.println();
        System.out.println("=> 죽지 않는 객체는 Survivor를 거쳐 Old로 승격되어 Old Gen 사용량을 늘린다.");
        System.out.println("   Old가 가득 차면 전체를 멈추는 Full GC가 일어난다(Example1의 단명 객체와 대비).");
    }

    // G1 기준 Old 영역 풀("G1 Old Gen")의 사용량을 MB로 반환.
    private static long oldGenUsedMB() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP && pool.getName().toLowerCase().contains("old")) {
                return pool.getUsage().getUsed() / (1024 * 1024);
            }
        }
        return -1;
    }

    private static void sleep() {
        try { Thread.sleep(100); } catch (InterruptedException ignored) { }
    }
}
