package com.study.part04_collections.s02_stringbuilder;

/**
 * 예시 1 / 3 — String += 반복의 문제: "불변이라 매번 새 객체가 생겨 루프에서 폭증한다."
 *
 * 이 예시가 답하려는 질문: 루프에서 문자열을 += 로 계속 이어붙이면 왜 느린가? StringBuilder를 쓰면
 * 얼마나 빨라지는가?
 *
 * 왜 이 시나리오인가: String은 불변(4.1)이라 += 한 번마다 "기존 내용 + 새 내용"을 담은 '새 String'을
 * 만든다. 즉 N번 이어붙이면 새 객체가 N개 생기고, 매번 이전 내용을 통째로 복사하므로 길이가 길어질수록
 * 복사 비용도 커진다(대략 O(n^2)). 반면 StringBuilder는 내부 char 배열을 '제자리에서' 늘려가며
 * append하므로 새 객체를 만들지 않는다(대략 O(n)). 같은 횟수(N)로 두 방식의 수행 시간을 측정해 비교한다.
 *
 * 예상 결과:
 *   - String += 방식이 StringBuilder 방식보다 압도적으로 느리다(수십~수백 배).
 *   - 결과 문자열의 내용/길이는 동일하다(둘 다 같은 문자열을 만든다).
 * -> "결과는 같지만 과정의 비용이 다르다." 루프 문자열 조립은 반드시 StringBuilder를 쓴다.
 *    (반복 횟수 N을 키울수록 차이가 더 벌어진다 — 직접 N을 바꿔 확인해볼 것)
 */
public class Example1_ConcatProblem {

    public static void main(String[] args) {
        System.out.println("[예시 1] String += 반복 vs StringBuilder 성능 비교");
        System.out.println();

        int n = 50_000; // 반복 횟수 (키울수록 String 방식이 급격히 느려짐)

        // 방식 A: String += (매번 새 객체 생성 -> 느림)
        long startA = System.currentTimeMillis();
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "x"; // 매 반복마다 새 String 생성 + 이전 내용 복사
        }
        long elapsedA = System.currentTimeMillis() - startA;

        // 방식 B: StringBuilder (내부 배열 제자리 확장 -> 빠름)
        long startB = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("x"); // 새 객체 안 만듦, 내부 배열에 추가
        }
        String result = sb.toString();
        long elapsedB = System.currentTimeMillis() - startB;

        System.out.println("반복 횟수 n = " + n);
        System.out.println("A) String +=    : " + elapsedA + " ms, 결과 길이 = " + s.length());
        System.out.println("B) StringBuilder: " + elapsedB + " ms, 결과 길이 = " + result.length());
        System.out.println("두 결과 내용 동일? " + s.equals(result));
        if (elapsedB > 0) {
            System.out.println("=> String += 가 약 " + (elapsedA / Math.max(1, elapsedB)) + "배 이상 느림");
        }

        System.out.println();
        System.out.println("=> String은 불변이라 += 마다 새 객체+복사 발생(O(n^2) 경향). StringBuilder는");
        System.out.println("   내부 배열을 제자리에서 늘려 append(O(n) 경향). 루프 조립은 StringBuilder!");
    }
}
