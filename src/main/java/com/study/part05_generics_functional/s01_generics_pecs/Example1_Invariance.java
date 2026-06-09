package com.study.part05_generics_functional.s01_generics_pecs;

import java.util.ArrayList;
import java.util.List;

/**
 * 예시 1 / 3 — 제네릭의 불공변성(invariance): "List<String>은 List<Object>가 아니다."
 *
 * 이 예시가 답하려는 질문: 배열은 String[]를 Object[]로 받을 수 있는데(공변), 제네릭도 그런가?
 *
 * 왜 이 시나리오인가: 배열은 '공변(covariant)'이라 Object[] arr = new String[]{...}가 된다. 하지만
 * 이건 위험하다 — arr[0] = 123(Integer) 같은 잘못된 대입이 컴파일은 통과하고 런타임에야
 * ArrayStoreException으로 터진다. 제네릭은 이 위험을 컴파일 시점에 막으려고 '불공변(invariant)'으로
 * 설계됐다. 즉 List<String>은 List<Object>의 하위 타입이 아니다. 그래서 List<Object> = List<String>
 * 대입 자체가 컴파일 에러다. 이를 통해 "왜 와일드카드(? extends/super)가 필요한가"의 동기를 본다.
 *
 * 예상 결과:
 *   - 배열(공변): Object[] = String[] 대입은 되지만, 잘못된 원소 대입이 런타임 ArrayStoreException.
 *   - 제네릭(불공변): List<Object> = List<String> 은 '컴파일 에러'(주석으로 표시) -> 애초에 막음.
 * -> 제네릭이 불공변인 이유는 타입 안전성을 '컴파일 시점에' 보장하기 위해서다. 그런데 너무 엄격해서
 *    "여러 하위 타입을 유연하게 받고 싶다"는 요구가 생기고, 그 해답이 와일드카드다(예시2,3).
 */
public class Example1_Invariance {

    public static void main(String[] args) {
        System.out.println("[예시 1] 제네릭 불공변성: List<String>은 List<Object>가 아니다");
        System.out.println();

        // 배열은 공변(covariant): Object[] <- String[] 대입 가능. 하지만 위험하다.
        Object[] objArray = new String[3];
        System.out.println("배열(공변): Object[] objArray = new String[3]  컴파일 OK");
        try {
            objArray[0] = 123; // 실제는 String[]인데 Integer 대입 시도 -> 런타임 예외
        } catch (ArrayStoreException e) {
            System.out.println("  objArray[0] = 123 -> ArrayStoreException (런타임에야 터짐 = 위험)");
        }

        System.out.println();

        // 제네릭은 불공변(invariant): List<Object> <- List<String> 대입 '컴파일 에러'
        List<String> strings = new ArrayList<>(List.of("a", "b"));
        // List<Object> objects = strings;  // <- 컴파일 에러: incompatible types
        // objects.add(123);                //    (만약 됐다면 여기서 List<String>에 Integer가 들어가는 사고)
        System.out.println("제네릭(불공변): List<Object> = List<String> 은 '컴파일 에러'(주석 처리)");
        System.out.println("  -> 잘못된 대입을 컴파일 시점에 원천 차단(배열의 런타임 폭발과 대비)");
        System.out.println("  현재 strings = " + strings);

        System.out.println();
        System.out.println("=> 제네릭은 타입 안전성을 위해 불공변이다. 단 너무 엄격해 '여러 하위 타입을");
        System.out.println("   유연하게 받기' 어렵다 -> 그 해법이 와일드카드(? extends / ? super)다(예시2,3).");
    }
}
