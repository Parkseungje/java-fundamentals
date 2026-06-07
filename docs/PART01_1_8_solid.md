# PART 1 — 객체지향(OOP) 기초: 1.8 SOLID 5원칙

> 이 문서는 커리큘럼 PART 1의 소단원 중 **1.8 SOLID 5원칙**을 다룬다.
> (커리큘럼에서 "9-섹션 마스터 프롬프트로 깊이 팔 단원"으로 표시된 핵심 구간 — 면접·실무 직결)
> PART 1의 마지막 소단원이며, 여기서 배운 원칙들이 PART 8(IoC/DI), PART 12(AOP)로 직접 이어진다.

---

## 1. 학습 내용 — 변경에 강한 코드의 5계명

OOP 문법(상속·다형성·추상화)을 안다고 해서 "잘 짠 코드"가 되는 것은 아니다. SOLID는
**변경이 닥쳤을 때 무너지지 않는 설계**를 위한 5가지 원칙이다.

### SRP — 단일 책임 원칙 (Single Responsibility Principle)
"클래스가 바뀌어야 하는 이유는 단 하나여야 한다." 한 클래스가 데이터 보관 + 저장 + 이메일
발송을 모두 하면, 그중 무엇이 바뀌어도 그 클래스를 건드려야 한다(변경 이유가 셋). `User`(데이터) /
`UserRepository`(저장) / `EmailService`(발송)로 책임을 쪼개면 각자 변경 이유가 하나가 된다.

### OCP — 개방-폐쇄 원칙 (Open-Closed Principle)
"확장에는 열리고, 수정에는 닫혀야 한다." 새 기능을 더할 때 기존 코드를 고치지 않고 새 코드를
추가하는 것만으로 끝나야 한다. `if-else` 타입 분기는 새 타입마다 분기를 수정해야 하므로 위반이다.
인터페이스 다형성(전략 패턴)으로 바꾸면 새 구현 클래스를 추가하기만 하면 된다. **전략 패턴은
OCP의 구현 도구**다.

### LSP — 리스코프 치환 원칙 (Liskov Substitution Principle)
"자식은 부모를 완벽히 대체할 수 있어야 한다." 부모 타입을 기대하는 코드에 자식을 넣어도 깨지면
안 된다. `Penguin extends Bird`에서 펭귄이 `fly()`를 막으면(예외), "모든 Bird는 난다"고
가정한 코드가 펭귄을 만났을 때 깨진다. **LSP가 깨지면 다형성도 깨진다** — 부모 타입으로 묶어
일관되게 다루는 것이 불가능해지기 때문이다.

### ISP — 인터페이스 분리 원칙 (Interface Segregation Principle)
"클라이언트는 사용하지 않는 메서드에 의존하면 안 된다." `Worker{work, eat}`처럼 뚱뚱한
인터페이스는, 먹지 않는 로봇에게도 `eat()` 구현을 강요한다. `Workable` / `Eatable`로 쪼개면
각 구현체가 필요한 능력만 갖는다.

### DIP — 의존 역전 원칙 (Dependency Inversion Principle)
"고수준 모듈은 저수준의 구체 구현이 아니라 추상화에 의존해야 한다." 주문 서비스가
`new EmailSender()`로 구체 클래스를 직접 만들면 강결합이다. `MessageSender` 인터페이스에
의존하고 구현을 외부에서 주입받으면, 같은 서비스로 이메일/SMS를 갈아끼울 수 있다.
**이것이 곧 DI(Dependency Injection)이고, PART 8 Spring IoC와 정확히 같은 원리**다.

---

## 2. 실습으로 확인하기

각 원칙을 **위반(Bad) vs 준수(Good)** 의 한 쌍으로 만들어, "위반하면 무엇이 무너지고 준수하면
무엇이 견고해지는지"를 실행으로 확인한다. 다섯 예시 모두 공통적으로 다음 가설을 검증한다.

> **공통 가설**: Bad와 Good은 정상 동작의 '결과'는 비슷하지만, **변경/확장/대체가 닥쳤을 때**
> Bad는 기존 코드 수정·런타임 폭발로 무너지고 Good은 견딘다.

### 모델 코드 (`com.study.part01_oop.s08_solid`)
원칙별로 한 파일에 Bad/Good을 함께 담았다(중첩 클래스로 자체 포함).

### 예시 5개 — 각 원칙의 위반 vs 준수

