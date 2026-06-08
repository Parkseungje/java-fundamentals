package com.study.part04_collections.s08_hash;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;

/**
 * 예시 3 / 3 — LoadFactor 0.75와 resize: "75%가 차면 버킷 배열을 2배로 늘리고 재배치한다."
 *
 * 이 예시가 답하려는 질문: HashMap의 내부 버킷 배열은 언제, 얼마나 커지나? LoadFactor 0.75는 무슨 뜻인가?
 *
 * 왜 이 시나리오인가: HashMap의 내부는 버킷 배열(Node[] table)이다. 원소가 늘어 배열의 일정 비율을
 * 넘으면 충돌이 잦아지므로, '버킷 배열을 2배로 늘리고 모든 원소를 새 배열에 재배치(rehash)'한다.
 * 이 기준 비율이 LoadFactor(기본 0.75)다. 즉 threshold = capacity × 0.75를 넘으면 resize한다.
 *   - 초기 capacity 16, threshold 12(=16×0.75): 13번째 원소에서 32로 확장.
 *   - capacity 32, threshold 24: 25번째에서 64로 확장 ...
 * 내부 table 길이(capacity)를 reflection으로 읽어, 원소를 추가하며 16 -> 32 -> 64로 2배씩 커지는
 * 과정을 직접 관찰한다. (JDK 모듈 제한으로 --add-opens 옵션이 있어야 보인다.)
 *
 * 예상 결과(--add-opens 실행 시):
 *   - capacity가 16 -> 32 -> 64 -> 128 ... 로 2배씩 증가.
 *   - 각 확장은 size가 capacity×0.75를 넘는 시점에 일어남(16→size 13, 32→size 25 ...).
 * -> LoadFactor 0.75는 "공간 효율 vs 충돌 확률"의 절충점이다. 작게 잡으면(예: 0.5) 충돌은 줄지만
 *    메모리 낭비가 크고, 크게 잡으면(예: 0.9) 메모리는 아끼지만 충돌이 늘어 느려진다.
 */
public class Example3_LoadFactorResize {

    public static void main(String[] args) {
        System.out.println("[예시 3] LoadFactor 0.75와 resize (capacity 2배 확장 관찰)");
        System.out.println();

        try {
            HashMap<Integer, Integer> map = new HashMap<>();
            Field tableField = HashMap.class.getDeclaredField("table");
            tableField.setAccessible(true); // --add-opens java.base/java.util=ALL-UNNAMED 필요

            int prevCap = -1;
            for (int i = 1; i <= 100; i++) {
                map.put(i, i);
                Object[] table = (Object[]) tableField.get(map);
                int cap = (table == null) ? 0 : table.length;
                if (cap != prevCap) {
                    int threshold = (int) (cap * 0.75);
                    System.out.println("  size=" + map.size() + " 일 때 capacity=" + cap
                            + " (threshold=" + threshold + ", 이 이상이면 다음 확장)");
                    prevCap = cap;
                }
            }
            System.out.println();
            System.out.println("=> capacity가 16 -> 32 -> 64 ... 로 2배씩 커진다. 기준은 size > capacity×0.75.");
        } catch (InaccessibleObjectException | NoSuchFieldException | IllegalAccessException e) {
            System.out.println("  (내부 table 직접 관찰은 모듈 제한으로 막힘. 아래 명령으로 다시 실행:)");
            System.out.println("  java --add-opens java.base/java.util=ALL-UNNAMED -cp ... Example3_LoadFactorResize");
            System.out.println("  참고: 기본 capacity 16, LoadFactor 0.75 -> threshold 12. 13번째 원소에서 32로 확장.");
            System.out.println("        이후 32(threshold 24) -> 64(48) -> 128 ... 로 2배씩 커진다.");
        }

        System.out.println();
        System.out.println("=> LoadFactor 0.75 = 공간 효율 vs 충돌 확률의 절충점.");
        System.out.println("   작으면 충돌↓ 메모리 낭비↑, 크면 메모리↓ 충돌↑(느려짐).");
    }
}
