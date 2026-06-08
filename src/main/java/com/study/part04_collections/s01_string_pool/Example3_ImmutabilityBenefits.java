package com.study.part04_collections.s01_string_pool;

import java.util.HashMap;
import java.util.Map;

/**
 * 예시 3 / 3 — 불변성의 이점: "불변이라서 안전하게 공유할 수 있고, 캐싱·HashMap 키로 믿을 만하다."
 *
 * 이 예시가 답하려는 질문: String이 불변이면 구체적으로 무엇이 좋은가? (캐싱·보안·스레드 안전이라고
 * 하는데 코드로는 어떻게 드러나나?)
 *
 * 왜 이 시나리오인가: 불변성의 이점 세 가지를 한 화면에서 보여준다.
 *   1) 안전한 공유: 같은 String을 여러 곳(여러 변수/스레드)이 공유해도, 아무도 그 내용을 바꿀 수
 *      없으니 서로 간섭이 없다(스레드 안전·보안의 근거). 메서드에 넘겨도 원본이 훼손되지 않는다.
 *   2) HashMap 키 안정성: String을 Map의 key로 쓰면, key의 내용이 절대 안 바뀌므로 hashCode도
 *      안 바뀐다. 그래서 넣은 값을 항상 같은 key로 다시 찾을 수 있다. (만약 key가 가변이라 내용이
 *      바뀌면 hashCode가 달라져 값을 못 찾는 사고가 난다.)
 *   3) hashCode 캐싱: String은 불변이라 hashCode를 한 번 계산해 내부에 캐싱해두고 재사용한다
 *      (값이 안 변하니 매번 다시 계산할 필요가 없다 -> 성능 이점).
 *
 * 예상 결과:
 *   - 공유된 문자열을 "수정"해도(새 객체 반환) 원래 공유 중인 변수는 그대로다.
 *   - String key로 넣은 값을 같은 문자열로 항상 다시 찾을 수 있다.
 *   - 같은 String의 hashCode를 여러 번 호출해도 동일(캐싱).
 * -> 불변성은 "한 번 만들면 안 변한다"는 단순한 성질이지만, 공유 안전성·자료구조 신뢰성·성능까지
 *    광범위한 이점을 만든다. 그래서 String이 불변으로 설계됐다.
 */
public class Example3_ImmutabilityBenefits {

    public static void main(String[] args) {
        System.out.println("[예시 3] 불변성의 이점: 안전한 공유 / HashMap 키 안정성 / hashCode 캐싱");
        System.out.println();

        // 1) 안전한 공유: 여러 변수가 같은 문자열을 공유해도 서로 간섭 없음
        String shared = "config-value";
        String alias = shared;                 // 같은 문자열을 공유
        String result = tryToModify(shared);   // 메서드에 넘겨 "수정" 시도
        System.out.println("[안전한 공유]");
        System.out.println("  메서드에 넘긴 뒤에도 shared = \"" + shared + "\"  <- 훼손 안 됨");
        System.out.println("  alias = \"" + alias + "\" (여전히 같은 값 공유)");
        System.out.println("  메서드 안에서 만든 새 값 = \"" + result + "\" (원본과 별개)");

        System.out.println();

        // 2) HashMap 키 안정성: 불변이라 hashCode가 안 바뀌어 항상 다시 찾을 수 있음
        Map<String, Integer> map = new HashMap<>();
        String key = "user:1";
        map.put(key, 100);
        System.out.println("[HashMap 키 안정성]");
        System.out.println("  put(\"user:1\", 100) 후 get(\"user:1\") = " + map.get("user:1")
                + "  <- 키 내용이 안 변해 항상 찾힘");

        System.out.println();

        // 3) hashCode 캐싱: 같은 문자열의 hashCode는 항상 같다(내부 캐싱)
        String h = "hashcode-test";
        System.out.println("[hashCode 캐싱]");
        System.out.println("  1번째 hashCode = " + h.hashCode());
        System.out.println("  2번째 hashCode = " + h.hashCode() + "  <- 동일(불변이라 한 번 계산 후 캐싱)");

        System.out.println();
        System.out.println("=> 불변성('한 번 만들면 안 변함')이 안전한 공유·자료구조 신뢰성·성능 이점을 만든다.");
        System.out.println("   그래서 String은 불변으로 설계됐다(스레드 안전·보안·캐싱).");
    }

    // 매개변수 s의 내용을 바꾸려 해도 불변이라 불가능. 새 문자열을 만들어 돌려줄 뿐 원본은 그대로.
    private static String tryToModify(String s) {
        return s.toUpperCase(); // 원본을 못 바꾸므로 새 객체를 만들어 반환
    }
}
