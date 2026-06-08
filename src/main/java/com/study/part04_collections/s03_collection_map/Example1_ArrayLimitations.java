package com.study.part04_collections.s03_collection_map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 예시 1 / 3 — 배열의 한계: "크기가 고정이고, 중간 삽입/삭제 메서드가 없다."
 *
 * 이 예시가 답하려는 질문: 배열만으로 데이터를 다루면 무엇이 불편한가? 컬렉션은 그것을 어떻게
 * 해결하는가?
 *
 * 왜 이 시나리오인가: 배열의 두 가지 근본 한계를 직접 부딪혀 본다.
 *   1) 크기 고정: int[] arr = new int[3] 은 한 번 만들면 길이를 못 늘린다. 더 담으려면 '더 큰
 *      배열을 새로 만들어 복사'해야 한다(개발자가 수동으로).
 *   2) 중간 삽입/삭제 메서드 없음: 배열에는 "인덱스 1에 끼워넣기" 같은 메서드가 없다. 직접 원소를
 *      한 칸씩 밀어서(복사) 처리해야 한다.
 * 그다음 같은 일을 ArrayList(컬렉션)로 하면 add/remove/add(index, ...) 메서드로 한 줄에 끝난다.
 * 즉 컬렉션은 "자료구조 + 그 조작 알고리즘"을 클래스로 묶어 제공한다.
 *
 * 예상 결과:
 *   - 배열: 크기를 못 늘려 새 배열 복사가 필요하고, 중간 삽입도 수동 시프트가 필요하다.
 *   - ArrayList: add로 자동 확장, add(index,...)/remove(index)로 중간 삽입·삭제가 한 줄.
 * -> 배열의 불편함이 컬렉션 등장의 이유다. 컬렉션 = 자료구조 + 알고리즘의 클래스화.
 */
public class Example1_ArrayLimitations {

    public static void main(String[] args) {
        System.out.println("[예시 1] 배열의 한계 vs 컬렉션(ArrayList)");
        System.out.println();

        // --- 배열: 크기 고정 ---
        int[] arr = new int[3];
        arr[0] = 10; arr[1] = 20; arr[2] = 30;
        System.out.println("[배열] 크기 고정: " + Arrays.toString(arr) + " (길이 " + arr.length + ")");
        // arr[3] = 40;  // <- ArrayIndexOutOfBoundsException! 길이를 못 늘린다.
        // 4번째를 담으려면 더 큰 배열을 새로 만들어 복사해야 한다(수동).
        int[] bigger = Arrays.copyOf(arr, 4);
        bigger[3] = 40;
        System.out.println("[배열] 한 칸 더 담으려면 새 배열 복사: " + Arrays.toString(bigger));

        // --- 배열: 중간 삽입 메서드 없음 -> 수동 시프트 ---
        // bigger의 인덱스 1에 99를 끼워넣으려면 뒤 원소들을 한 칸씩 밀어야 한다.
        int[] inserted = new int[bigger.length + 1];
        System.arraycopy(bigger, 0, inserted, 0, 1);            // 앞부분 복사
        inserted[1] = 99;                                       // 삽입
        System.arraycopy(bigger, 1, inserted, 2, bigger.length - 1); // 나머지 한 칸 밀어 복사
        System.out.println("[배열] 중간 삽입도 수동 시프트 필요: " + Arrays.toString(inserted));

        System.out.println();

        // --- ArrayList: 같은 일을 메서드 한 줄로 ---
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
        list.add(40);                 // 자동 확장 (크기 신경 안 씀)
        list.add(1, 99);              // 인덱스 1에 중간 삽입 (자동 시프트)
        System.out.println("[ArrayList] add / add(index,..) 로 한 줄: " + list);
        list.remove(Integer.valueOf(99)); // 중간 삭제도 한 줄
        System.out.println("[ArrayList] remove 후: " + list);

        System.out.println();
        System.out.println("=> 배열은 크기 고정 + 중간 삽입/삭제 메서드 없음(수동 복사 필요).");
        System.out.println("   컬렉션은 '자료구조 + 조작 알고리즘'을 클래스로 묶어 제공한다.");
    }
}
