package com.study.part04_collections.s02_stringbuilder;

/**
 * 예시 3 / 3 — StringBuilder vs StringBuffer: "빠른 비동기 vs 느린 스레드 안전."
 *
 * 이 예시가 답하려는 질문: StringBuilder와 StringBuffer는 무엇이 다른가? 언제 무엇을 써야 하나?
 *
 * 왜 이 시나리오인가: 둘은 기능(가변 문자열)이 거의 같지만, StringBuffer는 모든 메서드가
 * synchronized(동기화)라 '스레드 안전'하고 대신 동기화 비용 때문에 '느리다'. StringBuilder는
 * 동기화가 없어 '빠르지만' 여러 스레드가 동시에 쓰면 깨진다. 두 가지를 측정한다.
 *   1) 단일 스레드 성능: 같은 횟수 append를 둘로 측정 -> StringBuilder가 더 빠르다(동기화 없음).
 *   2) 멀티 스레드 안전성: 여러 스레드가 '하나의' 객체에 동시에 append.
 *      - StringBuilder: 동기화가 없어 갱신이 충돌 -> 최종 길이가 기대값보다 작거나 예외 발생.
 *      - StringBuffer: synchronized라 충돌 없이 -> 최종 길이가 정확히 기대값.
 *
 * 예상 결과:
 *   - 단일 스레드: StringBuilder 시간 < StringBuffer 시간 (Builder가 빠름)
 *   - 멀티 스레드: StringBuilder 최종 길이 != 기대값(깨짐) / StringBuffer 최종 길이 == 기대값(안전)
 * -> 단일 스레드면 StringBuilder(빠름)가 기본 선택. 여러 스레드가 '하나의' 가변 문자열을 공유해야 할
 *    때만 StringBuffer(안전). 실무에서는 대부분 StringBuilder를 쓴다(공유 자체를 피하는 게 보통).
 *    (멀티 스레드 결과는 실행마다 다를 수 있다 = 경쟁 상태의 특징, PART 7과 연결)
 */
public class Example3_BuilderVsBuffer {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] StringBuilder(빠름) vs StringBuffer(스레드 안전)");
        System.out.println();

        // ===== 1) 단일 스레드 성능 비교 =====
        int n = 5_000_000;
        long t1 = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append('x');
        long builderTime = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < n; i++) sbf.append('x');
        long bufferTime = System.currentTimeMillis() - t2;

        System.out.println("[단일 스레드 성능] " + n + "회 append");
        System.out.println("  StringBuilder: " + builderTime + " ms (동기화 없음 -> 빠름)");
        System.out.println("  StringBuffer : " + bufferTime + " ms (synchronized -> 느림)");

        System.out.println();

        // ===== 2) 멀티 스레드 안전성 비교 =====
        int threads = 4, appendsPerThread = 100_000;
        int expected = threads * appendsPerThread;

        // (A) StringBuilder를 여러 스레드가 공유 -> 동기화 없어 깨짐
        StringBuilder sharedBuilder = new StringBuilder();
        runConcurrentAppends(sharedBuilder::append, threads, appendsPerThread);
        System.out.println("[멀티 스레드] 기대 길이 = " + expected);
        System.out.println("  StringBuilder 최종 길이 = " + safeLength(sharedBuilder)
                + "  <- 기대값과 다르면 깨진 것(동기화 없음)");

        // (B) StringBuffer를 여러 스레드가 공유 -> synchronized라 안전
        StringBuffer sharedBuffer = new StringBuffer();
        runConcurrentAppends(sharedBuffer::append, threads, appendsPerThread);
        System.out.println("  StringBuffer  최종 길이 = " + sharedBuffer.length()
                + "  <- 기대값과 같으면 안전(synchronized)");

        System.out.println();
        System.out.println("=> 단일 스레드면 StringBuilder(빠름)가 기본. 여러 스레드가 하나의 가변 문자열을");
        System.out.println("   공유할 때만 StringBuffer(안전). 실무는 대부분 StringBuilder.");
    }

    // 여러 스레드가 동시에 append 작업을 수행
    private static void runConcurrentAppends(java.util.function.Consumer<String> appender,
                                             int threads, int appendsPerThread) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            ts[t] = new Thread(() -> {
                for (int i = 0; i < appendsPerThread; i++) {
                    try {
                        appender.accept("x");
                    } catch (Exception ignored) {
                        // StringBuilder는 동시 수정 중 내부 배열 인덱스 오류가 날 수도 있다
                    }
                }
            });
        }
        for (Thread th : ts) th.start();
        for (Thread th : ts) th.join();
    }

    private static int safeLength(StringBuilder sb) {
        try { return sb.length(); } catch (Exception e) { return -1; }
    }
}
