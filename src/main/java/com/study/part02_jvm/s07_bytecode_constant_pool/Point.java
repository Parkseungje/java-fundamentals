package com.study.part02_jvm.s07_bytecode_constant_pool;

/**
 * [모델] 객체 생성 바이트코드(new/dup/invokespecial)를 관찰하기 위한 단순 클래스.
 * Example1에서 new Point(...)를 컴파일했을 때 어떤 명령들이 나오는지 javap로 확인한다.
 */
public class Point {

    final int x;
    final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }
}
