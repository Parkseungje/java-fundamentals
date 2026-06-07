package com.study.part02_jvm.s02_runtime_data_areas;

/**
 * [모델] JVM 런타임 데이터 영역을 관찰하기 위한 클래스.
 *
 * 이 한 클래스 안에 영역별로 저장되는 요소가 모두 들어 있다.
 *   - producedCount (static 필드): Method Area에 1개. (2.1에서 본 클래스 변수)
 *   - name (인스턴스 필드): 객체마다 Heap에 따로.
 *   - factoryInfo() (static 메서드): Method Area의 'Static Zone'에 바이트코드가 있다.
 *   - greet() (인스턴스 메서드): Method Area의 'Non-Static Zone'에 바이트코드가 있다.
 *
 * Static Zone과 Non-Static Zone의 결정적 차이:
 *   static 메서드는 객체 없이 호출되지만, 인스턴스 메서드는 '어떤 객체의' 행동인지가 정해져야
 *   호출할 수 있다(this가 필요). 그래서 static 메서드 안에서 인스턴스 메서드를 '직접' 부를 수 없고,
 *   반드시 객체(Heap)를 거쳐야 한다. (-> Example1에서 확인)
 */
public class Robot {

    // 클래스 변수: Method Area에 1개. 만들어진 Robot 수를 공유 집계.
    static int producedCount = 0;

    // 인스턴스 변수: 객체마다 Heap에 따로.
    final String name;

    public Robot(String name) {
        this.name = name;
        producedCount++;
    }

    // static 메서드(Static Zone): 객체 없이 Robot.factoryInfo() 로 호출 가능.
    static void factoryInfo() {
        // 여기서 producedCount(static)는 접근 가능하지만, name(인스턴스 필드)이나 greet()(인스턴스
        // 메서드)는 '누구의 것인지' 정해지지 않아 직접 접근 불가.
        System.out.println("  공장 정보: 지금까지 생산된 로봇 수 = " + producedCount);
    }

    // 인스턴스 메서드(Non-Static Zone): 특정 Robot 객체의 행동. this(자기 객체)가 암묵적으로 필요.
    void greet() {
        System.out.println("  " + name + ": 삡-삡, 안녕하세요");
    }

    /**
     * static 메서드가 인스턴스 메서드를 호출하려면 어떻게 해야 하는지 보여준다.
     * 직접 greet() 호출은 불가능(어떤 객체의 greet인지 모름) -> 객체를 만들어 그 객체를 통해 호출.
     */
    static void staticCallsInstance() {
        // greet();  // <- 컴파일 에러: non-static method greet() cannot be referenced from a static context
        // 해결: 객체(Heap)를 통해서 호출한다.
        Robot temp = new Robot("임시로봇");
        temp.greet();
    }
}
