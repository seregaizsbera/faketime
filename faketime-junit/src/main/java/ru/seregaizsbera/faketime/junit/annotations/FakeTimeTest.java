package ru.seregaizsbera.faketime.junit.annotations;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.seregaizsbera.faketime.junit.ext.FakeTimeTestTemplateExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация шаблона тестового метода, из которого формируется набор тестов с различными настройками времени.
 * Работает аналогично {@link org.junit.jupiter.api.RepeatedTest}.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TestTemplate
@ExtendWith(FakeTimeTestTemplateExtension.class)
public @interface FakeTimeTest {
    String value() default "";
}
