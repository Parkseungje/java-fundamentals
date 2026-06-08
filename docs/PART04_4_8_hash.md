# PART 4 — 문자열과 컬렉션: 4.8 해시(Hash)의 원리

> 이 문서는 커리큘럼 PART 4의 소단원 중 **4.8 해시의 원리**를 다룬다.
> (커리큘럼에서 "★ HashMap 면접 단골"로 표시된 핵심 구간 / "9-섹션 깊이 학습" 권장)
> 4.5(Set)·4.7(Map)에서 의존했던 hashCode/equals와 버킷의 실체를 여기서 깊이 본다.

---

## 1. 학습 내용 — 해시 함수, 충돌, 체이닝, LoadFactor

### 왜 해시인가 (로우레벨의 불편함)
데이터가 많을 때 "이 값이 있나?"를 순차 검색하면 O(n) — 데이터가 많을수록 너무 느리다.
그래서 나온 질문: **"수학적으로 위치를 바로 계산할 수 없을까?"** 그 답이 **해시 함수**다.

- **해시 함수**: key를 정수 인덱스로 바꾸는 함수(`key → int`). 그 인덱스의 버킷에 바로 저장/조회한다.
- 좋은 해시 함수의 조건: **빠름 · 균등 분포 · 결정적**(같은 입력은 항상 같은 값).
- 덕분에 평균 **O(1)** 로 찾는다(위치를 계산으로 바로 알아내므로).

### 해시 충돌 (피할 수 없다)
서로 다른 입력이 같은 해시값을 갖는 것이 **해시 충돌**이다. 비둘기집 원리상(무한한 입력 → 유한한
버킷) 불가피하다. 충돌이 잦으면 같은 버킷에 원소가 쌓여 O(1)이 무너지고 **최악 O(n)** 이 된다.

### 충돌 해결 1 — 체이닝 (자바 HashMap 채택)
같은 버킷에 충돌한 원소들을 **연결 리스트로 연결**한다. 조회 시 그 버킷의 리스트를 훑는다.
- **Java 8부터**: 한 버킷의 원소가 **8개를 초과**하고 전체 용량이 64 이상이면, 그 버킷을 **Red-Black
  Tree로 변환(treeify)** 해 최악을 O(n)에서 **O(log n)** 으로 완화한다(단 key가 Comparable일 때 정렬 가능).

### 충돌 해결 2 — 오픈 어드레싱
충돌 시 다른 빈 버킷을 찾아 넣는 방식(선형 탐사/이차 탐사/이중 해싱). 메모리 효율은 좋지만
클러스터링(한 곳에 몰림)과 삭제 처리가 까다롭다. (자바 HashMap은 체이닝을 쓴다.)

### LoadFactor 0.75와 resize
HashMap의 내부는 버킷 배열이다. 원소가 늘어 일정 비율을 넘으면 충돌이 잦아지므로 **버킷 배열을
2배로 늘리고 모든 원소를 재배치(rehash)** 한다. 이 기준이 **LoadFactor(기본 0.75)** 다.
- threshold = capacity × 0.75. 초기 capacity 16 → threshold 12 → 13번째 원소에서 32로 확장.
- LoadFactor가 작으면(0.5) 충돌 ↓ 메모리 낭비 ↑, 크면(0.9) 메모리 ↓ 충돌 ↑(느려짐). 0.75는 절충점.

---

## 2. 실습으로 확인하기

> - **가설 1**: 순차 검색 O(n)은 해시 O(1)보다 큰 데이터에서 압도적으로 느리다.
> - **가설 2**: hashCode가 전부 같으면(충돌) 체이닝으로 O(n)이 되어 극단적으로 느려진다.
> - **가설 3**: HashMap capacity는 size가 capacity×0.75를 넘을 때 2배로 확장된다.

### 모델 코드 (`com.study.part04_collections.s08_hash`)
- `KeyGood`(고르게 분산되는 hashCode) / `KeyBad`(hashCode 항상 1 → 전부 충돌) — 예시2에서 대비.

### 예시 3개 — 각 예시가 답하는 질문

| 예시 | 답하는 질문 | 시나리오 |
|---|---|---|
| `Example1_WhyHash` | 왜 해시가 빠른가? | List vs HashSet contains 시간 |
| `Example2_CollisionChaining` | 충돌이 성능에? | KeyGood vs KeyBad get 시간 |
| `Example3_LoadFactorResize` | 언제 2배 확장? | capacity 증가 관찰 |