| 예시 | 원칙 | Bad (위반) | Good (준수) |
|---|---|---|---|
| `Example1_SRP` | 단일 책임 | `UserGod`가 검증+저장+메일 다 함 | User/UserRepository/EmailService 분리 |
| `Example2_OCP` | 개방-폐쇄 | `if-else`로 등급 분기 | `DiscountPolicy` 인터페이스 + 구현 추가 |
| `Example3_LSP` | 리스코프 치환 | `Penguin.fly()`가 예외 | `FlyingBird` 능력 분리 |
| `Example4_ISP` | 인터페이스 분리 | `Worker{work,eat}` 강요 | `Workable`/`Eatable` 분리 |
| `Example5_DIP` | 의존 역전 | `new EmailSender()` 직접 생성 | `MessageSender` 주입 |

### 실행
```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part01_oop.s08_solid.Example1_SRP
java -cp build/classes/java/main com.study.part01_oop.s08_solid.Example2_OCP
java -cp build/classes/java/main com.study.part01_oop.s08_solid.Example3_LSP
java -cp build/classes/java/main com.study.part01_oop.s08_solid.Example4_ISP
java -cp build/classes/java/main com.study.part01_oop.s08_solid.Example5_DIP
```

### 실행 결과 — 가설과 실제 비교

| 원칙 | 위반 시 드러난 문제(실행 결과) | 준수 시 확인된 견고함 |
|---|---|---|
| SRP | 메일 양식만 바뀌어도 `UserGod` 전체를 수정해야 함 | `EmailService`만 수정, 나머지 무관 |
| OCP | PLATINUM 추가하려면 분기 메서드를 **수정** | `PlatinumDiscount` 클래스만 **추가**, 서비스 코드 0 수정 |
| LSP | `letItFly(penguin)` → **UnsupportedOperationException** | 펭귄은 `FlyingBird`가 아니라 컴파일 단계에서 차단 |
| ISP | `robot.eat()` → **UnsupportedOperationException** | 로봇은 `eat()` 자체가 없어 오용 불가 |
| DIP | 항상 이메일 고정, SMS 전환 시 서비스 수정 | 같은 서비스에 구현만 주입해 이메일/SMS 전환 |

→ 모든 원칙에서 가설이 확인됐다. Bad는 "변경·확장·대체" 시점에 **기존 코드 수정** 또는
**런타임 예외**로 무너지고, Good은 그 충격을 구조로 흡수한다.

### 다섯 원칙을 관통하는 결론
SOLID는 서로 독립된 5개가 아니라 하나의 목표를 향한다: **"변경의 충격을 국소화하고, 확장을
새 코드 추가로 흡수하라."** 그 핵심 도구가 **추상화(인터페이스)에 의존하고 다형성으로 갈아끼우는
것**이다(OCP·LSP·ISP·DIP가 모두 인터페이스로 귀결된다). 특히 DIP의 "추상화 의존 + 외부 주입"은
**PART 8 Spring IoC/DI의 사상적 토대**이고, OCP의 전략 패턴은 **PART 12 AOP**까지 이어진다.
즉 1.8은 PART 1의 마무리이자 스프링으로 가는 다리다.

---

## 3. 자기 점검

- **Q. LSP가 깨지면 왜 다형성도 깨지는가?**
  - 내 답: 다형성은 "부모 타입으로 묶어 자식들을 일관되게 다루는 것"인데, 자식이 부모의 약속을
    어기면(Penguin이 fly 불가) 부모 타입으로 묶는 순간 런타임에 깨진다. 즉 자식을 부모 자리에
    넣을 수 없으니 다형성의 전제가 무너진다.
  - 근거 코드: `Example3_LSP`의 `letItFly(penguin)` 예외.

- **Q. DIP와 DI(Dependency Injection)의 관계는?**
  - 내 답: DIP는 "추상화에 의존하라"는 원칙(방향), DI는 그 의존 객체를 외부에서 주입하는
    구체적 기법(수단)이다. `Example5_DIP`의 생성자 주입이 DI이며, PART 8에서는 이 주입을
    Spring 컨테이너가 대신 해준다(IoC).

- **Q. (추가) 5원칙 중 4개가 결국 "인터페이스+다형성"으로 수렴하는 이유는?**
  - OCP(전략)·LSP(능력 분리)·ISP(인터페이스 쪼개기)·DIP(추상화 의존)가 모두 인터페이스를
    쓴다. "구체에 묶이지 말고 추상에 기대라"는 한 문장으로 묶어보고, SRP만 결이 다른 이유
    (SRP는 '추상화'가 아니라 '책임 경계'에 관한 것)도 정리해본다.
