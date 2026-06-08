package com.study.part03_gc.s03_algorithms;

/**
 * 예시 2 / 3 — Mark-and-Compact: "마킹 후 살아남은 객체를 앞으로 모아 단편화를 없앤다."
 *
 * 이 예시가 답하려는 질문: Mark-Sweep의 단편화 문제(Example1)를 어떻게 해결하는가?
 *
 * 왜 이 시나리오인가: Example1과 '완전히 같은 초기 힙'(짝수=live, 홀수=garbage)으로 시작한다.
 * Mark 단계까지는 동일하다. 차이는 그다음:
 *   - Sweep 대신 Compact: 살아남은(marked) 객체들을 힙의 '앞쪽으로 차곡차곡 이동(복사)'시키고,
 *     나머지 뒤쪽은 전부 비운다.
 * 그러면 살아있는 객체는 0,1,2,3에 빈틈없이 모이고, 빈 칸은 4,5,6,7에 '연속으로' 생긴다.
 * 그 결과 연속 빈 칸 길이가 커져서, 큰 객체도 할당할 수 있다 = 단편화 해소.
 *
 * 예상 결과:
 *   - Compact 후 살아있는 객체가 앞쪽(0~3)에 모이고, 빈 칸 4개가 뒤(4~7)에 연속으로 생김.
 *   - 연속 빈 칸 최대 길이 = 4 -> "연속 2칸 필요" 할당 성공(Example1은 실패했던 것).
 * -> Mark-Compact는 단편화를 없애는 대신, 객체를 옮기는 비용(이동/참조 갱신)이 든다.
 *    "단순하지만 단편화(Sweep)" vs "단편화 없지만 이동 비용(Compact)"의 트레이드오프를 직접 비교.
 */
public class Example2_MarkCompact {

    public static void main(String[] args) {
        System.out.println("[예시 2] Mark-and-Compact: 살아남은 객체를 앞으로 모아 단편화 제거");
        System.out.println();

        // Example1과 동일한 초기 힙
        MiniObject[] heap = new MiniObject[8];
        MiniObject root = new MiniObject("ROOT");
        for (int i = 0; i < 8; i++) {
            MiniObject obj = new MiniObject((i % 2 == 0 ? "live" : "garbage") + i);
            heap[i] = obj;
            if (i % 2 == 0) {
                root.pointTo(obj);
            }
        }
        printHeap("초기 힙", heap);

        // --- Mark (Example1과 동일) ---
        mark(root);

        // --- Compact: marked 객체를 앞쪽으로 모은다 ---
        int dest = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] != null && heap[i].marked) {
                heap[dest] = heap[i];      // 살아있는 객체를 앞쪽 dest 위치로 이동(복사)
                if (dest != i) heap[i] = null;
                dest++;
            } else {
                heap[i] = null;            // garbage 제거
            }
        }
        // dest 이후 칸을 확실히 비움
        for (int i = dest; i < heap.length; i++) heap[i] = null;
        printHeap("Compact 후", heap);

        // --- 단편화 해소 확인 ---
        int totalFree = countFree(heap);
        int maxRun = maxContiguousFree(heap);
        System.out.println("총 빈 칸 = " + totalFree + ", 연속 빈 칸 최대 길이 = " + maxRun);
        int need = 2;
        System.out.println("연속 " + need + "칸 필요한 객체 할당 가능? " + (maxRun >= need)
                + "  <- 빈 칸이 뒤쪽에 연속으로 모여 성공(Example1과 정반대)");

        System.out.println();
        System.out.println("=> Compact는 단편화를 없애지만 객체를 옮기는 비용이 든다.");
        System.out.println("   Sweep(단순·단편화) vs Compact(단편화 없음·이동 비용)의 트레이드오프.");
    }

    private static void mark(MiniObject obj) {
        if (obj == null || obj.marked) return;
        obj.marked = true;
        for (MiniObject child : obj.refs) mark(child);
    }

    private static void printHeap(String label, MiniObject[] heap) {
        StringBuilder sb = new StringBuilder(label + ": [");
        for (int i = 0; i < heap.length; i++) {
            sb.append(heap[i] == null ? "_" : heap[i].name);
            if (i < heap.length - 1) sb.append(", ");
        }
        sb.append("]");
        System.out.println(sb);
    }

    private static int countFree(MiniObject[] heap) {
        int c = 0;
        for (MiniObject o : heap) if (o == null) c++;
        return c;
    }

    private static int maxContiguousFree(MiniObject[] heap) {
        int max = 0, cur = 0;
        for (MiniObject o : heap) {
            if (o == null) { cur++; max = Math.max(max, cur); }
            else cur = 0;
        }
        return max;
    }
}
