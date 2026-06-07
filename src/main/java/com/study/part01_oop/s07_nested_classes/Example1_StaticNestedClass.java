package com.study.part01_oop.s07_nested_classes;

/**
 * 예시 1 / 3 — "static nested 클래스는 바깥 인스턴스 없이 쓸 수 있는가?"
 *
 * 이 예시가 답하려는 질문: University 안에 정의된 static nested 클래스 Building은,
 * University 객체를 만들지 않고도 생성할 수 있는가? 바깥의 어떤 멤버에 접근할 수 있나?
 *
 * 왜 이 시나리오인가: University 인스턴스를 '전혀 만들지 않고' new University.Building(...)으로
 * 바로 객체를 만든다. 만약 "static nested는 바깥 인스턴스에 묶이지 않는다"는 설명이 맞다면
 * 이 생성이 성공해야 하고, Building.info()는 static 필드 country에는 접근하되 인스턴스 필드
 * name에는 접근하지 못해야 한다(접근 시도는 University.java 안에 컴파일 에러로 주석 처리됨).
 *
 * 예상 결과:
 *   - new University.Building("공학관") 이 University 인스턴스 없이 성공
 *   - info() 출력에 country(=Korea)는 나오지만 name은 애초에 접근 불가
 * -> static nested 클래스는 "바깥 클래스의 이름공간 안에 있을 뿐, 바깥 인스턴스와는 독립"이다.
 *    바깥 인스턴스의 상태가 필요 없을 때 쓴다(헬퍼/빌더 등).
 */
public class Example1_StaticNestedClass {

    public static void main(String[] args) {
        System.out.println("[예시 1] static nested 클래스: 바깥(University) 인스턴스 없이 생성");
        System.out.println();

        // University 객체를 만들지 않았다는 점에 주목. static nested라 바로 생성 가능.
        University.Building building = new University.Building("공학관");
        building.info();

        System.out.println();
        System.out.println("=> University 인스턴스 없이 new University.Building(...) 으로 생성됨.");
        System.out.println("   static 멤버(country)는 접근하지만 인스턴스 멤버(name)는 접근 불가.");
        System.out.println("   = 바깥 인스턴스와 독립적인 중첩 클래스.");
    }
}
