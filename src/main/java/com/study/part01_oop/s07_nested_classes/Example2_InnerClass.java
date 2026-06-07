package com.study.part01_oop.s07_nested_classes;

/**
 * 예시 2 / 3 — "(non-static) inner 클래스는 바깥 인스턴스에 어떻게 묶이는가?"
 *
 * 이 예시가 답하려는 질문: inner 클래스 Student는 어떻게 생성하나? 바깥 인스턴스의 필드(name)에
 * 정말 직접 접근할 수 있나? 서로 다른 University 인스턴스로 만든 Student는 각각 다른 name을 보나?
 *
 * 왜 이 시나리오인가: 서로 다른 University 두 개(서울대/연세대)를 만들고, 각각으로부터 Student를
 * 생성한다. inner 클래스는 'univ.new Student(...)' 형태로만 만들 수 있다(바깥 인스턴스 필요).
 * 만약 "inner는 특정 바깥 인스턴스에 묶인다"는 설명이 맞다면, 각 Student.info()는 자신을 만든
 * University의 name을 보여줘야 한다(서울대 학생은 "서울대", 연세대 학생은 "연세대").
 *
 * 예상 결과:
 *   - University 인스턴스를 통해 univ.new Student(...) 로 생성됨 (인스턴스 없이는 생성 불가)
 *   - 서울대 학생 info() -> 소속 "서울대" / 연세대 학생 info() -> 소속 "연세대"
 * -> inner 클래스는 자신을 만든 바깥 인스턴스를 '숨은 참조'로 들고 있어 그 인스턴스 필드에 접근한다.
 *    Example1의 static nested(바깥 인스턴스와 독립)와의 결정적 차이가 이것이다.
 */
public class Example2_InnerClass {

    public static void main(String[] args) {
        System.out.println("[예시 2] inner 클래스: 바깥 인스턴스에 소속되어 그 필드(name)에 접근");
        System.out.println();

        University snu = new University("서울대");
        University yonsei = new University("연세대");

        // inner 클래스는 반드시 바깥 인스턴스를 통해 생성: univ.new Student(...)
        // new University.Student(...) 는 불가(어느 University 소속인지 정해야 하므로).
        University.Student studentA = snu.new Student("홍길동");
        University.Student studentB = yonsei.new Student("김연세");

        studentA.info(); // 소속: 서울대
        studentB.info(); // 소속: 연세대

        System.out.println();
        System.out.println("=> 같은 Student 클래스지만 만든 University에 따라 보이는 name이 다르다.");
        System.out.println("   inner 클래스는 자신을 생성한 바깥 인스턴스에 묶여 그 필드에 접근하기 때문.");
        System.out.println("   (Example1의 static nested는 바깥 인스턴스가 아예 없었다는 점과 대비)");
    }
}
