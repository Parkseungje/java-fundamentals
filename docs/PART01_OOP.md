# PART 1 — 객체지향(OOP) 기초

## 1.1 절차지향 → 객체지향 (왜 OOP가 등장했나)

### 실습 코드
- `com.study.part01_oop.s01_procedural_vs_oop`
  - `CarData` + `CarProceduralOps` — **절차지향 스타일** (C 구조체를 흉내냄: 데이터와 함수가 분리)
  - `Car` — **객체지향 스타일** (상태 + 행동을 캡슐화)
  - `ProceduralVsOopDemo` — 같은 "버그 시나리오"를 양쪽에 적용해 차이를 직접 실행으로 비교

### 실행
```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part01_oop.s01_procedural_vs_oop.ProceduralVsOopDemo
```
> IntelliJ에서 직접 실행(▶)하면 한글 출력이 정상적으로 보인다.
> (터미널에서 `javac`/`java`로 직접 돌리면 Windows 콘솔 인코딩(CP949) 때문에 한글이 깨질 수 있음 —
> `java -Dfile.encoding=UTF-8 ...` 로도 콘솔 코드페이지 자체가 안 바뀌면 깨질 수 있다. PART 6 I/O·인코딩에서 다시 다룰 주제.)

### 실행 결과로 확인하는 것
같은 "가속 50 → 브레이크 80" 시나리오를 적용했을 때:

| | 절차지향 (`CarData`/`CarProceduralOps`) | 객체지향 (`Car`) |
|---|---|---|
| 결과 speed | `-30` (음수가 그대로 노출) | `0` (브레이크 내부 규칙으로 클램프) |
| 외부 직접 대입 | `car.speed = 9999` 가능 (필드 public) | `car.speed = 9999` → **컴파일 에러** (private) |
| "규칙"이 강제되는 위치 | 없음 — 호출하는 모든 곳에서 검증을 반복해야 함 | `brake()` 내부 단 한 곳 |

→ 커리큘럼이 말하는 "데이터가 어디서 어떻게 바뀌는지 추적이 어렵다"는 문제가,
  `CarProceduralOps`처럼 **여러 static 메서드가 같은 데이터를 주무르는 구조**에서
  실제로 어떻게 나타나는지 코드로 확인할 수 있다.

### 자기 점검
- Q. C 구조체로 OOP를 흉내낼 수 있지만 자바와의 결정적 차이는?
  - [ ] `CarData`(필드 public, 행동 없음)와 `Car`(필드 private, 행동 포함)를 비교하며
        "캡슐화·상속·다형성을 **언어가 강제하는가, 관례에 맡기는가**"의 차이를 직접 정리한다.
  - 힌트: `CarData.speed = 9999`는 컴파일이 되지만 `Car.speed = 9999`는 안 된다 — 이 차이가
    "흉내"와 "언어 차원의 보장"의 경계선이다.

---

## PART 1 공통 실습 코드
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
