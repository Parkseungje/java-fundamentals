package com.study.part01_oop.s07_nested_classes;

/**
 * [모델] 중첩 클래스(nested class)의 두 종류를 한 바깥 클래스에 담아 비교한다.
 *
 * 핵심 비교축: "바깥(University) 인스턴스에 묶여 있는가?"
 *   - static nested class(Building): 안 묶여 있음. University 인스턴스 없이 독립적으로 생성.
 *     바깥의 static 멤버(country)만 접근 가능, 인스턴스 멤버(name)는 접근 불가.
 *   - inner class(Student): 묶여 있음. 반드시 University 인스턴스를 통해 생성.
 *     바깥 인스턴스의 필드(name)에 직접 접근 가능.
 *
 * 이 차이를 Example1(static nested)과 Example2(inner)에서 각각 확인한다.
 */
public class University {

    // 클래스 변수(static): 모든 University가 공유. static nested 클래스도 접근 가능.
    static String country = "Korea";

    // 인스턴스 변수: University 객체마다 다름. inner 클래스만 접근 가능(인스턴스에 묶여 있으므로).
    private final String name;

    public University(String name) {
        this.name = name;
    }

    /**
     * static nested class — 바깥 인스턴스에 묶이지 않는다.
     * new University.Building(...) 처럼 University 객체 없이 바로 생성할 수 있다.
     */
    public static class Building {
        private final String buildingName;

        public Building(String buildingName) {
            this.buildingName = buildingName;
        }

        public void info() {
            // country(static)는 접근 가능.
            // name(인스턴스 필드)은 여기서 접근 불가 — 어떤 University 인스턴스에도 묶여 있지 않으므로
            //   '누구의 name인지' 알 수 없다. (주석을 풀면 컴파일 에러)
            // System.out.println(name);  // <- 컴파일 에러
            System.out.println("건물: " + buildingName + " (국가: " + country + ")");
        }
    }

    /**
     * inner class(non-static) — 항상 특정 University 인스턴스에 소속된다.
     * 그래서 univ.new Student(...) 형태로, 바깥 인스턴스를 통해서만 생성할 수 있다.
     */
    public class Student {
        private final String studentName;

        public Student(String studentName) {
            this.studentName = studentName;
        }

        public void info() {
            // 바깥 인스턴스의 필드 name에 직접 접근 가능 — 이 Student는 특정 University에 묶여 있으므로.
            System.out.println("학생: " + studentName + " (소속 대학: " + name + ")");
        }
    }
}
