package com.study.part05_generics_functional.s03_reflection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * [모델] Reflection으로 읽을 커스텀 어노테이션.
 *
 * @Retention(RUNTIME)이 핵심이다. 어노테이션 정보를 '런타임까지' 유지해야 Reflection으로 읽을 수
 * 있다. (기본값 CLASS나 SOURCE면 런타임에 사라져서 못 읽는다.) Spring의 @Component, JPA의 @Entity,
 * Jackson의 @JsonProperty 등이 모두 RUNTIME 유지 어노테이션이고, 프레임워크가 Reflection으로 이를
 * 읽어 동작한다(-> Example3).
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Info {
    String value();
}
