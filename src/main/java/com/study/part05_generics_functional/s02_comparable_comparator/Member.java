package com.study.part05_generics_functional.s02_comparable_comparator;

/**
 * [모델] 정렬 실습용 회원. 이름(name)과 나이(age)를 가진다.
 *
 * Comparable<Member>를 구현해 '자연 순서(natural ordering)'를 나이 오름차순으로 정의한다.
 * 이렇게 해두면 Collections.sort(list), TreeSet, TreeMap 등이 별도 기준 없이도 이 compareTo를
 * 자동으로 사용한다(-> Example1). 다른 기준(이름순 등)은 외부에서 Comparator로 준다(-> Example2,3).
 *
 * compareTo의 약속: this가 o보다
 *   - 작으면 음수, 같으면 0, 크면 양수를 반환한다.
 * 여기서는 나이를 기준으로 비교한다. (a.age - b.age 같은 '빼기'는 오버플로 위험이 있어
 * Integer.compare를 쓴다 -> Example3에서 그 이유를 다룬다.)
 */
public class Member implements Comparable<Member> {

    final String name;
    final int age;

    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // 자연 순서: 나이 오름차순. Integer.compare로 오버플로 없이 안전하게 비교.
    @Override
    public int compareTo(Member o) {
        return Integer.compare(this.age, o.age);
    }

    @Override
    public String toString() {
        return name + "(" + age + ")";
    }
}
