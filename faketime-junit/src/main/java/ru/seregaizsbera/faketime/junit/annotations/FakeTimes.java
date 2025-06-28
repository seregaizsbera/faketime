package ru.seregaizsbera.faketime.junit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация-контейнер для множества {@link FakeTime}. Это технологическая аннотация.
 * Как правило, в коде она не используется.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FakeTimes {
    FakeTime[] value() default {};
}
