# PART 6 — I/O와 직렬화: 6.6 직렬화 (Serialization)

> 이 문서는 커리큘럼 PART 6의 소단원 중 **6.6 직렬화**를 다룬다.
> PART 6의 마지막. 객체를 바이트로 저장/복원하는 직렬화와 transient·serialVersionUID를 본다.

---

## 1. 학습 내용 — 직렬화와 그 주의점

### 직렬화란
**직렬화(serialization)** = 객체를 **바이트 스트림으로 변환**하는 것. 파일 저장·네트워크 전송에 쓴다.
반대로 바이트에서 객체로 복원하는 것이 **역직렬화(deserialization)**.
- 직렬화 대상 클래스는 **`Serializable`**(메서드 없는 **마커 인터페이스**)을 구현해야 한다. 없으면
  `NotSerializableException`.
- `ObjectOutputStream.writeObject()`로 직렬화, `ObjectInputStream.readObject()`로 역직렬화.
- **역직렬화는 생성자를 호출하지 않는다**(2.6). 바이트로부터 상태를 복원할 뿐이라, 생성자에만 있던
  검증/초기화가 적용되지 않을 수 있다.

### transient — 직렬화 제외 (보안)
`transient` 필드는 직렬화에서 **제외**된다. 복원하면 기본값(null/0)이 된다. **비밀번호·토큰·세션 같은
민감정보나 임시 캐시는 반드시 transient로** 둬야 한다 — 안 그러면 객체 바이트 스트림에 평문으로 그대로
들어가 파일/네트워크로 새어 나갈 위험이 있다.

### static — 직렬화 안 됨
`static` 필드는 '객체'가 아니라 '클래스'에 속하므로 객체 바이트에 포함되지 않는다. 복원 시 그 시점의
클래스 값이 보일 뿐, 직렬화 당시 값이 저장되는 게 아니다.

### serialVersionUID — 버전 식별자 (★ 운영 필수)
직렬화 바이트에는 '클래스 이름 + serialVersionUID(버전)'가 기록된다. 역직렬화 시 JVM은 "바이트의
UID == 현재 클래스의 UID"인지 확인하고, 다르면 **`InvalidClassException`**("호환 안 되는 버전")을 던진다.
- **명시하지 않으면** 컴파일러가 클래스 구조(필드·메서드)로부터 UID를 **자동 계산**한다. 필드를
  추가하는 등 클래스를 조금만 바꿔도 이 값이 **달라져서**, 예전에 저장한 바이트를 새 클래스로 읽을 때
  깨진다(InvalidClassException).
- **명시하면**(`private static final long serialVersionUID = 1L;`) 값이 고정되어, 호환 가능한 변경에는
  기존 데이터를 계속 읽을 수 있다. → **운영 시스템에서는 반드시 명시.**

---

## 2. 실습으로 확인하기

> - **가설 1**: 객체 → 바이트 → 객체 라운드트립이 되고, 역직렬화는 생성자를 호출하지 않는다.
> - **가설 2**: transient 필드는 복원 시 null(보안), static은 객체 바이트에 저장되지 않는다.
> - **가설 3**: serialVersionUID를 명시하면 고정, 명시 안 하면 클래스 구조에서 자동 계산(변경 시 바뀜).

### 모델 코드 (`com.study.part06_io.s06_serialization`)
- `Person`(Serializable, transient password, static count, serialVersionUID=1L) — 예시1·2.
- `WithoutUid`(UID 미명시) / `WithUid`(UID=100L) — 예시3.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_SerializeRoundtrip` | 직렬화 라운드트립? | write/readObject + 생성자 호출 여부 |
| `Example2_TransientAndStatic` | 제외되는 필드? | password(transient)/static |
| `Example3_SerialVersionUID` | UID 역할? | 명시 vs 자동계산 값 비교 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part06_io.s06_serialization.Example1_SerializeRoundtrip
java -cp build/classes/java/main com.study.part06_io.s06_serialization.Example2_TransientAndStatic
java -cp build/classes/java/main com.study.part06_io.s06_serialization.Example3_SerialVersionUID
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (라운드트립)** — 가설 1.
- 원본 생성 시 "Person 생성자 실행" 1번. 직렬화 → 115바이트. 역직렬화 시 **생성자 출력 없음**(안 거침).
- 복원본 != 원본(다른 객체)이지만 name·age 값은 같음. ✅

**예시 2 (transient/static)** — 가설 2.

| 필드 | 복원 결과 | 의미 |
|---|---|---|
| password (transient) | **null** | 직렬화 제외(보안) |
| name/age (일반) | 정상 복원 | 직렬화 대상 |
| instanceCount (static) | 현재 값 **999** (직렬화 당시 1 아님) | static은 객체에 저장 안 됨 |

→ 직렬화 대상은 인스턴스의 일반 필드뿐. transient(의도 제외)와 static(클래스 소속)은 빠진다. ✅

**예시 3 (serialVersionUID)** — 가설 3.

| 클래스 | UID |
|---|---|
| WithUid (명시 100L) | **100** (고정) |
| WithoutUid (미명시) | **2681132983624110811** (구조에서 자동 계산 — 클래스 변경 시 바뀜) |

→ 명시하면 고정, 안 하면 자동 계산값이 클래스 변경마다 달라져 기존 데이터 역직렬화가 깨진다. ✅
(실제 InvalidClassException은 write/read 사이 클래스 재컴파일이 필요해 한 실행으론 못 보여주며, 재현법은 코드 주석 참고.)

### 세 예시를 관통하는 결론
직렬화는 객체 상태를 바이트로 저장/복원하는 것이며(예시1, 복원은 생성자를 안 거침 — 2.6), 모든 필드가
저장되는 게 아니라 transient(보안 제외)와 static(클래스 소속)은 빠진다(예시2). 그리고 직렬화 데이터는
클래스 버전(serialVersionUID)에 묶여 있어, 명시하지 않으면 클래스를 바꿨을 때 기존 데이터를 못 읽는
사고가 난다(예시3). 이로써 PART 6(I/O·직렬화)가 마무리된다: 바이트/문자 스트림, 인코딩, 보조 스트림,
그리고 객체 ↔ 바이트 변환까지.

---

## 3. 자기 점검

- **Q. 비밀번호 필드를 직렬화 대상 클래스에 둘 때 주의점은?**
  - 내 답: `transient`를 붙여 직렬화에서 제외한다. 안 붙이면 객체 바이트 스트림에 평문으로 저장되어
    파일/네트워크로 노출될 수 있다. (Example2의 password=null)

- **Q. serialVersionUID를 명시하지 않으면 생기는 문제는?**
  - 내 답: 컴파일러가 클래스 구조로 UID를 자동 계산하는데, 필드 추가 등 변경 시 값이 바뀐다. 그러면
    예전에 저장한 바이트를 새 클래스로 역직렬화할 때 UID 불일치로 InvalidClassException이 난다. 그래서
    운영에선 반드시 명시. (Example3)

- **Q. (2.6 연결) 역직렬화로 만든 객체에 생성자 검증이 적용되지 않는 이유는?**
  - 역직렬화는 생성자를 거치지 않고 바이트로부터 상태를 복원하기 때문(Example1의 생성자 출력 없음).
    생성자에 둔 검증을 역직렬화에도 적용하려면 `readObject`를 커스터마이즈해야 한다.
