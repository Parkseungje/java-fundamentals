package com.study.part03_gc.s03_algorithms;

/**
 * 예시 1 / 3 — Mark-and-Sweep: "루트에서 마킹하고, 안 찍힌 것을 제거한다. 단 단편화가 남는다."
 *
 * 이 예시가 답하려는 질문: Mark-Sweep 알고리즘은 어떻게 동작하며, 그 단점(단편화)은 구체적으로
 * 어떤 문제를 일으키는가?
 *
 * 왜 이 시나리오인가: 슬롯 8칸짜리 '미니 힙'을 만들고, 짝수 칸(0,2,4,6)에는 루트에서 도달 가능한
 * 살아있는 객체를, 홀수 칸(1,3,5,7)에는 도달 불가능한 garbage를 둔다(일부러 번갈아 배치).
 *   - Mark 단계: 루트에서 출발해 도달 가능한 객체에 marked=true 표시.
 *   - Sweep 단계: marked 안 된 슬롯을 null로 비운다(객체 위치는 그대로 둠 = 제자리 제거).
 * Mark-Sweep은 살아남은 객체를 '움직이지 않으므로', sweep 후 빈 칸이 1,3,5,7처럼 여기저기
 * 흩어진다. 그러면 '총 빈 칸은 4개지만 연속으로 비어 있는 칸은 1개뿐'이라, 크기 2 이상이 필요한
 * 객체는 공간이 충분한데도 할당하지 못한다 = 단편화(fragmentation).
 *
 * 예상 결과:
 *   - Sweep 후 살아있는 객체는 제자리(0,2,4,6), 빈 칸은 1,3,5,7로 흩어짐.
 *   - 총 빈 칸 4개지만 '연속 빈 칸 최대 길이'는 1.
 *   - "연속 2칸 필요" 할당 시도 -> 실패(공간은 있는데 연속이 아니라서).
 * -> Mark-Sweep의 장점은 단순함, 단점은 압축(compaction)이 없어 단편화가 남는 것. (Example2가 해결)
 */
public class Example1_MarkSweep {

    public static void main(String[] args) {
        System.out.println("[예시 1] Mark-and-Sweep: 제자리 제거 -> 단편화 발생");
        System.out.println();

        // 미니 힙: 8칸. 짝수=살아있는 객체, 홀수=garbage (번갈아 배치해 단편화를 유도)
        MiniObject[] heap = new MiniObject[8];
        MiniObject root = new MiniObject("ROOT");
        for (int i = 0; i < 8; i++) {
            MiniObject obj = new MiniObject((i % 2 == 0 ? "live" : "garbage") + i);
            heap[i] = obj;
            if (i % 2 == 0) {
                root.pointTo(obj); // 짝수 칸만 루트에서 도달 가능하게
            }
        }
        printHeap("초기 힙", heap);

        // --- Mark: 루트에서 도달 가능한 객체 표시 ---
        mark(root);

        // --- Sweep: marked 안 된 슬롯을 제자리에서 제거(null) ---
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] != null && !heap[i].marked) {
                heap[i] = null; // 위치는 그대로, 내용만 제거 -> 빈 칸이 흩어짐
            }
        }
        printHeap("Sweep 후 ", heap);

        // --- 단편화 확인: 총 빈 칸 vs 연속 빈 칸 ---
        int totalFree = countFree(heap);
        int maxRun = maxContiguousFree(heap);
        System.out.println("총 빈 칸 = " + totalFree + ", 연속 빈 칸 최대 길이 = " + maxRun);
        int need = 2;
        System.out.println("연속 " + need + "칸 필요한 객체 할당 가능? " + (maxRun >= need)
                + "  <- 빈 칸은 " + totalFree + "개나 되는데 연속이 아니라 실패(단편화)");

        System.out.println();
        System.out.println("=> Mark-Sweep은 살아있는 객체를 안 움직여서 빠르고 단순하지만,");
        System.out.println("   빈 칸이 흩어져 '공간은 있는데 못 쓰는' 단편화가 남는다. -> Example2(Compact)가 해결.");
    }

    // 도달 가능성 추적: 루트에서 refs를 타고 가며 marked 표시
    private static void mark(MiniObject obj) {
        if (obj == null || obj.marked) return;
        obj.marked = true;
        for (MiniObject child : obj.refs) {
            mark(child);
        }
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
