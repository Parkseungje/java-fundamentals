package com.study.part07_concurrency.s09_async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 예시 2 / 3 — thenCompose / thenCombine / exceptionally: "작업을 연결·합치고, 예외를 복구한다."
 *
 * 이 예시가 답하려는 질문: 비동기 작업끼리 의존 관계로 '연결'하거나, 독립 작업 둘을 '합치거나',
 * 중간에 난 예외를 '복구'하려면?
 *
 * 왜 이 시나리오인가: 실무 비동기는 단일 작업이 아니라 여러 작업의 조합이다. 세 대표 연산을 본다.
 *   - thenCompose: 앞 결과로 '또 다른 비동기 작업'을 실행해 이어붙인다(의존 관계). 예: userId를 얻은
 *     뒤 그 id로 상세정보를 비동기 조회. (thenApply가 '값->값'이면, thenCompose는 '값->CompletableFuture'
 *     라서 중첩을 평탄화한다.)
 *   - thenCombine: '서로 독립인' 두 비동기 작업을 동시에 돌리고 둘 다 끝나면 결과를 합친다. 예:
 *     상품 가격 + 배송비를 각각 조회해 합산.
 *   - exceptionally: 체인 중간에 예외가 나면 잡아서 '대체값'으로 복구한다(try-catch의 비동기 버전).
 *
 * 예상 결과:
 *   - thenCompose: userId=42 -> "user-42의 상세정보" 로 연결.
 *   - thenCombine: 가격 1000 + 배송비 2500 = 3500.
 *   - exceptionally: 작업이 일부러 예외를 던지면 -> "기본값(-1)"으로 복구.
 * -> CompletableFuture는 thenCompose(연결)·thenCombine(합치기)·exceptionally(복구)로 비동기 작업을
 *    조립한다. 동기 코드의 '순차 호출/병렬 후 합산/try-catch'를 논블로킹으로 표현한 것이다.
 */
public class Example2_ComposeCombineException {

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("[예시 2] thenCompose / thenCombine / exceptionally");
        System.out.println();

        // (A) thenCompose — 앞 결과(userId)로 또 다른 비동기 작업(상세 조회)을 '연결'
        System.out.println("(A) thenCompose (의존 작업 연결):");
        CompletableFuture<String> composed = CompletableFuture
                .supplyAsync(() -> { sleep(50); return 42; })                 // userId 조회
                .thenCompose(userId ->                                         // 그 id로 상세 조회(비동기)
                        CompletableFuture.supplyAsync(() -> "user-" + userId + "의 상세정보"));
        System.out.println("    결과: " + composed.get());

        System.out.println();

        // (B) thenCombine — 독립인 두 작업을 동시에 돌리고 결과를 '합치기'
        System.out.println("(B) thenCombine (독립 작업 합치기):");
        CompletableFuture<Integer> price = CompletableFuture.supplyAsync(() -> { sleep(80); return 1000; });
        CompletableFuture<Integer> shipping = CompletableFuture.supplyAsync(() -> { sleep(80); return 2500; });
        CompletableFuture<Integer> total = price.thenCombine(shipping, (p, s) -> p + s); // 둘 다 끝나면 합산
        System.out.println("    가격+배송비 = " + total.get() + " (1000+2500, 두 작업 동시 진행 후 합산)");

        System.out.println();

        // (C) exceptionally — 중간 예외를 잡아 '대체값'으로 복구
        System.out.println("(C) exceptionally (예외 복구):");
        CompletableFuture<Integer> recovered = CompletableFuture
                .supplyAsync(() -> {
                    if (true) throw new RuntimeException("작업 실패!"); // 일부러 예외
                    return 100;
                })
                .exceptionally(ex -> {                                  // 예외 발생 시 복구
                    System.out.println("    예외 잡힘: " + ex.getMessage() + " -> 기본값으로 복구");
                    return -1;
                });
        System.out.println("    결과: " + recovered.get() + " (예외를 복구해 -1 반환)");

        System.out.println();
        System.out.println("=> thenCompose=의존 작업 연결(값->CompletableFuture), thenCombine=독립 작업 합치기,");
        System.out.println("   exceptionally=예외를 대체값으로 복구. 동기의 순차호출/병렬합산/try-catch의 비동기판이다.");
    }
}
