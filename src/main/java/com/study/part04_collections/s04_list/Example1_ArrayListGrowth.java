package com.study.part04_collections.s04_list;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 예시 1 / 3 — ArrayList 동적 확장: "기본 10에서 시작해 부족하면 1.5배 새 배열로 복사한다."
 *
 * 이 예시가 답하려는 질문: ArrayList는 내부적으로 배열인데 어떻게 크기가 자동으로 늘어나는가?
 * 초기 크기를 지정하면 왜 더 빠른가?
 *
 * 왜 이 시나리오인가: ArrayList의 내부는 사실 일반 배열(Object[])이다. 처음엔 작은 용량(기본 10)으로
 * 시작하고, 다 차면 '더 큰 배열(약 1.5배)을 새로 만들어 기존 내용을 통째로 복사'한 뒤 그 배열로
 * 교체한다. 이 확장+복사가 add 도중 여러 번 일어난다. 그래서 담을 개수를 미리 알면 처음부터 충분한
 * 크기로 만들어(new ArrayList<>(N)) 확장·복사를 없앨 수 있고, 그만큼 빨라진다.
 *   - 두 가지를 보여준다:
 *     (1) capacity(내부 배열 길이)가 10 -> 15 -> 22 ... 로 1.5배씩 커지는 과정 (reflection으로 관찰,
 *         단 JDK 모듈 제한으로 --add-opens 옵션이 있어야 보인다. 없으면 안내만 출력)
 *     (2) 초기 크기 미지정 vs 지정의 add 시간 비교 (지정이 더 빠름)
 *
 * 예상 결과:
 *   - (--add-opens 실행 시) capacity가 10, 15, 22, 33, 49... 처럼 약 1.5배로 증가.
 *   - 초기 크기 지정(new ArrayList<>(n))이 미지정보다 빠르다(확장·복사 없음).
 * -> 자동 확장은 공짜가 아니다(새 배열 복사). 개수를 알면 초기 용량을 주는 게 이득.
 */
public class Example1_ArrayListGrowth {

    public static void main(String[] args) {
        System.out.println("[예시 1] ArrayList 동적 확장(기본 10, 1.5배) + 초기 크기 효과");
        System.out.println();

        // (1) capacity 증가 관찰 (reflection — --add-opens 있어야 동작)
        System.out.println("[capacity 증가 관찰]");
        try {
            ArrayList<Integer> list = new ArrayList<>();
            Field f = ArrayList.class.getDeclaredField("elementData");
            f.setAccessible(true); // JDK 모듈 제한: --add-opens java.base/java.util=ALL-UNNAMED 필요
            int prev = -1;
            for (int i = 0; i < 50; i++) {
                list.add(i);
                int cap = ((Object[]) f.get(list)).length;
                if (cap != prev) {
                    System.out.println("  size=" + list.size() + " 일 때 capacity=" + cap
                            + (prev > 0 ? "  (이전의 약 " + String.format("%.1f", (double) cap / prev) + "배)" : ""));
                    prev = cap;
                }
            }
        } catch (InaccessibleObjectException | NoSuchFieldException | IllegalAccessException e) {
            System.out.println("  (내부 배열 직접 관찰은 모듈 제한으로 막힘. 아래 명령으로 다시 실행하면 보인다:)");
            System.out.println("  java --add-opens java.base/java.util=ALL-UNNAMED -cp ... Example1_ArrayListGrowth");
            System.out.println("  참고: 확장 정책은 capacity 10 -> 15 -> 22 -> 33 ... (약 1.5배)");
        }

        System.out.println();

        // (2) 초기 크기 미지정 vs 지정 성능 비교
        int n = 10_000_000;
        long t1 = System.currentTimeMillis();
        List<Integer> noCapacity = new ArrayList<>();        // 미지정 -> 여러 번 확장+복사
        for (int i = 0; i < n; i++) noCapacity.add(i);
        long timeNo = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        List<Integer> withCapacity = new ArrayList<>(n);     // 지정 -> 확장 없음
        for (int i = 0; i < n; i++) withCapacity.add(i);
        long timeWith = System.currentTimeMillis() - t2;

        System.out.println("[초기 크기 효과] " + n + "개 add");
        System.out.println("  new ArrayList<>()   : " + timeNo + " ms (확장+복사 여러 번)");
        System.out.println("  new ArrayList<>(n)  : " + timeWith + " ms (확장 없음)");

        System.out.println();
        System.out.println("=> ArrayList 내부는 배열이고, 차면 1.5배 새 배열로 복사해 확장한다.");
        System.out.println("   개수를 알면 초기 용량을 줘서 확장·복사를 없애면 더 빠르다.");
    }
}
