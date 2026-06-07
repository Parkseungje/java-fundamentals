# PART 1 — 객체지향(OOP) 기초

## 실습 코드
- `com.study.part01_oop.Animal` / `Dog` / `Cat` / `PolymorphismDemo`

## 실행
```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part01_oop.PolymorphismDemo
```

## 자기 점검 — 실험으로 확인하기

### Q. 캐스팅 전에 instanceof 검사가 왜 필요한가?
- [ ] `PolymorphismDemo`에서 `instanceof Dog d` 체크를 제거하고 무조건
      `((Dog) a).fetch()`로 캐스팅했을 때 `Cat` 인스턴스에서 어떤 예외가
      발생하는지 직접 확인하고 예외 메시지를 여기 기록한다.

### Q. C 구조체로 OOP를 흉내낼 수 있지만 자바와의 결정적 차이는?
- (직접 정리)

## javap로 확인해보기 (PART 2 예고)
```bash
javap -c build/classes/java/main/com/study/part01_oop/PolymorphismDemo.class
```
- `instanceof` + 패턴 매칭이 바이트코드 레벨에서 어떤 명령으로 컴파일되는지 확인
