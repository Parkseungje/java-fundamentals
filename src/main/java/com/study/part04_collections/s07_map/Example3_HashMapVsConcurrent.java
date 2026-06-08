package com.study.part04_collections.s07_map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 예시 3 / 3 — 스레드 안전성: "HashMap은 멀티스레드에서 값이 유실되고, ConcurrentHashMap은 안전하다."
 *
 * 이 예시가 답하려는 질문: 여러 스레드가 하나의 Map 값을 동시에 갱신하면 어떻게 되나? HashTable과
 * ConcurrentHashMap은 왜 다른가?
 *
 * 왜 이 시나리오인가: 고정된 key 100개의 값을 여러 스레드가 동시에 +1씩 증가시킨다(읽기-수정-쓰기).
 *   - HashMap: 동기화가 없어 "읽고-더하고-쓰는" 사이 다른 스레드가 끼어들면 갱신이 덮어써진다
 *     (lost update). 그래서 모든 값의 합이 기대값(스레드 수 × 반복)보다 작아진다.
 *   - ConcurrentHashMap: merge()는 '원자적'으로 갱신해 충돌 없이 정확한 합이 나온다.
 *   - HashTable도 안전하지만 '메서드 전체에 락(전체 잠금)'이라 느리다. ConcurrentHashMap은 락 단위를
 *     작게(버킷/노드) 쪼개 동시성이 높다 -> 그래서 HashTable 대신 ConcurrentHashMap이 권장된다.
 *
 * (참고: HashMap을 여러 스레드가 동시에 put하며 '크기를 늘리면' 내부 확장(resize) 중 무한 루프에
 *  빠지는 더 심각한 위험도 있다. 그래서 여기서는 key 수를 고정해 그 위험을 피하면서, '값 유실'만
 *  안전하게 관찰한다. 결론은 같다: 멀티스레드 공유 Map에 HashMap을 쓰면 안 된다.)
 *
 * 예상 결과:
 *   - HashMap: 합계 < 기대값 (lost update로 일부 증가가 사라짐) — 실행마다 다름
 *   - ConcurrentHashMap: 합계 == 기대값 (원자적 merge로 안전)
 * -> 멀티스레드 환경의 공유 Map은 ConcurrentHashMap을 쓴다(HashTable보다 빠름).
 */
public class Example3_HashMapVsConcurrent {

    static final int KEYS = 100;
    static final int THREADS = 4;
    static final int PER_THREAD = 100_000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 3] HashMap(값 유실) vs ConcurrentHashMap(안전)");
        System.out.println();

        long expected = (long) THREADS * PER_THREAD; // 모든 값의 합 기대치

        // (A) HashMap: 동기화 없는 read-modify-write -> lost update
        Map<Integer, Integer> hashMap = new HashMap<>(256); // resize 무한루프 방지 위해 미리 충분히 확보
        for (int k = 0; k < KEYS; k++) hashMap.put(k, 0);
        runConcurrentIncrements(hashMap, false);
        System.out.println("기대 합계 = " + expected);
        long sumHash = sumValues(hashMap);
        System.out.println("  HashMap          값 합계 = " + sumHash
                + (sumHash == expected ? " (우연히 일치)" : "  <- 유실 발생!(동기화 없음)"));

        // (B) ConcurrentHashMap: merge로 원자적 증가 -> 안전
        Map<Integer, Integer> chm = new ConcurrentHashMap<>();
        for (int k = 0; k < KEYS; k++) chm.put(k, 0);
        runConcurrentIncrements(chm, true);
        long sumChm = sumValues(chm);
        System.out.println("  ConcurrentHashMap 값 합계 = " + sumChm
                + (sumChm == expected ? "  <- 정확(안전)" : "  <- (예상과 다름)"));

        System.out.println();
        System.out.println("=> HashMap은 동시 갱신에서 값이 유실된다(쓰면 안 됨). ConcurrentHashMap은");
        System.out.println("   merge로 원자적 처리해 안전. HashTable도 안전하지만 전체 락이라 느려서,");
        System.out.println("   공유 Map은 ConcurrentHashMap이 표준 권장이다.");
    }

    private static void runConcurrentIncrements(Map<Integer, Integer> map, boolean atomic)
            throws InterruptedException {
        Thread[] ts = new Thread[THREADS];
        for (int t = 0; t < THREADS; t++) {
            ts[t] = new Thread(() -> {
                for (int i = 0; i < PER_THREAD; i++) {
                    int key = i % KEYS;
                    if (atomic) {
                        // ConcurrentHashMap.merge: 원자적 읽기-수정-쓰기
                        map.merge(key, 1, Integer::sum);
                    } else {
                        // HashMap: 비원자적 read-modify-write -> 갱신 유실 가능
                        map.put(key, map.get(key) + 1);
                    }
                }
            });
        }
        for (Thread th : ts) th.start();
        for (Thread th : ts) th.join();
    }

    private static long sumValues(Map<Integer, Integer> map) {
        long sum = 0;
        for (int v : map.values()) sum += v;
        return sum;
    }
}
