package ru.seregaizsbera.faketime.junit.annotations;

import ru.seregaizsbera.faketime.junit.FakeTimeProvider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для указания реализации источника настроек времени.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FakeTimeSpec {
    /**
     * Класс, в котором реализован источник настроек времени
     */
    Class<? extends FakeTimeProvider> value();
}
