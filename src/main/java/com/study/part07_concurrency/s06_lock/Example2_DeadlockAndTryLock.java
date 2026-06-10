package com.study.part07_concurrency.s06_lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 예시 2 / 3 — 데드락 재현 + tryLock으로 회피.
 *
 * 이 예시가 답하려는 질문: 데드락은 어떻게 생기나? tryLock으로 어떻게 피하나?
 *
 * 왜 이 시나리오인가: 데드락(교착 상태)의 전형은 '락을 잡는 순서가 엇갈릴 때'다.
 *   - 스레드1: lockA 잡고 -> lockB를 기다림
 *   - 스레드2: lockB 잡고 -> lockA를 기다림
 *   서로 상대가 쥔 락을 기다리며 영원히 멈춘다. lock()은 무한 대기라 빠져나올 수 없다(synchronized도 동일).
 * 해결: tryLock(시간)은 '정해진 시간만 시도'하고 못 얻으면 false를 돌려준다. 그러면 '이미 쥔 락을
 * 풀고 잠시 뒤 재시도'할 수 있어 교착에서 빠져나온다.
 *   - (A) 데드락 재현: 두 스레드를 엇갈린 순서로 lock()하게 만들고, 데몬으로 둬서 join 타임아웃으로
 *     "아직 안 끝남 = 데드락"을 감지한다(데몬이라 main 종료 시 JVM이 정리).
 *   - (B) 회피: tryLock(시간)으로 둘째 락을 못 얻으면 첫째 락을 풀고 재시도 -> 둘 다 정상 완료.
 *
 * 예상 결과:
 *   - (A) lock() 엇갈림: 두 스레드가 서로를 기다려 1.5초 뒤에도 안 끝남 -> "데드락 감지".
 *   - (B) tryLock 재시도: 못 얻으면 풀고 다시 시도해, 두 작업 모두 "완료".
 * -> 락 순서가 엇갈리면 데드락. tryLock(시간)으로 '못 얻으면 포기하고 재시도'하면 교착을 피할 수 있다.
 *    (근본 해결책은 '모든 스레드가 락을 같은 순서로 잡게' 하는 것 — 락 순서 통일.)
 */
public class Example2_DeadlockAndTryLock {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[예시 2] 데드락 재현 + tryLock 회피");
        System.out.println();

        // (A) 데드락 재현 — 엇갈린 순서로 lock()
        System.out.println("(A) lock() 엇갈림 -> 데드락:");
        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            lockA.lock();
            sleep(100);          // 그 사이 t2가 lockB를 잡게 함
            lockB.lock();        // t2가 쥔 lockB를 영원히 기다림(데드락)
            lockB.unlock(); lockA.unlock();
        });
        Thread t2 = new Thread(() -> {
            lockB.lock();
            sleep(100);
            lockA.lock();        // t1이 쥔 lockA를 영원히 기다림(데드락)
            lockA.unlock(); lockB.unlock();
        });
        t1.setDaemon(true); t2.setDaemon(true); // 데드락 나도 main 종료 시 JVM이 정리
        t1.start(); t2.start();

        t1.join(1500); // 정상이면 금방 끝남. 1.5초 지나도 살아있으면 데드락.
        boolean deadlocked = t1.isAlive() && t2.isAlive();
        System.out.println("    1.5초 뒤 두 스레드 살아있음? " + deadlocked
                + (deadlocked ? "  <- 데드락 발생!(서로의 락을 영원히 기다림)" : "  (정상 종료)"));

        System.out.println();

        // (B) tryLock으로 회피 — 못 얻으면 첫 락 풀고 재시도
        System.out.println("(B) tryLock(시간) -> 데드락 회피:");
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();
        Runnable safe = makeSafeTask(lock1, lock2);
        Runnable safeReverse = makeSafeTask(lock2, lock1); // 일부러 엇갈린 순서로 시도
        Thread s1 = new Thread(safe, "T1");
        Thread s2 = new Thread(safeReverse, "T2");
        s1.start(); s2.start();
        s1.join(); s2.join(); // tryLock 재시도 덕분에 둘 다 끝난다(데드락 없음)
        System.out.println("    두 작업 모두 완료 (tryLock으로 못 얻으면 풀고 재시도 -> 교착 회피)");

        System.out.println();
        System.out.println("=> 락 순서가 엇갈리면 데드락. tryLock(시간)은 못 얻으면 false -> 쥔 락 풀고 재시도해 회피.");
        System.out.println("   근본 해결은 '모든 스레드가 락을 같은 순서로' 잡는 것(락 순서 통일).");
    }

    // 두 락을 tryLock으로 얻되, 둘째를 못 얻으면 첫째를 풀고 잠깐 뒤 재시도
    static Runnable makeSafeTask(ReentrantLock first, ReentrantLock second) {
        return () -> {
            while (true) {
                boolean got1 = false, got2 = false;
                try {
                    got1 = first.tryLock(50, TimeUnit.MILLISECONDS);
                    if (got1) got2 = second.tryLock(50, TimeUnit.MILLISECONDS);
                    if (got1 && got2) {
                        // 두 락 다 얻음 -> 작업 후 종료
                        return;
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    if (got2) second.unlock();
                    if (got1) first.unlock();
                }
                sleep(10); // 둘 다 못 얻었으면 잠깐 쉬고 재시도(데드락 대신 양보)
            }
        };
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