### 실행
아래 명령은 모두 **프로젝트 루트(`C:\develop\study\java-fundamentals`)에서 실행**한다.
(docs 폴더 등 다른 위치에서 실행하면 `build/...` 상대경로를 못 찾아 에러가 난다. 그럴 땐 `cd ..`로 루트로 이동)

```bash
./gradlew compileJava
java -cp build/classes/java/main com.study.part04_collections.s08_hash.Example1_WhyHash
java -cp build/classes/java/main com.study.part04_collections.s08_hash.Example2_CollisionChaining
```

Example3의 capacity 증가를 직접 보려면 모듈 접근 옵션이 필요하다(내부 배열을 JDK가 막아둠):

```bash
java --add-opens java.base/java.util=ALL-UNNAMED -cp build/classes/java/main com.study.part04_collections.s08_hash.Example3_LoadFactorResize
```

### 실행 결과 — 가설과 실제 비교

**예시 1 (왜 해시)** — 가설 1. (크기 10만, contains 5만회, 없는 값 조회, 실측)

| | 시간 |
|---|---|
| List.contains | 9017 ms (순차 O(n)) |
| HashSet.contains | 3 ms (해시 O(1)) |

→ 약 **3000배 차이**. 순차 검색은 데이터가 많을수록 급격히 느려진다. ✅

**예시 2 (충돌·체이닝)** — 가설 2. (3만개 저장, get 3만회, 실측)

| key | 시간 |
|---|---|
| KeyGood (고른 분산) | 2 ms (여러 버킷, O(1)) |
| KeyBad (전부 충돌) | 4708 ms (한 버킷 긴 체인, O(n)) |

→ hashCode가 모두 같으면 한 버킷에 몰려 **약 2400배 느려진다**. 좋은 hashCode가 성능의 핵심. ✅

**예시 3 (LoadFactor·resize)** — 가설 3. `--add-opens`로 capacity 관찰(실측):

| size | capacity | threshold(×0.75) |
|---|---|---|
| 1 | 16 | 12 |
| 13 | 32 | 24 |
| 25 | 64 | 48 |
| 49 | 128 | 96 |
| 97 | 256 | 192 |

→ size가 capacity×0.75를 넘는 시점마다 capacity가 정확히 **2배**로 커진다(16→32→64→128→256). ✅

### 세 예시를 관통하는 결론
해시는 "위치를 계산으로 바로 찾자"는 아이디어로 순차 검색 O(n)을 O(1)로 바꾼다(예시1). 단 충돌은
피할 수 없고, 충돌이 잦으면(나쁜 hashCode) 체이닝으로 O(n)까지 퇴화한다(예시2) — 그래서 고른
분산이 중요하고 Java 8은 treeify로 최악을 완화한다. 그리고 HashMap은 충돌을 낮게 유지하려고
LoadFactor 0.75 기준으로 버킷 배열을 2배씩 늘린다(예시3). **HashMap의 빠름은 "좋은 해시 분산 +
적절한 LoadFactor + 충돌 관리(체이닝/트리)"의 합작**이다.

---

## 3. 자기 점검

- **Q. HashMap이 평균 O(1)인 이유와, 최악 O(n)이 되는 경우는?**
  - 내 답: hashCode로 위치를 바로 계산해 평균 O(1). 충돌이 잦아(특히 hashCode가 다 같으면) 한 버킷에
    몰리면 그 버킷을 순회해야 해 O(n)이 된다(Java 8 treeify면 O(log n)). (Example2)

- **Q. LoadFactor 0.75의 의미와 작게/크게 잡을 때의 트레이드오프는?**
  - 내 답: 버킷 배열의 75%가 차면 2배 확장. 작게(0.5) 잡으면 충돌↓·메모리 낭비↑, 크게(0.9) 잡으면
    메모리↓·충돌↑. 0.75가 절충점. (Example3의 16→32 확장 시점)

- **Q. (★ 면접) 사용자 정의 객체를 HashMap key로 쓸 때 hashCode/equals 규약은?**
  - equals가 true인 두 객체는 hashCode도 같아야 한다(안 그러면 다른 버킷으로 가 영영 못 찾음).
    KeyGood처럼 둘 다 일관되게 재정의해야 한다. 4.5의 PointWithEquals와 연결해 정리해본다.
