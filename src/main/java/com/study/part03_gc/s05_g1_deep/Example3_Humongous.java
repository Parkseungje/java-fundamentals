package com.study.part03_gc.s05_g1_deep;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;

/**
 * 예시 3 / 3 — Humongous(거대 객체): "리전 절반보다 큰 객체는 특별 취급된다."
 *
 * 이 예시가 답하려는 질문: 리전 크기보다 큰(정확히는 리전 절반 이상인) 객체는 어디에 어떻게
 * 저장되는가? 일반 객체와 다르게 처리되나?
 *
 * 왜 이 시나리오인가: G1은 객체를 리전에 담는데, '리전 절반 이상' 크기의 객체는 일반 리전에 넣기
 * 곤란하다. 그래서 이런 객체는 'Humongous(거대) 객체'로 분류되어, 연속된 Humongous 리전들에
 * 따로 저장되고 주로 Old처럼 취급된다. Humongous 할당은 비싸고 단편화를 유발할 수 있어, 큰 배열을
 * 자주 만드는 코드는 주의해야 한다.
 *   - 이 예시는 현재 리전 크기를 읽어, '리전 절반 이상' 크기의 배열을 일부러 할당한다.
 *   - -Xlog:gc 로 실행하면 "Humongous Allocation" 관련 GC가 로그에 보인다.
 *
 * 실행 권장: 리전 크기를 작게 고정해 humongous를 확실히 유발한다.
 *   java -XX:+UseG1GC -XX:G1HeapRegionSize=1m -Xlog:gc -cp ... Example3_Humongous
 *   (리전 1MB -> 절반 512KB 이상이면 humongous. 아래 코드는 리전 크기에 맞춰 2배 배열을 만든다.)
 *
 * 예상 결과:
 *   - 콘솔에 리전 크기와 '할당하는 배열 크기(리전의 2배)'가 출력된다.
 *   - -Xlog:gc 로 실행하면 "G1 Humongous Allocation" 또는 humongous 관련 GC 줄이 보인다.
 * -> 리전보다 큰 객체는 일반 객체와 다른 경로(Humongous 리전)로 처리된다. 큰 배열 남발은 G1에서
 *    성능 함정이 될 수 있다는 점을 기억할 것.
 */
public class Example3_Humongous {

    public static void main(String[] args) {
        HotSpotDiagnosticMXBean diag = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        long regionSize = Long.parseLong(diag.getVMOption("G1HeapRegionSize").getValue());

        System.out.println("[예시 3] Humongous(거대 객체): 리전 절반 이상 크기는 특별 취급");
        System.out.println("현재 G1HeapRegionSize = " + (regionSize / (1024 * 1024)) + " MB");
        System.out.println();

        // 리전의 2배 크기 배열 = 확실히 humongous (리전 절반 이상이면 humongous이므로 2배면 충분)
        int humongousSize = (int) Math.min(regionSize * 2, Integer.MAX_VALUE - 16);
        System.out.println("할당할 배열 크기 = " + (humongousSize / (1024 * 1024)) + " MB (리전의 약 2배 -> humongous)");

        System.out.println("humongous 배열을 대량 할당한다(일부는 보관해 힙을 채우고 GC를 유발)...");
        java.util.List<byte[]> keep = new java.util.ArrayList<>();
        for (int i = 0; i < 200; i++) {
            byte[] humongous = new byte[humongousSize]; // 리전보다 큰 객체 -> Humongous 리전에 저장
            humongous[0] = 1;
            keep.add(humongous);          // 보관해서 힙을 채운다 -> humongous 할당이 GC를 유발
            if (keep.size() > 30) {
                keep.remove(0);           // 너무 많이 쌓여 OOM 나지 않게 오래된 것부터 버림
            }
        }

        System.out.println();
        System.out.println("완료. -Xlog:gc 로 실행했다면 'Humongous Allocation' 관련 GC 줄이 보일 것이다.");
        System.out.println("=> 리전 절반 이상 객체는 Humongous로 분류되어 연속 리전에 따로 저장된다(주로 Old 취급).");
        System.out.println("   큰 배열 남발은 humongous 할당·단편화로 G1 성능 함정이 될 수 있다.");
    }
}
